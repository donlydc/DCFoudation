package com.dc.android.foundation.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.dc.android.foundation.designsystem.tokens.ColorTokens
import com.dc.android.foundation.designsystem.tokens.DcSpacing
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

private val LightColorScheme = lightColorScheme(
    primary = ColorTokens.Blue600,
    onPrimary = ColorTokens.White,
    primaryContainer = ColorTokens.Blue100,
    onPrimaryContainer = ColorTokens.Blue900,
    secondary = ColorTokens.Grey500,
    onSecondary = ColorTokens.White,
    secondaryContainer = ColorTokens.Grey100,
    onSecondaryContainer = ColorTokens.Grey900,
    tertiary = ColorTokens.Blue700,
    onTertiary = ColorTokens.White,
    tertiaryContainer = ColorTokens.Blue100,
    onTertiaryContainer = ColorTokens.Blue900,
    background = ColorTokens.Grey50,
    onBackground = ColorTokens.Grey900,
    surface = ColorTokens.White,
    onSurface = ColorTokens.Grey900,
    surfaceVariant = ColorTokens.Grey100,
    onSurfaceVariant = ColorTokens.Grey500,
    surfaceContainerLowest = ColorTokens.White,
    surfaceContainerLow = ColorTokens.White,
    surfaceContainer = ColorTokens.Grey50,
    surfaceContainerHigh = ColorTokens.Grey100,
    surfaceContainerHighest = ColorTokens.Grey100,
    outline = ColorTokens.Grey300,
    outlineVariant = ColorTokens.Grey200,
    error = ColorTokens.Red600,
    onError = ColorTokens.White,
    errorContainer = ColorTokens.Red100,
    onErrorContainer = ColorTokens.Red900,
    inverseSurface = ColorTokens.Grey900,
    inverseOnSurface = ColorTokens.Grey50,
    inversePrimary = ColorTokens.Blue300,
    scrim = Color(0xFF000000)
)

private val DarkColorScheme = darkColorScheme(
    primary = ColorTokens.Blue300,
    onPrimary = ColorTokens.Blue900,
    primaryContainer = ColorTokens.Blue800,
    onPrimaryContainer = ColorTokens.Blue100,
    secondary = ColorTokens.Grey400,
    onSecondary = ColorTokens.Ink900,
    secondaryContainer = ColorTokens.Ink600,
    onSecondaryContainer = ColorTokens.Grey200,
    tertiary = ColorTokens.Blue300,
    onTertiary = ColorTokens.Blue900,
    tertiaryContainer = ColorTokens.Blue800,
    onTertiaryContainer = ColorTokens.Blue100,
    background = ColorTokens.Ink900,
    onBackground = ColorTokens.Grey200,
    surface = ColorTokens.Ink800,
    onSurface = ColorTokens.Grey200,
    surfaceVariant = ColorTokens.Ink700,
    onSurfaceVariant = ColorTokens.Grey400,
    surfaceContainerLowest = ColorTokens.Ink900,
    surfaceContainerLow = ColorTokens.Ink800,
    surfaceContainer = ColorTokens.Ink800,
    surfaceContainerHigh = ColorTokens.Ink700,
    surfaceContainerHighest = ColorTokens.Ink600,
    outline = ColorTokens.Ink500,
    outlineVariant = ColorTokens.Ink600,
    error = ColorTokens.Red300,
    onError = ColorTokens.Red900,
    errorContainer = ColorTokens.Red900,
    onErrorContainer = ColorTokens.Red100,
    inverseSurface = ColorTokens.Grey200,
    inverseOnSurface = ColorTokens.Ink900,
    inversePrimary = ColorTokens.Blue600,
    scrim = Color(0xFF000000)
)

/**
 * Semantic theme entry point. Every screen roots its Compose tree here; feature UI reads
 * colors/typography/shapes via `MaterialTheme.*` inside this scope rather than importing
 * [ColorTokens] or other primitive tokens directly (DS-01, INV-08).
 */
@Composable
fun DcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    colorScheme: ColorScheme = DcThemeDefaults.colorScheme(darkTheme, dynamicColor),
    typography: Typography = DcThemeDefaults.Typography,
    shapes: Shapes = DcThemeDefaults.Shapes,
    extendedColors: DcColors = DcThemeDefaults.extendedColors(darkTheme),
    spacing: DcSpacing = DcSpacing.Default,
    uiScale: DcUiScale = DcUiScale.Standard,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalDcSpacing provides uiScale.applyTo(spacing),
        LocalDcColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = uiScale.applyTo(typography),
            shapes = shapes,
            content = content
        )
    }
}

/** Defaults for [DcTheme]. Components derive their own defaults from this theme layer. */
object DcThemeDefaults {
    val Typography: Typography = DcTypography
    val Shapes: Shapes = DcShapes

    @Composable
    fun colorScheme(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    fun extendedColors(darkTheme: Boolean): DcColors = if (darkTheme) {
        DcColors(
            success = ColorTokens.Green300,
            onSuccess = ColorTokens.Green900,
            successContainer = ColorTokens.Green900,
            onSuccessContainer = ColorTokens.Green100,
            warning = ColorTokens.Amber300,
            onWarning = ColorTokens.Amber900,
            warningContainer = ColorTokens.Amber900,
            onWarningContainer = ColorTokens.Amber100,
            info = ColorTokens.Blue300,
            onInfo = ColorTokens.Blue900,
            infoContainer = ColorTokens.Blue800,
            onInfoContainer = ColorTokens.Blue100,
            offline = ColorTokens.Red300,
            onOffline = ColorTokens.Red900
        )
    } else {
        DcColors(
            success = ColorTokens.Green600,
            onSuccess = ColorTokens.White,
            successContainer = ColorTokens.Green100,
            onSuccessContainer = ColorTokens.Green900,
            warning = ColorTokens.Amber500,
            onWarning = ColorTokens.Grey900,
            warningContainer = ColorTokens.Amber100,
            onWarningContainer = ColorTokens.Amber900,
            info = ColorTokens.Blue600,
            onInfo = ColorTokens.White,
            infoContainer = ColorTokens.Blue100,
            onInfoContainer = ColorTokens.Blue900,
            offline = ColorTokens.Red600,
            onOffline = ColorTokens.White
        )
    }
}
