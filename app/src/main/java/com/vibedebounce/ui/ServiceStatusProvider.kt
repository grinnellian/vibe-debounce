package com.vibedebounce.ui

import com.vibedebounce.service.DebounceNotificationService

interface ServiceStatusProviderContract {
    fun isServiceRunning(): Boolean
    fun activeWindowCount(): Int
    fun statusText(): String = if (isServiceRunning()) "Running" else "Stopped"
    fun activeWindowsText(): String {
        val count = activeWindowCount()
        val noun = if (count == 1) "window" else "windows"
        return "$count active debounce $noun"
    }
}

class ServiceStatusProvider : ServiceStatusProviderContract {
    override fun isServiceRunning(): Boolean = DebounceNotificationService.isRunning
    override fun activeWindowCount(): Int = DebounceNotificationService.activeWindowCount
}
