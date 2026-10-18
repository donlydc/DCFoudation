package com.dc.android.foundation.showcase.trip

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.dc.android.foundation.designsystem.components.DcEmptyState
import com.dc.android.foundation.designsystem.components.DcNavigationItem
import com.dc.android.foundation.designsystem.components.DcNavigationSuite

/** Màn hình trình diễn design system. Không phải feature thật; không có nguồn dữ liệu. */
@Composable
fun TripShowcaseHost() {
    val items = listOf(
        DcNavigationItem("home", "Home", Icons.Filled.Home),
        DcNavigationItem("history", "History", Icons.Filled.Check),
        DcNavigationItem("report", "Report", Icons.Filled.Warning),
        DcNavigationItem("more", "More", Icons.Filled.Settings)
    )
    var selectedId by rememberSaveable { mutableStateOf(items.first().id) }
    val selected = items.first { it.id == selectedId }

    DcNavigationSuite(
        items = items,
        selected = selected,
        onSelect = { selectedId = it.id }
    ) {
        when (selectedId) {
            "home" -> TripDetailsShowcaseScreen(onNavigateBack = { selectedId = "history" })
            "history" -> TripHistoryShowcaseScreen()
            "report" -> DcEmptyState("Coming soon", description = "Reports will appear here.")
            else -> DcEmptyState("Coming soon", description = "More tools will appear here.")
        }
    }
}
