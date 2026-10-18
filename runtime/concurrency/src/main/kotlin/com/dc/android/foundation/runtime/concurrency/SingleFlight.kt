package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import java.util.concurrent.ConcurrentHashMap

/**
 * Collapses concurrent calls sharing the same [key] into one in-flight execution — for work where
 * running it twice at once is wasteful or unsafe (token refresh, cache warm-up, a refresh
 * triggered from pull-to-refresh, `init`, and a background worker at the same time).
 *
 * [block] runs on [scope], not on any individual caller's coroutine: one caller cancelling its own
 * `run()` call does not cancel the shared execution or the other callers still awaiting it.
 */
class SingleFlight<K : Any, V>(private val scope: CoroutineScope) {

    private val inFlight = ConcurrentHashMap<K, Deferred<V>>()

    suspend fun run(key: K, block: suspend () -> V): V {
        val deferred = inFlight.computeIfAbsent(key) { registeredKey ->
            // LAZY prevents an immediate/Unconfined scope from completing before
            // computeIfAbsent has installed the Deferred in the map.
            scope.async(start = CoroutineStart.LAZY) { block() }.also { job ->
                job.invokeOnCompletion { inFlight.remove(registeredKey, job) }
            }
        }
        return deferred.await()
    }
}
