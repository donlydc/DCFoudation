package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** The one stop-timeout every ViewModel should share instead of retyping `WhileSubscribed(5000)`. */
val WhileUiSubscribed: SharingStarted = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds)

/** `stateIn` pinned to [WhileUiSubscribed] by default, so features stop retyping the sharing policy. */
fun <T> Flow<T>.stateInWhileSubscribed(
    scope: CoroutineScope,
    initialValue: T,
    stopTimeout: Duration = 5.seconds
): StateFlow<T> = stateIn(
    scope = scope,
    started = SharingStarted.WhileSubscribed(stopTimeout.inWholeMilliseconds),
    initialValue = initialValue
)

/**
 * Emits only the first value in each [window]; later values inside the same window are dropped.
 * Complements [kotlinx.coroutines.flow.debounce] (which keeps the *last* value) for inputs like a
 * submit button where the first tap should win and the rest should be ignored outright.
 *
 * Timed via [delay] (not a wall-clock [kotlin.time.TimeSource]) so the window respects whichever
 * dispatcher this flow runs on — including a `runTest` virtual-time scheduler in tests. The
 * returned flow completes as soon as the upstream completes (it does not wait out the last window).
 */
fun <T> Flow<T>.throttleFirst(window: Duration): Flow<T> = channelFlow {
    var windowOpen = true
    collect { value ->
        if (windowOpen) {
            windowOpen = false
            send(value)
            launch {
                delay(window)
                windowOpen = true
            }
        }
    }
    // Upstream is done: a pending window timer must not delay completion by up to [window].
    coroutineContext.cancelChildren()
}
