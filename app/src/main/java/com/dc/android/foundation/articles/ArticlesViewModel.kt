package com.dc.android.foundation.articles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dc.android.foundation.observability.Logger
import com.dc.android.foundation.runtime.concurrency.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val LOG_TAG = "ArticlesViewModel"

/**
 * Canonical feature shape (05-presentation-udf.md), same UiState/onAction/effects contract as
 * `GreetingViewModel`. [articlesRepository] is the real DAT-CACHE-01 instance: Room emits
 * immediately via [ArticlesRepository.observeArticles], and [ArticlesRepository.refresh] is what
 * `Refresh` triggers. Network/DB work runs on [dispatcherProvider]'s `io` dispatcher, never a
 * hard-coded `Dispatchers.*` (09-concurrency-and-threading.md).
 */
@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val articlesRepository: ArticlesRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val logger: Logger,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticlesUiState())
    val uiState: StateFlow<ArticlesUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ArticlesUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            articlesRepository.observeArticles().collect { articles ->
                _uiState.update { it.copy(articles = articles) }
            }
        }
    }

    fun onAction(action: ArticlesUiAction) {
        when (action) {
            ArticlesUiAction.Refresh -> refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val succeeded = withContext(dispatcherProvider.io) {
                articlesRepository.refresh()
            }
            // Count/status only — never log article title/body content (SEC-01).
            logger.d(LOG_TAG, "refresh: succeeded=$succeeded")
            _uiState.update { it.copy(isRefreshing = false, isShowingStaleData = !succeeded) }
            _effects.send(
                if (succeeded) {
                    ArticlesUiEffect.ShowMessage("Refreshed")
                } else {
                    ArticlesUiEffect.ShowMessage("Refresh failed, showing cached data")
                }
            )
        }
    }
}
