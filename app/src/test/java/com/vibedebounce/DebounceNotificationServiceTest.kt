package com.vibedebounce

import android.content.Context
import android.service.notification.StatusBarNotification
import androidx.test.core.app.ApplicationProvider
import com.vibedebounce.prefs.DebouncePrefs
import com.vibedebounce.service.DebounceNotificationService
import com.vibedebounce.service.NewThreadNotifier
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DebounceNotificationServiceTest {

    private lateinit var context: Context
    private lateinit var prefs: DebouncePrefs

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        prefs = DebouncePrefs(context)
    }

    @Test
    fun `onPreferenceChanged updates debounceWindowMs from SharedPreferences`() {
        val controller = Robolectric.buildService(DebounceNotificationService::class.java)
        val service = controller.create().get()
        prefs.debounceWindowSeconds = 45
        assertEquals(45_000L, service.debounceWindowMs)
    }

    @Test
    fun `initial debounceWindowMs reflects persisted value`() {
        prefs.debounceWindowSeconds = 120
        val controller = Robolectric.buildService(DebounceNotificationService::class.java)
        val service = controller.create().get()
        assertEquals(120_000L, service.debounceWindowMs)
    }

    @Test
    fun `listener is unregistered on destroy without leaking`() {
        val controller = Robolectric.buildService(DebounceNotificationService::class.java)
        val service = controller.create().get()
        controller.destroy()
        prefs.debounceWindowSeconds = 15
        assertEquals(DebouncePrefs.DEFAULT_SECONDS * 1000L, service.debounceWindowMs)
    }

    @Test
    fun `onListenerDisconnected cancels all timers and restores ringer`() {
        val controller = Robolectric.buildService(DebounceNotificationService::class.java)
        val service = controller.create().get()
        val rsm = service.getRingerStateManagerForTest()

        rsm.mute()
        rsm.mute()
        assertTrue(rsm.isActive())

        service.onListenerDisconnected()

        assertFalse(rsm.isActive())
        assertEquals(0, rsm.activeCount())
        assertFalse(DebounceNotificationService.isRunning)
        assertEquals(0, DebounceNotificationService.activeWindowCount)
    }

    @Test
    fun `onDestroy cancels all timers and restores ringer`() {
        val controller = Robolectric.buildService(DebounceNotificationService::class.java)
        val service = controller.create().get()
        val rsm = service.getRingerStateManagerForTest()

        rsm.mute()
        assertTrue(rsm.isActive())

        controller.destroy()

        assertFalse(rsm.isActive())
        assertEquals(0, rsm.activeCount())
    }

    @Test
    fun `clearAllTimersAndRestore is idempotent`() {
        val controller = Robolectric.buildService(DebounceNotificationService::class.java)
        val service = controller.create().get()

        service.onListenerDisconnected()
        controller.destroy()

        val rsm = service.getRingerStateManagerForTest()
        assertFalse(rsm.isActive())
    }

    @Test
    fun `first notification from new sender vibrates before muting`() {
        val controller = Robolectric.buildService(DebounceNotificationService::class.java)
        val service = controller.create().get()

        val mockNotifier: NewThreadNotifier = mock()
        service.setNotifierForTest(mockNotifier)

        val sbn = buildMockSbn("com.example.chat", "Alice")
        service.onNotificationPosted(sbn)

        verify(mockNotifier).vibrate()
    }

    @Test
    fun `first notification from new sender does not post new-thread notification when no other debounce active`() {
        val controller = Robolectric.buildService(DebounceNotificationService::class.java)
        val service = controller.create().get()

        val mockNotifier: NewThreadNotifier = mock()
        service.setNotifierForTest(mockNotifier)

        val sbn = buildMockSbn("com.example.chat", "Alice")
        service.onNotificationPosted(sbn)

        verify(mockNotifier).vibrate()
        verify(mockNotifier, never()).fire(any())
    }

    @Test
    fun `new sender during active debounce fires full notification`() {
        val controller = Robolectric.buildService(DebounceNotificationService::class.java)
        val service = controller.create().get()

        val mockNotifier: NewThreadNotifier = mock()
        service.setNotifierForTest(mockNotifier)

        val sbn1 = buildMockSbn("com.example.chat", "Alice")
        service.onNotificationPosted(sbn1)

        reset(mockNotifier)

        val sbn2 = buildMockSbn("com.example.chat", "Bob")
        service.onNotificationPosted(sbn2)

        // fire() handles vibration internally; mock won't delegate
        verify(mockNotifier, never()).vibrate()
        verify(mockNotifier).fire("Bob")
    }

    @Test
    fun `repeat notification from same sender during debounce does not vibrate`() {
        val controller = Robolectric.buildService(DebounceNotificationService::class.java)
        val service = controller.create().get()

        val mockNotifier: NewThreadNotifier = mock()
        service.setNotifierForTest(mockNotifier)

        val sbn = buildMockSbn("com.example.chat", "Alice")
        service.onNotificationPosted(sbn)

        reset(mockNotifier)

        service.onNotificationPosted(sbn)

        verify(mockNotifier, never()).vibrate()
        verify(mockNotifier, never()).fire(any())
    }

    private fun buildMockSbn(packageName: String, title: String): StatusBarNotification {
        val extras = android.os.Bundle().apply {
            putString(android.app.Notification.EXTRA_TITLE, title)
        }
        val notification = android.app.Notification().apply {
            this.extras = extras
        }
        val sbn: StatusBarNotification = mock()
        whenever(sbn.packageName).thenReturn(packageName)
        whenever(sbn.notification).thenReturn(notification)
        return sbn
    }
}
