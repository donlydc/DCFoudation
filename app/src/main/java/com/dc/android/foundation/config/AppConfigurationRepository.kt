package com.dc.android.foundation.config

import com.dc.android.foundation.data.preferences.PreferenceKey
import com.dc.android.foundation.data.preferences.PreferencesDataSource
import com.dc.android.foundation.designsystem.theme.DcUiScale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Application-level mapping over the generic preferences boundary.
 *
 * It owns stable keys and exposes typed functions so screens and ViewModels never manipulate raw
 * DataStore keys. Locale is intentionally not duplicated here; Android/AppCompat owns that state.
 */
@Singleton
class AppConfigurationRepository @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource
) {
    val configuration: Flow<AppConfiguration> = combine(
        preferencesDataSource.observe(THEME_MODE_KEY),
        preferencesDataSource.observe(UI_SCALE_KEY),
        preferencesDataSource.observe(DYNAMIC_COLOR_KEY)
    ) { themeMode, uiScale, dynamicColor ->
        AppConfiguration(
            themeMode = themeMode,
            uiScale = uiScale,
            dynamicColor = dynamicColor
        )
    }

    suspend fun setThemeMode(value: AppThemeMode) {
        preferencesDataSource.set(THEME_MODE_KEY, value)
    }

    suspend fun setUiScale(value: DcUiScale) {
        preferencesDataSource.set(UI_SCALE_KEY, value)
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        preferencesDataSource.set(DYNAMIC_COLOR_KEY, enabled)
    }

    /** Replaces all appearance values in one atomic preference transaction. */
    suspend fun update(value: AppConfiguration) {
        preferencesDataSource.edit {
            set(THEME_MODE_KEY, value.themeMode)
            set(UI_SCALE_KEY, value.uiScale)
            set(DYNAMIC_COLOR_KEY, value.dynamicColor)
        }
    }

    /** Restores application-owned settings; the system/app language preference is unchanged. */
    suspend fun reset() {
        preferencesDataSource.edit {
            remove(THEME_MODE_KEY)
            remove(UI_SCALE_KEY)
            remove(DYNAMIC_COLOR_KEY)
        }
    }

    private companion object {
        val THEME_MODE_KEY = PreferenceKey.EnumKey(
            name = "app_theme_mode",
            default = AppThemeMode.System,
            values = AppThemeMode.entries.toTypedArray()
        )
        val UI_SCALE_KEY = PreferenceKey.EnumKey(
            name = "app_ui_scale",
            default = DcUiScale.Standard,
            values = DcUiScale.entries.toTypedArray()
        )
        val DYNAMIC_COLOR_KEY = PreferenceKey.BooleanKey(
            name = "app_dynamic_color",
            default = false
        )
    }
}
