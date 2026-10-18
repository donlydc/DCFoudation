package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private object DefaultKey

/**
 * Named jobs scoped to one [CoroutineScope] (typically `viewModelScope`) — replaces hand-rolled
 * `Job?` bookkeeping at each call site for two recurring UDF bugs: a stale result from a
 * superseded search/filter request, and duplicate work from a double-tapped refresh button.
 */
class JobRegistry(private val scope: CoroutineScope) {

    private val lock = Any()
    private val jobs = mutableMapOf<Any, Job>()

    /** Cancels any job already running under [key], then launches [block] as the new one. */
    fun launchLatest(key: Any = DefaultKey, block: suspend CoroutineScope.() -> Unit): Job {
        val job = synchronized(lock) {
            jobs.remove(key)?.cancel()
            createRegisteredJob(key, block)
        }
        job.start()
        return job
    }

    /** Launches [block] only if no job is currently running under [key]; otherwise a no-op. */
    fun launchIfIdle(key: Any = DefaultKey, block: suspend CoroutineScope.() -> Unit): Job? {
        val job = synchronized(lock) {
            // A LAZY job is registered before it starts and is not active yet; presence, rather
            // than isActive, is therefore the atomic "busy" signal.
            if (jobs.containsKey(key)) return null
            createRegisteredJob(key, block)
        }
        job.start()
        return job
    }

    fun isRunning(key: Any = DefaultKey): Boolean = synchronized(lock) {
        jobs[key]?.isActive == true
    }

    fun cancel(key: Any = DefaultKey) {
        synchronized(lock) { jobs.remove(key) }?.cancel()
    }

    fun cancelAll() {
        val activeJobs = synchronized(lock) {
            jobs.values.toList().also { jobs.clear() }
        }
        activeJobs.forEach(Job::cancel)
    }

    /** Caller holds [lock]. LAZY start guarantees registration precedes even immediate completion. */
    private fun createRegisteredJob(
        key: Any,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = scope.launch(start = CoroutineStart.LAZY, block = block).also { job ->
        jobs[key] = job
        job.invokeOnCompletion {
            synchronized(lock) {
                if (jobs[key] === job) jobs.remove(key)
            }
        }
    }
}
