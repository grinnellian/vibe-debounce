package com.vibedebounce.prefs

import android.content.Context

class AppPrefs(context: Context) {

    companion object {
        private const val PREFS_FILE = "app_prefs"
        private const val KEY_SEEN_PACKAGES = "seen_packages"
        private const val KEY_PREFIX_ENABLED = "enabled_"
    }

    private val sharedPrefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    fun getSeenPackages(): Set<String> =
        sharedPrefs.getStringSet(KEY_SEEN_PACKAGES, emptySet()) ?: emptySet()

    fun addSeenPackage(packageName: String) {
        val current = getSeenPackages().toMutableSet()
        if (current.add(packageName)) {
            sharedPrefs.edit().putStringSet(KEY_SEEN_PACKAGES, current).apply()
        }
    }

    fun isAppEnabled(packageName: String): Boolean =
        sharedPrefs.getBoolean(KEY_PREFIX_ENABLED + packageName, true)

    fun setAppEnabled(packageName: String, enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_PREFIX_ENABLED + packageName, enabled).apply()
    }
}
