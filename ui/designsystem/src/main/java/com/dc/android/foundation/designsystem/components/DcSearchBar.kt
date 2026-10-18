package com.dc.android.foundation.designsystem.components

import androidx.compose.ui.res.stringResource
import com.dc.android.foundation.designsystem.R
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction

/**
 * Controlled search field with a caller-owned query and explicit clear action. It centralizes the
 * search IME action and accessible input semantics without owning search/filtering policy.
 */
@Composable
fun DcSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
    clearLabel: String = stringResource(R.string.dc_clear),
    enabled: Boolean = true
) {
    DcTextField(
        label = label,
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        trailingContent = if (query.isEmpty()) null else {
            {
                androidx.compose.material3.TextButton(onClick = { onQueryChange("") }) {
                    DcText(clearLabel, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                }
            }
        }
    )
}
