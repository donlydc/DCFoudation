package com.dc.android.foundation.showcase.trip

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dc.android.foundation.designsystem.components.DcBottomActionBar
import com.dc.android.foundation.designsystem.components.DcCard
import com.dc.android.foundation.designsystem.components.DcConfirmDialog
import com.dc.android.foundation.designsystem.components.DcDivider
import com.dc.android.foundation.designsystem.components.DcIconButton
import com.dc.android.foundation.designsystem.components.DcKeyValueRow
import com.dc.android.foundation.designsystem.components.DcPrimaryActionButton
import com.dc.android.foundation.designsystem.components.DcScaffold
import com.dc.android.foundation.designsystem.components.DcSectionHeader
import com.dc.android.foundation.designsystem.components.DcText
import com.dc.android.foundation.designsystem.components.DcTopAppBar
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

/** Trip data demonstrates a neutral detail-summary pattern; this is not a real feature. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TripDetailsShowcaseScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEndTripConfirmation by rememberSaveable { mutableStateOf(false) }
    val spacing = LocalDcSpacing.current

    DcScaffold(
        modifier = modifier,
        topBar = {
            DcTopAppBar(
                title = "Trip details",
                onNavigateBack = onNavigateBack,
                actions = {
                    DcIconButton(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More trip actions",
                        onClick = {}
                    )
                }
            )
        },
        bottomBar = {
            DcBottomActionBar {
                DcPrimaryActionButton(
                    text = "End Trip",
                    onClick = { showEndTripConfirmation = true },
                    destructive = true
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.medium),
        ) {
            DcSectionHeader(
                "Trip information",
                contentPadding = PaddingValues(top = spacing.large, bottom = spacing.small)
            )
            DcCard(modifier = Modifier.fillMaxWidth()) {
                DcKeyValueRow("Distance", "12.6 km")
                DcKeyValueRow("Duration", "24 min")
                DcKeyValueRow("Waiting time", "3 min")
            }
            DcSectionHeader(
                "Fare details",
                contentPadding = PaddingValues(top = spacing.large, bottom = spacing.small)
            )
            DcCard(modifier = Modifier.fillMaxWidth()) {
                DcKeyValueRow("Base fare", "225,000 đ")
                DcKeyValueRow("Waiting fee", "20,000 đ")
                DcDivider()
                DcKeyValueRow("Total", "245,000 đ", emphasized = true)
            }
        }
    }

    if (showEndTripConfirmation) {
        DcConfirmDialog(
            title = "End trip?",
            message = "Are you sure you want to end this trip? This action cannot be undone.",
            confirmLabel = "End Trip",
            onConfirm = { showEndTripConfirmation = false },
            onDismissRequest = { showEndTripConfirmation = false },
            dismissLabel = "Cancel",
            destructive = true
        )
    }
}
