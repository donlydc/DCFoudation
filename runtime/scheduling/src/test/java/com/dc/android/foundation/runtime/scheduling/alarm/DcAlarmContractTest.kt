package com.dc.android.foundation.runtime.scheduling.alarm

import com.dc.android.foundation.runtime.concurrency.DispatcherProvider
import com.dc.android.foundation.runtime.scheduling.DcTrigger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import kotlin.time.Duration.Companion.minutes

/** MP:SCH-03: codec, persisted-list robustness, store identity semantics and restore decisions. */
class DcAlarmContractTest {
    private val json = Json
    private val first = FirstTestReceiver::class.java
    private val second = SecondTestReceiver::class.java
    private val utc = ZoneId.of("UTC")
    private val newYork = ZoneId.of("America/New_York")

    private fun daily(id: String, receiver: Class<out android.content.BroadcastReceiver> = first) = DcAlarm(
        id = id,
        trigger = DcTrigger.DailyAt(LocalTime.of(8, 0), utc),
        receiver = receiver,
    )

    // ---- DcAlarm model --------------------------------------------------------------------

    @Test(timeout = 20_000)
    fun `alarm requires a non-blank id and an alarm trigger`() {
        assertThrows(IllegalArgumentException::class.java) { daily(" ") }
        // missedPolicy is passed explicitly: its default would fail first for a work trigger.
        assertThrows(IllegalArgumentException::class.java) {
            DcAlarm("work", DcTrigger.After(5.minutes), first, missedPolicy = DcMissedPolicy.Skip)
        }
        assertThrows(IllegalArgumentException::class.java) {
            DcAlarm("work", DcTrigger.Every(15.minutes), first, missedPolicy = DcMissedPolicy.Skip)
        }
    }

    @Test(timeout = 20_000)
    fun `default missed policy fires an exact alarm once and skips repeating ones`() {
        val instant = Instant.parse("2026-05-01T10:00:00Z")

        assertEquals(DcMissedPolicy.FireOnce, DcAlarm("a", DcTrigger.ExactAt(instant), first).missedPolicy)
        assertEquals(DcMissedPolicy.Skip, daily("b").missedPolicy)
        assertEquals(
            DcMissedPolicy.Skip,
            DcAlarm("c", DcTrigger.WeeklyAt(LocalTime.NOON, setOf(DayOfWeek.MONDAY), utc), first).missedPolicy,
        )
    }

    @Test(timeout = 20_000)
    fun `only exact daily and weekly triggers are alarm triggers`() {
        assertTrue(DcTrigger.ExactAt(Instant.EPOCH).isAlarmTrigger())
        assertTrue(DcTrigger.DailyAt(LocalTime.NOON, utc).isAlarmTrigger())
        assertTrue(DcTrigger.WeeklyAt(LocalTime.NOON, setOf(DayOfWeek.FRIDAY), utc).isAlarmTrigger())
        assertFalse(DcTrigger.After(5.minutes).isAlarmTrigger())
        assertFalse(DcTrigger.Every(15.minutes).isAlarmTrigger())
        assertThrows(IllegalStateException::class.java) { defaultMissedPolicy(DcTrigger.After(5.minutes)) }
    }

    @Test(timeout = 20_000)
    fun `next occurrence of an exact alarm is its instant regardless of the reference time`() {
        val instant = Instant.parse("2026-05-01T10:00:00Z")
        val alarm = DcAlarm("a", DcTrigger.ExactAt(instant), first)

        assertEquals(instant, alarm.nextOccurrence(Instant.parse("2020-01-01T00:00:00Z")))
        assertEquals(instant, alarm.nextOccurrence(Instant.parse("2030-01-01T00:00:00Z")))
    }

    // ---- Codec round trips ----------------------------------------------------------------

    @Test(timeout = 20_000)
    fun `every trigger kind round trips with all fields`() {
        val alarms = listOf(
            DcAlarm(
                id = "exact",
                trigger = DcTrigger.ExactAt(Instant.parse("2026-05-01T10:00:00Z")),
                receiver = first,
                payloadJson = "{\"id\":1}",
                exactness = DcExactness.Inexact,
                missedPolicy = DcMissedPolicy.Skip,
            ),
            DcAlarm(
                id = "daily",
                trigger = DcTrigger.DailyAt(LocalTime.of(7, 5, 30), newYork),
                receiver = second,
                exactness = DcExactness.AlarmClock,
                missedPolicy = DcMissedPolicy.FireOnce,
            ),
            DcAlarm(
                id = "weekly",
                trigger = DcTrigger.WeeklyAt(LocalTime.of(21, 45), setOf(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY), newYork),
                receiver = first,
            ),
        )

        alarms.forEach { alarm ->
            assertEquals(alarm, DcAlarmCodec.decode(json, DcAlarmCodec.encode(json, alarm)))
        }
        assertEquals(alarms, DcAlarmCodec.decodeList(json, DcAlarmCodec.encodeList(json, alarms)))
    }

    @Test(timeout = 20_000)
    fun `payload with quotes newlines and unicode survives a round trip`() {
        val alarm = daily("payload").copy(payloadJson = "{\"text\":\"a \\\"quoted\\\"\\nline é中\"}")

        assertEquals(alarm, DcAlarmCodec.decode(json, DcAlarmCodec.encode(json, alarm)))
    }

    @Test(timeout = 20_000)
    fun `missing optional fields fall back to the documented defaults`() {
        val receiver = first.name
        val daily = DcAlarmCodec.decode(
            json,
            """{"id":"d","receiver":"$receiver","trigger":"daily","time":"08:00","zone":"UTC"}""",
        )
        val exact = DcAlarmCodec.decode(
            json,
            """{"id":"e","receiver":"$receiver","trigger":"exact","instant":"2026-05-01T10:00:00Z"}""",
        )

        assertEquals(DcExactness.Exact, daily?.exactness)
        assertEquals(DcMissedPolicy.Skip, daily?.missedPolicy)
        assertNull(daily?.payloadJson)
        assertEquals(DcExactness.Exact, exact?.exactness)
        assertEquals(DcMissedPolicy.FireOnce, exact?.missedPolicy)
    }

    @Test(timeout = 20_000)
    fun `invalid single entries decode to null instead of throwing`() {
        val receiver = first.name
        val invalid = listOf(
            "",
            "null",
            "[]",
            "{}",
            """{"receiver":"$receiver","trigger":"daily","time":"08:00","zone":"UTC"}""",
            """{"id":"x","trigger":"daily","time":"08:00","zone":"UTC"}""",
            """{"id":"x","receiver":"com.example.Removed","trigger":"daily","time":"08:00","zone":"UTC"}""",
            """{"id":"x","receiver":"$receiver","trigger":"hourly","time":"08:00","zone":"UTC"}""",
            """{"id":"x","receiver":"$receiver","trigger":"daily","time":"25:61","zone":"UTC"}""",
            """{"id":"x","receiver":"$receiver","trigger":"daily","time":"08:00","zone":"Mars/Olympus"}""",
            """{"id":"x","receiver":"$receiver","trigger":"daily","time":"08:00"}""",
            """{"id":"x","receiver":"$receiver","trigger":"weekly","time":"08:00","zone":"UTC"}""",
            """{"id":"x","receiver":"$receiver","trigger":"weekly","time":"08:00","zone":"UTC","days":""}""",
            """{"id":"x","receiver":"$receiver","trigger":"weekly","time":"08:00","zone":"UTC","days":"FUNDAY"}""",
            """{"id":"x","receiver":"$receiver","trigger":"exact","instant":"not-an-instant"}""",
            """{"id":"x","receiver":"$receiver","trigger":"daily","time":"08:00","zone":"UTC","exactness":"Bogus"}""",
        )

        invalid.forEach { raw -> assertNull("should not decode: $raw", DcAlarmCodec.decode(json, raw)) }
    }

    @Test(timeout = 20_000)
    fun `a stale entry is dropped alone and does not discard the other persisted alarms`() {
        val alarms = listOf(daily("one", first), daily("stale", second), daily("three", first))
        val raw = DcAlarmCodec.encodeList(json, alarms)
            .replace(second.name, "com.example.RemovedReceiver")

        val decoded = DcAlarmCodec.decodeList(json, raw)

        assertEquals(listOf("one", "three"), decoded.map(DcAlarm::id))
    }

    @Test(timeout = 20_000)
    fun `an entry with an unknown time zone is dropped alone`() {
        val alarms = listOf(
            daily("one"),
            DcAlarm("bad-zone", DcTrigger.DailyAt(LocalTime.NOON, ZoneId.of("Asia/Bangkok")), first),
            daily("three"),
        )
        val raw = DcAlarmCodec.encodeList(json, alarms).replace("Asia/Bangkok", "Mars/Olympus")

        assertEquals(listOf("one", "three"), DcAlarmCodec.decodeList(json, raw).map(DcAlarm::id))
    }

    @Test(timeout = 20_000)
    fun `a corrupt or non-array list decodes to empty`() {
        assertEquals(emptyList<DcAlarm>(), DcAlarmCodec.decodeList(json, ""))
        assertEquals(emptyList<DcAlarm>(), DcAlarmCodec.decodeList(json, "garbage"))
        assertEquals(emptyList<DcAlarm>(), DcAlarmCodec.decodeList(json, "{}"))
        assertEquals(emptyList<DcAlarm>(), DcAlarmCodec.decodeList(json, "[]"))
        assertEquals(emptyList<DcAlarm>(), DcAlarmCodec.decodeList(json, "[1,2,\"x\"]"))
    }

    // ---- Request identity -----------------------------------------------------------------

    @Test(timeout = 20_000)
    fun `request code is deterministic per id and spreads across representative ids`() {
        assertEquals(requestCodeFor("reminder-1"), requestCodeFor("reminder-1"))
        val codes = (0 until 500).map { requestCodeFor("reminder-$it") }.toSet()
        assertTrue("unexpected request-code collisions: ${500 - codes.size}", codes.size >= 499)
    }

    // ---- Store semantics ------------------------------------------------------------------

    private val immediateDispatchers = object : DispatcherProvider {
        override val main = Dispatchers.Unconfined
        override val mainImmediate = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
    }

    private fun withStoreFile(block: suspend (File, FileDcAlarmStore) -> Unit) = runBlocking {
        val file = File.createTempFile("dc-alarm-contract", ".json")
        file.delete()
        try {
            block(file, FileDcAlarmStore(file, immediateDispatchers))
        } finally {
            file.delete()
            File("${file.path}.tmp").delete()
        }
    }

    @Test(timeout = 20_000)
    fun `putting the same id twice replaces the alarm`() = withStoreFile { _, store ->
        store.put(daily("same").copy(payloadJson = "1"))
        store.put(daily("same").copy(payloadJson = "2"))

        val stored = store.all()
        assertEquals(1, stored.size)
        assertEquals("2", stored.single().payloadJson)
    }

    @Test(timeout = 20_000)
    fun `store keeps other alarms sorted by id and ignores removing an unknown id`() = withStoreFile { _, store ->
        store.put(daily("b"))
        store.put(daily("a"))
        store.put(daily("c"))

        store.remove("does-not-exist")
        assertEquals(listOf("a", "b", "c"), store.all().map(DcAlarm::id))

        store.remove("b")
        assertEquals(listOf("a", "c"), store.all().map(DcAlarm::id))
    }

    @Test(timeout = 20_000)
    fun `writing leaves no temporary file behind`() = withStoreFile { file, store ->
        store.put(daily("a"))
        store.remove("a")

        assertFalse(File("${file.path}.tmp").exists())
    }

    @Test(timeout = 20_000)
    fun `a stale persisted entry does not wipe other alarms on the next write`() = withStoreFile { file, store ->
        file.writeText(
            DcAlarmCodec.encodeList(json, listOf(daily("keep"), daily("stale", second)))
                .replace(second.name, "com.example.RemovedReceiver"),
        )

        store.put(daily("new"))

        assertEquals(listOf("keep", "new"), store.all().map(DcAlarm::id))
    }

    // ---- Restore decision (boot / time / zone change) --------------------------------------

    private val now = Instant.parse("2026-06-01T12:00:00Z")

    @Test(timeout = 20_000)
    fun `restore re-arms a future exact alarm at its own instant`() {
        val at = Instant.parse("2026-06-02T09:00:00Z")
        val alarm = DcAlarm("a", DcTrigger.ExactAt(at), first)

        assertEquals(DcRestoreDecision.Schedule(at), alarm.restoreDecision(now))
    }

    @Test(timeout = 20_000)
    fun `restore fires a missed exact alarm once or drops it according to the policy`() {
        val past = DcTrigger.ExactAt(Instant.parse("2026-06-01T08:00:00Z"))

        assertEquals(
            DcRestoreDecision.Schedule(now),
            DcAlarm("a", past, first, missedPolicy = DcMissedPolicy.FireOnce).restoreDecision(now),
        )
        assertEquals(
            DcRestoreDecision.Drop,
            DcAlarm("b", past, first, missedPolicy = DcMissedPolicy.Skip).restoreDecision(now),
        )
    }

    @Test(timeout = 20_000)
    fun `an exact alarm due exactly now counts as missed`() {
        val due = DcTrigger.ExactAt(now)

        assertEquals(
            DcRestoreDecision.Schedule(now),
            DcAlarm("a", due, first, missedPolicy = DcMissedPolicy.FireOnce).restoreDecision(now),
        )
        assertEquals(
            DcRestoreDecision.Drop,
            DcAlarm("b", due, first, missedPolicy = DcMissedPolicy.Skip).restoreDecision(now),
        )
    }

    @Test(timeout = 20_000)
    fun `restore always re-arms repeating alarms at the next occurrence whatever the missed policy`() {
        val daily = DcAlarm(
            "d",
            DcTrigger.DailyAt(LocalTime.of(8, 0), utc),
            first,
            missedPolicy = DcMissedPolicy.FireOnce,
        )
        val weekly = DcAlarm("w", DcTrigger.WeeklyAt(LocalTime.of(8, 0), setOf(DayOfWeek.WEDNESDAY), utc), first)

        assertEquals(
            DcRestoreDecision.Schedule(Instant.parse("2026-06-02T08:00:00Z")),
            daily.restoreDecision(now),
        )
        assertEquals(
            DcRestoreDecision.Schedule(Instant.parse("2026-06-03T08:00:00Z")), // 2026-06-01 is a Monday
            weekly.restoreDecision(now),
        )
    }

    @Test(timeout = 20_000)
    fun `restore after a time zone change recomputes the instant from the stored wall time`() {
        val atNewYork = DcAlarm("d", DcTrigger.DailyAt(LocalTime.of(8, 0), newYork), first)

        assertEquals(
            DcRestoreDecision.Schedule(Instant.parse("2026-06-02T12:00:00Z")), // 08:00 EDT next day
            atNewYork.restoreDecision(now),
        )
        // Clock set back to just before today's occurrence: it resolves from the new "now".
        assertEquals(
            DcRestoreDecision.Schedule(Instant.parse("2026-06-01T12:00:00Z")),
            atNewYork.restoreDecision(Instant.parse("2026-06-01T11:59:00Z")),
        )
    }
}
