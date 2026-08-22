package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.dc.android.foundation.designsystem.tokens.SpacingTokens

/**
 * Centered progress state for a screen or content region. [label] is exposed to screen readers
 * because an indeterminate indicator alone does not explain what is loading.
 */
@Composable
fun DcLoadingState(
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Small)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.semantics { contentDescription = label }
        )
        DcText(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

/** A reusable no-content state with an optional caller-owned action. */
@Composable
fun DcEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Small)
    ) {
        DcText(text = title, style = MaterialTheme.typography.titleMedium)
        description?.let {
            DcText(text = it, style = MaterialTheme.typography.bodyMedium)
        }
        action?.invoke()
    }
}

/**
 * Recoverable error state. A retry control is displayed only when both retry inputs are supplied,
 * keeping error presentation separate from the feature's recovery decision.
 */
@Composable
fun DcErrorState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    retryLabel: String? = null,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Small)
    ) {
        DcText(text = title, style = MaterialTheme.typography.titleMedium)
        DcText(text = message, style = MaterialTheme.typography.bodyMedium)
        if (retryLabel != null && onRetry != null) {
            DcButton(text = retryLabel, onClick = onRetry)
        }
    }
}

/**
 * A compact non-modal connectivity notice. It uses the semantic error-container colors so it
 * remains legible in light, dark, and dynamic themes without exposing brand colors to callers.
 */
@Composable
fun DcOfflineBanner(
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(SpacingTokens.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Small)
        ) {
            DcText(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            action?.invoke()
        }
    }
}
