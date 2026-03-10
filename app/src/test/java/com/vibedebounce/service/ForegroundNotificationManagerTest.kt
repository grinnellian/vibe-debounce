package com.vibedebounce.service

import org.junit.Assert.*
import org.junit.Test

class ForegroundNotificationManagerTest {

    @Test
    fun `channel ID is debounce_service_status`() {
        assertEquals("debounce_service_status", ForegroundNotificationManager.CHANNEL_ID)
    }

    @Test
    fun `notification ID is 1`() {
        assertEquals(1, ForegroundNotificationManager.NOTIFICATION_ID)
    }

    @Test
    fun `statusText with 0 active returns Monitoring`() {
        assertEquals("Monitoring notifications", ForegroundNotificationManager.statusText(0))
    }

    @Test
    fun `statusText with 1 active returns singular`() {
        assertEquals("Debouncing 1 sender", ForegroundNotificationManager.statusText(1))
    }

    @Test
    fun `statusText with 3 active returns plural`() {
        assertEquals("Debouncing 3 senders", ForegroundNotificationManager.statusText(3))
    }
}
