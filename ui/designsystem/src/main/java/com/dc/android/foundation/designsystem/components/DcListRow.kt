package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.dc.android.foundation.designsystem.preview.DcThemePreviews
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.tokens.AccessibilityTokens
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

/**
 * Standard list row with theme-driven layout and accessibility policy. Clickable rows centrally
 * enforce the touch floor; callers may use the slot overload for a custom internal layout.
 */
@Composable
fun DcListRow(
    headline: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    ListItem(
        supportingContent = supportingText?.let {
            {
                DcText(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        modifier = modifier.rowModifier(selected, enabled, onClick, onLongClick)
    ) {
        DcText(
            text = headline,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Slot-based list row that retains selected, ripple, semantics and touch-target policy. */
@Composable
fun DcListRow(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.rowModifier(selected, enabled, onClick, onLongClick)
            .padding(
                horizontal = LocalDcSpacing.current.medium,
                vertical = LocalDcSpacing.current.component
            ),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun Modifier.rowModifier(
    selected: Boolean,
    enabled: Boolean,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?
): Modifier {
    val interactionModifier = when {
        onLongClick != null -> Modifier.combinedClickable(
            enabled = enabled,
            role = Role.Button,
            onClick = onClick ?: {},
            onLongClick = onLongClick
        )
        onClick != null -> Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
        else -> Modifier
    }
    return this
        .fillMaxWidth()
        .defaultMinSize(minHeight = AccessibilityTokens.MinTouchTarget)
        .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
        .then(interactionModifier)
}

@DcThemePreviews
@Composable
private fun DcListRowPreview() {
    DcTheme {
        Column(verticalArrangement = Arrangement.spacedBy(LocalDcSpacing.current.small)) {
            DcListRow(headline = "List item")
            DcListRow(
                headline = "Two-line item",
                supportingText = "Supporting information uses the secondary text color"
            )
            DcListRow(
                headline = "Selected item",
                supportingText = "Selected state uses the primary container",
                selected = true,
                onClick = {}
            )
        }
    }
}
