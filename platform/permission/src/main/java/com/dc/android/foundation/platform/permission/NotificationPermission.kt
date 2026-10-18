package com.dc.android.foundation.platform.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * The one real [PermissionState] call site this repo has: `android.permission.POST_NOTIFICATIONS`
 * (PERM-01). Hides the raw `ContextCompat`/`Build.VERSION` branching feature code would otherwise
 * need to duplicate.
 */
object NotificationPermission {

    // Kept as the platform string so reading this API is safe even when the caller runs pre-33.
    const val permissionName: String = "android.permission.POST_NOTIFICATIONS"

    /**
     * Below API 33 posting a notification never required a runtime permission, so this always
     * reports [PermissionState.Granted] pre-33. On 33+, reflects the live OS grant state —
     * [PermissionState.NotRequested] is never returned here (a plain `checkSelfPermission` call
     * can't distinguish "never asked" from "asked and denied"); callers seed their own
     * `NotRequested` default and only call this to learn `Granted`/`Denied`.
     */
    fun currentState(context: Context): PermissionState =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            PermissionState.Granted
        } else if (ContextCompat.checkSelfPermission(context, permissionName) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            PermissionState.Granted
        } else {
            PermissionState.Denied
        }

    /** Maps a Compose `rememberLauncherForActivityResult` request-result boolean to [PermissionState]. */
    fun fromRequestResult(granted: Boolean): PermissionState =
        if (granted) PermissionState.Granted else PermissionState.Denied
}
