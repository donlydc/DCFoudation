package com.dc.android.foundation.runtime.scheduling.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dc.android.foundation.runtime.concurrency.DefaultDispatcherProvider
import com.dc.android.foundation.runtime.scheduling.DcScheduleRequest
import com.dc.android.foundation.runtime.scheduling.work.AndroidDcWorkScheduler
import com.dc.android.foundation.runtime.scheduling.work.DcJobSpec
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

sealed interface DcAlarmOutcome {
    data object Handled : DcAlarmOutcome
    /** Long-running work is handed to WorkManager instead of remaining in the broadcast callback. */
    data class HandOff(val spec: DcJobSpec<*>) : DcAlarmOutcome
}

/**
 * Base receiver for app-specific alarm delivery. It schedules the following recurring occurrence
 * before invoking app code and always calls [PendingResult.finish] within the receiver budget.
 */
abstract class DcAlarmReceiver<P : Any>(
    private val serializer: KSerializer<P>? = null,
    private val json: Json = Json,
) : BroadcastReceiver() {
    abstract suspend fun onAlarm(context: Context, payload: P?): DcAlarmOutcome

    final override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        receiverScope.launch {
            try {
                val alarm = intent.getStringExtra(EXTRA_ALARM)?.let { DcAlarmCodec.decode(json, it) }
                alarm?.let { scheduledAlarm ->
                    val scheduler = defaultAlarmScheduler(appContext)
                    runCatching { scheduler.scheduleNextFromReceiver(scheduledAlarm) }
                    scheduler.markDelivered(scheduledAlarm)
                }
                when (val outcome = withTimeoutOrNull(RECEIVER_BUDGET) {
                    onAlarm(appContext, alarm?.payloadJson?.let(::decodePayload))
                }) {
                    is DcAlarmOutcome.HandOff -> AndroidDcWorkScheduler(appContext).enqueueOnce(outcome.spec)
                    DcAlarmOutcome.Handled, null -> Unit
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun decodePayload(raw: String): P? = serializer?.let { json.decodeFromString(it, raw) }

    private companion object {
        val receiverScope = CoroutineScope(SupervisorJob() + DefaultDispatcherProvider().default)
        val RECEIVER_BUDGET = 9.seconds
        const val EXTRA_ALARM = "dc_scheduling_alarm"
    }
}

internal fun defaultAlarmScheduler(context: Context): AndroidDcAlarmScheduler = AndroidDcAlarmScheduler(
    context = context,
    store = FileDcAlarmStore(context, DefaultDispatcherProvider()),
)
