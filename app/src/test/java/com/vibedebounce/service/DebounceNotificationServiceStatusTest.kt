package com.vibedebounce.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class DebounceNotificationServiceStatusTest {

    @Before
    fun setUp() {
        DebounceNotificationService.resetState()
    }

    @Test
    fun isRunning_isFalseByDefault() {
        assertFalse(DebounceNotificationService.isRunning)
    }

    @Test
    fun activeWindowCount_isZeroByDefault() {
        assertEquals(0, DebounceNotificationService.activeWindowCount)
    }
}
