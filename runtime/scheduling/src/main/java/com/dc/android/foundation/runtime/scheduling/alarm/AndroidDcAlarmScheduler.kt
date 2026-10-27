package com.dc.android.foundation.runtime.scheduling.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.dc.android.foundation.runtime.scheduling.DcScheduleOutcome
import java.time.Clock
import java.time.Instant
import java.util.zip.CRC32
import kotlinx.serialization.json.Json

/** AlarmManager policy plus durable state; no Hilt or application business dependency. */
class AndroidDcAlarmScheduler(
    private val context: Context,
    private val store: DcAlarmStore,
    private val clock: Clock = Clock.systemUTC(),
    private val json: Json = Json,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    suspend fun schedule(alarm: DcAlarm): DcScheduleOutcome {
        if (alarm.exactness != DcExactness.Inexact && !DcExactAlarmPermission.canScheduleExact(context)) {
            return DcScheduleOutcome.NeedsExactAlarmPermission
        }
        val occurrence = alarm.nextOccurrence(clock.instant())
        return runCatching {
            store.put(alarm)
            try {
                scheduleAt(alarm, occurrence)
            } catch (cause: Throwable) {
                runCatching { cancelPlatform(alarm) }
                store.remove(alarm.id)
                throw cause
            }
            DcScheduleOutcome.Scheduled
        }.getOrElse { cause ->
            DcScheduleOutcome.Rejected(cause.message ?: "Unable to schedule alarm.")
        }
    }

    suspend fun cancel(id: String) {
        store.all().firstOrNull { it.id == id }?.let(::cancelPlatform)
        store.remove(id)
    }

    suspend fun all(): List<DcAlarm> = store.all()

    suspend fun restore() {
        val now = clock.instant()
        store.all().forEach { alarm ->
            if (alarm.exactness != DcExactness.Inexact && !DcExactAlarmPermission.canScheduleExact(context)) return@forEach
            when (val decision = alarm.restoreDecision(now)) {
                is DcRestoreDecision.Schedule -> scheduleAt(alarm, decision.at)
                DcRestoreDecision.Drop -> store.remove(alarm.id)
            }
        }
    }

    internal suspend fun markDelivered(alarm: DcAlarm) {
        if (!alarm.trigger.isRepeating()) store.remove(alarm.id)
    }

    internal fun scheduleNextFromReceiver(alarm: DcAlarm) {
        if (!alarm.trigger.isRepeating()) return
        if (alarm.exactness != DcExactness.Inexact && !DcExactAlarmPermission.canScheduleExact(context)) return
        scheduleAt(alarm, alarm.nextOccurrence(clock.instant()))
    }

    internal fun cancelPlatform(alarm: DcAlarm) {
        alarmManager.cancel(pendingIntent(alarm))
    }

    @SuppressLint("MissingPermission") // Every exact path checks canScheduleExactAlarms first.
    private fun scheduleAt(alarm: DcAlarm, occurrence: Instant) {
        val operation = pendingIntent(alarm)
        val atMillis = occurrence.toEpochMilli()
        when (alarm.exactness) {
            DcExactness.AlarmClock -> alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(atMillis, null),
                operation,
            )
            DcExactness.Exact -> alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                atMillis,
                operation,
            )
            DcExactness.Inexact -> alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                atMillis,
                operation,
            )
        }
    }

    internal fun pendingIntent(alarm: DcAlarm): PendingIntent = PendingIntent.getBroadcast(
        context,
        requestCodeFor(alarm.id),
        Intent(context, alarm.receiver)
            .setAction(ACTION_ALARM)
            // Intent data is part of PendingIntent identity, preventing a rare request-code hash
            // collision from allowing one alarm to overwrite another.
            .setData(Uri.parse("dc-scheduling://alarm/${Uri.encode(alarm.id)}"))
            .putExtra(EXTRA_ALARM, DcAlarmCodec.encode(json, alarm)),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private companion object {
        const val ACTION_ALARM = "com.dc.android.foundation.runtime.scheduling.ALARM"
        const val EXTRA_ALARM = "dc_scheduling_alarm"
    }
}

internal fun requestCodeFor(id: String): Int {
    val checksum = CRC32().apply { update(id.encodeToByteArray()) }.value
    return (checksum xor (checksum ushr 32)).toInt()
}

private fun com.dc.android.foundation.runtime.scheduling.DcTrigger.isRepeating(): Boolean = when (this) {
    is com.dc.android.foundation.runtime.scheduling.DcTrigger.DailyAt,
    is com.dc.android.foundation.runtime.scheduling.DcTrigger.WeeklyAt -> true
    is com.dc.android.foundation.runtime.scheduling.DcTrigger.ExactAt,
    is com.dc.android.foundation.runtime.scheduling.DcTrigger.After,
    is com.dc.android.foundation.runtime.scheduling.DcTrigger.Every -> false
}
