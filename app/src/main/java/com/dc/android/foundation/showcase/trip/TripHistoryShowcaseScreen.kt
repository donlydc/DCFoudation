package com.dc.android.foundation.showcase.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dc.android.foundation.designsystem.components.DcCard
import com.dc.android.foundation.designsystem.components.DcChip
import com.dc.android.foundation.designsystem.components.DcEmptyState
import com.dc.android.foundation.designsystem.components.DcIconButton
import com.dc.android.foundation.designsystem.components.DcListRow
import com.dc.android.foundation.designsystem.components.DcScaffold
import com.dc.android.foundation.designsystem.components.DcStatusChip
import com.dc.android.foundation.designsystem.components.DcStatusTone
import com.dc.android.foundation.designsystem.components.DcText
import com.dc.android.foundation.designsystem.components.DcTopAppBar
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

/** Trip data demonstrates a neutral record-list pattern; this is not a real feature. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TripHistoryShowcaseScreen(modifier: Modifier = Modifier) {
    var selectedFilter by rememberSaveable { mutableStateOf("All") }
    val spacing = LocalDcSpacing.current
    val filters = listOf("All", "Completed", "Pending", "Cancelled")

    DcScaffold(
        modifier = modifier,
        topBar = {
            DcTopAppBar(
                title = "Trip History",
                actions = {
                    DcIconButton(Icons.Filled.Search, "Search trips", onClick = {})
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyRow(
                contentPadding = PaddingValues(
                    horizontal = spacing.medium,
                    vertical = spacing.small
                ),
                horizontalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                items(filters) { filter ->
                    DcChip(
                        label = filter,
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }
            val trips = TripShowcaseData.trips.filter {
                selectedFilter == "All" || it.status.label == selectedFilter
            }
            if (trips.isEmpty()) {
                DcEmptyState(
                    title = "No trips yet",
                    modifier = Modifier.fillMaxSize(),
                    description = "Trips matching this filter will appear here."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(spacing.component)
                ) {
                    items(trips, key = TripShowcaseItem::id) { trip ->
                        TripHistoryCard(trip)
                    }
                }
            }
        }
    }
}

@Composable
private fun TripHistoryCard(trip: TripShowcaseItem) {
    val spacing = LocalDcSpacing.current
    DcCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = {},
        contentPadding = PaddingValues.Zero
    ) {
        DcListRow(
            headline = trip.id,
            supportingText = "${trip.dateTime} · ${trip.summary}",
            trailingContent = {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(spacing.xSmall)
                ) {
                    DcText(trip.fare, style = MaterialTheme.typography.titleMedium)
                    DcStatusChip(text = trip.status.label, tone = trip.status.toTone())
                }
            }
        )
    }
}

private fun TripStatus.toTone(): DcStatusTone = when (this) {
    TripStatus.Active -> DcStatusTone.Info
    TripStatus.Completed -> DcStatusTone.Success
    TripStatus.Pending -> DcStatusTone.Warning
    TripStatus.Cancelled -> DcStatusTone.Error
}
