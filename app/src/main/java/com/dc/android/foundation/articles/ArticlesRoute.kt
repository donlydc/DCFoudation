package com.dc.android.foundation.articles

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dc.android.foundation.platform.permission.NotificationPermission
import kotlinx.coroutines.flow.collect

/**
 * Stateful entry point — the only place in this feature that resolves a ViewModel via DI (DI-03).
 * Second destination the app has (06-navigation.md's NAV-01 trigger condition).
 *
 * Also the feature's PERM-01 call site: `Route` is explicitly the layer allowed to touch
 * Android-framework permission plumbing (`rememberLauncherForActivityResult`,
 * `ActivityResultContracts.RequestPermission`) — `ArticlesViewModel`/`ArticlesScreen` never see a
 * raw permission API, only [com.dc.android.foundation.platform.permission.PermissionState].
 */
@Composable
fun ArticlesRoute(
    modifier: Modifier = Modifier,
    viewModel: ArticlesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onAction(
            ArticlesUiAction.UpdateNotificationPermissionState(
                NotificationPermission.fromRequestResult(granted)
            )
        )
    }

    LaunchedEffect(Unit) {
        viewModel.onAction(
            ArticlesUiAction.UpdateNotificationPermissionState(
                NotificationPermission.currentState(context)
            )
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ArticlesUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
                ArticlesUiEffect.LaunchNotificationPermissionRequest ->
                    permissionLauncher.launch(NotificationPermission.permissionName)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        ArticlesScreen(
            state = uiState,
            onAction = viewModel::onAction,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
