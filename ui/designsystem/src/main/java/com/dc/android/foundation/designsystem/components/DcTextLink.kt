package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import com.dc.android.foundation.designsystem.tokens.AccessibilityTokens

/** Inline-style link with a 48dp target and button semantics for assistive technology. */
@Composable
fun DcTextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    DcText(
        text = text,
        modifier = modifier
            .defaultMinSize(minHeight = AccessibilityTokens.MinTouchTarget)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.Underline),
        color = MaterialTheme.colorScheme.primary
    )
}
