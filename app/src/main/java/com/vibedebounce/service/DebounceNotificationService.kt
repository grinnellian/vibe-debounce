package com.vibedebounce.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Vibrator
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.vibedebounce.model.DebounceTimer
import com.vibedebounce.model.SenderKey
import com.vibedebounce.prefs.DebouncePrefs

class DebounceNotificationService : NotificationListenerService() {

    companion object {
        const val DEFAULT_DEBOUNCE_MS = DebouncePrefs.DEFAULT_SECONDS * 1000L
    }

    private lateinit var ringerStateManager: RingerStateManager
    private lateinit var notifier: NewThreadNotifier
    private lateinit var debouncePrefs: DebouncePrefs
    private val activeTimers = mutableMapOf<SenderKey, DebounceTimer>()
    private var debounceWindowMs = DEFAULT_DEBOUNCE_MS

    override fun onCreate() {
        super.onCreate()
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        ringerStateManager = RingerStateManager(audioManager)
        debouncePrefs = DebouncePrefs(this)
        debounceWindowMs = debouncePrefs.debounceWindowSeconds * 1000L

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifier = NewThreadNotifier(this, vibrator, nm)
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
            notifier.fire(title)
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

    fun setDebounceWindow(ms: Long) {
        debounceWindowMs = ms
        debouncePrefs.debounceWindowSeconds = (ms / 1000).toInt()
    }
}
