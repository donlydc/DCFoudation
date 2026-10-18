package com.dc.android.foundation.designsystem.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Elevation scale. The style guide favors thin borders over shadows: Level0 is the default,
 * Level1 separates a surface from its background, and Level2/Level3 are reserved for floating
 * layers such as menus and FABs.
 */
object ElevationTokens {
    val Level0: Dp = 0.dp
    val Level1: Dp = 1.dp
    val Level2: Dp = 3.dp
    val Level3: Dp = 6.dp
}

/** Border-width scale. One hairline width keeps foundation surfaces visually quiet. */
object StrokeTokens {
    val Hairline: Dp = 1.dp
}
