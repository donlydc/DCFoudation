package com.dc.android.foundation.greeting

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Minimal Gate D proof: a real @HiltViewModel with a constructor-injected dependency.
 * Intentionally not yet the full Route/UiState/UiAction/UiEffect contract — Phase E
 * (05-presentation-udf.md) formalizes this into the canonical feature shape.
 */
@HiltViewModel
class GreetingViewModel @Inject constructor(
    greetingProvider: GreetingProvider
) : ViewModel() {

    private val _greeting = MutableStateFlow(greetingProvider.greetingFor("Android"))
    val greeting: StateFlow<String> = _greeting.asStateFlow()
}
