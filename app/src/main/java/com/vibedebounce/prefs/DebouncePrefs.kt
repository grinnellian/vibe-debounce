package com.vibedebounce.prefs

import android.content.Context
import android.content.SharedPreferences

class DebouncePrefs(context: Context) {

    companion object {
        private const val PREFS_FILE = "debounce_prefs"
        private const val KEY_DEBOUNCE_SECONDS = "debounce_window_seconds"
        const val DEFAULT_SECONDS = 90
    }

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    var debounceWindowSeconds: Int
        get() = sharedPrefs.getInt(KEY_DEBOUNCE_SECONDS, DEFAULT_SECONDS)
        set(value) {
            sharedPrefs.edit().putInt(KEY_DEBOUNCE_SECONDS, value).apply()
        }
}
