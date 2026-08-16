package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Explicit dispatcher ownership (INV: no hard-coded `Dispatchers.IO` scattered through the
 * codebase). Consumers take this instead of referencing `kotlinx.coroutines.Dispatchers`
 * directly, so tests can swap in a deterministic fake (THR-02).
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val mainImmediate: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}
