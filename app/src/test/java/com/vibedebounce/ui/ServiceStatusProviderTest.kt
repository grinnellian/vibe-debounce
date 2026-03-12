package com.vibedebounce.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ServiceStatusProviderTest {

    @Test
    fun statusText_returnsRunning_whenServiceIsRunning() {
        val provider = FakeServiceStatusProvider(running = true, count = 0)
        assertEquals("Running", provider.statusText())
    }

    @Test
    fun statusText_returnsStopped_whenServiceIsNotRunning() {
        val provider = FakeServiceStatusProvider(running = false, count = 0)
        assertEquals("Stopped", provider.statusText())
    }

    @Test
    fun activeWindowsText_showsCountWithPlural() {
        val provider = FakeServiceStatusProvider(running = true, count = 3)
        assertEquals("3 active debounce windows", provider.activeWindowsText())
    }

    @Test
    fun activeWindowsText_showsZero() {
        val provider = FakeServiceStatusProvider(running = true, count = 0)
        assertEquals("0 active debounce windows", provider.activeWindowsText())
    }

    @Test
    fun activeWindowsText_usesSingularForCountOne() {
        val provider = FakeServiceStatusProvider(running = true, count = 1)
        assertEquals("1 active debounce window", provider.activeWindowsText())
    }
}
