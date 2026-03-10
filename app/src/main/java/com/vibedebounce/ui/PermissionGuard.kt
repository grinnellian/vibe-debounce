package com.vibedebounce.ui

data class PermissionExplanation(
    val permission: Permission,
    val title: String,
    val explanation: String
)

class PermissionGuard(private val checker: PermissionCheckerContract) {

    companion object {
        private val EXPLANATIONS = mapOf(
            Permission.NOTIFICATION_LISTENER to PermissionExplanation(
                permission = Permission.NOTIFICATION_LISTENER,
                title = "Notification Access",
                explanation = "Lets Debounce see when notifications arrive so it can detect message bursts from the same sender. Your message content is never read or stored."
            ),
            Permission.DND_ACCESS to PermissionExplanation(
                permission = Permission.DND_ACCESS,
                title = "Do Not Disturb Access",
                explanation = "Lets Debounce briefly silence your phone when repeat messages arrive, then restore your previous sound setting automatically."
            )
        )
    }

    fun isGuardActive(): Boolean = !checker.allPermissionsGranted()

    fun shouldShowSettings(): Boolean = !isGuardActive()

    fun shouldShowGuardCard(): Boolean = isGuardActive()

    fun missingPermissionExplanations(): List<PermissionExplanation> =
        checker.missingPermissions().mapNotNull { EXPLANATIONS[it] }
}
