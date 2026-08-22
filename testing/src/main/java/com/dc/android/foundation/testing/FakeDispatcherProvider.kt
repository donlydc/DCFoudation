package com.dc.android.foundation.testing

import com.dc.android.foundation.runtime.concurrency.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Test dispatcher provider whose dispatcher slots all share one scheduler-controlled dispatcher.
 *
 * Pass the dispatcher installed by [MainDispatcherRule] (usually `Dispatchers.Main`) so
 * `runTest` and `advanceUntilIdle` control every dispatcher hop made by the subject under test.
 */
public class FakeDispatcherProvider(
    dispatcher: CoroutineDispatcher
) : DispatcherProvider {
    override val main: CoroutineDispatcher = dispatcher
    override val mainImmediate: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
}
