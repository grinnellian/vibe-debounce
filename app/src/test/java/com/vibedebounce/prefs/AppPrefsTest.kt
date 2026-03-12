package com.vibedebounce.prefs

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppPrefsTest {

    private lateinit var context: Context
    private lateinit var appPrefs: AppPrefs

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        appPrefs = AppPrefs(context)
    }

    @Test
    fun `getSeenPackages returns empty set initially`() {
        assertEquals(emptySet<String>(), appPrefs.getSeenPackages())
    }

    @Test
    fun `addSeenPackage adds a package`() {
        appPrefs.addSeenPackage("com.example.app")
        assertTrue(appPrefs.getSeenPackages().contains("com.example.app"))
    }

    @Test
    fun `addSeenPackage is idempotent`() {
        appPrefs.addSeenPackage("com.example.app")
        appPrefs.addSeenPackage("com.example.app")
        assertEquals(1, appPrefs.getSeenPackages().size)
    }

    @Test
    fun `addSeenPackage accumulates multiple packages`() {
        appPrefs.addSeenPackage("com.example.one")
        appPrefs.addSeenPackage("com.example.two")
        appPrefs.addSeenPackage("com.example.three")
        assertEquals(3, appPrefs.getSeenPackages().size)
        assertTrue(appPrefs.getSeenPackages().containsAll(
            setOf("com.example.one", "com.example.two", "com.example.three")
        ))
    }

    @Test
    fun `isAppEnabled defaults to true`() {
        assertTrue(appPrefs.isAppEnabled("com.example.app"))
    }

    @Test
    fun `setAppEnabled false disables app`() {
        appPrefs.setAppEnabled("com.example.app", false)
        assertFalse(appPrefs.isAppEnabled("com.example.app"))
    }

    @Test
    fun `setAppEnabled true re-enables app`() {
        appPrefs.setAppEnabled("com.example.app", false)
        appPrefs.setAppEnabled("com.example.app", true)
        assertTrue(appPrefs.isAppEnabled("com.example.app"))
    }

    @Test
    fun `per-app state is independent`() {
        appPrefs.setAppEnabled("com.example.one", false)
        appPrefs.setAppEnabled("com.example.two", true)
        assertFalse(appPrefs.isAppEnabled("com.example.one"))
        assertTrue(appPrefs.isAppEnabled("com.example.two"))
    }

    @Test
    fun `data persists across instances`() {
        appPrefs.addSeenPackage("com.example.app")
        appPrefs.setAppEnabled("com.example.app", false)

        val appPrefs2 = AppPrefs(context)
        assertTrue(appPrefs2.getSeenPackages().contains("com.example.app"))
        assertFalse(appPrefs2.isAppEnabled("com.example.app"))
    }
}
