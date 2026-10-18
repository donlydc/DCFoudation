package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.dc.android.foundation.designsystem.R
import com.dc.android.foundation.designsystem.preview.DcThemePreviews
import com.dc.android.foundation.designsystem.theme.DcTheme

/**
 * Controlled popup menu. The caller owns [expanded]; every item row is at least 48dp (Material
 * menu item) and destructive items use the error color in addition to their text label. Selecting
 * an item reports it and then requests dismissal.
 */
@Composable
fun DcDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<DcMenuItem>,
    onItemClick: (DcMenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        items.forEach { item ->
            val contentColor = if (item.destructive) MaterialTheme.colorScheme.error else Color.Unspecified
            DropdownMenuItem(
                text = { DcText(item.label, color = contentColor) },
                onClick = {
                    onItemClick(item)
                    onDismissRequest()
                },
                enabled = item.enabled,
                leadingIcon = item.icon?.let { icon ->
                    { DcIcon(icon, contentDescription = null) }
                }
            )
        }
    }
}

/**
 * "More options" icon button with its own menu. Expansion is pure UI state and is kept internally
 * so app bars do not need to hoist it. Place inside `DcTopAppBar(actions = …)` for actions beyond
 * the two or three that stay visible.
 */
@Composable
fun DcOverflowMenu(
    items: List<DcMenuItem>,
    onItemClick: (DcMenuItem) -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = stringResource(R.string.dc_more_options)
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box(modifier) {
        DcIconButton(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = contentDescription,
            onClick = { expanded = true }
        )
        DcDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            items = items,
            onItemClick = onItemClick
        )
    }
}

/**
 * Plain tooltip shown on long-press/hover/keyboard focus. It supplements, never replaces, a
 * control's own `contentDescription`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DcTooltip(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
        tooltip = { PlainTooltip { DcText(text, style = MaterialTheme.typography.bodySmall) } },
        state = rememberTooltipState(),
        modifier = modifier,
        content = content
    )
}

@DcThemePreviews
@Composable
private fun DcOverflowMenuPreview() {
    DcTheme {
        DcOverflowMenu(
            items = listOf(DcMenuItem("share", "Share"), DcMenuItem("delete", "Delete", destructive = true)),
            onItemClick = {}
        )
    }
}
