package com.dc.android.foundation.articles

/** Everything [ArticlesScreen] needs to render. UDF state flows down. */
data class ArticlesUiState(
    val articles: List<Article> = emptyList(),
    val isRefreshing: Boolean = false,
    /** `true` when the last refresh failed and the list shown is cached/stale data (DAT-CACHE-01). */
    val isShowingStaleData: Boolean = false
)
