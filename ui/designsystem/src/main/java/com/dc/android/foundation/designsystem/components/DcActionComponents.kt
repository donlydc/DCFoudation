package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dc.android.foundation.designsystem.tokens.AccessibilityTokens

/** Visual treatment for icon-only actions. */
enum class DcIconButtonVariant {
    /** No container; suited to app bars and list rows. */
    Standard,

    /** Quiet tinted container for a locally emphasized action. */
    Tonal,

    /** Soft error-container treatment for destructive actions. */
    SoftDestructive
}

/** Renders a vector icon without exposing a Material icon call site to feature UI. */
@Composable
fun DcIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Icon(imageVector = imageVector, contentDescription = contentDescription, modifier = modifier)
}

/**
 * Icon-only action requiring a localized [contentDescription] and enforcing the 48dp touch floor.
 */
@Composable
fun DcIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: DcIconButtonVariant = DcIconButtonVariant.Standard
) {
    val targetModifier = modifier.defaultMinSize(
        minWidth = AccessibilityTokens.MinTouchTarget,
        minHeight = AccessibilityTokens.MinTouchTarget
    )
    val iconContent: @Composable () -> Unit = {
        DcIcon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }

    when (variant) {
        DcIconButtonVariant.Standard -> IconButton(
            onClick = onClick,
            modifier = targetModifier,
            enabled = enabled,
            content = iconContent
        )
        DcIconButtonVariant.Tonal -> FilledTonalIconButton(
            onClick = onClick,
            modifier = targetModifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.medium,
            content = iconContent
        )
        DcIconButtonVariant.SoftDestructive -> FilledTonalIconButton(
            onClick = onClick,
            modifier = targetModifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.medium,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            content = iconContent
        )
    }
}

/** Floating action with centrally enforced touch-target sizing. */
@Composable
fun DcFab(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(onClick = onClick, modifier = modifier.defaultMinSize(
        minWidth = AccessibilityTokens.MinTouchTarget,
        minHeight = AccessibilityTokens.MinTouchTarget
    )) {
        DcIcon(imageVector, contentDescription)
    }
}

/** Extended floating action using the same icon accessibility policy as [DcIconButton]. */
@Composable
fun DcExtendedFab(
    text: String,
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        text = { DcText(text, style = MaterialTheme.typography.labelLarge) },
        icon = { DcIcon(imageVector, contentDescription) },
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = AccessibilityTokens.MinTouchTarget)
    )
}
