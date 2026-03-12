package com.vibedebounce.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibedebounce.databinding.ActivityMainBinding
import com.vibedebounce.prefs.AppPrefs
import com.vibedebounce.prefs.DebouncePrefs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var debouncePrefs: DebouncePrefs
    private lateinit var appPrefs: AppPrefs
    private lateinit var permissionChecker: PermissionChecker
    private lateinit var permissionGuard: PermissionGuard
    private lateinit var serviceStatusProvider: ServiceStatusProviderContract
    private lateinit var appListAdapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        debouncePrefs = DebouncePrefs(this)
        appPrefs = AppPrefs(this)
        permissionChecker = PermissionChecker(this)
        permissionGuard = PermissionGuard(permissionChecker)
        serviceStatusProvider = ServiceStatusProvider()

        setupDebounceSlider()
        setupPermissionButtons()
        setupAppList()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        updateServiceStatus()
        populateAppList()
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

        // Update individual permission statuses
        val notificationAccessGranted = permissionChecker.isNotificationListenerEnabled()
        val dndAccessGranted = permissionChecker.isDndAccessGranted()
        binding.statusNotificationAccess.text = if (notificationAccessGranted) "Granted" else "Not granted"
        binding.statusDndAccess.text = if (dndAccessGranted) "Granted" else "Not granted"

        // Update inline permission explanations
        val states = permissionGuard.permissionRowStates()
        for (state in states) {
            when (state.permission) {
                Permission.NOTIFICATION_LISTENER -> {
                    binding.explanationNotificationAccess.visibility = if (state.granted) View.GONE else View.VISIBLE
                    binding.checkmarkNotificationAccess.visibility = if (state.granted) View.VISIBLE else View.GONE
                    binding.btnNotificationAccess.visibility = if (state.granted) View.GONE else View.VISIBLE
                }
                Permission.DND_ACCESS -> {
                    binding.explanationDndAccess.visibility = if (state.granted) View.GONE else View.VISIBLE
                    binding.checkmarkDndAccess.visibility = if (state.granted) View.VISIBLE else View.GONE
                    binding.btnDndAccess.visibility = if (state.granted) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun setupAppList() {
        appListAdapter = AppListAdapter { packageName, enabled ->
            appPrefs.setAppEnabled(packageName, enabled)
        }
        binding.appList.layoutManager = LinearLayoutManager(this)
        binding.appList.adapter = appListAdapter
    }

    private fun populateAppList() {
        val seenPackages = appPrefs.getSeenPackages().sorted()
        if (seenPackages.isEmpty()) {
            binding.appListEmpty.visibility = View.VISIBLE
            binding.appList.visibility = View.GONE
            return
        }

        binding.appListEmpty.visibility = View.GONE
        binding.appList.visibility = View.VISIBLE

        val pm = packageManager
        val items = seenPackages.map { pkg ->
            val appInfo = try {
                pm.getApplicationInfo(pkg, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
            AppItem(
                packageName = pkg,
                label = appInfo?.let { pm.getApplicationLabel(it).toString() } ?: pkg,
                icon = appInfo?.let { pm.getApplicationIcon(it) },
                enabled = appPrefs.isAppEnabled(pkg)
            )
        }
        appListAdapter.submitList(items)
    }
}
