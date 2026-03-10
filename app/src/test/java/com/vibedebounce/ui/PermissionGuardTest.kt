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
    fun `permissionRowStates returns not-granted for missing notification listener`() {
        val guard = PermissionGuard(FakePermissionChecker(notificationListener = false, dndAccess = true))
        val states = guard.permissionRowStates()
        val nl = states.first { it.permission == Permission.NOTIFICATION_LISTENER }
        assertFalse(nl.granted)
    }

    @Test
    fun `permissionRowStates returns granted for granted notification listener`() {
        val guard = PermissionGuard(FakePermissionChecker(notificationListener = true, dndAccess = true))
        val states = guard.permissionRowStates()
        val nl = states.first { it.permission == Permission.NOTIFICATION_LISTENER }
        assertTrue(nl.granted)
    }

    @Test
    fun `permissionRowStates returns not-granted for missing dnd access`() {
        val guard = PermissionGuard(FakePermissionChecker(notificationListener = true, dndAccess = false))
        val states = guard.permissionRowStates()
        val dnd = states.first { it.permission == Permission.DND_ACCESS }
        assertFalse(dnd.granted)
    }

    @Test
    fun `permissionRowStates returns granted for granted dnd access`() {
        val guard = PermissionGuard(FakePermissionChecker(notificationListener = true, dndAccess = true))
        val states = guard.permissionRowStates()
        val dnd = states.first { it.permission == Permission.DND_ACCESS }
        assertTrue(dnd.granted)
    }

    @Test
    fun `permissionRowStates always returns both permissions`() {
        val guard = PermissionGuard(FakePermissionChecker(notificationListener = false, dndAccess = false))
        val states = guard.permissionRowStates()
        assertEquals(2, states.size)
        assertTrue(states.any { it.permission == Permission.NOTIFICATION_LISTENER })
        assertTrue(states.any { it.permission == Permission.DND_ACCESS })
    }

    @Test
    fun `permissionRowStates all granted when all permissions granted`() {
        val guard = PermissionGuard(FakePermissionChecker(notificationListener = true, dndAccess = true))
        val states = guard.permissionRowStates()
        assertTrue(states.all { it.granted })
    }
}
