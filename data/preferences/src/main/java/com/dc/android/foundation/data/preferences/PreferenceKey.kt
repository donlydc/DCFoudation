package com.dc.android.foundation.data.preferences

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/** Typed preference key with its safe default held at the storage boundary. */
sealed class PreferenceKey<T>(val name: String, val default: T) {
    class IntKey(name: String, default: Int) : PreferenceKey<Int>(name, default)
    class LongKey(name: String, default: Long) : PreferenceKey<Long>(name, default)
    class FloatKey(name: String, default: Float) : PreferenceKey<Float>(name, default)
    class BooleanKey(name: String, default: Boolean) : PreferenceKey<Boolean>(name, default)
    class StringKey(name: String, default: String) : PreferenceKey<String>(name, default)
    class StringSetKey(name: String, default: Set<String>) : PreferenceKey<Set<String>>(name, default)

    class EnumKey<E : Enum<E>>(
        name: String,
        default: E,
        internal val values: Array<E>
    ) : PreferenceKey<E>(name, default) {
        internal fun decode(raw: String): E = values.firstOrNull { it.name == raw } ?: default
    }

    class JsonKey<T>(
        name: String,
        default: T,
        internal val serializer: KSerializer<T>
    ) : PreferenceKey<T>(name, default) {
        internal fun decode(json: Json, raw: String): T =
            runCatching { json.decodeFromString(serializer, raw) }.getOrDefault(default)
    }
}
