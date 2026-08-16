package com.dc.android.foundation.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dc.android.foundation.designsystem.components.DcButton
import com.dc.android.foundation.designsystem.components.DcText
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.tokens.SpacingTokens

/**
 * Stateless — receives [state], sends [onAction]. No ViewModel/DI/DAO/Retrofit import here
 * (DI-03, DEP-03), so it stays trivially previewable/testable. Uses design-system semantic APIs
 * only, no raw `.dp` literals (DS-03).
 */
@Composable
fun ArticlesScreen(
    state: ArticlesUiState,
    onAction: (ArticlesUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.Medium),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Small)
    ) {
        if (state.isShowingStaleData) {
            DcText(text = "Showing cached data — refresh failed")
        }
        DcButton(
            text = if (state.isRefreshing) "Refreshing..." else "Refresh",
            onClick = { onAction(ArticlesUiAction.Refresh) },
            enabled = !state.isRefreshing
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(SpacingTokens.Small)) {
            items(state.articles, key = { it.id }) { article ->
                Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.XSmall)) {
                    DcText(text = article.title)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticlesScreenPreview() {
    DcTheme {
        ArticlesScreen(
            state = ArticlesUiState(
                articles = listOf(
                    Article(id = 1, authorId = 1, title = "First article", body = "Body")
                )
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Stale data")
@Composable
private fun ArticlesScreenStalePreview() {
    DcTheme {
        ArticlesScreen(
            state = ArticlesUiState(isShowingStaleData = true),
            onAction = {}
        )
    }
}
