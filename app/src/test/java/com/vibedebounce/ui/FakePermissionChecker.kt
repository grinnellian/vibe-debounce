package com.vibedebounce.ui

class FakePermissionChecker(
    private val notificationListener: Boolean,
    private val dndAccess: Boolean
) : PermissionCheckerContract {
    override fun isNotificationListenerEnabled(): Boolean = notificationListener
    override fun isDndAccessGranted(): Boolean = dndAccess
}
