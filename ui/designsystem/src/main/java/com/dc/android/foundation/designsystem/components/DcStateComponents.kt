package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dc.android.foundation.designsystem.preview.DcThemePreviews
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

enum class DcLoadingSize { Inline, Screen }

/**
 * Centered progress state for a screen or content region. [label] is exposed to screen readers
 * because an indeterminate indicator alone does not explain what is loading.
 */
@Composable
fun DcLoadingState(
    label: String,
    modifier: Modifier = Modifier,
    size: DcLoadingSize = DcLoadingSize.Screen
) {
    val spacing = LocalDcSpacing.current
    when (size) {
        DcLoadingSize.Inline -> Row(
            modifier = modifier.padding(spacing.component),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.component)
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(20.dp)
                    .semantics { contentDescription = label },
                strokeWidth = 2.dp
            )
            DcText(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DcLoadingSize.Screen -> Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(40.dp)
                    .semantics { contentDescription = label }
            )
            DcText(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** A reusable no-content state with an optional caller-owned action. */
@Composable
fun DcEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    action: (@Composable () -> Unit)? = null,
    icon: ImageVector? = null
) {
    val spacing = LocalDcSpacing.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.component)
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DcText(text = title, style = MaterialTheme.typography.titleMedium)
        description?.let {
            DcText(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    onRetry: (() -> Unit)? = null,
    icon: ImageVector? = null
) {
    val spacing = LocalDcSpacing.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.component)
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
        DcText(text = title, style = MaterialTheme.typography.titleMedium)
        DcText(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (retryLabel != null && onRetry != null) {
            DcButton(
                text = retryLabel,
                onClick = onRetry,
                variant = DcButtonVariant.Secondary
            )
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
    val spacing = LocalDcSpacing.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            DcText(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            action?.invoke()
        }
    }
}

@DcThemePreviews
@Composable
private fun DcStateComponentsPreview() {
    DcTheme {
        Column {
            DcLoadingState(label = "Loading", size = DcLoadingSize.Inline)
            DcEmptyState(
                title = "Nothing here yet",
                description = "New content will appear here.",
                icon = Icons.Filled.Warning
            )
            DcErrorState(
                title = "Could not load content",
                message = "Check your connection and try again.",
                retryLabel = "Retry",
                onRetry = {},
                icon = Icons.Filled.Warning
            )
            DcOfflineBanner(message = "You are offline")
        }
    }
}
