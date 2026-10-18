package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dc.android.foundation.designsystem.preview.DcThemePreviews
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.tokens.AccessibilityTokens
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing
import com.dc.android.foundation.designsystem.tokens.StrokeTokens

/** Visual intent for [DcButton]. Destructive actions require caller-owned confirmation. */
enum class DcButtonVariant { Primary, Secondary, Tertiary, Destructive, Text }

/** Theme-derived defaults for [DcButton]; no second component-specific theme layer is needed. */
object DcButtonDefaults {
    /** Minimum height for a screen-level action. */
    val MinHeight: Dp = 48.dp

    /** Compact progress indicator that does not change the button's measured height. */
    val ProgressSize: Dp = 18.dp

    val ProgressStroke: Dp = 2.dp

    val ContentPadding: PaddingValues
        @Composable get() = PaddingValues(
            horizontal = LocalDcSpacing.current.medium,
            vertical = LocalDcSpacing.current.small
        )

    @Composable
    fun colors(variant: DcButtonVariant): ButtonColors = when (variant) {
        DcButtonVariant.Primary -> ButtonDefaults.buttonColors()
        DcButtonVariant.Secondary -> ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
        DcButtonVariant.Tertiary, DcButtonVariant.Text -> ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
        DcButtonVariant.Destructive -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    }
}

/**
 * Policy-owned action button: it always meets the accessibility touch floor and `loading`
 * disables clicks to prevent duplicate submissions. Use [DcButtonVariant.Destructive] only with
 * a caller-owned confirmation step.
 */
@Composable
fun DcButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: DcButtonVariant = DcButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentPadding: PaddingValues = DcButtonDefaults.ContentPadding,
    colors: ButtonColors = DcButtonDefaults.colors(variant)
) {
    DcButton(
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        enabled = enabled && !loading,
        contentPadding = contentPadding,
        colors = colors
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(DcButtonDefaults.ProgressSize),
                color = LocalContentColor.current,
                strokeWidth = DcButtonDefaults.ProgressStroke
            )
        } else {
            leadingIcon?.let { DcIcon(imageVector = it, contentDescription = null) }
        }
        if (loading || leadingIcon != null) {
            Spacer(Modifier.width(LocalDcSpacing.current.small))
        }
        DcText(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1
        )
        if (!loading) trailingIcon?.let { DcIcon(imageVector = it, contentDescription = null) }
    }
}

/** Full-width primary screen action for forms and bottom action containers. */
@Composable
fun DcPrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    destructive: Boolean = false
) {
    DcButton(
        text = text,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        variant = if (destructive) DcButtonVariant.Destructive else DcButtonVariant.Primary,
        enabled = enabled,
        loading = loading
    )
}

/**
 * Slot-based action button. It preserves DS touch-target and variant policy while allowing an
 * application to supply a custom internal layout.
 */
@Composable
fun DcButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: DcButtonVariant = DcButtonVariant.Primary,
    enabled: Boolean = true,
    contentPadding: PaddingValues = DcButtonDefaults.ContentPadding,
    colors: ButtonColors = DcButtonDefaults.colors(variant),
    content: @Composable RowScope.() -> Unit
) {
    val targetModifier = modifier.defaultMinSize(
        minWidth = AccessibilityTokens.MinTouchTarget,
        minHeight = DcButtonDefaults.MinHeight
    )
    when (variant) {
        DcButtonVariant.Secondary -> OutlinedButton(
            onClick = onClick, modifier = targetModifier, enabled = enabled,
            shape = MaterialTheme.shapes.medium,
            colors = colors,
            border = BorderStroke(StrokeTokens.Hairline, MaterialTheme.colorScheme.outline),
            contentPadding = contentPadding, content = content
        )
        DcButtonVariant.Tertiary, DcButtonVariant.Text -> TextButton(
            onClick = onClick, modifier = targetModifier, enabled = enabled,
            shape = MaterialTheme.shapes.medium,
            colors = colors,
            contentPadding = contentPadding, content = content
        )
        else -> Button(
            onClick = onClick, modifier = targetModifier, enabled = enabled,
            shape = MaterialTheme.shapes.medium,
            contentPadding = contentPadding, colors = colors, content = content
        )
    }
}

@DcThemePreviews
@Composable
private fun DcButtonPreview() {
    DcTheme {
        Column(verticalArrangement = Arrangement.spacedBy(LocalDcSpacing.current.medium)) {
            DcButtonVariant.entries.forEach { variant ->
                Column(verticalArrangement = Arrangement.spacedBy(LocalDcSpacing.current.small)) {
                    DcText(variant.name, style = MaterialTheme.typography.titleSmall)
                    DcButton(text = "Normal", variant = variant, onClick = {})
                    DcButton(text = "Disabled", variant = variant, enabled = false, onClick = {})
                    DcButton(text = "Loading", variant = variant, loading = true, onClick = {})
                    DcButton(
                        text = "With icon",
                        variant = variant,
                        leadingIcon = Icons.Filled.Check,
                        onClick = {}
                    )
                }
            }
        }
    }
}
