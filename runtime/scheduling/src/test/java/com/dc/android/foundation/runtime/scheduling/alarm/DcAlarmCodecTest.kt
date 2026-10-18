package com.dc.android.foundation.runtime.scheduling.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dc.android.foundation.runtime.scheduling.DcTrigger
import com.dc.android.foundation.runtime.concurrency.DispatcherProvider
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

class DcAlarmCodecTest {
    @Test
    fun `alarm JSON round trips weekly wall clock data`() {
        val alarm = DcAlarm(
            id = "morning-reminder",
            trigger = DcTrigger.WeeklyAt(
                time = LocalTime.of(7, 0),
                days = setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY),
                zone = ZoneId.of("Asia/Bangkok"),
            ),
            receiver = TestReceiver::class.java,
            payloadJson = "{\"id\":1}",
        )

        val decoded = DcAlarmCodec.decode(Json, DcAlarmCodec.encode(Json, alarm))

        assertEquals(alarm.id, decoded?.id)
        assertEquals(alarm.trigger, decoded?.trigger)
        assertEquals(alarm.receiver, decoded?.receiver)
    }

    @Test
    fun `corrupt persisted JSON does not produce an alarm`() {
        assertNull(DcAlarmCodec.decode(Json, "not-json"))
    }

    @Test
    fun `request code differs for representative distinct ids`() {
        assertNotEquals(requestCodeFor("reminder-1"), requestCodeFor("reminder-2"))
    }

    @Test
    fun `file store round trips and recovers from corrupt content`() = runBlocking {
        val file = File.createTempFile("dc-alarm-store", ".json")
        file.delete()
        try {
            val store = FileDcAlarmStore(file, testDispatchers)
            val alarm = DcAlarm(
                id = "persisted",
                trigger = DcTrigger.DailyAt(LocalTime.of(8, 0), ZoneId.of("UTC")),
                receiver = TestReceiver::class.java,
            )

            store.put(alarm)
            assertEquals(listOf(alarm), store.all())
            store.remove(alarm.id)
            assertEquals(emptyList<DcAlarm>(), store.all())

            file.writeText("invalid")
            assertEquals(emptyList<DcAlarm>(), store.all())
        } finally {
            file.delete()
            File("${file.path}.tmp").delete()
        }
    }

    @Test
    fun `separate store instances do not lose concurrent alarms`() = runBlocking {
        val file = File.createTempFile("dc-alarm-store-concurrent", ".json")
        file.delete()
        try {
            val first = FileDcAlarmStore(file, defaultDispatchers)
            val second = FileDcAlarmStore(file, defaultDispatchers)

            coroutineScope {
                (0 until 40).map { index ->
                    async {
                        val store = if (index % 2 == 0) first else second
                        store.put(
                            DcAlarm(
                                id = "alarm-$index",
                                trigger = DcTrigger.DailyAt(LocalTime.of(8, 0), ZoneId.of("UTC")),
                                receiver = TestReceiver::class.java,
                            )
                        )
                    }
                }.awaitAll()
            }

            assertEquals(40, first.all().size)
        } finally {
            file.delete()
            File("${file.path}.tmp").delete()
        }
    }

    class TestReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = Unit
    }

    private companion object {
        val testDispatchers = object : DispatcherProvider {
            override val main = Dispatchers.Unconfined
            override val mainImmediate = Dispatchers.Unconfined
            override val io = Dispatchers.Unconfined
            override val default = Dispatchers.Unconfined
        }
        val defaultDispatchers = object : DispatcherProvider {
            override val main = Dispatchers.Default
            override val mainImmediate = Dispatchers.Default
            override val io = Dispatchers.Default
            override val default = Dispatchers.Default
        }
    }
}
