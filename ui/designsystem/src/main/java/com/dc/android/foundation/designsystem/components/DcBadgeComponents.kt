package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dc.android.foundation.designsystem.preview.DcThemePreviews
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.theme.LocalDcColors
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

/** Text shown inside a count badge; counts above [maxCount] collapse to "<maxCount>+". */
fun formatBadgeCount(count: Int, maxCount: Int = 99): String =
    if (count > maxCount) "$maxCount+" else count.toString()

/** Solid container/content color pair for [tone]; shared by badge-like indicators. */
@Composable
internal fun solidToneColors(tone: DcStatusTone): Pair<Color, Color> {
    val colorScheme = MaterialTheme.colorScheme
    val dc = LocalDcColors.current
    return when (tone) {
        DcStatusTone.Neutral -> colorScheme.onSurfaceVariant to colorScheme.surface
        DcStatusTone.Info -> dc.info to dc.onInfo
        DcStatusTone.Success -> dc.success to dc.onSuccess
        DcStatusTone.Warning -> dc.warning to dc.onWarning
        DcStatusTone.Error -> colorScheme.error to colorScheme.onError
    }
}

/**
 * Small status indicator: a dot when [count] is null, otherwise a number capped at [maxCount].
 * [contentDescription] should describe the meaning ("3 unread messages"); when null the visible
 * number is read as-is.
 */
@Composable
fun DcBadge(
    modifier: Modifier = Modifier,
    count: Int? = null,
    maxCount: Int = 99,
    tone: DcStatusTone = DcStatusTone.Error,
    contentDescription: String? = null
) {
    val (container, content) = solidToneColors(tone)
    val described = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }
    if (count == null) {
        Surface(
            modifier = modifier.then(described).size(8.dp),
            shape = CircleShape,
            color = container
        ) {}
    } else {
        Surface(
            modifier = modifier.then(described).defaultMinSize(minWidth = 16.dp, minHeight = 16.dp),
            shape = CircleShape,
            color = container,
            contentColor = content
        ) {
            DcText(
                text = formatBadgeCount(count, maxCount),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = LocalDcSpacing.current.xSmall)
            )
        }
    }
}

/** Anchors a [badge] to the top-end corner of [content] (typically an icon). */
@Composable
fun DcBadgedBox(
    badge: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier) {
        content()
        Box(Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-4).dp)) { badge() }
    }
}

@DcThemePreviews
@Composable
private fun DcBadgePreview() {
    DcTheme {
        androidx.compose.foundation.layout.Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)) {
            DcBadge()
            DcBadge(count = 3)
            DcBadge(count = 120)
            DcBadge(count = 7, tone = DcStatusTone.Success)
        }
    }
}
