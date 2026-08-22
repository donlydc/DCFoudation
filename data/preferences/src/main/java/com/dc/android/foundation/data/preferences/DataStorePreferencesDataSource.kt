package com.dc.android.foundation.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

/**
 * Typed DataStore Preferences implementation. Expected read I/O failures and malformed persisted
 * enum/JSON values resolve to a key's declared default at this boundary (ERR-01).
 */
class DataStorePreferencesDataSource(
    private val dataStore: DataStore<Preferences>,
    private val migrations: List<PreferencesMigration> = emptyList(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : PreferencesDataSource {

    private val migrationMutex = Mutex()
    private var migrationsApplied = false

    override fun <T> observe(key: PreferenceKey<T>): Flow<T> = flow {
        ensureMigrations()
        emitAll(
            dataStore.data
                .catch { cause ->
                    if (cause is IOException) emit(emptyPreferences()) else throw cause
                }
                .map { preferences -> preferences.read(key) }
        )
    }

    override suspend fun <T> get(key: PreferenceKey<T>): T = observe(key).first()

    override suspend fun <T> set(key: PreferenceKey<T>, value: T) {
        edit { set(key, value) }
    }

    override suspend fun remove(key: PreferenceKey<*>) {
        edit { remove(key) }
    }

    override suspend fun clear() {
        ensureMigrations()
        dataStore.edit { it.clear() }
    }

    override suspend fun edit(block: PreferencesEditor.() -> Unit) {
        ensureMigrations()
        val pending = PendingPreferencesEditor()
        pending.block()
        dataStore.edit { preferences -> pending.applyTo(preferences) }
    }

    private suspend fun ensureMigrations() {
        migrationMutex.withLock {
            if (migrationsApplied) return

            val versionKey = intPreferencesKey(SCHEMA_VERSION_KEY)
            var currentVersion = dataStore.data
                .catch { cause ->
                    if (cause is IOException) emit(emptyPreferences()) else throw cause
                }
                .first()[versionKey] ?: 0

            migrations.sortedBy(PreferencesMigration::toVersion).forEach { migration ->
                require(migration.toVersion > 0) { "Migration versions must be positive." }
                if (migration.toVersion <= currentVersion) return@forEach

                val pending = PendingPreferencesEditor()
                migration.migrate(pending)
                dataStore.edit { preferences ->
                    pending.applyTo(preferences)
                    preferences[versionKey] = migration.toVersion
                }
                currentVersion = migration.toVersion
            }
            migrationsApplied = true
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> Preferences.read(key: PreferenceKey<T>): T = when (key) {
        is PreferenceKey.IntKey -> this[intPreferencesKey(key.name)] ?: key.default
        is PreferenceKey.LongKey -> this[longPreferencesKey(key.name)] ?: key.default
        is PreferenceKey.FloatKey -> this[floatPreferencesKey(key.name)] ?: key.default
        is PreferenceKey.BooleanKey -> this[booleanPreferencesKey(key.name)] ?: key.default
        is PreferenceKey.StringKey -> this[stringPreferencesKey(key.name)] ?: key.default
        is PreferenceKey.StringSetKey -> this[stringSetPreferencesKey(key.name)] ?: key.default
        is PreferenceKey.EnumKey<*> -> {
            val raw = this[stringPreferencesKey(key.name)]
            if (raw == null) key.default else key.decode(raw)
        }
        is PreferenceKey.JsonKey<*> -> {
            val raw = this[stringPreferencesKey(key.name)]
            if (raw == null) key.default else key.decode(json, raw)
        }
    } as T

    @Suppress("UNCHECKED_CAST")
    private fun <T> MutablePreferences.write(key: PreferenceKey<T>, value: T) {
        when (key) {
            is PreferenceKey.IntKey -> this[intPreferencesKey(key.name)] = value as Int
            is PreferenceKey.LongKey -> this[longPreferencesKey(key.name)] = value as Long
            is PreferenceKey.FloatKey -> this[floatPreferencesKey(key.name)] = value as Float
            is PreferenceKey.BooleanKey -> this[booleanPreferencesKey(key.name)] = value as Boolean
            is PreferenceKey.StringKey -> this[stringPreferencesKey(key.name)] = value as String
            is PreferenceKey.StringSetKey -> this[stringSetPreferencesKey(key.name)] = value as Set<String>
            is PreferenceKey.EnumKey<*> -> this[stringPreferencesKey(key.name)] = (value as Enum<*>).name
            is PreferenceKey.JsonKey<*> -> this[stringPreferencesKey(key.name)] = key.encode(json, value)
        }
    }

    private fun MutablePreferences.removeValue(key: PreferenceKey<*>) {
        when (key) {
            is PreferenceKey.IntKey -> remove(intPreferencesKey(key.name))
            is PreferenceKey.LongKey -> remove(longPreferencesKey(key.name))
            is PreferenceKey.FloatKey -> remove(floatPreferencesKey(key.name))
            is PreferenceKey.BooleanKey -> remove(booleanPreferencesKey(key.name))
            is PreferenceKey.StringKey,
            is PreferenceKey.EnumKey<*>,
            is PreferenceKey.JsonKey<*> -> remove(stringPreferencesKey(key.name))
            is PreferenceKey.StringSetKey -> remove(stringSetPreferencesKey(key.name))
        }
    }

    private inner class PendingPreferencesEditor : PreferencesEditor {
        private val operations = mutableListOf<PreferenceOperation>()

        override fun <T> set(key: PreferenceKey<T>, value: T) {
            operations += PreferenceOperation.Set(key, value)
        }

        override fun remove(key: PreferenceKey<*>) {
            operations += PreferenceOperation.Remove(key)
        }

        override fun clear() {
            operations += PreferenceOperation.Clear
        }

        fun applyTo(preferences: MutablePreferences) {
            operations.forEach { operation ->
                when (operation) {
                    is PreferenceOperation.Set<*> -> writeUnchecked(preferences, operation)
                    is PreferenceOperation.Remove -> preferences.removeValue(operation.key)
                    PreferenceOperation.Clear -> preferences.clear()
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun writeUnchecked(preferences: MutablePreferences, operation: PreferenceOperation.Set<*>) {
            preferences.write(
                operation.key as PreferenceKey<Any?>,
                operation.value
            )
        }
    }

    private companion object {
        const val SCHEMA_VERSION_KEY = "dc_preferences_schema_version"
    }
}

private sealed interface PreferenceOperation {
    data class Set<T>(val key: PreferenceKey<T>, val value: T) : PreferenceOperation
    data class Remove(val key: PreferenceKey<*>) : PreferenceOperation
    data object Clear : PreferenceOperation
}

@Suppress("UNCHECKED_CAST")
private fun <T> PreferenceKey.JsonKey<*>.encode(json: Json, value: T): String =
    json.encodeToString(serializer as kotlinx.serialization.KSerializer<T>, value)
