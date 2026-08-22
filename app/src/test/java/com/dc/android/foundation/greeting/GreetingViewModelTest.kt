package com.dc.android.foundation.greeting

import androidx.lifecycle.SavedStateHandle
import com.dc.android.foundation.data.preferences.PreferencesDataSource
import com.dc.android.foundation.testing.FakeDispatcherProvider
import com.dc.android.foundation.testing.InMemoryPreferencesDataSource
import com.dc.android.foundation.testing.MainDispatcherRule
import com.dc.android.foundation.testing.NoOpLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

/** Gate E/F/G requirement: state-transition coverage for the sample feature. */
@OptIn(ExperimentalCoroutinesApi::class)
class GreetingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeProvider = FakeGreetingProvider()
    private val fakeDispatcherProvider = FakeDispatcherProvider(Dispatchers.Main)

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        preferencesDataSource: PreferencesDataSource = InMemoryPreferencesDataSource()
    ) = GreetingViewModel(
        fakeProvider,
        GreetingPreferencesRepository(preferencesDataSource),
        fakeDispatcherProvider,
        NoOpLogger,
        savedStateHandle
    )

    @Test
    fun `initial state greets the first name`() {
        val viewModel = createViewModel()

        assertEquals("Hello Android!", viewModel.uiState.value.message)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `Refresh action advances to the next name`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(GreetingUiAction.Refresh)
        advanceUntilIdle()

        assertEquals("Hello World!", viewModel.uiState.value.message)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `Refresh action cycles back to the first name after the last`() = runTest {
        val viewModel = createViewModel()

        repeat(3) {
            viewModel.onAction(GreetingUiAction.Refresh)
            advanceUntilIdle()
        }

        assertEquals("Hello Android!", viewModel.uiState.value.message)
    }

    @Test
    fun `Refresh action exposes a user message until acknowledged`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(GreetingUiAction.Refresh)
        advanceUntilIdle()

        assertEquals("Refreshed", viewModel.uiState.value.userMessage)

        viewModel.onAction(GreetingUiAction.UserMessageShown)

        assertEquals(null, viewModel.uiState.value.userMessage)
    }

    @Test
    fun `nameIndex survives via SavedStateHandle`() = runTest {
        val handle = SavedStateHandle()
        val first = createViewModel(handle)
        first.onAction(GreetingUiAction.Refresh)
        advanceUntilIdle()

        // A fresh ViewModel over the same handle simulates process-death restoration.
        val restored = createViewModel(handle)

        assertEquals("Hello World!", restored.uiState.value.message)
        assertEquals(1, handle.get<Int>("nameIndex"))
    }

    @Test
    fun `restart without SavedStateHandle falls back to the persisted preference`() = runTest {
        val sharedPreferences = InMemoryPreferencesDataSource()
        val first = createViewModel(preferencesDataSource = sharedPreferences)
        first.onAction(GreetingUiAction.Refresh)
        advanceUntilIdle()

        // A fresh SavedStateHandle simulates a full app restart (not just process death),
        // sharing only the durable preferences store.
        val restarted = createViewModel(
            savedStateHandle = SavedStateHandle(),
            preferencesDataSource = sharedPreferences
        )
        advanceUntilIdle()

        assertEquals("Hello World!", restarted.uiState.value.message)
    }
}

private class FakeGreetingProvider : GreetingProvider {
    override fun greetingFor(name: String): String = "Hello $name!"
}
