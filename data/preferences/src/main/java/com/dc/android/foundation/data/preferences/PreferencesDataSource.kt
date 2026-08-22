package com.dc.android.foundation.data.preferences

import kotlinx.coroutines.flow.Flow

/**
 * Generic key-value preference contract (DAT-01: business-neutral — no feature-specific
 * keys/types here; a feature wraps this with its own mapping, see
 * `app/.../greeting/GreetingPreferencesRepository.kt`).
 */
interface PreferencesDataSource {
    fun <T> observe(key: PreferenceKey<T>): Flow<T>
    suspend fun <T> get(key: PreferenceKey<T>): T
    suspend fun <T> set(key: PreferenceKey<T>, value: T)
    suspend fun remove(key: PreferenceKey<*>)
    suspend fun clear()
    /**
     * Applies related mutations atomically. The editor callback is deliberately non-suspending:
     * storage implementations may hold their transaction lock while applying it (INV-05).
     */
    suspend fun edit(block: PreferencesEditor.() -> Unit)
}
