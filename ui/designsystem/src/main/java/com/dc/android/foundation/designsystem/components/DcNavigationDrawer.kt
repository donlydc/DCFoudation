package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.dc.android.foundation.designsystem.preview.DcThemePreviews
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

/** A drawer destination. [icon] is required so every row keeps a consistent alignment rhythm. */
data class DcDrawerItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String = label
)

/** A related group of drawer destinations, optionally introduced by a short [title]. */
data class DcDrawerSection(
    val title: String? = null,
    val items: List<DcDrawerItem>
)

/**
 * Business-neutral drawer content for a caller-owned `ModalNavigationDrawer` or
 * `PermanentNavigationDrawer`. Keep this list focused on top-level destinations instead of using
 * the drawer as a home for every application action.
 */
@Composable
fun DcNavigationDrawerContent(
    sections: List<DcDrawerSection>,
    selectedItemId: String?,
    onItemClick: (DcDrawerItem) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null
) {
    val spacing = LocalDcSpacing.current
    val colorScheme = MaterialTheme.colorScheme

    ModalDrawerSheet(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            header?.invoke()
            sections.forEachIndexed { index, section ->
                if (index > 0) {
                    DcDivider()
                    Spacer(modifier = Modifier.height(spacing.component))
                }
                section.title?.let { title ->
                    DcText(
                        text = title,
                        modifier = Modifier.padding(
                            horizontal = spacing.medium,
                            vertical = spacing.small
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(
                    modifier = Modifier.padding(horizontal = spacing.small),
                    verticalArrangement = Arrangement.spacedBy(spacing.xSmall)
                ) {
                    section.items.forEach { item ->
                        NavigationDrawerItem(
                            label = {
                                DcText(
                                    text = item.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            selected = item.id == selectedItemId,
                            onClick = { onItemClick(item) },
                            icon = { DcIcon(item.icon, item.contentDescription) },
                            shape = MaterialTheme.shapes.medium,
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = colorScheme.primaryContainer,
                                selectedTextColor = colorScheme.onPrimaryContainer,
                                selectedIconColor = colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    }
}

/** A compact, reusable identity header for [DcNavigationDrawerContent]. */
@Composable
fun DcNavigationDrawerHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    avatar: (@Composable () -> Unit)? = null
) {
    val spacing = LocalDcSpacing.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        avatar?.let {
            it()
            Spacer(modifier = Modifier.width(spacing.medium))
        }
        Column(modifier = Modifier.weight(1f)) {
            DcText(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            subtitle?.let {
                Spacer(modifier = Modifier.height(spacing.xSmall))
                DcText(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@DcThemePreviews
@Composable
private fun DcNavigationDrawerContentPreview() {
    DcTheme {
        DcNavigationDrawerContent(
            sections = listOf(
                DcDrawerSection(
                    title = "Main",
                    items = listOf(
                        DcDrawerItem("home", "Home", Icons.Default.Home),
                        DcDrawerItem("settings", "Settings", Icons.Default.Settings)
                    )
                )
            ),
            selectedItemId = "home",
            onItemClick = {},
            header = {
                DcNavigationDrawerHeader(
                    title = "DC Foundation",
                    subtitle = "foundation@example.com"
                )
            }
        )
    }
}

@DcThemePreviews
@Composable
private fun DcNavigationDrawerHeaderPreview() {
    DcTheme {
        DcNavigationDrawerHeader(
            title = "DC Foundation",
            subtitle = "foundation@example.com",
            avatar = { DcIcon(Icons.Default.Person, contentDescription = null) }
        )
    }
}
