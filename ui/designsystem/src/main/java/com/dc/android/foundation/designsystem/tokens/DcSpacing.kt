package com.dc.android.foundation.designsystem.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Product-selectable spacing scale. Supply a different instance to [LocalDcSpacing] through
 * `DcTheme` to change an application's density without changing component call sites.
 *
 * Scale usage: `xSmall` (4dp) separates related text lines, `small` (8dp) separates a label
 * from its control, `component` (12dp) separates controls in one block, `medium` (16dp) is
 * normal screen/card padding, `large` (24dp) separates sections, and `xLarge` (32dp)
 * separates major sections.
 */
data class DcSpacing(
    val none: Dp = 0.dp,
    val xSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val xLarge: Dp = 32.dp,
    val xxLarge: Dp = 48.dp,
    /** Spacing between adjacent controls in the same content block. */
    val component: Dp = 12.dp
) {
    companion object {
        val Default = DcSpacing()
        val Compact = DcSpacing(
            small = 6.dp,
            medium = 12.dp,
            large = 20.dp,
            xLarge = 28.dp,
            component = 8.dp
        )
    }
}

/** The spacing scale provided by [com.dc.android.foundation.designsystem.theme.DcTheme]. */
val LocalDcSpacing = staticCompositionLocalOf { DcSpacing.Default }
