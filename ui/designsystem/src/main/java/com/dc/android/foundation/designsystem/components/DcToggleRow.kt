package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.dc.android.foundation.designsystem.tokens.AccessibilityTokens
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

/** Visual control used by [DcToggleRow]; the entire row remains the interactive target. */
enum class DcToggleControl { Checkbox, Switch, Radio }

/**
 * Form toggle whose label and supporting text are part of the same 48dp interactive target as
 * its control. This prevents the common small-control-only hit area.
 */
@Composable
fun DcToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    control: DcToggleControl = DcToggleControl.Checkbox,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AccessibilityTokens.MinTouchTarget)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = when (control) {
                    DcToggleControl.Checkbox -> Role.Checkbox
                    DcToggleControl.Switch -> Role.Switch
                    DcToggleControl.Radio -> Role.RadioButton
                },
                onValueChange = onCheckedChange
            )
            .padding(horizontal = LocalDcSpacing.current.medium, vertical = LocalDcSpacing.current.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LocalDcSpacing.current.small)
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
            DcText(label, style = MaterialTheme.typography.bodyLarge)
            supportingText?.let { DcText(it, style = MaterialTheme.typography.bodyMedium) }
        }
        when (control) {
            DcToggleControl.Checkbox -> Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)
            DcToggleControl.Switch -> Switch(checked = checked, onCheckedChange = null, enabled = enabled)
            DcToggleControl.Radio -> RadioButton(selected = checked, onClick = null, enabled = enabled)
        }
    }
}
