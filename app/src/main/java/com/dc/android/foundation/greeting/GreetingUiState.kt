package com.dc.android.foundation.greeting

/** Everything [GreetingScreen] needs to render. UDF state flows down. */
data class GreetingUiState(
    val message: String,
    val isRefreshing: Boolean = false,
    /** Durable until the UI reports that it has been displayed. */
    val userMessage: String? = null
)
