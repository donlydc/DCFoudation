package com.dc.android.foundation.platform.permission

/**
 * DC-owned, engine-neutral runtime-permission state (PERM-01, DEP-04, INV-07). Feature code reads
 * this instead of calling `ActivityCompat.checkSelfPermission`/`shouldShowRequestPermissionRationale`
 * directly — same "infra type hides the raw Android API" shape as `Destination`
 * (`:platform:navigation`) and `PreferencesDataSource` (`:data:preferences`).
 *
 * Deliberately three states, not four: this repo's one real call site (`ArticlesRoute` requesting
 * `POST_NOTIFICATIONS`) never needs to distinguish "denied once" from "permanently denied, only
 * settings can fix it" — that distinction requires tracking `shouldShowRequestPermissionRationale`
 * across requests plus a settings deep link, which is a real rationale-dialog flow this feature
 * doesn't build (see `13-permissions.md` PERM-01 for why that stays a documented, not-yet-built
 * extension point).
 */
sealed interface PermissionState {
    /** No request/check has resolved a definite answer yet. */
    data object NotRequested : PermissionState

    /** The permission is currently granted. */
    data object Granted : PermissionState

    /** The permission was checked or requested and is not granted. */
    data object Denied : PermissionState
}
