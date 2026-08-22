package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.dc.android.foundation.designsystem.tokens.AccessibilityTokens

/**
 * Standard list row with an optional semantic button action. Interactive rows centrally enforce
 * the accessibility touch target rather than making each feature reimplement click handling.
 */
@Composable
fun DcListRow(
    headline: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val clickModifier = onClick?.let { click ->
        Modifier.clickable(enabled = enabled, role = Role.Button, onClick = click)
    } ?: Modifier

    ListItem(
        headlineContent = {
            Text(text = headline, style = MaterialTheme.typography.bodyLarge)
        },
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AccessibilityTokens.MinTouchTarget)
            .then(clickModifier),
        supportingContent = supportingText?.let { text ->
            { Text(text = text, style = MaterialTheme.typography.bodyMedium) }
        },
        trailingContent = trailingContent
    )
}
