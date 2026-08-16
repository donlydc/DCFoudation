package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dc.android.foundation.designsystem.tokens.AccessibilityTokens

/**
 * Semantic button. Feature UI uses this instead of Material3 `Button` directly so the
 * minimum touch target ([AccessibilityTokens.MinTouchTarget]) is enforced in one place (DS-02)
 * rather than every call site remembering to size it.
 */
@Composable
fun DcButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(
            minWidth = AccessibilityTokens.MinTouchTarget,
            minHeight = AccessibilityTokens.MinTouchTarget
        ),
        enabled = enabled
    ) {
        Text(text)
    }
}
