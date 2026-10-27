package com.dc.android.foundation.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/** PREF-01 typed contract and PREF-02 migration idempotency/recovery, against an in-memory store. */
class PreferencesContractTest {

    private enum class Mode { System, Dark }

    private val intKey = PreferenceKey.IntKey("int", 5)
    private val longKey = PreferenceKey.LongKey("long", 6L)
    private val floatKey = PreferenceKey.FloatKey("float", 1.5f)
    private val boolKey = PreferenceKey.BooleanKey("bool", true)
    private val stringKey = PreferenceKey.StringKey("string", "d")
    private val setKey = PreferenceKey.StringSetKey("set", setOf("a"))
    private val enumKey = PreferenceKey.EnumKey("enum", Mode.System, Mode.entries.toTypedArray())
    private val jsonKey = PreferenceKey.JsonKey("json", listOf(1), ListSerializer(Int.serializer()))

    @Test
    fun `missing keys resolve to their declared defaults`() = runTest {
        val ds = DataStorePreferencesDataSource(MemoryStore())

        assertEquals(5, ds.get(intKey))
        assertEquals(6L, ds.get(longKey))
        assertEquals(1.5f, ds.get(floatKey))
        assertEquals(true, ds.get(boolKey))
        assertEquals("d", ds.get(stringKey))
        assertEquals(setOf("a"), ds.get(setKey))
        assertEquals(Mode.System, ds.get(enumKey))
        assertEquals(listOf(1), ds.get(jsonKey))
    }

    @Test
    fun `set then get round-trips every key type`() = runTest {
        val ds = DataStorePreferencesDataSource(MemoryStore())

        ds.set(intKey, 1)
        ds.set(longKey, 2L)
        ds.set(floatKey, 3.5f)
        ds.set(boolKey, false)
        ds.set(stringKey, "x")
        ds.set(setKey, setOf("p", "q"))
        ds.set(enumKey, Mode.Dark)
        ds.set(jsonKey, listOf(7, 8))

        assertEquals(1, ds.get(intKey))
        assertEquals(2L, ds.get(longKey))
        assertEquals(3.5f, ds.get(floatKey))
        assertEquals(false, ds.get(boolKey))
        assertEquals("x", ds.get(stringKey))
        assertEquals(setOf("p", "q"), ds.get(setKey))
        assertEquals(Mode.Dark, ds.get(enumKey))
        assertEquals(listOf(7, 8), ds.get(jsonKey))
    }

    @Test
    fun `remove restores the default and clear resets everything`() = runTest {
        val ds = DataStorePreferencesDataSource(MemoryStore())
        ds.set(intKey, 1)
        ds.set(enumKey, Mode.Dark)
        ds.set(jsonKey, listOf(9))

        ds.remove(intKey)
        ds.remove(enumKey)
        assertEquals(5, ds.get(intKey))
        assertEquals(Mode.System, ds.get(enumKey))
        assertEquals(listOf(9), ds.get(jsonKey))

        ds.clear()
        assertEquals(listOf(1), ds.get(jsonKey))
    }

    @Test
    fun `malformed json storage resolves to the key default`() = runTest {
        val store = MemoryStore(mutablePreferencesOf(stringPreferencesKey("json") to "{not json"))

        assertEquals(listOf(1), DataStorePreferencesDataSource(store).get(jsonKey))
    }

    @Test
    fun `observe emits default then each change then default after remove`() = runTest {
        val ds = DataStorePreferencesDataSource(MemoryStore())

        ds.observe(intKey).test {
            assertEquals(5, awaitItem())
            ds.set(intKey, 10)
            assertEquals(10, awaitItem())
            ds.set(intKey, 11)
            assertEquals(11, awaitItem())
            ds.remove(intKey)
            assertEquals(5, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `values survive re-creating the data source over the same store`() = runTest {
        val store = MemoryStore()
        DataStorePreferencesDataSource(store).set(stringKey, "persisted")

        assertEquals("persisted", DataStorePreferencesDataSource(store).get(stringKey))
    }

    @Test
    fun `concurrent writes to distinct keys are all kept`() = runTest {
        val store = MemoryStore()
        val ds = DataStorePreferencesDataSource(store)
        val keys = (0 until 20).map { PreferenceKey.IntKey("k$it", -1) }

        keys.mapIndexed { index, key -> async { ds.set(key, index) } }.awaitAll()

        keys.forEachIndexed { index, key -> assertEquals(index, ds.get(key)) }
    }

    @Test
    fun `remove and clear on an empty store are no-ops`() = runTest {
        val ds = DataStorePreferencesDataSource(MemoryStore())

        ds.remove(intKey)
        ds.clear()

        assertEquals(5, ds.get(intKey))
    }

    // --- PREF-02 migrations -----------------------------------------------------------------

    @Test
    fun `migrations are idempotent across data source instances`() = runTest {
        val store = MemoryStore()
        val applied = mutableListOf<Int>()
        val migrations = listOf(migration(1, applied) { set(intKey, 100) })

        DataStorePreferencesDataSource(store, migrations).get(intKey)
        DataStorePreferencesDataSource(store, migrations).set(intKey, 7)
        assertEquals(7, DataStorePreferencesDataSource(store, migrations).get(intKey))

        assertEquals(listOf(1), applied)
        assertEquals(1, store.snapshot()[intPreferencesKey("dc_preferences_schema_version")])
    }

    @Test
    fun `only migrations above the stored schema version run`() = runTest {
        val store = MemoryStore(mutablePreferencesOf(intPreferencesKey("dc_preferences_schema_version") to 1))
        val applied = mutableListOf<Int>()

        DataStorePreferencesDataSource(
            store,
            listOf(migration(1, applied) { set(intKey, 1) }, migration(2, applied) { set(intKey, 2) })
        ).get(intKey)

        assertEquals(listOf(2), applied)
    }

    @Test
    fun `failed migration keeps earlier data and version and is retried by a new instance`() = runTest {
        val store = MemoryStore(mutablePreferencesOf(stringPreferencesKey("legacy") to "keep"))
        val applied = mutableListOf<Int>()
        val ok = migration(1, applied) { set(intKey, 1) }
        val broken = object : PreferencesMigration {
            override val toVersion = 2
            override fun migrate(editor: PreferencesEditor) {
                throw IllegalStateException("boom")
            }
        }

        try {
            DataStorePreferencesDataSource(store, listOf(ok, broken)).get(intKey)
            fail("expected the failing migration to propagate")
        } catch (expected: IllegalStateException) {
            assertEquals("boom", expected.message)
        }

        assertEquals(1, store.snapshot()[intPreferencesKey("dc_preferences_schema_version")])
        assertEquals("keep", store.snapshot()[stringPreferencesKey("legacy")])
        assertEquals(1, store.snapshot()[intPreferencesKey("int")])

        val fixed = migration(2, applied) { set(intKey, 2) }
        assertEquals(2, DataStorePreferencesDataSource(store, listOf(ok, fixed)).get(intKey))
        assertEquals(listOf(1, 2), applied)
        assertTrue(store.snapshot()[stringPreferencesKey("legacy")] == "keep")
    }

    private fun migration(
        version: Int,
        applied: MutableList<Int>,
        block: PreferencesEditor.() -> Unit
    ): PreferencesMigration = object : PreferencesMigration {
        override val toVersion: Int = version
        override fun migrate(editor: PreferencesEditor) {
            applied += version
            editor.block()
        }
    }
}

private class MemoryStore(initial: Preferences = emptyPreferences()) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)
    override val data: Flow<Preferences> = state

    fun snapshot(): Preferences = state.value

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}
