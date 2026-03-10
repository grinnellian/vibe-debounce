package com.vibedebounce.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.vibedebounce.R

class ForegroundNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "debounce_service_status"
        const val NOTIFICATION_ID = 1

        fun statusText(activeCount: Int): String = when {
            activeCount == 0 -> "Monitoring notifications"
            activeCount == 1 -> "Debouncing 1 sender"
            else -> "Debouncing $activeCount senders"
        }
    }

    init {
        createChannel()
    }

    fun buildNotification(activeCount: Int) =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Debounce")
            .setContentText(statusText(activeCount))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Service status",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Ongoing notification while debounce service is active"
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }
}
