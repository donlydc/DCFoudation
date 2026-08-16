package com.dc.android.foundation.greeting

import androidx.lifecycle.SavedStateHandle
import com.dc.android.foundation.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

/** Gate E requirement: state-transition coverage for the sample feature. */
@OptIn(ExperimentalCoroutinesApi::class)
class GreetingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeProvider = FakeGreetingProvider()

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        GreetingViewModel(fakeProvider, savedStateHandle)

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
    fun `Refresh action emits a ShowMessage effect`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<GreetingUiEffect>()
        val collector = launch { viewModel.effects.toList(effects) }

        viewModel.onAction(GreetingUiAction.Refresh)
        advanceUntilIdle()

        assertEquals(1, effects.size)
        assertEquals("Refreshed", (effects.single() as GreetingUiEffect.ShowMessage).text)
        collector.cancel()
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
}

private class FakeGreetingProvider : GreetingProvider {
    override fun greetingFor(name: String): String = "Hello $name!"
}
