package com.dc.android.foundation.greeting

import com.dc.android.foundation.data.preferences.PreferencesDataSource
import com.dc.android.foundation.data.preferences.PreferenceKey
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

private val LastNameIndexKey = PreferenceKey.IntKey(
    name = "greeting_last_name_index",
    default = 0
)

/**
 * Mapping boundary (DAT-01): translates the generic [PreferencesDataSource] typed-key API into
 * the feature's own vocabulary. `GreetingViewModel` depends on this, never on
 * [PreferencesDataSource] directly.
 */
class GreetingPreferencesRepository @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource
) {
    fun lastNameIndex(): Flow<Int> = preferencesDataSource.observe(LastNameIndexKey)

    suspend fun setLastNameIndex(index: Int) {
        preferencesDataSource.set(LastNameIndexKey, index)
    }
}
