package com.dc.android.foundation.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.dc.android.foundation.designsystem.R
import com.dc.android.foundation.designsystem.preview.DcThemePreviews
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.tokens.AccessibilityTokens
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing
import com.dc.android.foundation.designsystem.tokens.MotionTokens

/** Maximum readable width of page content on large windows. */
enum class DcContentWidth(internal val max: Dp) {
    /** Forms and reading text. */
    Readable(640.dp),

    /** Dashboards and denser detail pages. */
    Wide(840.dp),

    /** No limit. */
    Full(Dp.Unspecified)
}

/**
 * Centers content and caps its width on medium/expanded windows so forms and detail pages do not
 * stretch edge to edge. It reads no window-size class: it only applies a maximum width.
 */
@Composable
fun DcContentContainer(
    modifier: Modifier = Modifier,
    maxWidth: DcContentWidth = DcContentWidth.Readable,
    horizontalPadding: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val padding = if (horizontalPadding) LocalDcSpacing.current.medium else LocalDcSpacing.current.none
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Box(
            Modifier
                .then(if (maxWidth.max.isSpecified) Modifier.widthIn(max = maxWidth.max) else Modifier)
                .fillMaxWidth()
                .padding(horizontal = padding),
            content = content
        )
    }
}

/** Responsive grid whose column count follows [minCellWidth]; spacing comes from the theme. */
@Composable
fun DcAdaptiveGrid(
    modifier: Modifier = Modifier,
    minCellWidth: Dp = DcGridDefaults.MinCell,
    contentPadding: PaddingValues = PaddingValues(LocalDcSpacing.current.medium),
    content: LazyGridScope.() -> Unit
) {
    val spacing = LocalDcSpacing.current
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minCellWidth),
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
        content = content
    )
}

object DcGridDefaults {
    val MinCell: Dp = 160.dp
}

/**
 * Footer for the last `item {}` of a paged list: shows progress, a retry affordance, or an
 * optional end-of-list note. It is Paging-agnostic; the feature maps its load state to these
 * arguments.
 */
@Composable
fun DcListAppendState(
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    endReachedText: String? = null,
    retryLabel: String = stringResource(R.string.dc_retry),
    loadingLabel: String = stringResource(R.string.dc_loading)
) {
    val spacing = LocalDcSpacing.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(spacing.medium)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.small)
    ) {
        when {
            errorMessage != null -> {
                DcText(errorMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                if (onRetry != null) DcTextLink(retryLabel, onClick = onRetry)
            }
            isLoading -> DcLoadingState(loadingLabel, size = DcLoadingSize.Inline)
            endReachedText != null -> DcText(
                endReachedText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Collapsible group. The caller owns [expanded]; animation uses [MotionTokens] durations. */
@Composable
fun DcExpandableSection(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    expandedStateLabel: String = stringResource(R.string.dc_expanded),
    collapsedStateLabel: String = stringResource(R.string.dc_collapsed),
    content: @Composable () -> Unit
) {
    val spacing = LocalDcSpacing.current
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(MotionTokens.DURATION_MEDIUM_MS),
        label = "dc-expandable-chevron"
    )
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = AccessibilityTokens.MinTouchTarget)
                .clickable(role = Role.Button) { onExpandedChange(!expanded) }
                .semantics { stateDescription = if (expanded) expandedStateLabel else collapsedStateLabel }
                .padding(horizontal = spacing.medium, vertical = spacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            Column(Modifier.weight(1f)) {
                DcText(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                supportingText?.let { DcText(it, style = MaterialTheme.typography.bodyMedium) }
            }
            DcIcon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.rotate(rotation))
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(MotionTokens.DURATION_MEDIUM_MS)) + fadeIn(tween(MotionTokens.DURATION_MEDIUM_MS)),
            exit = shrinkVertically(tween(MotionTokens.DURATION_MEDIUM_MS)) + fadeOut(tween(MotionTokens.DURATION_SHORT_MS))
        ) {
            Column(Modifier.padding(horizontal = spacing.medium)) { content() }
        }
    }
}

/** Non-interactive dots showing the current page of a pager/onboarding flow. */
@Composable
fun DcPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    contentDescription: String = stringResource(R.string.dc_page_indicator, currentPage + 1, pageCount)
) {
    val spacing = LocalDcSpacing.current
    Row(
        modifier = modifier.clearAndSetSemantics { this.contentDescription = contentDescription },
        horizontalArrangement = Arrangement.spacedBy(spacing.xSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { page ->
            val color = if (page == currentPage) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
            Box(Modifier.size(8.dp).background(color, CircleShape))
        }
    }
}

/** Loading placeholder shaped like a [DcListRow]; [lines] text lines and an optional avatar. */
@Composable
fun DcSkeletonListRow(
    modifier: Modifier = Modifier,
    lines: Int = 2,
    showLeading: Boolean = true,
    animated: Boolean = true,
    label: String = stringResource(R.string.dc_loading)
) {
    val spacing = LocalDcSpacing.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.medium, vertical = spacing.small)
            .clearAndSetSemantics { contentDescription = label },
        horizontalArrangement = Arrangement.spacedBy(spacing.component),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showLeading) {
            Box(Modifier.size(DcAvatarSize.Medium.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.small)) {
            repeat(lines.coerceAtLeast(1)) { index ->
                DcSkeleton(
                    modifier = if (index == lines - 1 && lines > 1) Modifier.widthIn(max = 160.dp) else Modifier,
                    size = DcSkeletonSize.Line,
                    animated = animated
                )
            }
        }
    }
}

/** Skeleton for a card-shaped block. */
@Composable
fun DcSkeletonCard(modifier: Modifier = Modifier, animated: Boolean = true) {
    DcCard(modifier = modifier) {
        DcSkeleton(size = DcSkeletonSize.Title, animated = animated)
        DcSkeleton(size = DcSkeletonSize.Block, animated = animated)
    }
}

/**
 * Primary/secondary action pair. Side by side on wide space (secondary first so the primary is
 * last in reading order); stacked, primary on top, when width is narrow or font scale is large.
 */
@Composable
fun DcButtonRow(
    primary: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    secondary: (@Composable (Modifier) -> Unit)? = null,
    stackBelowWidth: Dp = DcButtonRowDefaults.StackThreshold
) {
    val spacing = LocalDcSpacing.current
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(modifier.fillMaxWidth()) {
        if (secondary == null) {
            primary(Modifier.fillMaxWidth())
        } else if (shouldStackButtons(maxWidth, stackBelowWidth, fontScale)) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.component)) {
                primary(Modifier.fillMaxWidth())
                secondary(Modifier.fillMaxWidth())
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.component)) {
                secondary(Modifier.weight(1f))
                primary(Modifier.weight(1f))
            }
        }
    }
}

object DcButtonRowDefaults {
    val StackThreshold: Dp = 320.dp
    const val StackFontScale: Float = 1.3f
}

internal fun shouldStackButtons(available: Dp, threshold: Dp, fontScale: Float): Boolean =
    available < threshold || fontScale >= DcButtonRowDefaults.StackFontScale

/** Tab appearance. */
enum class DcTabStyle { Primary, Secondary }

/** Tabs for switching between sibling views; the feature owns any pager. */
@Composable
fun <T> DcTabRow(
    tabs: List<DcOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    style: DcTabStyle = DcTabStyle.Primary,
    scrollable: Boolean = tabs.size > DcTabRowDefaults.FixedTabLimit,
    badgeCount: (T) -> Int? = { null }
) {
    val selectedIndex = tabs.indexOfFirst { it.value == selected }.coerceAtLeast(0)
    val tabContent: @Composable () -> Unit = {
        tabs.forEachIndexed { index, option ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onSelect(option.value) },
                enabled = option.enabled,
                text = {
                    val count = badgeCount(option.value)
                    if (count != null && count > 0) {
                        DcBadgedBox(badge = { DcBadge(count = count) }) {
                            DcText(option.label, maxLines = 1, style = MaterialTheme.typography.titleSmall)
                        }
                    } else {
                        DcText(option.label, maxLines = 1, style = MaterialTheme.typography.titleSmall)
                    }
                }
            )
        }
    }
    when {
        style == DcTabStyle.Primary && scrollable ->
            PrimaryScrollableTabRow(selectedTabIndex = selectedIndex, modifier = modifier, tabs = tabContent)
        style == DcTabStyle.Primary ->
            PrimaryTabRow(selectedTabIndex = selectedIndex, modifier = modifier, tabs = tabContent)
        scrollable ->
            SecondaryScrollableTabRow(selectedTabIndex = selectedIndex, modifier = modifier, tabs = tabContent)
        else ->
            SecondaryTabRow(selectedTabIndex = selectedIndex, modifier = modifier, tabs = tabContent)
    }
}

object DcTabRowDefaults {
    const val FixedTabLimit: Int = 4
}

@DcThemePreviews
@Composable
private fun DcLayoutPreview() {
    DcTheme {
        Column {
            DcTabRow(listOf(DcOption(0, "All"), DcOption(1, "Unread")), selected = 0, onSelect = {}, badgeCount = { if (it == 1) 4 else null })
            DcExpandableSection("Details", expanded = true, onExpandedChange = {}) { DcText("Body") }
            DcSkeletonListRow()
            DcPageIndicator(pageCount = 3, currentPage = 1)
            DcListAppendState(isLoading = true, errorMessage = null)
        }
    }
}
