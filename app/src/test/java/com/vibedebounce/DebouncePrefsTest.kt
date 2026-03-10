package com.vibedebounce

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.vibedebounce.prefs.DebouncePrefs
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DebouncePrefsTest {

    private lateinit var prefs: DebouncePrefs

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        prefs = DebouncePrefs(context)
    }

    @Test
    fun `returns default 90 seconds when no value stored`() {
        assertEquals(90, prefs.debounceWindowSeconds)
    }

    @Test
    fun `stores and retrieves debounce window`() {
        prefs.debounceWindowSeconds = 45
        assertEquals(45, prefs.debounceWindowSeconds)
    }

    @Test
    fun `overwrites previous value`() {
        prefs.debounceWindowSeconds = 60
        prefs.debounceWindowSeconds = 120
        assertEquals(120, prefs.debounceWindowSeconds)
    }

    @Test
    fun `persists across new DebouncePrefs instances`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        prefs.debounceWindowSeconds = 30
        val prefs2 = DebouncePrefs(context)
        assertEquals(30, prefs2.debounceWindowSeconds)
    }
}
