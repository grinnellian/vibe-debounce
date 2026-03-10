package com.vibedebounce.service

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.vibedebounce.prefs.AppPrefs
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
}
