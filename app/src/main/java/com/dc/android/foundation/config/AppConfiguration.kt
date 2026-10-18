package com.dc.android.foundation.config

import com.dc.android.foundation.designsystem.theme.DcUiScale

/** Theme preference resolved by the application shell, not by individual screens. */
enum class AppThemeMode {
    System,
    Light,
    Dark;

    fun resolveDarkTheme(systemInDarkTheme: Boolean): Boolean = when (this) {
        System -> systemInDarkTheme
        Light -> false
        Dark -> true
    }
}

/** User-controlled configuration that can be applied without changing business features. */
data class AppConfiguration(
    val themeMode: AppThemeMode = AppThemeMode.System,
    val uiScale: DcUiScale = DcUiScale.Standard,
    val dynamicColor: Boolean = false
)
