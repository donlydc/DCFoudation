package com.dc.android.foundation.greeting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dc.android.foundation.designsystem.components.DcButton
import com.dc.android.foundation.designsystem.components.DcText
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

/**
 * Stateless — receives [state], sends [onAction]. No ViewModel/DI import here (DI-03,
 * DEP-03), so it stays trivially previewable/testable.
 */
@Composable
fun GreetingScreen(
    state: GreetingUiState,
    onAction: (GreetingUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalDcSpacing.current
    Column(
        modifier = modifier.padding(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.small)
    ) {
        DcText(text = state.message)
        DcButton(
            text = if (state.isRefreshing) "Refreshing..." else "Refresh",
            onClick = { onAction(GreetingUiAction.Refresh) },
            enabled = !state.isRefreshing,
            loading = state.isRefreshing
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GreetingScreenPreview() {
    DcTheme {
        GreetingScreen(state = GreetingUiState(message = "Hello Android!"), onAction = {})
    }
}

@Preview(showBackground = true, name = "Refreshing")
@Composable
private fun GreetingScreenRefreshingPreview() {
    DcTheme {
        GreetingScreen(
            state = GreetingUiState(message = "Hello Android!", isRefreshing = true),
            onAction = {}
        )
    }
}
