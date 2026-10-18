package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dc.android.foundation.designsystem.R
import com.dc.android.foundation.designsystem.preview.DcThemePreviews
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.theme.LocalDcColors
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

/** Defaults for [DcInlineMessage]. */
object DcInlineMessageDefaults {
    fun icon(tone: DcStatusTone): ImageVector = when (tone) {
        DcStatusTone.Neutral, DcStatusTone.Info -> Icons.Filled.Info
        DcStatusTone.Success -> Icons.Filled.CheckCircle
        DcStatusTone.Warning, DcStatusTone.Error -> Icons.Filled.Warning
    }
}

/**
 * Persistent in-content message (banner). Tone changes color *and* icon; text is always present.
 * Warning and error messages are announced politely when they appear.
 */
@Composable
fun DcInlineMessage(
    message: String,
    tone: DcStatusTone,
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = DcInlineMessageDefaults.icon(tone),
    action: (@Composable () -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    dismissLabel: String = stringResource(R.string.dc_dismiss)
) {
    val spacing = LocalDcSpacing.current
    val colorScheme = MaterialTheme.colorScheme
    val dc = LocalDcColors.current
    val (container, content) = when (tone) {
        DcStatusTone.Neutral -> colorScheme.surfaceVariant to colorScheme.onSurfaceVariant
        DcStatusTone.Info -> dc.infoContainer to dc.onInfoContainer
        DcStatusTone.Success -> dc.successContainer to dc.onSuccessContainer
        DcStatusTone.Warning -> dc.warningContainer to dc.onWarningContainer
        DcStatusTone.Error -> colorScheme.errorContainer to colorScheme.onErrorContainer
    }
    val announce = tone == DcStatusTone.Warning || tone == DcStatusTone.Error
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (announce) Modifier.semantics { liveRegion = LiveRegionMode.Polite } else Modifier),
        color = container,
        contentColor = content,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            icon?.let { DcIcon(it, contentDescription = null, modifier = Modifier.size(20.dp)) }
            Column(Modifier.weight(1f)) {
                title?.let { DcText(it, style = MaterialTheme.typography.titleSmall) }
                DcText(message, style = MaterialTheme.typography.bodyMedium)
            }
            action?.invoke()
            onDismiss?.let {
                DcIconButton(Icons.Filled.Close, dismissLabel, onClick = it)
            }
        }
    }
}

/** Semantic circular progress sizes. */
enum class DcProgressSize(internal val dp: Dp) { Small(16.dp), Medium(24.dp), Large(48.dp) }

/**
 * Linear progress. [progress] is read lazily to avoid recomposing parents; null means
 * indeterminate. [label] names what is progressing for screen readers.
 */
@Composable
fun DcLinearProgress(
    progress: (() -> Float)?,
    label: String,
    modifier: Modifier = Modifier
) {
    val described = modifier
        .fillMaxWidth()
        .semantics { contentDescription = label }
    if (progress == null) {
        LinearProgressIndicator(modifier = described)
    } else {
        LinearProgressIndicator(progress = progress, modifier = described)
    }
}

/** Circular progress; see [DcLinearProgress] for [progress] and [label]. */
@Composable
fun DcCircularProgress(
    progress: (() -> Float)?,
    label: String,
    modifier: Modifier = Modifier,
    size: DcProgressSize = DcProgressSize.Medium
) {
    val sized = modifier
        .size(size.dp)
        .semantics { contentDescription = label }
    if (progress == null) {
        CircularProgressIndicator(modifier = sized, strokeWidth = 2.dp)
    } else {
        CircularProgressIndicator(progress = progress, modifier = sized, strokeWidth = 2.dp)
    }
}

/** How long a [DcSnackbarMessage] stays visible. */
enum class DcMessageDuration { Short, Long, Indefinite }

/** Outcome of a shown snackbar. */
enum class DcSnackbarResult { ActionPerformed, Dismissed }

/** Typed snackbar content, independent of Material's `SnackbarData`. */
data class DcSnackbarMessage(
    val text: String,
    val tone: DcStatusTone = DcStatusTone.Neutral,
    val actionLabel: String? = null,
    val duration: DcMessageDuration = DcMessageDuration.Short
) {
    init {
        require(duration != DcMessageDuration.Indefinite || actionLabel != null) {
            "An indefinite snackbar needs an action so the user can dismiss it."
        }
    }
}

/** State holder for [DcSnackbarHost]; hoist it next to the scaffold. */
@Stable
class DcSnackbarHostState {
    internal val host = SnackbarHostState()
    internal var current by mutableStateOf<DcSnackbarMessage?>(null)
        private set

    /** Suspends until the message is dismissed or its action is performed. */
    suspend fun show(message: DcSnackbarMessage): DcSnackbarResult {
        current = message
        try {
            val result = host.showSnackbar(
                message = message.text,
                actionLabel = message.actionLabel,
                withDismissAction = message.duration == DcMessageDuration.Indefinite,
                duration = when (message.duration) {
                    DcMessageDuration.Short -> SnackbarDuration.Short
                    DcMessageDuration.Long -> SnackbarDuration.Long
                    DcMessageDuration.Indefinite -> SnackbarDuration.Indefinite
                }
            )
            return if (result == SnackbarResult.ActionPerformed) {
                DcSnackbarResult.ActionPerformed
            } else {
                DcSnackbarResult.Dismissed
            }
        } finally {
            if (current === message) current = null
        }
    }
}

/** Renders snackbars from [state] using tone colors; pass it as `DcScaffold(snackbarHost = …)`. */
@Composable
fun DcSnackbarHost(state: DcSnackbarHostState, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val dc = LocalDcColors.current
    SnackbarHost(hostState = state.host, modifier = modifier) { data ->
        val (container, content, action) = when (state.current?.tone ?: DcStatusTone.Neutral) {
            DcStatusTone.Neutral -> Triple(colorScheme.inverseSurface, colorScheme.inverseOnSurface, colorScheme.inversePrimary)
            DcStatusTone.Info -> Triple(dc.info, dc.onInfo, dc.onInfo)
            DcStatusTone.Success -> Triple(dc.success, dc.onSuccess, dc.onSuccess)
            DcStatusTone.Warning -> Triple(dc.warning, dc.onWarning, dc.onWarning)
            DcStatusTone.Error -> Triple(colorScheme.error, colorScheme.onError, colorScheme.onError)
        }
        Snackbar(
            snackbarData = data,
            shape = MaterialTheme.shapes.medium,
            containerColor = container,
            contentColor = content,
            actionColor = action,
            dismissActionContentColor = content
        )
    }
}

/**
 * Shows [message] once and acknowledges it through [onMessageShown] before the snackbar suspends,
 * so state-owned one-shot messages are not replayed on recomposition or rotation.
 */
@Composable
fun DcSnackbarEffect(
    message: DcSnackbarMessage?,
    state: DcSnackbarHostState,
    onMessageShown: () -> Unit,
    onAction: () -> Unit = {}
) {
    LaunchedEffect(message) {
        message?.let {
            onMessageShown()
            if (state.show(it) == DcSnackbarResult.ActionPerformed) onAction()
        }
    }
}

@DcThemePreviews
@Composable
private fun DcInlineMessagePreview() {
    DcTheme {
        Column(verticalArrangement = Arrangement.spacedBy(LocalDcSpacing.current.small)) {
            DcStatusTone.entries.forEach { tone ->
                DcInlineMessage(message = "${tone.name} message", tone = tone, onDismiss = {})
            }
            DcLinearProgress(progress = { 0.4f }, label = "Uploading")
            DcCircularProgress(progress = null, label = "Loading")
        }
    }
}
