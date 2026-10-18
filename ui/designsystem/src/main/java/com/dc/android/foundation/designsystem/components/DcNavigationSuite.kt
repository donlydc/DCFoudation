package com.dc.android.foundation.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow

/** A top-level item rendered as a bottom bar, rail, or drawer according to available window size. */
data class DcNavigationItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String = label,
    /** Unread/pending count; null or 0 shows no badge. */
    val badgeCount: Int? = null
)

/**
 * Adaptive top-level navigation. One declaration becomes a navigation bar, rail, or permanent
 * drawer via Material3's NavigationSuiteScaffold, keeping feature code independent of form factor.
 */
@Composable
fun DcNavigationSuite(
    items: List<DcNavigationItem>,
    selected: DcNavigationItem,
    onSelect: (DcNavigationItem) -> Unit,
    alwaysShowLabel: Boolean = true,
    content: @Composable () -> Unit
) {
    requireSupportedNavigationItemCount(items.size)

    val colorScheme = MaterialTheme.colorScheme
    val itemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = colorScheme.primary,
            selectedTextColor = colorScheme.primary,
            indicatorColor = colorScheme.primaryContainer,
            unselectedIconColor = colorScheme.onSurfaceVariant,
            unselectedTextColor = colorScheme.onSurfaceVariant
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = colorScheme.primary,
            selectedTextColor = colorScheme.primary,
            indicatorColor = colorScheme.primaryContainer,
            unselectedIconColor = colorScheme.onSurfaceVariant,
            unselectedTextColor = colorScheme.onSurfaceVariant
        ),
        navigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = colorScheme.primaryContainer,
            selectedIconColor = colorScheme.primary,
            selectedTextColor = colorScheme.primary,
            unselectedIconColor = colorScheme.onSurfaceVariant,
            unselectedTextColor = colorScheme.onSurfaceVariant
        )
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            items.forEach { item ->
                item(
                    selected = item.id == selected.id,
                    onClick = { onSelect(item) },
                    icon = {
                        val count = item.badgeCount
                        if (count != null && count > 0) {
                            DcBadgedBox(badge = { DcBadge(count = count) }) {
                                DcIcon(item.icon, item.contentDescription)
                            }
                        } else {
                            DcIcon(item.icon, item.contentDescription)
                        }
                    },
                    label = {
                        DcText(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    alwaysShowLabel = alwaysShowLabel,
                    colors = itemColors
                )
            }
        },
        content = content
    )
}

internal fun requireSupportedNavigationItemCount(itemCount: Int) {
    require(itemCount in 3..5) {
        "DcNavigationSuite supports 3 to 5 top-level destinations; move remaining items " +
            "into More or a navigation drawer."
    }
}
