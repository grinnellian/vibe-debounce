package com.vibedebounce.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.vibedebounce.R

/**
 * Fires a vibration and optionally posts a notification for a new conversation thread.
 * Uses the Vibrator API directly -- does NOT touch ringer mode.
 * This is safe to call regardless of the current ringer state because
 * Vibrator.vibrate() works in silent mode when the app holds VIBRATE permission.
 */
class NewThreadNotifier(
    private val context: Context,
    private val vibrator: Vibrator,
    private val notificationManager: NotificationManager
) {
    companion object {
        const val CHANNEL_ID = "debounce_new_thread"
        const val VIBRATION_DURATION_MS = 200L
    }

    init {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "New conversation alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when a new sender messages during an active debounce"
        }
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Fires a vibration using the Vibrator API directly.
     * Does NOT post a notification. Does NOT touch ringer mode.
     * Safe to call regardless of current ringer state.
     */
    fun vibrate() {
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                VIBRATION_DURATION_MS,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }

    /**
     * Vibrates and posts a notification for a new conversation thread.
     * Uses Vibrator API directly -- does NOT touch ringer mode.
     * Safe to call regardless of current ringer state.
     */
    fun fire(senderName: String) {
        vibrate()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New conversation")
            .setContentText("Message from $senderName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(senderName.hashCode(), notification)
    }
}
