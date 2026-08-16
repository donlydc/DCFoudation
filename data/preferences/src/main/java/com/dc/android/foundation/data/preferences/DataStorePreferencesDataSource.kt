package com.dc.android.foundation.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/**
 * DataStore-backed implementation. Infra exceptions are caught here, at the data-source
 * boundary, and mapped to a safe default rather than propagating to the ViewModel (ERR-01).
 */
class DataStorePreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferencesDataSource {

    override fun getInt(key: String, default: Int): Flow<Int> {
        val preferencesKey = intPreferencesKey(key)
        return dataStore.data
            .catch { cause ->
                if (cause is IOException) emit(emptyPreferences()) else throw cause
            }
            .map { preferences -> preferences[preferencesKey] ?: default }
    }

    override suspend fun setInt(key: String, value: Int) {
        val preferencesKey = intPreferencesKey(key)
        dataStore.edit { preferences -> preferences[preferencesKey] = value }
    }
}
