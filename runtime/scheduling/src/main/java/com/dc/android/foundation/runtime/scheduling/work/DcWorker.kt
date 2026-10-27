package com.dc.android.foundation.runtime.scheduling.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.dc.android.foundation.core.AppError
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

sealed interface DcJobResult {
    data object Success : DcJobResult
    data object Retry : DcJobResult
    data class Failure(val error: AppError) : DcJobResult
}

/**
 * WorkManager base class with typed input, bounded retries, and an error category that can be
 * surfaced through [DcJobState] without exposing framework exceptions to callers.
 */
abstract class DcWorker<I : Any>(
    context: Context,
    params: WorkerParameters,
    private val serializer: KSerializer<I>? = null,
    private val json: Json = Json,
) : CoroutineWorker(context, params) {

    abstract suspend fun run(input: I?): DcJobResult

    suspend fun reportProgress(fraction: Float) {
        setProgress(workDataOf(PROGRESS_KEY to fraction.coerceIn(0f, 1f)))
    }

    final override suspend fun doWork(): Result {
        val result = try {
            run(decodeInput())
        } catch (cause: CancellationException) {
            throw cause
        } catch (cause: SerializationException) {
            DcJobResult.Failure(AppError.Serialization(cause))
        } catch (cause: Throwable) {
            DcJobResult.Failure(AppError.Unknown(cause))
        }
        return result.toWorkResult(
            attempt = runAttemptCount + 1,
            maxAttempts = inputData.getInt(MAX_ATTEMPTS_KEY, DcRetryPolicy.Default.maxAttempts),
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun decodeInput(): I? {
        val raw = inputData.getString(INPUT_KEY) ?: return null
        val inputSerializer = serializer ?: return null
        return json.decodeFromString(inputSerializer as KSerializer<I>, raw)
    }

    private fun DcJobResult.toWorkResult(attempt: Int, maxAttempts: Int): Result =
        when (val verdict = toVerdict(attempt, maxAttempts)) {
            DcWorkVerdict.Success -> Result.success()
            DcWorkVerdict.Retry -> Result.retry()
            is DcWorkVerdict.Failed -> Result.failure(workDataOf(ERROR_KIND_KEY to verdict.kind))
        }

    internal companion object {
        const val INPUT_KEY = "dc_scheduling_input"
        const val MAX_ATTEMPTS_KEY = "dc_scheduling_max_attempts"
        const val PROGRESS_KEY = "dc_scheduling_progress"
        const val ERROR_KIND_KEY = "dc_scheduling_error_kind"

        fun appErrorFrom(data: Data): AppError? = errorFromKind(data.getString(ERROR_KIND_KEY))
    }
}

/**
 * Framework-free outcome of one worker run: what [DcWorker.doWork] will tell WorkManager. Kept
 * separate from `ListenableWorker.Result` so the retry-ceiling and error-kind policy is plain
 * JVM-testable logic (MP:SCH-01).
 */
internal sealed interface DcWorkVerdict {
    data object Success : DcWorkVerdict
    data object Retry : DcWorkVerdict
    data class Failed(val kind: String) : DcWorkVerdict
}

/** `attempt` is one-based (`runAttemptCount + 1`); a retry past [maxAttempts] becomes a terminal failure. */
internal fun DcJobResult.toVerdict(attempt: Int, maxAttempts: Int): DcWorkVerdict = when (this) {
    DcJobResult.Success -> DcWorkVerdict.Success
    DcJobResult.Retry ->
        if (DcRetryPolicy(maxAttempts = maxAttempts).canRetry(attempt)) DcWorkVerdict.Retry
        else DcWorkVerdict.Failed(KIND_RETRY_EXHAUSTED)
    is DcJobResult.Failure -> DcWorkVerdict.Failed(error.workKind())
}

internal const val KIND_RETRY_EXHAUSTED = "retry_exhausted"

/**
 * Inverse of [workKind]. Deliberately lossy: only the variant crosses WorkManager output data, so
 * `Http` restores with `code = 0` and no `cause`/`serverMessage` (never persist server text).
 */
internal fun errorFromKind(kind: String?): AppError? = when (kind) {
    "connectivity" -> AppError.Connectivity()
    "http" -> AppError.Http(code = 0)
    "serialization" -> AppError.Serialization()
    "storage" -> AppError.Storage()
    "unknown", KIND_RETRY_EXHAUSTED -> AppError.Unknown()
    else -> null
}

/** Exhaustive over [AppError]: a new variant must be given a kind here and in [errorFromKind]. */
internal fun AppError.workKind(): String = when (this) {
    is AppError.Connectivity -> "connectivity"
    is AppError.Http -> "http"
    is AppError.Serialization -> "serialization"
    is AppError.Storage -> "storage"
    is AppError.Unknown -> "unknown"
}
