package com.dc.android.foundation.runtime.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Generic, business-neutral WorkManager scheduling helper (BG-01, DEC-04). Centralizes the
 * periodic-work policy this repo wants for every recurring job — network-connected constraint,
 * `KEEP` dedupe so re-enqueuing on every process start doesn't reset the schedule — so callers
 * don't hand-roll `WorkManager` wiring per feature.
 *
 * Deliberately has no Hilt import: `:runtime:background` stays DI-framework-agnostic (INV-03),
 * same split as `:runtime:concurrency`/`:data:preferences`. The actual `@HiltWorker` classes live
 * in `:app`, the only module allowed to own Hilt-DI-annotated types (see
 * `14-background-work.md` BG-01 for the full placement rationale).
 */
object PeriodicSyncScheduler {

    /**
     * Enqueues [workerClass] as unique periodic work named [uniqueWorkName], repeating every
     * [repeatIntervalMinutes] (WorkManager's own minimum is 15 minutes — passing less is silently
     * clamped up by WorkManager itself, not by this helper). Requires network connectivity.
     */
    fun enqueue(
        context: Context,
        workerClass: Class<out ListenableWorker>,
        uniqueWorkName: String,
        repeatIntervalMinutes: Long
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequest.Builder(
            workerClass,
            repeatIntervalMinutes,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueWorkName,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
