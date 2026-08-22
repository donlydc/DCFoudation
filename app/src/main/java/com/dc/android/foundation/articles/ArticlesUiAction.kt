package com.dc.android.foundation.articles

import com.dc.android.foundation.platform.permission.PermissionState

/** User intents [ArticlesScreen] can send back up to [ArticlesViewModel]. */
sealed interface ArticlesUiAction {
    data object Refresh : ArticlesUiAction

    /** User tapped "Enable notifications" — ask [ArticlesViewModel] to trigger the OS dialog. */
    data object RequestNotificationPermission : ArticlesUiAction

    /** Route reports that the current user-visible message has completed display. */
    data object UserMessageShown : ArticlesUiAction

    /** [ArticlesRoute] reporting a permission check/request result back into the UDF loop. */
    data class UpdateNotificationPermissionState(val state: PermissionState) : ArticlesUiAction
}
