package com.vibedebounce

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.vibedebounce.prefs.DebouncePrefs
import com.vibedebounce.service.DebounceNotificationService
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
        // Arrange: create service, manually invoke onCreate
        val controller = Robolectric.buildService(DebounceNotificationService::class.java)
        val service = controller.create().get()

        // Act: change the pref (simulating user moving slider)
        prefs.debounceWindowSeconds = 45

        // Assert: service picked up the new value
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

        // After destroy, changing prefs should NOT update the service's field.
        prefs.debounceWindowSeconds = 15
        // Service is destroyed; debounceWindowMs should retain its last value (the default 90s).
        assertEquals(DebouncePrefs.DEFAULT_SECONDS * 1000L, service.debounceWindowMs)
    }
}
