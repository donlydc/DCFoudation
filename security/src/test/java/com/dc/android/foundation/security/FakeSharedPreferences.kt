package com.dc.android.foundation.security

import android.content.SharedPreferences

/**
 * Pure-JVM in-memory [SharedPreferences] (only the members `EncryptedSharedPreferencesSecureStorage`
 * uses are meaningful) so the storage's lifecycle/failure mapping is testable without Keystore.
 * [failOnGet] simulates `EncryptedSharedPreferences` failing to decrypt (it throws `SecurityException`).
 */
internal class FakeSharedPreferences(private val failOnGet: Throwable? = null) : SharedPreferences {
    val data = mutableMapOf<String, String?>()

    override fun getString(key: String?, defValue: String?): String? {
        failOnGet?.let { throw it }
        return if (data.containsKey(key)) data[key] else defValue
    }

    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        private val puts = mutableMapOf<String, String?>()
        private val removes = mutableSetOf<String>()
        private var clear = false

        override fun putString(key: String, value: String?) = also { puts[key] = value }
        override fun remove(key: String) = also { removes += key; puts.remove(key) }
        override fun clear() = also { clear = true }
        override fun commit(): Boolean { apply(); return true }
        override fun apply() {
            if (clear) data.clear()
            removes.forEach { data.remove(it) }
            data.putAll(puts)
        }

        override fun putStringSet(key: String, values: MutableSet<String>?) = this
        override fun putInt(key: String, value: Int) = this
        override fun putLong(key: String, value: Long) = this
        override fun putFloat(key: String, value: Float) = this
        override fun putBoolean(key: String, value: Boolean) = this
    }

    override fun getAll(): MutableMap<String, *> = data.toMutableMap()
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues
    override fun getInt(key: String?, defValue: Int) = defValue
    override fun getLong(key: String?, defValue: Long) = defValue
    override fun getFloat(key: String?, defValue: Float) = defValue
    override fun getBoolean(key: String?, defValue: Boolean) = defValue
    override fun contains(key: String?) = data.containsKey(key)
    override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
    override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
}
