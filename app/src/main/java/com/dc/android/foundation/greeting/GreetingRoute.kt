package com.dc.android.foundation.greeting

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dc.android.foundation.designsystem.components.DcSnackbarEffect
import com.dc.android.foundation.designsystem.components.DcSnackbarHost
import com.dc.android.foundation.designsystem.components.DcSnackbarHostState
import com.dc.android.foundation.designsystem.components.DcSnackbarMessage
import com.dc.android.foundation.designsystem.components.DcScaffold

/**
 * Stateful entry point — the only place in this feature that resolves a ViewModel via DI
 * (DI-03) and the only destination the app has today (06-navigation.md: no NavHost yet,
 * nothing to navigate between).
 */
@Composable
fun GreetingRoute(
    modifier: Modifier = Modifier,
    viewModel: GreetingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { DcSnackbarHostState() }

    DcSnackbarEffect(
        message = uiState.userMessage?.let { DcSnackbarMessage(it) },
        state = snackbarHostState,
        onMessageShown = { viewModel.onAction(GreetingUiAction.UserMessageShown) }
    )

    DcScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { DcSnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        GreetingScreen(
            state = uiState,
            onAction = viewModel::onAction,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
