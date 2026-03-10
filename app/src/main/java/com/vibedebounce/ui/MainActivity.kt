package com.vibedebounce.ui

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vibedebounce.databinding.ActivityMainBinding
import com.vibedebounce.prefs.DebouncePrefs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var debouncePrefs: DebouncePrefs
    private lateinit var permissionChecker: PermissionChecker
    private lateinit var permissionGuard: PermissionGuard
    private lateinit var serviceStatusProvider: ServiceStatusProviderContract

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        debouncePrefs = DebouncePrefs(this)
        permissionChecker = PermissionChecker(this)
        permissionGuard = PermissionGuard(permissionChecker)
        serviceStatusProvider = ServiceStatusProvider()

        setupDebounceSlider()
        setupPermissionButtons()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        updateServiceStatus()
    }

    private fun updateServiceStatus() {
        binding.serviceStatusValue.text = serviceStatusProvider.statusText()
        binding.activeWindowsValue.text = serviceStatusProvider.activeWindowsText()
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
        val showSettings = permissionGuard.shouldShowSettings()

        binding.settingsSection.visibility = if (showSettings) View.VISIBLE else View.GONE
        binding.permissionGuardCard.visibility = if (permissionGuard.shouldShowGuardCard()) View.VISIBLE else View.GONE

        if (permissionGuard.shouldShowGuardCard()) {
            populateExplanations(permissionGuard.missingPermissionExplanations())
        }

        // Update individual permission statuses
        val notificationAccessGranted = permissionChecker.isNotificationListenerEnabled()
        val dndAccessGranted = permissionChecker.isDndAccessGranted()
        binding.statusNotificationAccess.text = if (notificationAccessGranted) "Granted" else "Not granted"
        binding.statusDndAccess.text = if (dndAccessGranted) "Granted" else "Not granted"
        binding.btnNotificationAccess.isEnabled = !notificationAccessGranted
        binding.btnDndAccess.isEnabled = !dndAccessGranted
    }

    private fun populateExplanations(explanations: List<PermissionExplanation>) {
        binding.permissionExplanationsContainer.removeAllViews()
        for (explanation in explanations) {
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.bottomMargin = (12 * resources.displayMetrics.density).toInt()
                layoutParams = params
            }

            val titleView = TextView(this).apply {
                text = explanation.title
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
            }

            val descView = TextView(this).apply {
                text = explanation.explanation
                textSize = 13f
            }

            container.addView(titleView)
            container.addView(descView)
            binding.permissionExplanationsContainer.addView(container)
        }
    }
}
