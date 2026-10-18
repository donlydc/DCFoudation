package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dc.android.foundation.designsystem.preview.DcThemePreviews
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.theme.LocalDcColors
import com.dc.android.foundation.designsystem.tokens.AccessibilityTokens
import com.dc.android.foundation.designsystem.tokens.ElevationTokens
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing
import com.dc.android.foundation.designsystem.tokens.StrokeTokens

/** Visual treatment for [DcCard]. */
enum class DcCardVariant {
    /** Default surface with a quiet border and no shadow. */
    Outlined,

    /** Raised surface for content that must separate from a busy background. */
    Elevated
}

/** Card surface with theme-owned shape/elevation and optional accessible row-wide click handling. */
@Composable
fun DcCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(LocalDcSpacing.current.medium),
    variant: DcCardVariant = DcCardVariant.Outlined,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = onClick?.let {
        Modifier.defaultMinSize(minHeight = AccessibilityTokens.MinTouchTarget)
            .clickable(role = Role.Button, onClick = it)
    } ?: Modifier
    Card(
        modifier = modifier.then(clickableModifier),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = when (variant) {
            DcCardVariant.Outlined -> BorderStroke(
                StrokeTokens.Hairline,
                MaterialTheme.colorScheme.outlineVariant
            )
            DcCardVariant.Elevated -> null
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = when (variant) {
                DcCardVariant.Outlined -> ElevationTokens.Level0
                DcCardVariant.Elevated -> ElevationTokens.Level1
            }
        ),
        content = {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    )
}

/**
 * A business-neutral label/value pair for summaries and detail surfaces.
 *
 * Use [emphasized] for a total or another final value. The label is deliberately quieter than
 * the value in the normal state so callers do not need to duplicate typography or color policy.
 */
@Composable
fun DcKeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    val spacing = LocalDcSpacing.current
    val valueStyle = if (emphasized) {
        MaterialTheme.typography.titleMedium
    } else {
        MaterialTheme.typography.bodyLarge
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.xSmall),
        horizontalArrangement = Arrangement.spacedBy(spacing.component),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DcText(
            text = label,
            modifier = Modifier.weight(1f),
            style = if (emphasized) valueStyle else MaterialTheme.typography.bodyMedium,
            color = if (emphasized) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        DcText(
            text = value,
            style = valueStyle,
            textAlign = TextAlign.End
        )
    }
}

/** Selectable compact label; long labels are constrained by Material3's chip layout. */
@Composable
fun DcChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        label = { DcText(label, maxLines = 1) }
    )
}

/** Status tone for [DcStatusChip]. The required text keeps status independent of color alone. */
enum class DcStatusTone { Neutral, Info, Success, Warning, Error }

/**
 * Compact, non-interactive status label. It intentionally has no click handling or 48dp visual
 * minimum because it communicates state rather than acting as a control.
 */
@Composable
fun DcStatusChip(
    text: String,
    tone: DcStatusTone,
    modifier: Modifier = Modifier,
    showIndicator: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme
    val dcColors = LocalDcColors.current
    val (containerColor, contentColor, indicatorColor) = when (tone) {
        DcStatusTone.Neutral -> Triple(
            colorScheme.surfaceVariant,
            colorScheme.onSurfaceVariant,
            colorScheme.onSurfaceVariant
        )
        DcStatusTone.Info -> Triple(
            dcColors.infoContainer,
            dcColors.onInfoContainer,
            dcColors.info
        )
        DcStatusTone.Success -> Triple(
            dcColors.successContainer,
            dcColors.onSuccessContainer,
            dcColors.success
        )
        DcStatusTone.Warning -> Triple(
            dcColors.warningContainer,
            dcColors.onWarningContainer,
            dcColors.warning
        )
        DcStatusTone.Error -> Triple(
            colorScheme.errorContainer,
            colorScheme.onErrorContainer,
            colorScheme.error
        )
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = LocalDcSpacing.current.small,
                vertical = LocalDcSpacing.current.xSmall
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showIndicator) {
                Box(
                    Modifier
                        .size(6.dp)
                        .background(indicatorColor, CircleShape)
                )
                Spacer(Modifier.width(LocalDcSpacing.current.xSmall))
            }
            DcText(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        }
    }
}

/** Theme-colored divider with an optional semantic leading-content inset. */
@Composable
fun DcDivider(
    modifier: Modifier = Modifier,
    leadingInset: Boolean = false
) {
    HorizontalDivider(
        modifier = if (leadingInset) modifier.padding(start = LocalDcSpacing.current.medium) else modifier,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@DcThemePreviews
@Composable
private fun DcCardPreview() {
    DcTheme {
        Column(verticalArrangement = Arrangement.spacedBy(LocalDcSpacing.current.medium)) {
            DcCard(variant = DcCardVariant.Outlined) {
                DcText("Outlined card", style = MaterialTheme.typography.titleMedium)
                DcText("A quiet bordered surface", style = MaterialTheme.typography.bodyMedium)
            }
            DcCard(variant = DcCardVariant.Elevated) {
                DcText("Elevated card", style = MaterialTheme.typography.titleMedium)
                DcText("A subtly raised surface", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@DcThemePreviews
@Composable
private fun DcKeyValueRowPreview() {
    DcTheme {
        DcCard {
            DcKeyValueRow("Subtotal", "$120.00")
            DcKeyValueRow("Service fee", "$8.00")
            DcDivider()
            DcKeyValueRow("Total", "$128.00", emphasized = true)
        }
    }
}

@DcThemePreviews
@Composable
private fun DcStatusChipPreview() {
    DcTheme {
        Column(verticalArrangement = Arrangement.spacedBy(LocalDcSpacing.current.small)) {
            DcStatusTone.entries.forEach { tone ->
                DcStatusChip(text = tone.name, tone = tone)
            }
        }
    }
}
