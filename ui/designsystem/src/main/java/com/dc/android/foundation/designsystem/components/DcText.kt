package com.dc.android.foundation.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

/** Semantic text. Defaults to `MaterialTheme.typography.bodyLarge` instead of a raw [TextStyle] (DS-01). */
@Composable
fun DcText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge
) {
    Text(text = text, modifier = modifier, style = style)
}
