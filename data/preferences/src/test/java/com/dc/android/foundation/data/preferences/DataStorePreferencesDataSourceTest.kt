package com.dc.android.foundation.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DataStorePreferencesDataSourceTest {

    @Test
    fun `migrations run in version order once before the first read`() = runTest {
        val store = InMemoryPreferencesStore()
        val valueKey = PreferenceKey.IntKey("value", default = 0)
        val appliedVersions = mutableListOf<Int>()
        val dataSource = DataStorePreferencesDataSource(
            dataStore = store,
            migrations = listOf(
                migration(version = 2, appliedVersions) { set(valueKey, 2) },
                migration(version = 1, appliedVersions) { set(valueKey, 1) }
            )
        )

        assertEquals(2, dataSource.get(valueKey))
        assertEquals(2, dataSource.get(valueKey))

        assertEquals(listOf(1, 2), appliedVersions)
        assertEquals(2, store.updateCount)
    }

    @Test
    fun `edit commits related mutations in one DataStore update`() = runTest {
        val store = InMemoryPreferencesStore()
        val nameKey = PreferenceKey.StringKey("name", default = "")
        val countKey = PreferenceKey.IntKey("count", default = 0)
        val dataSource = DataStorePreferencesDataSource(store)

        dataSource.edit {
            set(nameKey, "DC")
            set(countKey, 7)
        }

        assertEquals(1, store.updateCount)
        assertEquals("DC", dataSource.get(nameKey))
        assertEquals(7, dataSource.get(countKey))
    }

    @Test
    fun `invalid enum storage resolves to the key default`() = runTest {
        val key = PreferenceKey.EnumKey("theme", ThemeMode.System, ThemeMode.entries.toTypedArray())
        val store = InMemoryPreferencesStore(
            mutablePreferencesOf(stringPreferencesKey("theme") to "NO_LONGER_VALID")
        )

        assertEquals(ThemeMode.System, DataStorePreferencesDataSource(store).get(key))
    }

    @Test
    fun `io read failure resolves to the key default`() = runTest {
        val key = PreferenceKey.IntKey("retries", default = 3)
        val dataSource = DataStorePreferencesDataSource(FailingReadPreferencesStore())

        assertEquals(3, dataSource.get(key))
    }

    private fun migration(
        version: Int,
        appliedVersions: MutableList<Int>,
        block: PreferencesEditor.() -> Unit
    ): PreferencesMigration = object : PreferencesMigration {
        override val toVersion: Int = version

        override fun migrate(editor: PreferencesEditor) {
            appliedVersions += version
            editor.block()
        }
    }

    private enum class ThemeMode { System, Light, Dark }
}

private class InMemoryPreferencesStore(
    initial: Preferences = emptyPreferences()
) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)
    var updateCount: Int = 0
        private set

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        updateCount += 1
        state.value = updated
        return updated
    }
}

private class FailingReadPreferencesStore : DataStore<Preferences> {
    override val data: Flow<Preferences> = flow { throw IOException("disk unavailable") }

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        throw AssertionError("updateData must not be called when reading a default")
    }
}
