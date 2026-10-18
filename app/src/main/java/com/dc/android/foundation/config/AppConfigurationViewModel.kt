package com.dc.android.foundation.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dc.android.foundation.designsystem.theme.DcUiScale
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AppConfigurationAction {
    data class SetThemeMode(val value: AppThemeMode) : AppConfigurationAction
    data class SetUiScale(val value: DcUiScale) : AppConfigurationAction
    data class SetDynamicColor(val enabled: Boolean) : AppConfigurationAction
    data object Reset : AppConfigurationAction
}

/** Process-visible owner for user-controlled application configuration. */
@HiltViewModel
class AppConfigurationViewModel @Inject constructor(
    private val repository: AppConfigurationRepository
) : ViewModel() {

    val configuration: StateFlow<AppConfiguration> = repository.configuration.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppConfiguration()
    )

    fun onAction(action: AppConfigurationAction) {
        viewModelScope.launch {
            when (action) {
                is AppConfigurationAction.SetThemeMode -> repository.setThemeMode(action.value)
                is AppConfigurationAction.SetUiScale -> repository.setUiScale(action.value)
                is AppConfigurationAction.SetDynamicColor -> repository.setDynamicColor(action.enabled)
                AppConfigurationAction.Reset -> repository.reset()
            }
        }
    }
}
