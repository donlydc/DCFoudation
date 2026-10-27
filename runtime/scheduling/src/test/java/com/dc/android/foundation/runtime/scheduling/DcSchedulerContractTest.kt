package com.dc.android.foundation.runtime.scheduling

import androidx.work.ListenableWorker
import com.dc.android.foundation.runtime.scheduling.alarm.DcAlarm
import com.dc.android.foundation.runtime.scheduling.alarm.DcExactness
import com.dc.android.foundation.runtime.scheduling.alarm.FirstTestReceiver
import com.dc.android.foundation.runtime.scheduling.work.DcJobSpec
import kotlinx.serialization.builtins.serializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/** MP:SCH-01/03: pure parts of the facade contract (routing inputs, identity, request building). */
class DcSchedulerContractTest {
    private fun spec(name: String) = DcJobSpec<Unit>(name = name, worker = ListenableWorker::class)

    @Test(timeout = 20_000)
    fun `work id must equal the unique work name`() {
        assertNull(spec("articles-sync").identityMismatch("articles-sync"))

        val message = spec("articles-sync").identityMismatch("other-id")

        assertNotNull(message)
        assertTrue(message!!.contains("other-id"))
        assertTrue(message.contains("articles-sync"))
    }

    @Test(timeout = 20_000)
    fun `trigger invariants fail at construction`() {
        assertThrows(IllegalArgumentException::class.java) {
            DcTrigger.WeeklyAt(LocalTime.NOON, emptySet(), ZoneId.of("UTC"))
        }
        assertThrows(IllegalArgumentException::class.java) { DcTrigger.After((-1).seconds) }
        DcTrigger.After(0.seconds)
        DcTrigger.Every(15.minutes, flex = null)
        DcTrigger.WeeklyAt(LocalTime.NOON, setOf(DayOfWeek.MONDAY), ZoneId.of("UTC"))
    }

    @Test(timeout = 20_000)
    fun `work request wraps the job spec`() {
        val job = spec("job")

        assertEquals(job, DcScheduleRequest.work(job).spec)
    }

    @Test(timeout = 20_000)
    fun `alarm request without payload carries no json and uses exact delivery by default`() {
        val request = DcScheduleRequest.alarm<String>(receiver = FirstTestReceiver::class)

        assertEquals(FirstTestReceiver::class.java, request.receiver)
        assertNull(request.payloadJson)
        assertEquals(DcExactness.Exact, request.exactness)
        assertNull("a null policy lets the facade pick the per-trigger default", request.missedPolicy)
    }

    @Test(timeout = 20_000)
    fun `alarm request encodes its payload with the supplied serializer`() {
        val request = DcScheduleRequest.alarm(
            receiver = FirstTestReceiver::class,
            payload = "hello",
            serializer = String.serializer(),
            exactness = DcExactness.Inexact,
        )

        assertEquals("\"hello\"", request.payloadJson)
        assertEquals(DcExactness.Inexact, request.exactness)
    }

    @Test(timeout = 20_000)
    fun `alarm payload without a serializer is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            DcScheduleRequest.alarm(receiver = FirstTestReceiver::class, payload = "hello")
        }
    }

    @Test(timeout = 20_000)
    fun `scheduled alarm item exposes the alarm id`() {
        val alarm = DcAlarm(
            id = "reminder",
            trigger = DcTrigger.DailyAt(LocalTime.of(9, 0), ZoneId.of("UTC")),
            receiver = FirstTestReceiver::class.java,
        )

        assertEquals("reminder", DcScheduledItem.Alarm(alarm).id)
    }
}
