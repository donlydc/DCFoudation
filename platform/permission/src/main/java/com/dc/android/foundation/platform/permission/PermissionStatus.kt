package com.dc.android.foundation.platform.permission

/**
 * Richer, additive permission model (MP:PERM-01). [PermissionState] is intentionally left
 * unchanged (app `when`s are exhaustive over it); this type distinguishes the cases the master
 * plan requires: denied vs denied-with-rationale vs permanently denied vs unavailable on this OS.
 */
sealed interface PermissionStatus {
    data object NotRequested : PermissionStatus
    data object Granted : PermissionStatus
    /** Denied; the OS will still show its dialog, UI may show rationale first. */
    data object DeniedShouldShowRationale : PermissionStatus
    /** Denied and the OS will not prompt again; only app settings can fix it. */
    data object PermanentlyDenied : PermissionStatus
    /** Permission does not exist / is not needed on this OS level. */
    data object Unavailable : PermissionStatus

    /** Lossy projection to the legacy 3-state model. */
    fun toState(): PermissionState = when (this) {
        Granted, Unavailable -> PermissionState.Granted
        NotRequested -> PermissionState.NotRequested
        DeniedShouldShowRationale, PermanentlyDenied -> PermissionState.Denied
    }
}

/** Pure Android permission semantics, no framework calls. */
object PermissionStatusResolver {
    /**
     * @param supportedOnThisOs false when the permission is not defined on the running API level.
     * @param granted live grant state.
     * @param shouldShowRationale `shouldShowRequestPermissionRationale` result.
     * @param requestedBefore whether the app has launched a request for it already (app-tracked,
     *   since the OS cannot distinguish "never asked" from "permanently denied" without it).
     */
    fun resolve(
        supportedOnThisOs: Boolean,
        granted: Boolean,
        shouldShowRationale: Boolean,
        requestedBefore: Boolean
    ): PermissionStatus = when {
        !supportedOnThisOs -> PermissionStatus.Unavailable
        granted -> PermissionStatus.Granted
        shouldShowRationale -> PermissionStatus.DeniedShouldShowRationale
        requestedBefore -> PermissionStatus.PermanentlyDenied
        else -> PermissionStatus.NotRequested
    }
}
