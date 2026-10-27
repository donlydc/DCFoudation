package com.dc.android.foundation.runtime.scheduling.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.dc.android.foundation.core.AppError
import com.dc.android.foundation.runtime.scheduling.work.DcWorker.Companion.ERROR_KIND_KEY
import com.dc.android.foundation.runtime.scheduling.work.DcWorker.Companion.INPUT_KEY
import com.dc.android.foundation.runtime.scheduling.work.DcWorker.Companion.MAX_ATTEMPTS_KEY
import com.dc.android.foundation.runtime.scheduling.work.DcWorker.Companion.PROGRESS_KEY
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

interface DcWorkScheduler {
    fun enqueueOnce(spec: DcJobSpec<*>, replaceExisting: Boolean = false)
    fun enqueuePeriodic(
        spec: DcJobSpec<*>,
        every: Duration,
        flex: Duration? = null,
        keepExisting: Boolean = true,
    )

    fun cancel(name: String)
    fun cancelByTag(tag: String)
    fun observe(name: String): Flow<DcJobState>
}

class AndroidDcWorkScheduler(
    context: Context,
    private val json: Json = Json,
) : DcWorkScheduler {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    override fun enqueueOnce(spec: DcJobSpec<*>, replaceExisting: Boolean) {
        val request = OneTimeWorkRequest.Builder(spec.worker.java)
            .setConstraints(spec.constraints.toAndroid())
            .setInputData(spec.inputData(json))
            .setInitialDelay(spec.initialDelay.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(
                spec.retry.backoffPolicy(),
                spec.retry.initialBackoff.inWholeMilliseconds,
                TimeUnit.MILLISECONDS,
            )
            .apply {
                spec.allTags().forEach(::addTag)
                if (spec.expedited) setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }
            .build()
        workManager.enqueueUniqueWork(
            spec.name,
            existingWorkPolicy(replaceExisting),
            request,
        )
    }

    override fun enqueuePeriodic(
        spec: DcJobSpec<*>,
        every: Duration,
        flex: Duration?,
        keepExisting: Boolean,
    ) {
        validatePeriodicInterval(every, flex)
        val builder = if (flex == null) {
            PeriodicWorkRequest.Builder(spec.worker.java, every.inWholeMinutes, TimeUnit.MINUTES)
        } else {
            PeriodicWorkRequest.Builder(
                spec.worker.java,
                every.inWholeMinutes,
                TimeUnit.MINUTES,
                flex.inWholeMinutes,
                TimeUnit.MINUTES,
            )
        }
        val request = builder
            .setConstraints(spec.constraints.toAndroid())
            .setInputData(spec.inputData(json))
            .setBackoffCriteria(
                spec.retry.backoffPolicy(),
                spec.retry.initialBackoff.inWholeMilliseconds,
                TimeUnit.MILLISECONDS,
            )
            .apply { spec.allTags().forEach(::addTag) }
            .build()
        workManager.enqueueUniquePeriodicWork(
            spec.name,
            existingPeriodicWorkPolicy(keepExisting),
            request,
        )
    }

    override fun cancel(name: String) {
        workManager.cancelUniqueWork(name)
    }

    override fun cancelByTag(tag: String) {
        workManager.cancelAllWorkByTag(tag)
    }

    override fun observe(name: String): Flow<DcJobState> =
        workManager.getWorkInfosForUniqueWorkFlow(name).map { infos ->
            infos.maxByOrNull { info -> info.runAttemptCount }?.toDcState() ?: DcJobState.Enqueued
        }
}

/** Fails at declaration time instead of letting WorkManager silently clamp short periods. */
fun validatePeriodicInterval(every: Duration, flex: Duration? = null) {
    require(every >= 15.minutes) { "Periodic work must repeat at least every 15 minutes." }
    if (flex != null) {
        require(flex >= 5.minutes) { "Periodic work flex must be at least 5 minutes." }
        require(flex < every) { "Periodic work flex must be shorter than its interval." }
    }
}

private fun DcConstraints.toAndroid(): Constraints = Constraints.Builder()
    .setRequiredNetworkType(network.toNetworkType())
    .setRequiresCharging(requiresCharging)
    .setRequiresBatteryNotLow(requiresBatteryNotLow)
    .setRequiresStorageNotLow(requiresStorageNotLow)
    .setRequiresDeviceIdle(requiresDeviceIdle)
    .build()

@Suppress("UNCHECKED_CAST")
private fun DcJobSpec<*>.inputData(json: Json) = workDataOf(
    MAX_ATTEMPTS_KEY to retry.maxAttempts,
    INPUT_KEY to input?.let { value ->
        json.encodeToString(inputSerializer as KSerializer<Any>, value)
    },
)

private fun WorkInfo.toDcState(): DcJobState = dcJobState(
    state = state,
    progress = progress.getFloat(PROGRESS_KEY, Float.NaN).takeUnless(Float::isNaN),
    failure = { DcWorker.appErrorFrom(outputData) },
)

/** Pure WorkInfo-state mapping; [failure] is only evaluated for `FAILED`. */
internal fun dcJobState(
    state: WorkInfo.State,
    progress: Float?,
    failure: () -> AppError?,
): DcJobState = when (state) {
    WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> DcJobState.Enqueued
    WorkInfo.State.RUNNING -> DcJobState.Running(progress)
    WorkInfo.State.SUCCEEDED -> DcJobState.Succeeded
    WorkInfo.State.FAILED -> DcJobState.Failed(failure())
    WorkInfo.State.CANCELLED -> DcJobState.Cancelled
}

/**
 * Idempotency policy for one-time work (MP:SCH-01): KEEP means a second enqueue of the same unique
 * name is a no-op while the first is unfinished; only an explicit replace cancels and restarts it.
 */
internal fun existingWorkPolicy(replaceExisting: Boolean): ExistingWorkPolicy =
    if (replaceExisting) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP

/** KEEP preserves the already-registered periodic request (its old interval/constraints stay in force). */
internal fun existingPeriodicWorkPolicy(keepExisting: Boolean): ExistingPeriodicWorkPolicy =
    if (keepExisting) ExistingPeriodicWorkPolicy.KEEP else ExistingPeriodicWorkPolicy.UPDATE

internal fun DcRetryPolicy.backoffPolicy(): BackoffPolicy =
    if (exponential) BackoffPolicy.EXPONENTIAL else BackoffPolicy.LINEAR

internal fun NetworkRequirement.toNetworkType(): NetworkType = when (this) {
    NetworkRequirement.None -> NetworkType.NOT_REQUIRED
    NetworkRequirement.Connected -> NetworkType.CONNECTED
    NetworkRequirement.Unmetered -> NetworkType.UNMETERED
}

/** The caller's tags plus the unique name, so `cancelByTag(name)` also reaches the job. */
internal fun DcJobSpec<*>.allTags(): Set<String> = tags + name
