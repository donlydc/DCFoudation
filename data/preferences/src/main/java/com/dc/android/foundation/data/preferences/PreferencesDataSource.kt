package com.dc.android.foundation.data.preferences

import kotlinx.coroutines.flow.Flow

/**
 * Generic key-value preference contract (DAT-01: business-neutral — no feature-specific
 * keys/types here; a feature wraps this with its own mapping, see
 * `app/.../greeting/GreetingPreferencesRepository.kt`).
 */
interface PreferencesDataSource {
    fun getInt(key: String, default: Int): Flow<Int>
    suspend fun setInt(key: String, value: Int)
}
