package com.dc.android.foundation.runtime.scheduling.work

import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.WorkInfo
import com.dc.android.foundation.core.AppError
import com.dc.android.foundation.core.isRetryable
import com.dc.android.foundation.runtime.concurrency.RetryPolicy
import com.dc.android.foundation.runtime.concurrency.retrying
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.serializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/** MP:SCH-01 / MP:SCH-06: pure mapping and policy contracts of the WorkManager path. */
class DcWorkContractTest {
    private fun spec(
        name: String = "job",
        tags: Set<String> = emptySet(),
    ) = DcJobSpec<Unit>(name = name, worker = ListenableWorker::class, tags = tags)

    private val everyVariant: List<AppError> = listOf(
        AppError.Connectivity(),
        AppError.Http(code = 503, serverMessage = "secret server text"),
        AppError.Serialization(),
        AppError.Storage(),
        AppError.Unknown(),
    )

    // ---- DcJobSpec ---------------------------------------------------------------------------

    @Test(timeout = 20_000)
    fun `job spec defaults declare an unconstrained non-expedited job with the default retry`() {
        val job = spec()

        assertEquals(DcConstraints.None, job.constraints)
        assertEquals(DcRetryPolicy.Default, job.retry)
        assertEquals(kotlin.time.Duration.ZERO, job.initialDelay)
        assertFalse(job.expedited)
        assertTrue(job.tags.isEmpty())
        assertNull(job.input)
    }

    @Test(timeout = 20_000)
    fun `job spec rejects a blank name`() {
        assertThrows(IllegalArgumentException::class.java) { spec(name = "") }
        assertThrows(IllegalArgumentException::class.java) { spec(name = "   ") }
    }

    @Test(timeout = 20_000)
    fun `job spec rejects an input without a serializer and accepts one with it`() {
        assertThrows(IllegalArgumentException::class.java) {
            DcJobSpec(name = "job", worker = ListenableWorker::class, input = "payload")
        }
        val withSerializer = DcJobSpec(
            name = "job",
            worker = ListenableWorker::class,
            input = "payload",
            inputSerializer = String.serializer(),
        )
        assertEquals("payload", withSerializer.input)
    }

    @Test(timeout = 20_000)
    fun `every job is tagged with its own unique name so cancelByTag can reach it`() {
        assertEquals(setOf("job"), spec().allTags())
        assertEquals(setOf("sync", "job"), spec(tags = setOf("sync")).allTags())
        assertEquals(setOf("job"), spec(tags = setOf("job")).allTags())
    }

    // ---- Constraints and enqueue policy mapping -----------------------------------------------

    @Test(timeout = 20_000)
    fun `constraint presets express the documented requirements`() {
        assertEquals(DcConstraints(), DcConstraints.None)
        assertEquals(NetworkRequirement.None, DcConstraints.None.network)
        assertEquals(NetworkRequirement.Connected, DcConstraints.Network.network)
        assertFalse(DcConstraints.Network.requiresCharging)
        assertEquals(NetworkRequirement.Unmetered, DcConstraints.Sync.network)
        assertTrue(DcConstraints.Sync.requiresCharging)
        assertFalse(DcConstraints.Sync.requiresBatteryNotLow)
        assertFalse(DcConstraints.Sync.requiresStorageNotLow)
        assertFalse(DcConstraints.Sync.requiresDeviceIdle)
    }

    @Test(timeout = 20_000)
    fun `every network requirement maps to a distinct WorkManager network type`() {
        assertEquals(NetworkType.NOT_REQUIRED, NetworkRequirement.None.toNetworkType())
        assertEquals(NetworkType.CONNECTED, NetworkRequirement.Connected.toNetworkType())
        assertEquals(NetworkType.UNMETERED, NetworkRequirement.Unmetered.toNetworkType())
        assertEquals(
            NetworkRequirement.entries.size,
            NetworkRequirement.entries.map { it.toNetworkType() }.toSet().size,
        )
    }

    @Test(timeout = 20_000)
    fun `enqueue is idempotent by default and replaces only on request`() {
        assertEquals(ExistingWorkPolicy.KEEP, existingWorkPolicy(replaceExisting = false))
        assertEquals(ExistingWorkPolicy.REPLACE, existingWorkPolicy(replaceExisting = true))
        assertEquals(ExistingPeriodicWorkPolicy.KEEP, existingPeriodicWorkPolicy(keepExisting = true))
        assertEquals(ExistingPeriodicWorkPolicy.UPDATE, existingPeriodicWorkPolicy(keepExisting = false))
    }

    @Test(timeout = 20_000)
    fun `retry policy selects the WorkManager backoff curve`() {
        assertEquals(BackoffPolicy.EXPONENTIAL, DcRetryPolicy(exponential = true).backoffPolicy())
        assertEquals(BackoffPolicy.LINEAR, DcRetryPolicy(exponential = false).backoffPolicy())
        assertEquals(BackoffPolicy.EXPONENTIAL, DcRetryPolicy.Default.backoffPolicy())
    }

    @Test(timeout = 20_000)
    fun `periodic interval and flex accept their exact minimums`() {
        validatePeriodicInterval(15.minutes)
        validatePeriodicInterval(15.minutes, 5.minutes)
        validatePeriodicInterval(60.minutes, 59.minutes)
        assertThrows(IllegalArgumentException::class.java) { validatePeriodicInterval(60.minutes, 60.minutes) }
        assertThrows(IllegalArgumentException::class.java) { validatePeriodicInterval(60.minutes, 4.minutes) }
    }

    // ---- WorkInfo.State -> DcJobState ------------------------------------------------------------

    @Test(timeout = 20_000)
    fun `every WorkInfo state maps to a job state and the failure is read only when failed`() {
        val failure = AppError.Storage()
        WorkInfo.State.entries.forEach { state ->
            var failureReads = 0
            val mapped = dcJobState(state, progress = 0.5f) { failureReads += 1; failure }

            val expected = when (state) {
                WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> DcJobState.Enqueued
                WorkInfo.State.RUNNING -> DcJobState.Running(0.5f)
                WorkInfo.State.SUCCEEDED -> DcJobState.Succeeded
                WorkInfo.State.FAILED -> DcJobState.Failed(failure)
                WorkInfo.State.CANCELLED -> DcJobState.Cancelled
            }
            assertEquals("state $state", expected, mapped)
            assertEquals("state $state", if (state == WorkInfo.State.FAILED) 1 else 0, failureReads)
        }
    }

    @Test(timeout = 20_000)
    fun `running without reported progress and failed without a known error stay representable`() {
        assertEquals(DcJobState.Running(null), dcJobState(WorkInfo.State.RUNNING, progress = null) { null })
        assertEquals(DcJobState.Failed(null), dcJobState(WorkInfo.State.FAILED, progress = null) { null })
    }

    // ---- DcWorker verdict and error kind (CORE-01 on the Worker path) ------------------------------

    @Test(timeout = 20_000)
    fun `success and failure results map directly`() {
        assertEquals(DcWorkVerdict.Success, DcJobResult.Success.toVerdict(attempt = 1, maxAttempts = 3))
        everyVariant.forEach { error ->
            assertEquals(
                DcWorkVerdict.Failed(error.workKind()),
                DcJobResult.Failure(error).toVerdict(attempt = 1, maxAttempts = 3),
            )
        }
    }

    @Test(timeout = 20_000)
    fun `retry is allowed below the ceiling and becomes a terminal failure at it`() {
        assertEquals(DcWorkVerdict.Retry, DcJobResult.Retry.toVerdict(attempt = 1, maxAttempts = 3))
        assertEquals(DcWorkVerdict.Retry, DcJobResult.Retry.toVerdict(attempt = 2, maxAttempts = 3))
        assertEquals(
            DcWorkVerdict.Failed(KIND_RETRY_EXHAUSTED),
            DcJobResult.Retry.toVerdict(attempt = 3, maxAttempts = 3),
        )
        assertEquals(
            DcWorkVerdict.Failed(KIND_RETRY_EXHAUSTED),
            DcJobResult.Retry.toVerdict(attempt = 1, maxAttempts = 1),
        )
    }

    @Test(timeout = 20_000)
    fun `every AppError variant has a distinct kind that restores the same variant`() {
        val kinds = everyVariant.map { it.workKind() }
        assertEquals(everyVariant.size, kinds.toSet().size)

        everyVariant.forEach { error ->
            val restored = errorFromKind(error.workKind())
            assertNotNull(restored)
            assertEquals(error::class, restored!!::class)
        }
    }

    @Test(timeout = 20_000)
    fun `restoring an error from its kind is lossy by design`() {
        val restored = errorFromKind(AppError.Http(code = 503, serverMessage = "secret server text").workKind())

        assertEquals(AppError.Http(code = 0), restored)
        assertNull((restored as AppError.Http).serverMessage)
        assertNull(restored.cause)
        assertFalse("a restored Http error is not retryable (code 0)", restored.isRetryable)
    }

    @Test(timeout = 20_000)
    fun `retry exhaustion restores as an unknown error and unrecognised kinds restore as null`() {
        assertEquals(AppError.Unknown(), errorFromKind(KIND_RETRY_EXHAUSTED))
        assertEquals(AppError.Unknown(), errorFromKind("unknown"))
        assertNull(errorFromKind(null))
        assertNull(errorFromKind(""))
        assertNull(errorFromKind("Connectivity"))
        assertNull(errorFromKind("from-a-future-version"))
    }

    // ---- DcRetryPolicy (persisted, OS-driven) vs RetryPolicy (in-process) ---------------------------

    @Test(timeout = 20_000)
    fun `retry policy validation matches WorkManager limits`() {
        assertThrows(IllegalArgumentException::class.java) { DcRetryPolicy(maxAttempts = 0) }
        assertThrows(IllegalArgumentException::class.java) { DcRetryPolicy(initialBackoff = 9_999.milliseconds) }
        assertEquals(10.seconds, DcRetryPolicy(initialBackoff = 10.seconds).initialBackoff)
    }

    @Test(timeout = 20_000)
    fun `NoRetry never retries and Default allows four retries`() {
        assertFalse(DcRetryPolicy.NoRetry.canRetry(attempt = 1))
        assertEquals(5, DcRetryPolicy.Default.maxAttempts)
        assertEquals(30.seconds, DcRetryPolicy.Default.initialBackoff)
        assertTrue(DcRetryPolicy.Default.exponential)
        assertTrue(DcRetryPolicy.Default.canRetry(attempt = 4))
        assertFalse(DcRetryPolicy.Default.canRetry(attempt = 5))
    }

    @Test(timeout = 20_000)
    fun `both retry policies count maxAttempts as total executions`() = runTest {
        for (maxAttempts in 1..6) {
            var inProcessExecutions = 0
            val inProcess = RetryPolicy(
                maxAttempts = maxAttempts,
                initialDelay = 0.milliseconds,
                jitter = 0.0,
                retryIf = { true },
            )
            runCatching { retrying(inProcess) { inProcessExecutions += 1; error("fail") } }

            val persisted = DcRetryPolicy(maxAttempts = maxAttempts)
            var persistedExecutions = 1
            var attempt = 1
            while (persisted.canRetry(attempt)) {
                persistedExecutions += 1
                attempt += 1
            }

            assertEquals("maxAttempts=$maxAttempts", maxAttempts, inProcessExecutions)
            assertEquals("maxAttempts=$maxAttempts", inProcessExecutions, persistedExecutions)
        }
    }
}
