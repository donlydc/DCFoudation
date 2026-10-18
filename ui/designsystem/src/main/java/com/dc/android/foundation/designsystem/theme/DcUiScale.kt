package com.dc.android.foundation.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import com.dc.android.foundation.designsystem.tokens.DcSpacing

/**
 * User-selectable application UI scale.
 *
 * This scales semantic spacing and typography while leaving the 48dp accessibility touch floor
 * unchanged. System font scaling still applies on top of the selected value.
 */
enum class DcUiScale(internal val factor: Float) {
    Compact(0.90f),
    Standard(1.00f),
    Comfortable(1.10f)
}

internal fun DcUiScale.applyTo(spacing: DcSpacing): DcSpacing = spacing.copy(
    xSmall = spacing.xSmall * factor,
    small = spacing.small * factor,
    medium = spacing.medium * factor,
    large = spacing.large * factor,
    xLarge = spacing.xLarge * factor,
    xxLarge = spacing.xxLarge * factor,
    component = spacing.component * factor
)

internal fun DcUiScale.applyTo(typography: Typography): Typography = typography.copy(
    displayLarge = typography.displayLarge.scaledBy(factor),
    displayMedium = typography.displayMedium.scaledBy(factor),
    displaySmall = typography.displaySmall.scaledBy(factor),
    headlineLarge = typography.headlineLarge.scaledBy(factor),
    headlineMedium = typography.headlineMedium.scaledBy(factor),
    headlineSmall = typography.headlineSmall.scaledBy(factor),
    titleLarge = typography.titleLarge.scaledBy(factor),
    titleMedium = typography.titleMedium.scaledBy(factor),
    titleSmall = typography.titleSmall.scaledBy(factor),
    bodyLarge = typography.bodyLarge.scaledBy(factor),
    bodyMedium = typography.bodyMedium.scaledBy(factor),
    bodySmall = typography.bodySmall.scaledBy(factor),
    labelLarge = typography.labelLarge.scaledBy(factor),
    labelMedium = typography.labelMedium.scaledBy(factor),
    labelSmall = typography.labelSmall.scaledBy(factor)
)

private fun TextStyle.scaledBy(factor: Float): TextStyle = copy(
    fontSize = fontSize * factor,
    lineHeight = lineHeight * factor
)
