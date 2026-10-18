package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import com.dc.android.foundation.designsystem.preview.DcThemePreviews
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.tokens.AccessibilityTokens
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

/** Layout of [DcChipGroup]. */
enum class DcChipGroupLayout { Wrap, Scroll }

/**
 * Single-choice segmented control for two to five short, mutually exclusive options. For more
 * options use [DcDropdownField] or [DcSelectionSheet].
 */
@Composable
fun <T> DcSegmentedButton(
    options: List<DcOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option.value == selected,
                onClick = { onSelect(option.value) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                enabled = enabled && option.enabled,
                label = { DcText(option.label, maxLines = 1, style = MaterialTheme.typography.labelLarge) }
            )
        }
    }
}

/** Multi-choice variant of [DcSegmentedButton]. */
@Composable
fun <T> DcMultiSegmentedButton(
    options: List<DcOption<T>>,
    selected: Set<T>,
    onSelectionChange: (Set<T>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    MultiChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                checked = option.value in selected,
                onCheckedChange = {
                    onSelectionChange(toggleSelection(selected, option.value, DcSelectionMode.Multiple))
                },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                enabled = enabled && option.enabled,
                label = { DcText(option.label, maxLines = 1, style = MaterialTheme.typography.labelLarge) }
            )
        }
    }
}

@Composable
private fun GroupLabel(label: String?) {
    label?.let {
        DcText(
            text = it,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .padding(horizontal = LocalDcSpacing.current.medium, vertical = LocalDcSpacing.current.small)
                .semantics { heading() }
        )
    }
}

/** Mutually exclusive radio options exposed as one accessibility group. */
@Composable
fun <T> DcRadioGroup(
    options: List<DcOption<T>>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    Column(modifier.selectableGroup()) {
        GroupLabel(label)
        options.forEach { option ->
            DcToggleRow(
                label = option.label,
                checked = option.value == selected,
                onCheckedChange = { onSelect(option.value) },
                supportingText = option.supportingText,
                control = DcToggleControl.Radio,
                enabled = option.enabled
            )
        }
    }
}

/**
 * Independent checkbox options. When [selectAllLabel] is set a leading tri-state row selects or
 * clears every enabled option.
 */
@Composable
fun <T> DcCheckboxGroup(
    options: List<DcOption<T>>,
    selected: Set<T>,
    onSelectionChange: (Set<T>) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    selectAllLabel: String? = null
) {
    val selectable = options.filter { it.enabled }.map { it.value }.toSet()
    Column(modifier.selectableGroup()) {
        GroupLabel(label)
        if (selectAllLabel != null) {
            val state = selectAllState(selected, selectable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = AccessibilityTokens.MinTouchTarget)
                    .triStateToggleable(
                        state = state,
                        role = Role.Checkbox,
                        onClick = {
                            onSelectionChange(
                                if (state == ToggleableState.On) selected - selectable else selected + selectable
                            )
                        }
                    )
                    .padding(horizontal = LocalDcSpacing.current.medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LocalDcSpacing.current.small)
            ) {
                DcText(selectAllLabel, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                TriStateCheckbox(state = state, onClick = null)
            }
        }
        options.forEach { option ->
            DcToggleRow(
                label = option.label,
                checked = option.value in selected,
                onCheckedChange = {
                    onSelectionChange(toggleSelection(selected, option.value, DcSelectionMode.Multiple))
                },
                supportingText = option.supportingText,
                control = DcToggleControl.Checkbox,
                enabled = option.enabled
            )
        }
    }
}

internal fun <T> selectAllState(selected: Set<T>, selectable: Set<T>): ToggleableState = when {
    selectable.isEmpty() || selected.none { it in selectable } -> ToggleableState.Off
    selectable.all { it in selected } -> ToggleableState.On
    else -> ToggleableState.Indeterminate
}

/** Filter chips driven by [DcSelectionMode]; wraps by default or scrolls horizontally. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> DcChipGroup(
    options: List<DcOption<T>>,
    selected: Set<T>,
    onSelectionChange: (Set<T>) -> Unit,
    modifier: Modifier = Modifier,
    selectionMode: DcSelectionMode = DcSelectionMode.Single,
    layout: DcChipGroupLayout = DcChipGroupLayout.Wrap
) {
    val spacing = LocalDcSpacing.current
    val chip: @Composable (DcOption<T>) -> Unit = { option ->
        DcChip(
            label = option.label,
            selected = option.value in selected,
            onClick = { onSelectionChange(toggleSelection(selected, option.value, selectionMode)) },
            enabled = option.enabled
        )
    }
    when (layout) {
        DcChipGroupLayout.Wrap -> FlowRow(
            modifier = modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.small)
        ) { options.forEach { chip(it) } }

        DcChipGroupLayout.Scroll -> LazyRow(
            modifier = modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(spacing.small)
        ) { items(options) { chip(it) } }
    }
}

/** Typed dropdown; selection is by [DcOption.value] rather than by label text. */
@Composable
fun <T> DcDropdownField(
    label: String,
    options: List<DcOption<T>>,
    selected: T?,
    onSelect: (T) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = ""
) {
    val selectedLabel = options.firstOrNull { it.value == selected }?.label ?: placeholder
    DcDropdownField(
        label = label,
        selectedLabel = selectedLabel,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        options = options.map { it.label },
        onOptionSelected = { chosen ->
            options.firstOrNull { it.label == chosen && it.enabled }?.let { onSelect(it.value) }
        },
        modifier = modifier,
        enabled = enabled
    )
}

@DcThemePreviews
@Composable
private fun DcSelectionPreview() {
    DcTheme {
        val options = listOf(DcOption(1, "Day"), DcOption(2, "Week"), DcOption(3, "Month"))
        Column(verticalArrangement = Arrangement.spacedBy(LocalDcSpacing.current.medium)) {
            DcSegmentedButton(options, selected = 1, onSelect = {})
            DcChipGroup(options, selected = setOf(2), onSelectionChange = {})
            DcRadioGroup(options, selected = 3, onSelect = {}, label = "Range")
            DcCheckboxGroup(options, selected = setOf(1), onSelectionChange = {}, selectAllLabel = "All")
        }
    }
}
