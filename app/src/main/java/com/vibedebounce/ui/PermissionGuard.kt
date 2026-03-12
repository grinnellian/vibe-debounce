package com.vibedebounce.ui

data class PermissionRowState(
    val permission: Permission,
    val granted: Boolean
)

class PermissionGuard(private val checker: PermissionCheckerContract) {

    fun isGuardActive(): Boolean = !checker.allPermissionsGranted()

    fun shouldShowSettings(): Boolean = !isGuardActive()

    fun shouldShowGuardCard(): Boolean = isGuardActive()

    fun permissionRowStates(): List<PermissionRowState> {
        val missing = checker.missingPermissions().toSet()
        return listOf(Permission.NOTIFICATION_LISTENER, Permission.DND_ACCESS).map { perm ->
            PermissionRowState(permission = perm, granted = perm !in missing)
        }
    }
}
