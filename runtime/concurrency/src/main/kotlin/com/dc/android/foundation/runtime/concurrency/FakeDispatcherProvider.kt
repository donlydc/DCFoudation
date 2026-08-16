package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.CoroutineDispatcher

/** All four dispatchers point at [dispatcher] — for deterministic tests (17-testing.md). */
class FakeDispatcherProvider(dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val main: CoroutineDispatcher = dispatcher
    override val mainImmediate: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
}
