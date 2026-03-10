package com.vibedebounce.ui

import org.junit.Assert.*
import org.junit.Test

class PermissionGuardTest {

    @Test
    fun `guard is active when any permission missing`() {
        val guard = PermissionGuard(FakePermissionChecker(notificationListener = false, dndAccess = true))
        assertTrue(guard.isGuardActive())
    }

    @Test
    fun `guard is inactive when all permissions granted`() {
        val guard = PermissionGuard(FakePermissionChecker(notificationListener = true, dndAccess = true))
        assertFalse(guard.isGuardActive())
    }

    @Test
    fun `settings section visible only when guard inactive`() {
        val guard = PermissionGuard(FakePermissionChecker(notificationListener = true, dndAccess = true))
        assertTrue(guard.shouldShowSettings())
    }

    @Test
    fun `settings section hidden when guard active`() {
        val guard = PermissionGuard(FakePermissionChecker(notificationListener = false, dndAccess = false))
        assertFalse(guard.shouldShowSettings())
    }

    @Test
    fun `guard card visible when guard active`() {
        val guard = PermissionGuard(FakePermissionChecker(notificationListener = false, dndAccess = true))
        assertTrue(guard.shouldShowGuardCard())
    }

    @Test
    fun `guard card hidden when guard inactive`() {
        val guard = PermissionGuard(FakePermissionChecker(notificationListener = true, dndAccess = true))
        assertFalse(guard.shouldShowGuardCard())
    }

    @Test
    fun `explanation text includes notification listener when missing`() {
        val guard = PermissionGuard(FakePermissionChecker(notificationListener = false, dndAccess = true))
        val explanations = guard.missingPermissionExplanations()
        assertEquals(1, explanations.size)
        assertEquals(Permission.NOTIFICATION_LISTENER, explanations[0].permission)
    }

    @Test
    fun `explanation text includes both when both missing`() {
        val guard = PermissionGuard(FakePermissionChecker(notificationListener = false, dndAccess = false))
        val explanations = guard.missingPermissionExplanations()
        assertEquals(2, explanations.size)
    }
}
