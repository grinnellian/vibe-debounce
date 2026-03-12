package com.vibedebounce.service

import android.app.Notification
import android.content.Context
import android.os.Bundle
import android.service.notification.StatusBarNotification
import androidx.test.core.app.ApplicationProvider
import com.vibedebounce.prefs.AppPrefs
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DebounceNotificationServiceAppFilterTest {

    private lateinit var context: Context
    private lateinit var service: DebounceNotificationService
    private lateinit var appPrefs: AppPrefs

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        appPrefs = AppPrefs(context)
        val controller = Robolectric.buildService(DebounceNotificationService::class.java)
        service = controller.create().get()
        DebounceNotificationService.resetState()
    }

    private fun createMockSbn(packageName: String, title: String): StatusBarNotification {
        val extras = Bundle().apply {
            putString(Notification.EXTRA_TITLE, title)
        }
        val notification = mock(Notification::class.java).apply {
            this.extras = extras
        }
        val sbn = mock(StatusBarNotification::class.java)
        whenever(sbn.packageName).thenReturn(packageName)
        whenever(sbn.notification).thenReturn(notification)
        return sbn
    }

    @Test
    fun `recordSeenApp adds package to AppPrefs`() {
        service.recordSeenApp("com.example.chat")
        assertTrue(appPrefs.getSeenPackages().contains("com.example.chat"))
    }

    @Test
    fun `recordSeenApp is idempotent`() {
        service.recordSeenApp("com.example.chat")
        service.recordSeenApp("com.example.chat")
        assertEquals(1, appPrefs.getSeenPackages().size)
    }

    @Test
    fun `shouldDebounce returns true when app is enabled`() {
        assertTrue(service.shouldDebounce("com.example.chat"))
    }

    @Test
    fun `shouldDebounce returns false when app is disabled`() {
        appPrefs.setAppEnabled("com.example.chat", false)
        assertFalse(service.shouldDebounce("com.example.chat"))
    }

    @Test
    fun `shouldDebounce reflects changed state`() {
        appPrefs.setAppEnabled("com.example.chat", false)
        assertFalse(service.shouldDebounce("com.example.chat"))
        appPrefs.setAppEnabled("com.example.chat", true)
        assertTrue(service.shouldDebounce("com.example.chat"))
    }

    @Test
    fun `onNotificationPosted ignores notifications from own package`() {
        val sbn = createMockSbn("com.vibedebounce", "Test sender")
        service.onNotificationPosted(sbn)
        assertFalse(appPrefs.getSeenPackages().contains("com.vibedebounce"))
    }

    @Test
    fun `onNotificationPosted still processes notifications from other packages`() {
        val sbn = createMockSbn("com.example.chat", "Alice")
        service.onNotificationPosted(sbn)
        assertTrue(appPrefs.getSeenPackages().contains("com.example.chat"))
    }

    @Test
    fun `own package notification does not create debounce timer`() {
        val sbn = createMockSbn("com.vibedebounce", "New conversation")
        service.onNotificationPosted(sbn)
        assertEquals(0, DebounceNotificationService.activeWindowCount)
    }
}
