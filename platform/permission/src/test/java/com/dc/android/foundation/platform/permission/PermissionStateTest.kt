package com.dc.android.foundation.platform.permission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * MP:PERM-01 — pure-logic tests for [PermissionState] and [NotificationPermission.fromRequestResult].
 *
 * Not covered here (needs Context/Build.VERSION; would require Robolectric or an instrumented test):
 * [NotificationPermission.currentState] — pre-API-33 => Granted, API 33+ => live OS grant state.
 */
class PermissionStateTest {

    @Test
    fun fromRequestResult_true_mapsToGranted() {
        assertSame(PermissionState.Granted, NotificationPermission.fromRequestResult(true))
    }

    @Test
    fun fromRequestResult_false_mapsToDenied() {
        assertSame(PermissionState.Denied, NotificationPermission.fromRequestResult(false))
    }

    @Test
    fun fromRequestResult_neverReturnsNotRequested() {
        val results = listOf(true, false).map { NotificationPermission.fromRequestResult(it) }

        results.forEach { assertNotEquals(PermissionState.NotRequested, it) }
    }

    @Test
    fun transition_notRequested_toGranted_afterApprovedRequest() {
        val before: PermissionState = PermissionState.NotRequested

        val after = NotificationPermission.fromRequestResult(granted = true)

        assertNotEquals(before, after)
        assertEquals(PermissionState.Granted, after)
    }

    @Test
    fun transition_notRequested_toDenied_afterRejectedRequest() {
        val before: PermissionState = PermissionState.NotRequested

        val after = NotificationPermission.fromRequestResult(granted = false)

        assertNotEquals(before, after)
        assertEquals(PermissionState.Denied, after)
    }

    @Test
    fun transition_denied_toGranted_whenUserGrantsOnRetry() {
        val first = NotificationPermission.fromRequestResult(granted = false)
        val second = NotificationPermission.fromRequestResult(granted = true)

        assertEquals(PermissionState.Denied, first)
        assertEquals(PermissionState.Granted, second)
    }

    @Test
    fun transition_granted_toDenied_whenRevokedAndRechecked() {
        val first = NotificationPermission.fromRequestResult(granted = true)
        val second = NotificationPermission.fromRequestResult(granted = false)

        assertEquals(PermissionState.Granted, first)
        assertEquals(PermissionState.Denied, second)
    }

    @Test
    fun permissionState_hasThreeDistinctStates() {
        val states = listOf(
            PermissionState.NotRequested,
            PermissionState.Granted,
            PermissionState.Denied
        )

        assertEquals(3, states.toSet().size)
    }

    @Test
    fun exhaustiveWhen_coversEveryPermissionState() {
        // Compile-time guard: a new PermissionState (e.g. a "permanently denied" state) must be
        // handled here, prompting the matching transition tests to be added.
        fun label(state: PermissionState): String = when (state) {
            PermissionState.NotRequested -> "notRequested"
            PermissionState.Granted -> "granted"
            PermissionState.Denied -> "denied"
        }

        assertEquals("notRequested", label(PermissionState.NotRequested))
        assertEquals("granted", label(PermissionState.Granted))
        assertEquals("denied", label(PermissionState.Denied))
    }

    @Test
    fun permissionName_isPostNotificationsPlatformString() {
        assertEquals("android.permission.POST_NOTIFICATIONS", NotificationPermission.permissionName)
    }
}
