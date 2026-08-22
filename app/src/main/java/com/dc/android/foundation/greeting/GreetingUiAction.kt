package com.dc.android.foundation.greeting

/** User intents [GreetingScreen] can send back up to [GreetingViewModel]. */
sealed interface GreetingUiAction {
    data object Refresh : GreetingUiAction
    data object UserMessageShown : GreetingUiAction
}
