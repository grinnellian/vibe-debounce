package com.vibedebounce

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.vibedebounce.prefs.DebouncePrefs
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DebouncePrefsListenerTest {

    private lateinit var prefs: DebouncePrefs

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        prefs = DebouncePrefs(context)
    }

    @Test
    fun `registerOnChangeListener fires when debounceWindowSeconds changes`() {
        var firedKey: String? = null
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            firedKey = key
        }
        prefs.registerOnChangeListener(listener)
        prefs.debounceWindowSeconds = 42
        assertEquals(DebouncePrefs.KEY_DEBOUNCE_SECONDS, firedKey)
        prefs.unregisterOnChangeListener(listener)
    }

    @Test
    fun `unregisterOnChangeListener stops notifications`() {
        var callCount = 0
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            callCount++
        }
        prefs.registerOnChangeListener(listener)
        prefs.debounceWindowSeconds = 50
        prefs.unregisterOnChangeListener(listener)
        prefs.debounceWindowSeconds = 60
        assertEquals(1, callCount)
    }
}
