package com.dc.android.foundation.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dc.android.foundation.R
import com.dc.android.foundation.designsystem.components.DcButton
import com.dc.android.foundation.designsystem.components.DcCard
import com.dc.android.foundation.designsystem.components.DcContentState
import com.dc.android.foundation.designsystem.components.DcText
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing
import com.dc.android.foundation.platform.permission.PermissionState

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
    val spacing = LocalDcSpacing.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.small)
    ) {
        if (state.notificationPermissionState != PermissionState.Granted) {
            DcButton(
                text = stringResource(R.string.articles_enable_notifications),
                onClick = { onAction(ArticlesUiAction.RequestNotificationPermission) }
            )
        }
        DcButton(
            text = stringResource(if (state.isRefreshing) R.string.articles_refreshing else R.string.articles_refresh),
            onClick = { onAction(ArticlesUiAction.Refresh) },
            enabled = !state.isRefreshing,
            loading = state.isRefreshing
        )
        DcContentState(
            value = state.articles,
            isLoading = state.isRefreshing,
            errorMessage = if (state.isShowingStaleData) {
                stringResource(R.string.articles_stale_data)
            } else {
                null
            },
            modifier = Modifier.weight(1f),
            isEmpty = { it.isEmpty() },
            onRetry = { onAction(ArticlesUiAction.Refresh) },
            emptyTitle = stringResource(R.string.articles_empty_title)
        ) { articles ->
            LazyColumn(verticalArrangement = Arrangement.spacedBy(spacing.component)) {
                items(articles, key = { it.id }) { article ->
                    DcCard(modifier = Modifier.fillMaxWidth()) {
                        DcText(text = article.title)
                        DcText(text = article.body)
                    }
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
