package com.vibedebounce.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.vibedebounce.R
import com.vibedebounce.model.DebounceTimer
import com.vibedebounce.model.SenderKey

class DebounceNotificationService : NotificationListenerService() {

    companion object {
        const val CHANNEL_ID = "debounce_new_thread"
        const val DEFAULT_DEBOUNCE_MS = 90_000L
    }

    private lateinit var ringerStateManager: RingerStateManager
    private val activeTimers = mutableMapOf<SenderKey, DebounceTimer>()
    private var debounceWindowMs = DEFAULT_DEBOUNCE_MS

    override fun onCreate() {
        super.onCreate()
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        ringerStateManager = RingerStateManager(audioManager)
        createNotificationChannel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val key = SenderKey(sbn.packageName, title)

        val existingTimer = activeTimers[key]
        if (existingTimer != null) {
            existingTimer.reset(debounceWindowMs)
            return
        }

        if (ringerStateManager.isActive()) {
            fireNewThreadNotification(title)
        }

        ringerStateManager.mute()
        val timer = DebounceTimer { expiredKey -> onTimerExpired(expiredKey) }
        timer.start(key, debounceWindowMs)
        activeTimers[key] = timer
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // We don't cancel debounce when notifications are dismissed
    }

    private fun onTimerExpired(key: SenderKey) {
        activeTimers.remove(key)
        ringerStateManager.release()
    }

    private fun fireNewThreadNotification(senderName: String) {
        val savedMode = (getSystemService(Context.AUDIO_SERVICE) as AudioManager).ringerMode
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.ringerMode = AudioManager.RINGER_MODE_NORMAL

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New conversation")
            .setContentText("Message from $senderName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(senderName.hashCode(), notification)

        am.ringerMode = savedMode
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "New conversation alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when a new sender messages during an active debounce"
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    fun setDebounceWindow(ms: Long) {
        debounceWindowMs = ms
    }
}
