package com.dc.android.foundation.designsystem.components

import androidx.compose.ui.res.stringResource
import com.dc.android.foundation.designsystem.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

internal enum class DcContentStateBranch { Content, Error, Loading, Empty }

internal fun resolveDcContentStateBranch(
    hasContent: Boolean,
    isLoading: Boolean,
    hasError: Boolean
): DcContentStateBranch = when {
    hasContent -> DcContentStateBranch.Content
    hasError -> DcContentStateBranch.Error
    isLoading -> DcContentStateBranch.Loading
    else -> DcContentStateBranch.Empty
}

/**
 * Applies the common content-state priority: stale data remains visible during refresh/failure;
 * only an empty value becomes a blocking loading/error/empty state.
 */
@Composable
fun <T> DcContentState(
    value: T?,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    isEmpty: (T) -> Boolean = { false },
    onRetry: (() -> Unit)? = null,
    loadingLabel: String = stringResource(R.string.dc_loading),
    emptyTitle: String = stringResource(R.string.dc_nothing_here_yet),
    retryLabel: String = stringResource(R.string.dc_retry),
    loadingContent: @Composable () -> Unit = { DcLoadingState(loadingLabel) },
    errorContent: @Composable (String) -> Unit = { message ->
        DcErrorState(
            title = stringResource(R.string.dc_could_not_load_content),
            message = message,
            retryLabel = if (onRetry == null) null else retryLabel,
            onRetry = onRetry
        )
    },
    emptyContent: @Composable () -> Unit = { DcEmptyState(emptyTitle) },
    content: @Composable (T) -> Unit
) {
    when (resolveDcContentStateBranch(
        hasContent = value != null && !isEmpty(value),
        isLoading = isLoading,
        hasError = errorMessage != null
    )) {
        DcContentStateBranch.Content -> Column(modifier = modifier) {
            if (errorMessage != null) DcOfflineBanner(message = errorMessage)
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = loadingLabel }
                )
            }
            content(requireNotNull(value))
        }
        DcContentStateBranch.Error -> Box(modifier = modifier.fillMaxSize()) { errorContent(requireNotNull(errorMessage)) }
        DcContentStateBranch.Loading -> Box(modifier = modifier.fillMaxSize()) { loadingContent() }
        DcContentStateBranch.Empty -> Box(modifier = modifier.fillMaxSize()) { emptyContent() }
    }
}

/**
 * Displays each durable state message once, then immediately acknowledges it through
 * [onMessageShown]. The message remains state-owned; this composable owns only the UI effect.
 *
 * Legacy Material-typed overload kept for source compatibility (DEC-08). Prefer the
 * [DcSnackbarMessage]/[DcSnackbarHostState] overload in `DcMessageComponents.kt`, which also
 * supports tone, action and duration; migrating also means hosting the snackbar with
 * `DcSnackbarHost(DcSnackbarHostState)` instead of Material's `SnackbarHost`.
 */
@Deprecated(
    message = "Use the DcSnackbarMessage/DcSnackbarHostState overload and DcSnackbarHost (DEC-08).",
    replaceWith = ReplaceWith(
        expression = "DcSnackbarEffect(message?.let { DcSnackbarMessage(it) }, snackbarHostState, onMessageShown)",
        imports = ["com.dc.android.foundation.designsystem.components.DcSnackbarMessage"]
    )
)
@Composable
fun DcSnackbarEffect(
    message: String?,
    snackbarHostState: SnackbarHostState,
    onMessageShown: () -> Unit
) {
    LaunchedEffect(message) {
        message?.let {
            onMessageShown()
            snackbarHostState.showSnackbar(it)
        }
    }
}
