package com.vibedebounce.ui

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.provider.Settings

enum class Permission {
    NOTIFICATION_LISTENER,
    DND_ACCESS
}

interface PermissionCheckerContract {
    fun isNotificationListenerEnabled(): Boolean
    fun isDndAccessGranted(): Boolean

    fun allPermissionsGranted(): Boolean =
        isNotificationListenerEnabled() && isDndAccessGranted()

    fun missingPermissions(): List<Permission> = buildList {
        if (!isNotificationListenerEnabled()) add(Permission.NOTIFICATION_LISTENER)
        if (!isDndAccessGranted()) add(Permission.DND_ACCESS)
    }
}

class PermissionChecker(private val context: Context) : PermissionCheckerContract {

    override fun isNotificationListenerEnabled(): Boolean {
        val cn = ComponentName(context, "com.vibedebounce.service.DebounceNotificationService")
        val flat = Settings.Secure.getString(
            context.contentResolver, "enabled_notification_listeners"
        ) ?: return false
        return flat.contains(cn.flattenToString())
    }

    override fun isDndAccessGranted(): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.isNotificationPolicyAccessGranted
    }
}
