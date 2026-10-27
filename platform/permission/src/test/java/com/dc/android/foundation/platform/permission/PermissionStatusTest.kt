package com.dc.android.foundation.platform.permission

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionStatusTest {
    private fun r(sup: Boolean = true, g: Boolean = false, s: Boolean = false, b: Boolean = false) =
        PermissionStatusResolver.resolve(sup, g, s, b)

    @Test fun unsupported_isUnavailable_evenIfGranted() = assertEquals(PermissionStatus.Unavailable, r(sup = false, g = true))
    @Test fun granted() = assertEquals(PermissionStatus.Granted, r(g = true))
    @Test fun rationale() = assertEquals(PermissionStatus.DeniedShouldShowRationale, r(s = true, b = true))
    @Test fun requestedNoRationale_isPermanent() = assertEquals(PermissionStatus.PermanentlyDenied, r(b = true))
    @Test fun neverAsked() = assertEquals(PermissionStatus.NotRequested, r())

    @Test fun toState_projection() {
        assertEquals(PermissionState.Granted, PermissionStatus.Unavailable.toState())
        assertEquals(PermissionState.Denied, PermissionStatus.PermanentlyDenied.toState())
        assertEquals(PermissionState.Denied, PermissionStatus.DeniedShouldShowRationale.toState())
        assertEquals(PermissionState.NotRequested, PermissionStatus.NotRequested.toState())
    }

    @Test fun tracker_blocksDoubleRequest_untilComplete() {
        val t = PermissionRequestTracker()
        assertTrue(t.tryBegin("p"))
        assertFalse(t.tryBegin("p"))
        assertTrue(t.tryBegin("q"))
        t.complete("p")
        assertTrue(t.tryBegin("p"))
    }

    @Test fun fake_recordsRequestsAndUpdatesStatus() = runTest {
        val f = FakePermissionRequester(onRequest = { _, _ -> PermissionStatus.PermanentlyDenied })
        assertEquals(PermissionStatus.NotRequested, f.check("p"))
        assertEquals(PermissionStatus.PermanentlyDenied, f.request("p"))
        assertEquals(PermissionStatus.PermanentlyDenied, f.check("p"))
        assertEquals(listOf("p"), f.requests)
    }
}
