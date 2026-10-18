package com.dc.android.foundation.designsystem.tokens

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Corner-radius scale backing [com.dc.android.foundation.designsystem.theme.DcShapes]. */
object ShapeTokens {
    val None: CornerBasedShape = RoundedCornerShape(0.dp)
    val ExtraSmall: CornerBasedShape = RoundedCornerShape(4.dp)
    val Small: CornerBasedShape = RoundedCornerShape(8.dp)
    val Medium: CornerBasedShape = RoundedCornerShape(12.dp)
    val Large: CornerBasedShape = RoundedCornerShape(16.dp)
    val ExtraLarge: CornerBasedShape = RoundedCornerShape(20.dp)
    val Full: CornerBasedShape = RoundedCornerShape(50)
}
