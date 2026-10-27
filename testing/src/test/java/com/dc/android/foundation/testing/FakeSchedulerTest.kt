package com.dc.android.foundation.testing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dc.android.foundation.runtime.scheduling.DcScheduleOutcome
import com.dc.android.foundation.runtime.scheduling.DcScheduleRequest
import com.dc.android.foundation.runtime.scheduling.DcTrigger
import com.dc.android.foundation.runtime.scheduling.work.DcJobState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class FakeSchedulerTest {

    /** Only its class name is used; it is never instantiated. */
    private class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = Unit
    }

    private val trigger = DcTrigger.ExactAt(Instant.parse("2026-05-01T10:00:00Z"))
    private val request = DcScheduleRequest.alarm<String>(receiver = Receiver::class)

    @Test(timeout = 20_000)
    fun `records schedule and cancel commands and returns the configured outcome`() = runTest {
        val scheduler = FakeScheduler()

        assertEquals(DcScheduleOutcome.Scheduled, scheduler.schedule("a", trigger, request))
        scheduler.nextOutcome = DcScheduleOutcome.NeedsExactAlarmPermission
        assertEquals(DcScheduleOutcome.NeedsExactAlarmPermission, scheduler.schedule("b", trigger, request))
        scheduler.cancel("a")

        assertEquals(listOf("a", "b"), scheduler.scheduleCommands.map { it.id })
        assertEquals(trigger, scheduler.scheduleCommands.first().trigger)
        assertEquals(listOf("a"), scheduler.cancelledIds)
    }

    @Test(timeout = 20_000)
    fun `observe starts enqueued and replays the latest emitted state per id`() = runTest {
        val scheduler = FakeScheduler()

        assertEquals(DcJobState.Enqueued, scheduler.observe("job").first())

        scheduler.emitState("job", DcJobState.Running(0.5f))

        assertEquals(DcJobState.Running(0.5f), scheduler.observe("job").first())
        assertEquals(DcJobState.Enqueued, scheduler.observe("other").first())
    }
}
