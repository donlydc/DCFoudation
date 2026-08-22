package com.dc.android.foundation.data.preferences

/** Atomic preference mutation surface used by [PreferencesDataSource.edit] and migrations. */
interface PreferencesEditor {
    fun <T> set(key: PreferenceKey<T>, value: T)
    fun remove(key: PreferenceKey<*>)
    fun clear()
}

/**
 * Versioned preference migration. Implementations must be idempotent for their target version
 * and may only describe editor mutations; they must not perform I/O or suspend.
 */
interface PreferencesMigration {
    val toVersion: Int
    fun migrate(editor: PreferencesEditor)
}
