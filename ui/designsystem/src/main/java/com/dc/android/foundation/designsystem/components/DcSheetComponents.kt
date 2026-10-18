package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

/**
 * Bottom sheet listing actions. Prefer it to [DcDropdownMenu] on phones when entries are numerous,
 * need icons/descriptions, or are destructive. The caller owns whether it is composed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DcActionSheet(
    items: List<DcMenuItem>,
    onItemClick: (DcMenuItem) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    DcModalBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier) {
        title?.let {
            DcSectionHeader(it, contentPadding = PaddingValues(horizontal = LocalDcSpacing.current.medium))
        }
        items.forEach { item ->
            DcListRow(
                headline = item.label,
                enabled = item.enabled,
                leadingContent = item.icon?.let { icon ->
                    { DcIcon(icon, contentDescription = null) }
                },
                onClick = {
                    onItemClick(item)
                    onDismissRequest()
                }
            )
        }
    }
}

/**
 * Bottom sheet for choosing one typed option; use instead of a dropdown for long lists. The
 * selected row is marked with `selected` semantics and a check icon (not color alone).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DcSelectionSheet(
    title: String,
    options: List<DcOption<T>>,
    selected: T?,
    onSelect: (T) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    DcModalBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier) {
        DcSectionHeader(title, contentPadding = PaddingValues(horizontal = LocalDcSpacing.current.medium))
        options.forEach { option ->
            val isSelected = option.value == selected
            DcListRow(
                headline = option.label,
                supportingText = option.supportingText,
                enabled = option.enabled,
                selected = isSelected,
                trailingContent = if (isSelected) {
                    { DcIcon(Icons.Filled.Check, contentDescription = null) }
                } else {
                    null
                },
                onClick = {
                    onSelect(option.value)
                    onDismissRequest()
                }
            )
        }
    }
}
