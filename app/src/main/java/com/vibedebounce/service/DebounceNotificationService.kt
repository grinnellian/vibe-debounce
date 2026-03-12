package com.vibedebounce.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Vibrator
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.VisibleForTesting
import com.vibedebounce.model.DebounceTimer
import com.vibedebounce.model.SenderKey
import com.vibedebounce.prefs.AppPrefs
import com.vibedebounce.prefs.DebouncePrefs

class DebounceNotificationService : NotificationListenerService() {

    companion object {
        const val DEFAULT_DEBOUNCE_MS = DebouncePrefs.DEFAULT_SECONDS * 1000L

        @Volatile
        var isRunning: Boolean = false
            private set

        @Volatile
        var activeWindowCount: Int = 0
            private set

        internal fun resetState() {
            isRunning = false
            activeWindowCount = 0
        }
    }

    private lateinit var ringerStateManager: RingerStateManager
    private lateinit var notifier: NewThreadNotifier
    private lateinit var debouncePrefs: DebouncePrefs
    private lateinit var appPrefs: AppPrefs
    private lateinit var foregroundNotificationManager: ForegroundNotificationManager
    private val activeTimers = mutableMapOf<SenderKey, DebounceTimer>()

    @VisibleForTesting
    internal var debounceWindowMs = DEFAULT_DEBOUNCE_MS

    @VisibleForTesting
    internal val activeTimerCount: Int get() = activeTimers.size

    @VisibleForTesting
    internal fun getRingerStateManagerForTest(): RingerStateManager = ringerStateManager

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == DebouncePrefs.KEY_DEBOUNCE_SECONDS) {
            debounceWindowMs = debouncePrefs.debounceWindowSeconds * 1000L
        }
    }

    override fun onCreate() {
        super.onCreate()
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        ringerStateManager = RingerStateManager(audioManager)
        debouncePrefs = DebouncePrefs(this)
        appPrefs = AppPrefs(this)
        debounceWindowMs = debouncePrefs.debounceWindowSeconds * 1000L
        debouncePrefs.registerOnChangeListener(prefsListener)

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifier = NewThreadNotifier(this, vibrator, nm)
        foregroundNotificationManager = ForegroundNotificationManager(this)
    }

    override fun onListenerConnected() {
        isRunning = true
        activeWindowCount = activeTimers.size
        startForeground(
            ForegroundNotificationManager.NOTIFICATION_ID,
            foregroundNotificationManager.buildNotification(activeTimers.size)
        )
    }

    override fun onListenerDisconnected() {
        clearAllTimersAndRestore()
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return

        recordSeenApp(sbn.packageName)
        if (!shouldDebounce(sbn.packageName)) return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val key = SenderKey(sbn.packageName, title)

        val existingTimer = activeTimers[key]
        if (existingTimer != null) {
            existingTimer.reset(debounceWindowMs)
            return
        }

        // Always vibrate for the first notification from a new sender.
        // If another debounce window is already active, also post the new-thread notification.
        // fire() calls vibrate() internally, so call one or the other -- not both.
        if (ringerStateManager.isActive()) {
            notifier.fire(title)
        } else {
            notifier.vibrate()
        }

        ringerStateManager.mute()
        val timer = DebounceTimer { expiredKey -> onTimerExpired(expiredKey) }
        timer.start(key, debounceWindowMs)
        activeTimers[key] = timer
        activeWindowCount = activeTimers.size
        updateForegroundNotification()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // We don't cancel debounce when notifications are dismissed
    }

    private fun onTimerExpired(key: SenderKey) {
        activeTimers.remove(key)
        activeWindowCount = activeTimers.size
        ringerStateManager.release()
        updateForegroundNotification()
    }

    private fun updateForegroundNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(
            ForegroundNotificationManager.NOTIFICATION_ID,
            foregroundNotificationManager.buildNotification(activeTimers.size)
        )
    }

    private fun clearAllTimersAndRestore() {
        activeTimers.values.forEach { it.cancel() }
        activeTimers.clear()
        activeWindowCount = 0
        if (::ringerStateManager.isInitialized) {
            ringerStateManager.releaseAll()
        }
    }

    @VisibleForTesting
    internal fun setNotifierForTest(notifier: NewThreadNotifier) {
        this.notifier = notifier
    }

    @VisibleForTesting
    internal fun recordSeenApp(packageName: String) {
        appPrefs.addSeenPackage(packageName)
    }

    @VisibleForTesting
    internal fun shouldDebounce(packageName: String): Boolean {
        return appPrefs.isAppEnabled(packageName)
    }

    override fun onDestroy() {
        clearAllTimersAndRestore()
        debouncePrefs.unregisterOnChangeListener(prefsListener)
        super.onDestroy()
    }
}
