package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import com.dc.android.foundation.designsystem.R
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dc.android.foundation.designsystem.preview.DcFontScalePreviews
import com.dc.android.foundation.designsystem.preview.DcThemePreviews
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.tokens.AccessibilityTokens
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

/**
 * Controlled text input that centralizes the minimum touch target and error semantics.
 * Labels and supporting text come from the caller so features can localize them.
 */
@Composable
fun DcTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    errorText: String? = null,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    maxLength: Int? = null,
    showCounter: Boolean = maxLength != null,
    counterContentDescription: ((Int, Int) -> String)? = null
) {
    val hasError = isError || errorText != null
    val support = errorText ?: supportingText
    val showsCounter = showCounter && maxLength != null
    val counterDescription = if (showsCounter) {
        counterContentDescription?.invoke(value.length, maxLength!!)
            ?: stringResource(R.string.dc_character_counter, value.length, maxLength!!)
    } else {
        null
    }

    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(enforceMaxLength(it, maxLength)) },
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .semantics {
                if (hasError && support != null) {
                    error(support)
                }
            },
        enabled = enabled,
        readOnly = readOnly,
        isError = hasError,
        label = { Text(label) },
        supportingText = if (support != null || showsCounter) {
            {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(support.orEmpty(), modifier = Modifier.weight(1f))
                    if (showsCounter) {
                        Text(
                            text = "${value.length}/$maxLength",
                            modifier = Modifier.semantics { contentDescription = counterDescription!! }
                        )
                    }
                }
            }
        } else {
            null
        },
        placeholder = placeholder?.let { text ->
            { DcText(text, style = MaterialTheme.typography.bodyLarge) }
        },
        leadingIcon = leadingIcon?.let { icon ->
            { DcIcon(icon, contentDescription = null) }
        },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        trailingIcon = trailingContent,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * Controlled password input with a locally-owned, saveable reveal toggle. Visibility labels are
 * required so the caller supplies localized, meaningful descriptions for the toggle action.
 */
@Composable
fun DcPasswordTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    showPasswordLabel: String,
    hidePasswordLabel: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val toggleLabel = if (passwordVisible) hidePasswordLabel else showPasswordLabel

    DcTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        keyboardActions = keyboardActions,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingContent = {
            TextButton(
                onClick = { passwordVisible = !passwordVisible },
                modifier = Modifier.defaultMinSize(
                    minWidth = AccessibilityTokens.MinTouchTarget,
                    minHeight = AccessibilityTokens.MinTouchTarget
                ),
                enabled = enabled
            ) {
                DcText(toggleLabel, maxLines = 1)
            }
        }
    )
}

@DcThemePreviews
@Composable
private fun DcTextFieldPreview() {
    DcTheme {
        Column(verticalArrangement = Arrangement.spacedBy(LocalDcSpacing.current.component)) {
            DcTextField(
                label = "Name",
                value = "",
                placeholder = "Enter a name",
                onValueChange = {}
            )
            DcTextField(
                label = "Email",
                value = "invalid",
                errorText = "Enter a valid email address",
                onValueChange = {}
            )
            DcTextField(
                label = "Disabled",
                value = "Unavailable",
                enabled = false,
                onValueChange = {}
            )
        }
    }
}

@DcFontScalePreviews
@Composable
private fun DcTextFieldFontScalePreview() {
    DcTheme {
        DcTextField(
            label = "Account name",
            value = "A readable value at every font scale",
            supportingText = "Supporting text must remain visible without overlap",
            onValueChange = {}
        )
    }
}

/** Truncates [text] to [maxLength] when a limit is set; the DS never lets input exceed it. */
internal fun enforceMaxLength(text: String, maxLength: Int?): String =
    if (maxLength != null && text.length > maxLength) text.take(maxLength) else text
