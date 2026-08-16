package com.dc.android.foundation.articles

/** One-shot events — consumed exactly once by [ArticlesRoute], never replayed as state. */
sealed interface ArticlesUiEffect {
    data class ShowMessage(val text: String) : ArticlesUiEffect

    /** Tells [ArticlesRoute] (the only layer allowed to touch the real permission API) to launch the OS dialog. */
    data object LaunchNotificationPermissionRequest : ArticlesUiEffect
}
