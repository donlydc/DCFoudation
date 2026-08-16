package com.dc.android.foundation.articles

/** User intents [ArticlesScreen] can send back up to [ArticlesViewModel]. */
sealed interface ArticlesUiAction {
    data object Refresh : ArticlesUiAction
}
