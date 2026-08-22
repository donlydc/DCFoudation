package com.dc.android.foundation.articles

/** UI-integration effects that cannot be represented as rendered screen state. */
sealed interface ArticlesUiEffect {
    /** Tells [ArticlesRoute] (the only layer allowed to touch the real permission API) to launch the OS dialog. */
    data object LaunchNotificationPermissionRequest : ArticlesUiEffect
}
