package com.vibedebounce.ui

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.vibedebounce.databinding.ActivityMainBinding
import com.vibedebounce.prefs.DebouncePrefs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var debouncePrefs: DebouncePrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        debouncePrefs = DebouncePrefs(this)
        setupDebounceSlider()
        setupPermissionButtons()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun setupDebounceSlider() {
        binding.debounceSlider.max = 180
        val saved = debouncePrefs.debounceWindowSeconds
        binding.debounceSlider.progress = saved
        updateSliderLabel(saved)

        binding.debounceSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = maxOf(progress, 15)
                updateSliderLabel(value)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val value = maxOf(seekBar?.progress ?: DebouncePrefs.DEFAULT_SECONDS, 15)
                debouncePrefs.debounceWindowSeconds = value
            }
        })
    }

    private fun updateSliderLabel(seconds: Int) {
        binding.debounceValue.text = "${seconds}s"
    }

    private fun setupPermissionButtons() {
        binding.btnNotificationAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        binding.btnDndAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }
    }

    private fun updatePermissionStatus() {
        val notificationAccessGranted = isNotificationListenerEnabled()
        val dndAccessGranted = isDndAccessGranted()

        binding.statusNotificationAccess.text = if (notificationAccessGranted) "Granted" else "Not granted"
        binding.statusDndAccess.text = if (dndAccessGranted) "Granted" else "Not granted"

        binding.btnNotificationAccess.isEnabled = !notificationAccessGranted
        binding.btnDndAccess.isEnabled = !dndAccessGranted
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val cn = ComponentName(this, "com.vibedebounce.service.DebounceNotificationService")
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: return false
        return flat.contains(cn.flattenToString())
    }

    private fun isDndAccessGranted(): Boolean {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.isNotificationPolicyAccessGranted
    }
}
