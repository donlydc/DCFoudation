package com.dc.android.foundation.designsystem.components

import androidx.compose.ui.res.stringResource
import com.dc.android.foundation.designsystem.R
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dc.android.foundation.designsystem.preview.DcThemePreviews
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

/**
 * Modal sheet for focused choices, action lists, and filters. It ignores dismiss requests while
 * [isProcessing] so a submission cannot be abandoned mid-flight. The caller owns state and
 * decides when to remove the sheet. A flow that replaces an entire screen belongs on its own
 * destination instead of in this component.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DcModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false,
    sheetState: SheetState = androidx.compose.material3.rememberModalBottomSheetState(),
    showDragHandle: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = LocalDcSpacing.current
    ModalBottomSheet(
        onDismissRequest = { if (!isProcessing) onDismissRequest() },
        modifier = modifier,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = if (showDragHandle) {
            { BottomSheetDefaults.DragHandle() }
        } else {
            null
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = spacing.medium, vertical = spacing.component),
                content = content
            )
        }
    )
}

/**
 * Confirmation dialog with at most two actions, a stable cancel/confirm order, and a processing
 * dismissal guard. The caller owns visibility and should only compose this dialog when it is
 * visible. Put forms and longer flows in a bottom sheet or their own screen.
 *
 * Example: `if (state.showConfirmation) { DcConfirmDialog(...) }`.
 */
@Composable
fun DcConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = stringResource(R.string.dc_cancel),
    isProcessing: Boolean = false,
    destructive: Boolean = false,
    icon: ImageVector? = null
) {
    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismissRequest() },
        modifier = modifier,
        icon = icon?.let {
            { DcIcon(imageVector = it, contentDescription = null) }
        },
        title = { DcText(title, style = MaterialTheme.typography.titleLarge) },
        text = { DcText(message, style = MaterialTheme.typography.bodyMedium) },
        dismissButton = {
            DcButton(
                text = dismissLabel,
                onClick = onDismissRequest,
                enabled = !isProcessing,
                variant = DcButtonVariant.Text
            )
        },
        confirmButton = {
            DcButton(
                text = confirmLabel,
                onClick = onConfirm,
                enabled = !isProcessing,
                loading = isProcessing,
                variant = if (destructive) DcButtonVariant.Destructive else DcButtonVariant.Primary
            )
        },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        iconContentColor = if (destructive) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        }
    )
}

/** Informational dialog that keeps Material's dismissal/action ordering consistent across screens. */
@Composable
fun DcAlertDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    DcSingleActionDialog(
        title = title,
        message = message,
        confirmLabel = confirmLabel,
        onConfirm = onConfirm,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        icon = icon,
        iconContentColor = MaterialTheme.colorScheme.primary
    )
}

/** A single-action informational dialog. The caller owns whether it is composed. */
@Composable
fun DcInfoDialog(
    title: String,
    message: String,
    confirmText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    DcSingleActionDialog(
        title = title,
        message = message,
        confirmLabel = confirmText,
        onConfirm = onDismiss,
        onDismissRequest = onDismiss,
        modifier = modifier,
        icon = icon,
        iconContentColor = MaterialTheme.colorScheme.primary
    )
}

/**
 * A single-action error dialog. Pass a user-understandable recovery message, never a raw
 * exception or infrastructure error string.
 */
@Composable
fun DcErrorDialog(
    title: String,
    message: String,
    confirmText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    DcSingleActionDialog(
        title = title,
        message = message,
        confirmLabel = confirmText,
        onConfirm = onDismiss,
        onDismissRequest = onDismiss,
        modifier = modifier,
        icon = Icons.Default.Warning,
        iconContentColor = MaterialTheme.colorScheme.error
    )
}

@Composable
private fun DcSingleActionDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    icon: ImageVector?,
    iconContentColor: Color
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        icon = icon?.let {
            { DcIcon(imageVector = it, contentDescription = null) }
        },
        title = { DcText(title, style = MaterialTheme.typography.titleLarge) },
        text = { DcText(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            DcButton(text = confirmLabel, onClick = onConfirm)
        },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        iconContentColor = iconContentColor
    )
}

@DcThemePreviews
@Composable
private fun DcConfirmDialogPreview() {
    DcTheme {
        DcConfirmDialog(
            title = "Save changes?",
            message = "Your updates will be available the next time this screen opens.",
            confirmLabel = "Save",
            onConfirm = {},
            onDismissRequest = {}
        )
    }
}

@DcThemePreviews
@Composable
private fun DcDestructiveConfirmDialogPreview() {
    DcTheme {
        DcConfirmDialog(
            title = "Delete item?",
            message = "This action cannot be undone.",
            confirmLabel = "Delete",
            onConfirm = {},
            onDismissRequest = {},
            destructive = true,
            icon = Icons.Default.Warning
        )
    }
}

@DcThemePreviews
@Composable
private fun DcInfoDialogPreview() {
    DcTheme {
        DcInfoDialog(
            title = "All set",
            message = "Your preferences have been updated.",
            confirmText = "Done",
            onDismiss = {}
        )
    }
}

@DcThemePreviews
@Composable
private fun DcErrorDialogPreview() {
    DcTheme {
        DcErrorDialog(
            title = "Unable to continue",
            message = "Check your connection and try again.",
            confirmText = "Close",
            onDismiss = {}
        )
    }
}

/** Semantic skeleton heights. */
enum class DcSkeletonSize(internal val dp: Dp) {
    Line(16.dp),
    Title(24.dp),
    Block(96.dp)
}

/** Skeleton using a semantic height; prefer this over the [Dp] overload. */
@Composable
fun DcSkeleton(
    size: DcSkeletonSize,
    modifier: Modifier = Modifier,
    animated: Boolean = true
) {
    DcSkeleton(modifier = modifier, height = size.dp, animated = animated)
}

/**
 * Lightweight loading placeholder. Setting [animated] false lets callers honor a user/system
 * reduced-motion preference without adding a shimmer dependency.
 */
@Composable
fun DcSkeleton(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    animated: Boolean = true
) {
    val alpha = if (animated) {
        rememberInfiniteTransition(label = "dc-skeleton").animateFloat(
            initialValue = 0.45f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(750, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dc-skeleton-alpha"
        ).value
    } else {
        0.55f
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
    )
}
