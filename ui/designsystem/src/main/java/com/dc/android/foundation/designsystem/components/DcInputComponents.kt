package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.dc.android.foundation.designsystem.tokens.AccessibilityTokens

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
    trailingContent: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .defaultMinSize(minHeight = AccessibilityTokens.MinTouchTarget)
            .semantics {
                if (isError && supportingText != null) {
                    error(supportingText)
                }
            },
        enabled = enabled,
        readOnly = readOnly,
        isError = isError,
        label = { Text(label) },
        supportingText = supportingText?.let { text -> { Text(text) } },
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        trailingIcon = trailingContent
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
                Text(toggleLabel)
            }
        }
    )
}
