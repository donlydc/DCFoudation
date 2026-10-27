package com.dc.android.foundation.runtime.scheduling.alarm

import android.content.BroadcastReceiver
import com.dc.android.foundation.runtime.scheduling.DcTrigger
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Internal JSON codec; public alarm models deliberately avoid leaking a persistence format. */
internal object DcAlarmCodec {
    fun encode(json: Json, alarm: DcAlarm): String = json.encodeToString(JsonObject.serializer(), alarm.toJson())

    fun decode(json: Json, raw: String): DcAlarm? = runCatching {
        raw.toJsonObject(json)?.toAlarm()
    }.getOrNull()

    fun encodeList(json: Json, alarms: List<DcAlarm>): String = json.encodeToString(
        JsonArray.serializer(),
        JsonArray(alarms.map { alarm -> alarm.toJson() }),
    )

    /**
     * Decodes entry by entry: a corrupt or stale entry (receiver class removed, unknown zone or
     * enum name, wrong JSON shape) is dropped on its own. Failing the whole list would make the
     * next store write overwrite every other persisted alarm.
     */
    fun decodeList(json: Json, raw: String): List<DcAlarm> = runCatching {
        json.parseToJsonElement(raw).jsonArray.mapNotNull { element ->
            runCatching { element.jsonObject.toAlarm() }.getOrNull()
        }
    }.getOrDefault(emptyList())

    private fun String.toJsonObject(json: Json): JsonObject? = json.parseToJsonElement(this).jsonObject

    private fun DcAlarm.toJson(): JsonObject = buildJsonObject {
        put("id", id)
        put("receiver", receiver.name)
        payloadJson?.let { put("payload", it) }
        put("exactness", exactness.name)
        put("missedPolicy", missedPolicy.name)
        when (val value = trigger) {
            is DcTrigger.ExactAt -> {
                put("trigger", "exact")
                put("instant", value.instant.toString())
            }
            is DcTrigger.DailyAt -> {
                put("trigger", "daily")
                put("time", value.time.toString())
                put("zone", value.zone.id)
            }
            is DcTrigger.WeeklyAt -> {
                put("trigger", "weekly")
                put("time", value.time.toString())
                put("zone", value.zone.id)
                put("days", value.days.joinToString(",") { it.name })
            }
            is DcTrigger.After, is DcTrigger.Every -> error("Work triggers cannot be serialized as alarms.")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun JsonObject.toAlarm(): DcAlarm? {
        val id = this["id"]?.jsonPrimitive?.contentOrNull ?: return null
        val receiverName = this["receiver"]?.jsonPrimitive?.contentOrNull ?: return null
        val receiver = Class.forName(receiverName).asSubclass(BroadcastReceiver::class.java)
        val trigger = when (this["trigger"]?.jsonPrimitive?.contentOrNull) {
            "exact" -> DcTrigger.ExactAt(Instant.parse(this["instant"]?.jsonPrimitive?.contentOrNull ?: return null))
            "daily" -> DcTrigger.DailyAt(
                LocalTime.parse(this["time"]?.jsonPrimitive?.contentOrNull ?: return null),
                ZoneId.of(this["zone"]?.jsonPrimitive?.contentOrNull ?: return null),
            )
            "weekly" -> DcTrigger.WeeklyAt(
                LocalTime.parse(this["time"]?.jsonPrimitive?.contentOrNull ?: return null),
                this["days"]?.jsonPrimitive?.contentOrNull
                    ?.split(',')
                    ?.filter(String::isNotBlank)
                    ?.mapTo(linkedSetOf(), DayOfWeek::valueOf)
                    ?.takeIf(Set<DayOfWeek>::isNotEmpty)
                    ?: return null,
                ZoneId.of(this["zone"]?.jsonPrimitive?.contentOrNull ?: return null),
            )
            else -> return null
        }
        return DcAlarm(
            id = id,
            trigger = trigger,
            receiver = receiver as Class<out BroadcastReceiver>,
            payloadJson = this["payload"]?.jsonPrimitive?.contentOrNull,
            exactness = this["exactness"]?.jsonPrimitive?.contentOrNull
                ?.let(DcExactness::valueOf) ?: DcExactness.Exact,
            missedPolicy = this["missedPolicy"]?.jsonPrimitive?.contentOrNull
                ?.let(DcMissedPolicy::valueOf) ?: defaultMissedPolicy(trigger),
        )
    }
}
