package com.dc.android.foundation.greeting

import com.dc.android.foundation.data.preferences.PreferencesDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

private const val KEY_LAST_NAME_INDEX = "greeting_last_name_index"

/**
 * Mapping boundary (DAT-01): translates the generic [PreferencesDataSource] key/int API into
 * the feature's own vocabulary. `GreetingViewModel` depends on this, never on
 * [PreferencesDataSource] directly.
 */
class GreetingPreferencesRepository @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource
) {
    fun lastNameIndex(): Flow<Int> = preferencesDataSource.getInt(KEY_LAST_NAME_INDEX, default = 0)

    suspend fun setLastNameIndex(index: Int) {
        preferencesDataSource.setInt(KEY_LAST_NAME_INDEX, index)
    }
}
