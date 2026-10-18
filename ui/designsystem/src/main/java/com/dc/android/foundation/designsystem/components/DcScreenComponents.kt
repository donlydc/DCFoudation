package com.dc.android.foundation.designsystem.components

import androidx.compose.ui.res.stringResource
import com.dc.android.foundation.designsystem.R
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.dc.android.foundation.designsystem.preview.DcThemePreviews
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

/** Supported layouts for [DcTopAppBar]. */
enum class DcTopAppBarStyle { Small, Centered, Large }

/**
 * Top app bar that centralizes status-bar inset handling, long-title truncation and the presence
 * of a back action. A null [onNavigateBack] means a top-level destination, so no back affordance
 * is rendered. [navigateBackLabel] is the navigation icon's localized content description.
 * Keep two or three visible [actions] at most and place additional actions in overflow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DcTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    style: DcTopAppBarStyle = DcTopAppBarStyle.Small,
    onNavigateBack: (() -> Unit)? = null,
    navigateBackLabel: String = stringResource(R.string.dc_back),
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val titleContent: @Composable () -> Unit = {
        DcText(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
    val navigationIcon: @Composable () -> Unit = {
        onNavigateBack?.let { back ->
            DcIconButton(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = navigateBackLabel,
                onClick = back
            )
        }
    }
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface
    )
    when (style) {
        DcTopAppBarStyle.Small -> TopAppBar(
            title = titleContent, modifier = modifier, navigationIcon = navigationIcon,
            actions = actions, colors = colors, scrollBehavior = scrollBehavior
        )
        DcTopAppBarStyle.Centered -> CenterAlignedTopAppBar(
            title = titleContent, modifier = modifier, navigationIcon = navigationIcon,
            actions = actions, colors = colors, scrollBehavior = scrollBehavior
        )
        DcTopAppBarStyle.Large -> LargeTopAppBar(
            title = titleContent, modifier = modifier, navigationIcon = navigationIcon,
            actions = actions, colors = colors, scrollBehavior = scrollBehavior
        )
    }
}

/** Section title with heading semantics and consistent surrounding spacing. */
@Composable
fun DcSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    contentPadding: PaddingValues? = null
) {
    val resolvedPadding = contentPadding ?: PaddingValues(
        start = LocalDcSpacing.current.medium,
        end = LocalDcSpacing.current.medium,
        top = LocalDcSpacing.current.large,
        bottom = LocalDcSpacing.current.small
    )
    androidx.compose.foundation.layout.Column(
        modifier = modifier.padding(resolvedPadding)
    ) {
        DcText(
            title,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleMedium
        )
        supportingText?.let { DcText(it, style = MaterialTheme.typography.bodyMedium) }
    }
}

/**
 * Persistent bottom action container. Navigation and IME padding are applied once so form
 * actions are not hidden by the keyboard or system bar.
 */
@Composable
fun DcBottomActionBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(modifier = modifier) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
                .padding(LocalDcSpacing.current.medium),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(LocalDcSpacing.current.small),
            content = content
        )
    }
}

/** Screen scaffold with safe drawing insets and a single standard snackbar/content arrangement. */
@Composable
fun DcScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        contentWindowInsets = WindowInsets.safeDrawing,
        content = content
    )
}

@DcThemePreviews
@Composable
private fun DcTopAppBarPreview() {
    DcTheme {
        Column {
            DcTopAppBar(
                title = "Small app bar",
                style = DcTopAppBarStyle.Small,
                onNavigateBack = {}
            )
            DcTopAppBar(
                title = "Centered app bar",
                style = DcTopAppBarStyle.Centered,
                onNavigateBack = {}
            )
            DcTopAppBar(
                title = "Large app bar",
                style = DcTopAppBarStyle.Large,
                onNavigateBack = {}
            )
        }
    }
}
