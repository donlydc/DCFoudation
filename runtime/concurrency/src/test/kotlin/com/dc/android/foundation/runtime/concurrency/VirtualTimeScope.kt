package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import java.io.Closeable

/**
 * Test-owned scope for the primitive under test, driven by the same virtual-time scheduler as the
 * enclosing `runTest`.
 *
 * Unlike `backgroundScope`, work launched here is *foreground* for the scheduler, so
 * `advanceUntilIdle()` runs it. The scope is deliberately not a child of the test scope; always
 * wrap use in [use] so any still-running job is cancelled (no orphan jobs leak between tests).
 * Uncaught failures are collected in [failures] instead of failing the run unpredictably.
 */
internal class VirtualTimeScope(
    testScope: TestScope,
    job: Job = SupervisorJob(),
) : Closeable {
    val failures = mutableListOf<Throwable>()
    val scope: CoroutineScope = CoroutineScope(
        job + StandardTestDispatcher(testScope.testScheduler) +
            CoroutineExceptionHandler { _, throwable -> failures += throwable },
    )

    override fun close() {
        scope.cancel()
    }
}
