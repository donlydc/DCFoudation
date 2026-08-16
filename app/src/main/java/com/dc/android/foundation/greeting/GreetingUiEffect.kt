package com.dc.android.foundation.greeting

/** One-shot events — consumed exactly once by [GreetingRoute], never replayed as state. */
sealed interface GreetingUiEffect {
    data class ShowMessage(val text: String) : GreetingUiEffect
}
