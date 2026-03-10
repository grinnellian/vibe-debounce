package com.vibedebounce.ui

class FakeServiceStatusProvider(
    private val running: Boolean,
    private val count: Int
) : ServiceStatusProviderContract {
    override fun isServiceRunning(): Boolean = running
    override fun activeWindowCount(): Int = count
}
