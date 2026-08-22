package com.dc.android.foundation.testing

import com.dc.android.foundation.data.preferences.PreferenceKey
import com.dc.android.foundation.data.preferences.PreferencesDataSource
import com.dc.android.foundation.data.preferences.PreferencesEditor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Deterministic in-memory [PreferencesDataSource] fake for unit tests. */
public class InMemoryPreferencesDataSource : PreferencesDataSource {
    private val mutex = Mutex()
    private val values = MutableStateFlow<Map<String, Any?>>(emptyMap())

    override fun <T> observe(key: PreferenceKey<T>): Flow<T> = values.map { entries ->
        @Suppress("UNCHECKED_CAST")
        (entries[key.name] as? T) ?: key.default
    }

    override suspend fun <T> get(key: PreferenceKey<T>): T = mutex.withLock {
        @Suppress("UNCHECKED_CAST")
        (values.value[key.name] as? T) ?: key.default
    }

    override suspend fun <T> set(key: PreferenceKey<T>, value: T) {
        edit { set(key, value) }
    }

    override suspend fun remove(key: PreferenceKey<*>) {
        edit { remove(key) }
    }

    override suspend fun clear() {
        mutex.withLock { values.value = emptyMap() }
    }

    override suspend fun edit(block: PreferencesEditor.() -> Unit) {
        mutex.withLock {
            val mutableValues = values.value.toMutableMap()
            val editor = InMemoryEditor(mutableValues)
            editor.block()
            values.value = mutableValues.toMap()
        }
    }

    private class InMemoryEditor(
        private val values: MutableMap<String, Any?>
    ) : PreferencesEditor {
        override fun <T> set(key: PreferenceKey<T>, value: T) {
            values[key.name] = value
        }

        override fun remove(key: PreferenceKey<*>) {
            values.remove(key.name)
        }

        override fun clear() {
            values.clear()
        }
    }
}
