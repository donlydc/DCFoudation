package com.dc.android.foundation.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dc.android.foundation.designsystem.components.*
import com.dc.android.foundation.designsystem.preview.DcThemePreviews
import com.dc.android.foundation.designsystem.preview.DcWindowPreviews
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.theme.LocalDcColors
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

/** Visual contract for the complete design system, ordered to match the UI style guide. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignSystemCatalog(modifier: Modifier = Modifier) {
    val spacing = LocalDcSpacing.current
    var selectedChip by rememberSaveable { mutableStateOf(false) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var dropdownExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedOption by rememberSaveable { mutableStateOf("System") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var dialog by rememberSaveable { mutableStateOf<String?>(null) }
    var showSheet by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.large)
    ) {
        DcTopAppBar(title = "DC Design System catalog")
        ColorSection()
        TypographySection()

        CatalogSection("3. Buttons") {
            DcButton("Primary", onClick = {})
            DcButton("Secondary", onClick = {}, variant = DcButtonVariant.Secondary)
            DcButton("Tertiary", onClick = {}, variant = DcButtonVariant.Tertiary)
            DcButton("Destructive", onClick = {}, variant = DcButtonVariant.Destructive)
            DcButton("Disabled", onClick = {}, enabled = false)
            DcButton("Loading", onClick = {}, loading = true)
            DcButton("With icon", onClick = {}, leadingIcon = Icons.Filled.Check)
            DcPrimaryActionButton("Full-width primary action", onClick = {})
            DcText("Pressed treatment follows Material interaction state.", style = MaterialTheme.typography.bodySmall)
            DcTextLink("Text link", onClick = {})
            DcFab(Icons.Filled.Check, "Confirm", onClick = {})
            DcExtendedFab("Create", Icons.Filled.Check, "Create", onClick = {})
        }

        CatalogSection("4. Text fields") {
            DcTextField("Display name", "Donly", {}, supportingText = "Supporting text")
            DcTextField("Search", "", {}, placeholder = "Search", leadingIcon = Icons.Filled.Search)
            DcTextField("Invalid value", "Value", {}, errorText = "Explain how to fix this value")
            DcTextField("Disabled", "Value", {}, enabled = false)
            DcTextField("Read only", "Value", {}, readOnly = true)
            DcPasswordTextField("Password", "password", {}, "Show password", "Hide password")
            DcDropdownField(
                "Theme", selectedOption, dropdownExpanded, { dropdownExpanded = it },
                listOf("System", "Light", "Dark"), { selectedOption = it }
            )
            DcSearchBar(searchQuery, { searchQuery = it }, "Search catalog")
            DcToggleRow(
                "Enable setting", enabled, { enabled = it },
                supportingText = "The entire row is interactive", control = DcToggleControl.Switch
            )
        }

        CatalogSection("5. Top app bars") {
            DcTopAppBar("Small", onNavigateBack = {})
            DcTopAppBar("Centered", style = DcTopAppBarStyle.Centered)
            DcTopAppBar("Large", style = DcTopAppBarStyle.Large, actions = {
                DcIconButton(Icons.Filled.Search, "Search", onClick = {})
            })
        }

        CatalogSection("6. Adaptive navigation") {
            val navItems = listOf(
                DcNavigationItem("home", "Home", Icons.Filled.Home),
                DcNavigationItem("saved", "Saved", Icons.Filled.Check),
                DcNavigationItem("settings", "Settings", Icons.Filled.Settings)
            )
            Box(Modifier.fillMaxWidth().height(240.dp)) {
                DcNavigationSuite(navItems, navItems.first(), {}) {
                    DcText("Bar, rail, or drawer follows window width", modifier = Modifier.padding(spacing.medium))
                }
            }
        }

        CatalogSection("7. Navigation drawer") {
            val drawerItems = listOf(
                DcDrawerItem("home", "Home", Icons.Filled.Home),
                DcDrawerItem("settings", "Settings", Icons.Filled.Settings)
            )
            DcNavigationDrawerContent(
                sections = listOf(
                    DcDrawerSection("Main", drawerItems),
                    DcDrawerSection(items = listOf(DcDrawerItem("help", "Help", Icons.Filled.Warning)))
                ),
                selectedItemId = "home",
                onItemClick = {},
                modifier = Modifier.height(360.dp),
                header = {
                    DcNavigationDrawerHeader(
                        title = "Account",
                        subtitle = "account@example.com",
                        avatar = { DcAvatar("Account") }
                    )
                }
            )
        }

        CatalogSection("8. Icon buttons") {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
                DcIcon(Icons.Filled.Check, contentDescription = null)
                DcIconButton(Icons.Filled.Check, "Standard", onClick = {})
                DcIconButton(Icons.Filled.Settings, "Tonal", onClick = {}, variant = DcIconButtonVariant.Tonal)
                DcIconButton(Icons.Filled.Delete, "Delete", onClick = {}, variant = DcIconButtonVariant.SoftDestructive)
            }
        }

        CatalogSection("9. Cards and list rows") {
            DcCard { DcText("Outlined card") }
            DcCard(variant = DcCardVariant.Elevated) { DcText("Elevated card") }
            DcCard(onClick = {}) { DcText("Clickable card") }
            DcCard {
                DcKeyValueRow("Subtotal", "$120.00")
                DcKeyValueRow("Service fee", "$8.00")
                DcDivider()
                DcKeyValueRow("Total", "$128.00", emphasized = true)
            }
            DcListRow(
                headline = "Account settings", supportingText = "Manage sign-in and security",
                leadingContent = { DcAvatar("Donly") }, selected = true, onClick = {}
            )
            DcAsyncImage(null, "Empty image sample", modifier = Modifier.size(48.dp))
            DcDivider(leadingInset = true)
        }

        CatalogSection("10. Chips") {
            DcChip("Filter", selectedChip, { selectedChip = !selectedChip })
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
                DcStatusTone.entries.forEach { tone -> DcStatusChip(tone.name, tone) }
            }
        }

        CatalogSection("11. Dialogs") {
            DcButton("Confirm", onClick = { dialog = "confirm" })
            DcButton("Destructive", onClick = { dialog = "destructive" })
            DcButton("Information", onClick = { dialog = "info" })
            DcButton("Error", onClick = { dialog = "error" })
            DcButton("Alert", onClick = { dialog = "alert" })
        }

        CatalogSection("12. Bottom sheet and layout") {
            DcButton("Show bottom sheet", onClick = { showSheet = true })
            DcPullToRefreshBox(isRefreshing = false, onRefresh = {}) {
                DcText("Pull-to-refresh content", modifier = Modifier.padding(spacing.small))
            }
            DcBottomActionBar { DcButton("Save", onClick = {}) }
            Box(Modifier.fillMaxWidth().height(120.dp)) {
                DcScaffold { DcText("Scaffold content", modifier = Modifier.padding(it)) }
            }
        }

        CatalogSection("13. States") {
            DcLoadingState("Loading inline", size = DcLoadingSize.Inline)
            DcLoadingState("Loading screen")
            DcSkeleton()
            DcEmptyState("No saved items", description = "Items you save will appear here.", icon = Icons.Filled.Search)
            DcErrorState(
                "Could not load items", "Check your connection and try again.",
                retryLabel = "Retry", onRetry = {}, icon = Icons.Filled.Warning
            )
            DcOfflineBanner("You are offline. Changes will sync when connected.")
            DcContentState(
                value = listOf("Cached item"), isLoading = true,
                errorMessage = "Showing cached data while refreshing"
            ) { DcText(it.first()) }
            DcSnackbarEffect(null, snackbarHostState, {})
        }

        ExtendedCatalogSections()

        SpacingAndShapeSection()
    }

    when (dialog) {
        "confirm" -> DcConfirmDialog("Continue?", "Confirm this action.", "Continue", {}, { dialog = null })
        "destructive" -> DcConfirmDialog(
            "Delete item?", "This action cannot be undone.", "Delete", {}, { dialog = null }, destructive = true
        )
        "info" -> DcInfoDialog("Information", "A single-action dialog.", "OK", { dialog = null })
        "error" -> DcErrorDialog("Something went wrong", "Try again in a moment.", "OK", { dialog = null })
        "alert" -> DcAlertDialog("Alert", "A standard alert dialog.", "OK", { dialog = null }, { dialog = null })
    }
    if (showSheet) {
        DcModalBottomSheet(onDismissRequest = { showSheet = false }) {
            DcText("Bottom sheet with drag handle")
            DcButton("Close", onClick = { showSheet = false })
        }
    }
}

@Composable
internal fun CatalogSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(LocalDcSpacing.current.component)) {
        DcSectionHeader(title)
        content()
    }
}

@Composable
private fun ColorSection() {
    val scheme = MaterialTheme.colorScheme
    val extended = LocalDcColors.current
    CatalogSection("1. Color roles") {
        listOf(
            "primary" to scheme.primary, "primaryContainer" to scheme.primaryContainer,
            "surface" to scheme.surface, "background" to scheme.background,
            "surfaceVariant" to scheme.surfaceVariant, "outline" to scheme.outline,
            "error" to scheme.error, "errorContainer" to scheme.errorContainer,
            "success" to extended.success, "warning" to extended.warning, "info" to extended.info
        ).forEach { (name, color) -> ColorSwatch(name, color) }
    }
}

@Composable
private fun ColorSwatch(name: String, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(LocalDcSpacing.current.component)) {
        Box(Modifier.size(32.dp).background(color, MaterialTheme.shapes.small))
        DcText(name, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TypographySection() {
    val typography = MaterialTheme.typography
    CatalogSection("2. Typography") {
        listOf(
            "displaySmall" to typography.displaySmall, "headlineSmall" to typography.headlineSmall,
            "titleLarge" to typography.titleLarge, "titleMedium" to typography.titleMedium,
            "titleSmall" to typography.titleSmall, "bodyLarge" to typography.bodyLarge,
            "bodyMedium" to typography.bodyMedium, "bodySmall" to typography.bodySmall,
            "labelLarge" to typography.labelLarge, "labelMedium" to typography.labelMedium,
            "labelSmall" to typography.labelSmall
        ).forEach { (name, style) -> TypographySample(name, style) }
    }
}

@Composable
private fun TypographySample(name: String, style: TextStyle) {
    DcText("$name · ${style.fontSize.value.toInt()}sp", style = style)
}

@Composable
private fun SpacingAndShapeSection() {
    val spacing = LocalDcSpacing.current
    CatalogSection("14. Spacing and shape") {
        listOf(
            "4" to spacing.xSmall, "8" to spacing.small, "12" to spacing.component,
            "16" to spacing.medium, "24" to spacing.large, "32" to spacing.xLarge
        ).forEach { (name, value) -> SpacingSample(name, value) }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
            listOf(
                MaterialTheme.shapes.small, MaterialTheme.shapes.medium,
                MaterialTheme.shapes.large, MaterialTheme.shapes.extraLarge
            ).forEach { shape ->
                Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, shape))
            }
        }
    }
}

@Composable
private fun SpacingSample(name: String, width: Dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(LocalDcSpacing.current.component)) {
        Box(Modifier.height(8.dp).fillMaxWidth(width.value / 32f).background(MaterialTheme.colorScheme.primary))
        DcText("${name}dp", style = MaterialTheme.typography.bodySmall)
    }
}

@DcThemePreviews
@DcWindowPreviews
@Composable
private fun DesignSystemCatalogPreview() {
    DcTheme { DesignSystemCatalog() }
}
