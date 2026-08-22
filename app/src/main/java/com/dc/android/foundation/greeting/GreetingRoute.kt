package com.dc.android.foundation.greeting

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onAction(GreetingUiAction.UserMessageShown)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        GreetingScreen(
            state = uiState,
            onAction = viewModel::onAction,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
