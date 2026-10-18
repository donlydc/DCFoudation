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
                if (spec.retry.exponential) BackoffPolicy.EXPONENTIAL else BackoffPolicy.LINEAR,
                spec.retry.initialBackoff.inWholeMilliseconds,
                TimeUnit.MILLISECONDS,
            )
            .apply {
                spec.tags.plus(spec.name).forEach(::addTag)
                if (spec.expedited) setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }
            .build()
        workManager.enqueueUniqueWork(
            spec.name,
            if (replaceExisting) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
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
                if (spec.retry.exponential) BackoffPolicy.EXPONENTIAL else BackoffPolicy.LINEAR,
                spec.retry.initialBackoff.inWholeMilliseconds,
                TimeUnit.MILLISECONDS,
            )
            .apply { spec.tags.plus(spec.name).forEach(::addTag) }
            .build()
        workManager.enqueueUniquePeriodicWork(
            spec.name,
            if (keepExisting) ExistingPeriodicWorkPolicy.KEEP else ExistingPeriodicWorkPolicy.UPDATE,
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
    .setRequiredNetworkType(
        when (network) {
            NetworkRequirement.None -> NetworkType.NOT_REQUIRED
            NetworkRequirement.Connected -> NetworkType.CONNECTED
            NetworkRequirement.Unmetered -> NetworkType.UNMETERED
        },
    )
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

private fun WorkInfo.toDcState(): DcJobState = when (state) {
    WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> DcJobState.Enqueued
    WorkInfo.State.RUNNING -> DcJobState.Running(progress.getFloat(PROGRESS_KEY, Float.NaN).takeUnless(Float::isNaN))
    WorkInfo.State.SUCCEEDED -> DcJobState.Succeeded
    WorkInfo.State.FAILED -> DcJobState.Failed(DcWorker.appErrorFrom(outputData))
    WorkInfo.State.CANCELLED -> DcJobState.Cancelled
}
