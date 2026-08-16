package com.dc.android.foundation.designsystem.tokens

import androidx.compose.ui.graphics.Color

/**
 * Primitive color palette. Not brand-final — this is the neutral seed palette carried
 * over from the stock template until a real brand spec exists. Feature UI must not
 * reference these directly; go through [com.dc.android.foundation.designsystem.theme.DcTheme]'s
 * semantic MaterialTheme.colorScheme instead (DS-01).
 */
object ColorTokens {
    val Purple80 = Color(0xFFD0BCFF)
    val PurpleGrey80 = Color(0xFFCCC2DC)
    val Pink80 = Color(0xFFEFB8C8)

    val Purple40 = Color(0xFF6650A4)
    val PurpleGrey40 = Color(0xFF625B71)
    val Pink40 = Color(0xFF7D5260)
}
