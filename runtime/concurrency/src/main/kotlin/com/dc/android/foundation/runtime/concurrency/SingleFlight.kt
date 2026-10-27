package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import java.util.concurrent.ConcurrentHashMap

/**
 * Collapses concurrent calls sharing the same [key] into one in-flight execution — for work where
 * running it twice at once is wasteful or unsafe (token refresh, cache warm-up, a refresh
 * triggered from pull-to-refresh, `init`, and a background worker at the same time).
 *
 * Semantics (MP:CON-01):
 * - [block] runs on [scope], not on any individual caller's coroutine: one caller cancelling its own
 *   `run()` call does not cancel the shared execution or the other callers still awaiting it. If
 *   *every* caller cancels, the execution still runs to completion (it is owned by [scope]).
 * - The key is released as soon as the execution completes (success, failure or cancellation).
 *   Results are never cached: the next call after completion executes [block] again.
 * - A failure of [block] is delivered to every caller that joined that execution, and does **not**
 *   cancel [scope]: each execution runs under its own `SupervisorJob` (child of [scope]'s job) that
 *   is completed with the execution, so a `SingleFlight` never keeps a joined scope from finishing.
 * - Cancelling [scope] cancels every in-flight execution; callers (and later calls) then receive a
 *   `CancellationException`.
 * - A call arriving in the instant between completion and key release may still join the finished
 *   execution and receive its result.
 * - The first caller's [block] wins; a joining caller's [block] is never invoked. Use one value
 *   type per key.
 */
class SingleFlight<K : Any, V>(private val scope: CoroutineScope) {

    private val inFlight = ConcurrentHashMap<K, Deferred<V>>()

    suspend fun run(key: K, block: suspend () -> V): V {
        inFlight[key]?.let { return it.await() }

        // Per-execution supervisor: a failing block reaches its awaiters only and never cancels
        // [scope], and the supervisor is completed together with the execution so it can never
        // keep a joined parent scope (runBlocking / coroutineScope / runTest) from finishing.
        val supervisor = SupervisorJob(scope.coroutineContext[Job])

        // LAZY keeps the execution from starting (and, with an immediate/Unconfined scope, from
        // completing) before the Deferred is installed in the map. The completion handler is
        // registered outside any ConcurrentHashMap callback: on an already-cancelled scope the
        // handler fires immediately, and removing from inside computeIfAbsent throws
        // IllegalStateException("Recursive update").
        val candidate = CoroutineScope(scope.coroutineContext + supervisor)
            .async(start = CoroutineStart.LAZY) { block() }
        val existing = inFlight.putIfAbsent(key, candidate)
        if (existing != null) {
            candidate.cancel()
            supervisor.complete()
            return existing.await()
        }
        candidate.invokeOnCompletion {
            supervisor.complete()
            inFlight.remove(key, candidate)
        }
        return candidate.await()
    }
}
