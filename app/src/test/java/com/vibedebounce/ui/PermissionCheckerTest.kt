package com.vibedebounce.ui

import org.junit.Assert.*
import org.junit.Test

class PermissionCheckerTest {

    @Test
    fun `allPermissionsGranted returns true when both permissions granted`() {
        val checker = FakePermissionChecker(notificationListener = true, dndAccess = true)
        assertTrue(checker.allPermissionsGranted())
    }

    @Test
    fun `allPermissionsGranted returns false when notification listener missing`() {
        val checker = FakePermissionChecker(notificationListener = false, dndAccess = true)
        assertFalse(checker.allPermissionsGranted())
    }

    @Test
    fun `allPermissionsGranted returns false when DND access missing`() {
        val checker = FakePermissionChecker(notificationListener = true, dndAccess = false)
        assertFalse(checker.allPermissionsGranted())
    }

    @Test
    fun `allPermissionsGranted returns false when both missing`() {
        val checker = FakePermissionChecker(notificationListener = false, dndAccess = false)
        assertFalse(checker.allPermissionsGranted())
    }

    @Test
    fun `missingPermissions returns empty list when all granted`() {
        val checker = FakePermissionChecker(notificationListener = true, dndAccess = true)
        assertTrue(checker.missingPermissions().isEmpty())
    }

    @Test
    fun `missingPermissions returns both when none granted`() {
        val checker = FakePermissionChecker(notificationListener = false, dndAccess = false)
        val missing = checker.missingPermissions()
        assertEquals(2, missing.size)
        assertTrue(missing.contains(Permission.NOTIFICATION_LISTENER))
        assertTrue(missing.contains(Permission.DND_ACCESS))
    }

    @Test
    fun `missingPermissions returns only missing one`() {
        val checker = FakePermissionChecker(notificationListener = true, dndAccess = false)
        val missing = checker.missingPermissions()
        assertEquals(1, missing.size)
        assertTrue(missing.contains(Permission.DND_ACCESS))
    }
}
