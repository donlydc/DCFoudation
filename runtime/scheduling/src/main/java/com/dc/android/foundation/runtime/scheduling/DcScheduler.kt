package com.dc.android.foundation.runtime.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import com.dc.android.foundation.runtime.concurrency.DefaultDispatcherProvider
import com.dc.android.foundation.runtime.concurrency.DispatcherProvider
import com.dc.android.foundation.runtime.scheduling.alarm.AndroidDcAlarmScheduler
import com.dc.android.foundation.runtime.scheduling.alarm.DcAlarm
import com.dc.android.foundation.runtime.scheduling.alarm.DcExactness
import com.dc.android.foundation.runtime.scheduling.alarm.DcMissedPolicy
import com.dc.android.foundation.runtime.scheduling.alarm.FileDcAlarmStore
import com.dc.android.foundation.runtime.scheduling.work.AndroidDcWorkScheduler
import com.dc.android.foundation.runtime.scheduling.work.DcJobSpec
import com.dc.android.foundation.runtime.scheduling.work.DcJobState
import com.dc.android.foundation.runtime.scheduling.work.DcWorkScheduler
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/** Describes intent; the facade selects AlarmManager or WorkManager from this type. */
sealed interface DcTrigger {
    data class ExactAt(val instant: Instant) : DcTrigger
    data class DailyAt(val time: LocalTime, val zone: ZoneId = ZoneId.systemDefault()) : DcTrigger
    data class WeeklyAt(
        val time: LocalTime,
        val days: Set<DayOfWeek>,
        val zone: ZoneId = ZoneId.systemDefault(),
    ) : DcTrigger {
        init {
            require(days.isNotEmpty()) { "A weekly schedule needs at least one day." }
        }
    }
    data class After(val delay: Duration) : DcTrigger {
        init { require(!delay.isNegative()) { "A work delay cannot be negative." } }
    }
    data class Every(val interval: Duration, val flex: Duration? = null) : DcTrigger
}

sealed interface DcScheduleRequest {
    data class Work(val spec: DcJobSpec<*>) : DcScheduleRequest

    data class Alarm(
        val receiver: Class<out BroadcastReceiver>,
        val payloadJson: String?,
        val exactness: DcExactness,
        val missedPolicy: DcMissedPolicy?,
    ) : DcScheduleRequest

    companion object {
        fun work(spec: DcJobSpec<*>): Work = Work(spec)

        fun <P : Any> alarm(
            receiver: KClass<out BroadcastReceiver>,
            payload: P? = null,
            serializer: KSerializer<P>? = null,
            exactness: DcExactness = DcExactness.Exact,
            missedPolicy: DcMissedPolicy? = null,
            json: Json = Json,
        ): Alarm {
            require(payload == null || serializer != null) {
                "A non-null alarm payload requires its KSerializer."
            }
            return Alarm(
                receiver = receiver.java,
                payloadJson = payload?.let { json.encodeToString(serializer!!, it) },
                exactness = exactness,
                missedPolicy = missedPolicy,
            )
        }
    }
}

sealed interface DcScheduleOutcome {
    data object Scheduled : DcScheduleOutcome
    data object NeedsExactAlarmPermission : DcScheduleOutcome
    data class Rejected(val reason: String) : DcScheduleOutcome
}

sealed interface DcScheduledItem {
    val id: String
    data class Alarm(val alarm: DcAlarm) : DcScheduledItem { override val id: String = alarm.id }
}

interface DcScheduler {
    suspend fun schedule(id: String, trigger: DcTrigger, request: DcScheduleRequest): DcScheduleOutcome
    suspend fun cancel(id: String)
    suspend fun scheduled(): List<DcScheduledItem>
    fun observe(id: String): Flow<DcJobState>
}

/** Android implementation; create it at the composition root and expose only [DcScheduler]. */
class AndroidDcScheduler(
    context: Context,
    dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
    private val workScheduler: DcWorkScheduler = AndroidDcWorkScheduler(context),
    private val alarmScheduler: AndroidDcAlarmScheduler = AndroidDcAlarmScheduler(
        context = context,
        store = FileDcAlarmStore(context, dispatchers),
    ),
) : DcScheduler {
    override suspend fun schedule(
        id: String,
        trigger: DcTrigger,
        request: DcScheduleRequest,
    ): DcScheduleOutcome = when (trigger) {
        is DcTrigger.ExactAt, is DcTrigger.DailyAt, is DcTrigger.WeeklyAt -> {
            val alarmRequest = request as? DcScheduleRequest.Alarm
                ?: return DcScheduleOutcome.Rejected("An exact or wall-clock trigger requires an alarm request.")
            alarmScheduler.schedule(
                DcAlarm(
                    id = id,
                    trigger = trigger,
                    receiver = alarmRequest.receiver,
                    payloadJson = alarmRequest.payloadJson,
                    exactness = alarmRequest.exactness,
                    missedPolicy = alarmRequest.missedPolicy ?: com.dc.android.foundation.runtime.scheduling.alarm.defaultMissedPolicy(trigger),
                ),
            )
        }
        is DcTrigger.After -> {
            val work = request as? DcScheduleRequest.Work
                ?: return DcScheduleOutcome.Rejected("An After trigger requires a work request.")
            runCatching {
                workScheduler.enqueueOnce(work.spec.copyInitialDelay(trigger.delay))
            }.fold(
                onSuccess = { DcScheduleOutcome.Scheduled },
                onFailure = { DcScheduleOutcome.Rejected(it.message ?: "Invalid one-time work request.") },
            )
        }
        is DcTrigger.Every -> {
            val work = request as? DcScheduleRequest.Work
                ?: return DcScheduleOutcome.Rejected("An Every trigger requires a work request.")
            runCatching {
                workScheduler.enqueuePeriodic(work.spec, trigger.interval, trigger.flex)
            }.fold(
                onSuccess = { DcScheduleOutcome.Scheduled },
                onFailure = { DcScheduleOutcome.Rejected(it.message ?: "Invalid periodic work request.") },
            )
        }
    }

    override suspend fun cancel(id: String) {
        workScheduler.cancel(id)
        alarmScheduler.cancel(id)
    }

    override suspend fun scheduled(): List<DcScheduledItem> = alarmScheduler.all().map(DcScheduledItem::Alarm)

    override fun observe(id: String): Flow<DcJobState> = workScheduler.observe(id)
}

@Suppress("UNCHECKED_CAST")
private fun DcJobSpec<*>.copyInitialDelay(delay: Duration): DcJobSpec<Any> = DcJobSpec(
    name = name,
    worker = worker,
    input = input as Any?,
    inputSerializer = inputSerializer as KSerializer<Any>?,
    constraints = constraints,
    retry = retry,
    initialDelay = delay,
    expedited = expedited,
    tags = tags,
)
