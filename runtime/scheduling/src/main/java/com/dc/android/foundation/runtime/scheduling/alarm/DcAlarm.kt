package com.dc.android.foundation.runtime.scheduling.alarm

import android.content.BroadcastReceiver
import com.dc.android.foundation.runtime.scheduling.DcTrigger
import java.time.Instant
import kotlin.reflect.KClass

data class DcAlarm(
    val id: String,
    val trigger: DcTrigger,
    val receiver: Class<out BroadcastReceiver>,
    val payloadJson: String? = null,
    val exactness: DcExactness = DcExactness.Exact,
    val missedPolicy: DcMissedPolicy = defaultMissedPolicy(trigger),
) {
    init {
        require(id.isNotBlank()) { "An alarm needs a non-blank id." }
        require(trigger.isAlarmTrigger()) { "An alarm requires ExactAt, DailyAt, or WeeklyAt." }
    }
}

enum class DcExactness { AlarmClock, Exact, Inexact }

/**
 * What to do with an `ExactAt` alarm whose instant passed while the device was off. It has no
 * effect on `DailyAt`/`WeeklyAt`: restore always re-arms the next future occurrence for those.
 */
enum class DcMissedPolicy { FireOnce, Skip }

internal fun DcTrigger.isAlarmTrigger(): Boolean = when (this) {
    is DcTrigger.ExactAt, is DcTrigger.DailyAt, is DcTrigger.WeeklyAt -> true
    is DcTrigger.After, is DcTrigger.Every -> false
}

internal fun defaultMissedPolicy(trigger: DcTrigger): DcMissedPolicy = when (trigger) {
    is DcTrigger.ExactAt -> DcMissedPolicy.FireOnce
    is DcTrigger.DailyAt, is DcTrigger.WeeklyAt -> DcMissedPolicy.Skip
    is DcTrigger.After, is DcTrigger.Every -> error("Work triggers cannot be alarms.")
}

internal fun DcAlarm.nextOccurrence(after: Instant): Instant = when (val scheduled = trigger) {
    is DcTrigger.ExactAt -> scheduled.instant
    is DcTrigger.DailyAt -> com.dc.android.foundation.runtime.scheduling.nextDaily(
        after = after,
        time = scheduled.time,
        zone = scheduled.zone,
    )
    is DcTrigger.WeeklyAt -> com.dc.android.foundation.runtime.scheduling.nextWeekly(
        after = after,
        time = scheduled.time,
        days = scheduled.days,
        zone = scheduled.zone,
    )
    is DcTrigger.After, is DcTrigger.Every -> error("Work triggers cannot be alarms.")
}

/** Pure outcome of re-arming one persisted alarm after reboot / time change (MP:SCH-03). */
internal sealed interface DcRestoreDecision {
    /** Arm the platform alarm for [at]. */
    data class Schedule(val at: Instant) : DcRestoreDecision

    /** The alarm can never fire again; remove it from the store. */
    data object Drop : DcRestoreDecision
}

internal fun DcAlarm.restoreDecision(now: Instant): DcRestoreDecision = when (val scheduled = trigger) {
    is DcTrigger.ExactAt -> when {
        scheduled.instant > now -> DcRestoreDecision.Schedule(scheduled.instant)
        missedPolicy == DcMissedPolicy.FireOnce -> DcRestoreDecision.Schedule(now)
        else -> DcRestoreDecision.Drop
    }
    is DcTrigger.DailyAt, is DcTrigger.WeeklyAt -> DcRestoreDecision.Schedule(nextOccurrence(now))
    is DcTrigger.After, is DcTrigger.Every -> error("Work triggers cannot be alarms.")
}
