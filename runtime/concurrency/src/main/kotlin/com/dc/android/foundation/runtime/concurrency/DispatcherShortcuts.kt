package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/** Shorthand for `withContext(io) { ... }` at data-source call sites (THR-02). */
suspend inline fun <T> DispatcherProvider.onIo(crossinline block: suspend CoroutineScope.() -> T): T =
    withContext(io) { block() }

/** Shorthand for `withContext(default) { ... }` around CPU-bound mapping/sorting work. */
suspend inline fun <T> DispatcherProvider.onDefault(crossinline block: suspend CoroutineScope.() -> T): T =
    withContext(default) { block() }

/** Moves everything upstream of this point onto [DispatcherProvider.io] without a bare `flowOn(Dispatchers.IO)`. */
fun <T> Flow<T>.flowOnIo(dispatchers: DispatcherProvider): Flow<T> = flowOn(dispatchers.io)
