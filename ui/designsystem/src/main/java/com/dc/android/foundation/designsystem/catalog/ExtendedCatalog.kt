package com.dc.android.foundation.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dc.android.foundation.designsystem.components.*
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing
import kotlinx.coroutines.launch
import java.time.LocalDate

private val Ranges = listOf(DcOption("d", "Day"), DcOption("w", "Week"), DcOption("m", "Month"))
private val MenuItems = listOf(
    DcMenuItem("share", "Share", Icons.Filled.Share),
    DcMenuItem("delete", "Delete", Icons.Filled.Delete, destructive = true)
)

/** Catalog sections for the extended widget set (menus, selection, forms, feedback, layout). */
@Composable
internal fun ExtendedCatalogSections() {
    val spacing = LocalDcSpacing.current
    val scope = rememberCoroutineScope()
    val snackbarState = remember { DcSnackbarHostState() }
    var segmented by rememberSaveable { mutableStateOf("d") }
    var chips by rememberSaveable { mutableStateOf(setOf("w")) }
    var radio by rememberSaveable { mutableStateOf("m") }
    var checks by rememberSaveable { mutableStateOf(setOf("d")) }
    var typed by rememberSaveable { mutableStateOf<String?>("w") }
    var typedExpanded by rememberSaveable { mutableStateOf(false) }
    var tab by rememberSaveable { mutableStateOf("d") }
    var expanded by rememberSaveable { mutableStateOf(true) }
    var favorite by rememberSaveable { mutableStateOf(false) }
    var stepper by rememberSaveable { mutableStateOf(2) }
    var slider by rememberSaveable { mutableStateOf(0.4f) }
    var text by rememberSaveable { mutableStateOf("") }
    var otp by rememberSaveable { mutableStateOf("12") }
    var date by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var actionSheet by rememberSaveable { mutableStateOf(false) }
    var selectionSheet by rememberSaveable { mutableStateOf(false) }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var rangeStart by rememberSaveable { mutableStateOf(0.2f) }
    var rangeEnd by rememberSaveable { mutableStateOf(0.7f) }

    Column(verticalArrangement = Arrangement.spacedBy(spacing.large)) {
        CatalogSection("15. Menus, tooltip, sheets") {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
                DcOverflowMenu(MenuItems, onItemClick = {})
                DcTooltip("Open menu") {
                    DcIconButton(Icons.Filled.Share, "Open menu", onClick = { menuExpanded = true })
                }
                DcDropdownMenu(menuExpanded, { menuExpanded = false }, MenuItems, onItemClick = {})
            }
            DcButton("Action sheet", onClick = { actionSheet = true }, variant = DcButtonVariant.Secondary)
            DcButton("Selection sheet", onClick = { selectionSheet = true }, variant = DcButtonVariant.Secondary)
        }

        CatalogSection("16. Selection") {
            DcSegmentedButton(Ranges, segmented, { segmented = it })
            DcMultiSegmentedButton(Ranges, chips, { chips = it })
            DcChipGroup(Ranges, chips, { chips = it }, selectionMode = DcSelectionMode.Multiple)
            DcChipGroup(Ranges, chips, { chips = it }, selectionMode = DcSelectionMode.SingleOptional, layout = DcChipGroupLayout.Scroll)
            DcRadioGroup(Ranges, radio, { radio = it }, label = "Range")
            DcCheckboxGroup(Ranges, checks, { checks = it }, label = "Ranges", selectAllLabel = "All")
            DcDropdownField("Typed range", Ranges, typed, { typed = it }, typedExpanded, { typedExpanded = it })
        }

        CatalogSection("17. Forms") {
            DcFormSection(title = "Profile", supportingText = "Shown on your public page") {
                DcTextArea("Bio", text, { text = it }, maxLength = 120)
                DcStepper(stepper, { stepper = it }, 0..5, "Guests")
                DcSlider(slider, { slider = it }, "Volume", { "${(it * 100).toInt()}%" })
                DcRangeSlider(rangeStart..rangeEnd, { rangeStart = it.start; rangeEnd = it.endInclusive }, "Budget", { "${(it.start * 100).toInt()}–${(it.endInclusive * 100).toInt()}" })
                DcOtpField(otp, { otp = it }, errorText = null)
                DcDateField("Birthday", date, { date = it }, formattedValue = date?.toString().orEmpty())
            }
        }

        CatalogSection("18. Feedback") {
            DcStatusTone.entries.forEach { DcInlineMessage("${it.name} message", it, onDismiss = {}) }
            DcLinearProgress({ 0.4f }, "Uploading")
            DcLinearProgress(null, "Syncing")
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
                DcCircularProgress(null, "Loading", size = DcProgressSize.Small)
                DcCircularProgress({ 0.6f }, "Progress", size = DcProgressSize.Large)
            }
            DcButton("Show snackbar", onClick = {
                scope.launch { snackbarState.show(DcSnackbarMessage("Saved", DcStatusTone.Success, actionLabel = "Undo")) }
            }, variant = DcButtonVariant.Secondary)
            Box(Modifier.fillMaxWidth().height(80.dp)) {
                DcScaffold(snackbarHost = { DcSnackbarHost(snackbarState) }) {}
            }
            DcSnackbarEffect(null as DcSnackbarMessage?, snackbarState, {})
            DcSkeletonListRow()
            DcSkeletonCard()
        }

        CatalogSection("19. Badges, tabs, toggles") {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.medium)) {
                DcBadge()
                DcBadge(count = 3)
                DcBadge(count = 120)
                DcBadgedBox(badge = { DcBadge(count = 5) }) { DcIcon(Icons.Filled.Home, "Home") }
                DcIconToggleButton(favorite, { favorite = it }, Icons.Filled.Favorite, Icons.Filled.FavoriteBorder, "Favorite")
            }
            DcAvatarSize.entries.let { sizes ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
                    sizes.forEach { DcAvatar("Donly", size = it) }
                }
            }
            DcTabRow(Ranges, tab, { tab = it }, badgeCount = { if (it == "w") 2 else null })
            DcTabRow(Ranges, tab, { tab = it }, style = DcTabStyle.Secondary)
            DcPageIndicator(pageCount = 4, currentPage = 1)
        }

        CatalogSection("20. Layout and lists") {
            DcExpandableSection("Details", expanded, { expanded = it }, supportingText = "Tap to toggle") {
                DcText("Expandable body content")
            }
            DcListAppendState(isLoading = true, errorMessage = null)
            DcListAppendState(isLoading = false, errorMessage = "Could not load more", onRetry = {})
            DcListAppendState(isLoading = false, errorMessage = null, endReachedText = "You have reached the end")
            DcButtonRow(primary = { DcButton("Save", onClick = {}, modifier = it) }, secondary = { DcButton("Cancel", onClick = {}, variant = DcButtonVariant.Secondary, modifier = it) })
            DcContentContainer(maxWidth = DcContentWidth.Readable) { DcCard { DcText("Width-capped content") } }
            DcAdaptiveGrid(Modifier.height(160.dp)) {
                items(4) { DcCard { DcText("Cell ${it + 1}") } }
            }
        }
    }

    if (actionSheet) {
        DcActionSheet(MenuItems, onItemClick = {}, onDismissRequest = { actionSheet = false }, title = "Actions")
    }
    if (selectionSheet) {
        DcSelectionSheet("Range", Ranges, typed, { typed = it }, { selectionSheet = false })
    }
}
