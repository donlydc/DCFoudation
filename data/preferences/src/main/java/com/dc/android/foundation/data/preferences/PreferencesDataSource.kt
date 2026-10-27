package com.dc.android.foundation.data.preferences

import kotlinx.coroutines.flow.Flow

/**
 * Generic key-value preference contract (DAT-01: business-neutral — no feature-specific
 * keys/types here; a feature wraps this with its own mapping, see
 * `app/.../greeting/GreetingPreferencesRepository.kt`).
 *
 * Contract (PREF-01):
 * - Every [PreferenceKey] carries its own default. [get] and [observe] never fail for a missing,
 *   malformed (enum/JSON) or unreadable (`IOException`) value; they yield `key.default`.
 * - [observe] emits the current value first, then every change, and re-emits after [remove] or
 *   [clear] with the default. It does not de-duplicate beyond what the store itself does.
 * - [set], [remove], [clear] and [edit] are serialized by the underlying DataStore; a value that
 *   completed a write is visible to every later [get] and survives re-creating the data source
 *   over the same store (restart).
 * - Write failures are NOT swallowed: an `IOException` from a write propagates to the caller.
 *
 * Config vs secret (PREF-03): this is ordinary, unencrypted app storage for non-sensitive
 * configuration and UI preferences only. Never store tokens, passwords, API keys or other
 * secrets here; use the `:security` secure-storage primitives instead.
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
