package com.dc.android.foundation.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Replaces [Dispatchers.Main] with a deterministic test dispatcher for a JUnit test.
 *
 * Use this for ViewModels that launch work in `viewModelScope`. The rule deliberately does not
 * own a test scope; the test remains responsible for using `runTest` and advancing virtual time.
 */
@OptIn(ExperimentalCoroutinesApi::class)
public class MainDispatcherRule(
    public val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
