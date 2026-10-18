package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * Process-lifetime scope for work that must outlive a screen without using GlobalScope.
 *
 * The application composition root owns this scope's lifecycle and connects [onUncaughtException]
 * to its crash-reporting policy; this module deliberately has no DI or observability dependency.
 */
class AppCoroutineScope(
    dispatchers: DispatcherProvider,
    onUncaughtException: (Throwable) -> Unit
) : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        SupervisorJob() + dispatchers.default + CoroutineExceptionHandler { _, throwable ->
            onUncaughtException(throwable)
        }
}
