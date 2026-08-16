package com.dc.android.foundation.greeting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private val NAMES = listOf("Android", "World", "DC")
private const val KEY_NAME_INDEX = "nameIndex"

/**
 * Canonical feature shape (05-presentation-udf.md): UiState down via [uiState], actions up
 * via [onAction], one-shot events via [effects]. [nameIndex] survives process death through
 * [SavedStateHandle] rather than being silently lost (04-app-bootstrap-and-lifecycle.md).
 */
@HiltViewModel
class GreetingViewModel @Inject constructor(
    private val greetingProvider: GreetingProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var nameIndex: Int
        get() = savedStateHandle[KEY_NAME_INDEX] ?: 0
        set(value) {
            savedStateHandle[KEY_NAME_INDEX] = value
        }

    private val _uiState = MutableStateFlow(
        GreetingUiState(message = greetingProvider.greetingFor(NAMES[nameIndex]))
    )
    val uiState: StateFlow<GreetingUiState> = _uiState.asStateFlow()

    private val _effects = Channel<GreetingUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onAction(action: GreetingUiAction) {
        when (action) {
            GreetingUiAction.Refresh -> refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            nameIndex = (nameIndex + 1) % NAMES.size
            val message = greetingProvider.greetingFor(NAMES[nameIndex])
            _uiState.update { it.copy(message = message, isRefreshing = false) }
            _effects.send(GreetingUiEffect.ShowMessage("Refreshed"))
        }
    }
}
