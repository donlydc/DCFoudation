package com.dc.android.foundation.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colors that are not represented by Material3's [androidx.compose.material3.ColorScheme].
 * Apps may replace this entire set from [DcTheme] to match their brand.
 */
data class DcColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
    val offline: Color,
    val onOffline: Color
)

/** Extended semantic colors provided by [DcTheme]. */
val LocalDcColors = staticCompositionLocalOf { DcThemeDefaults.extendedColors(false) }
