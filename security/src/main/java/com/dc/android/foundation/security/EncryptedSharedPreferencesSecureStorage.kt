package com.dc.android.foundation.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.dc.android.foundation.runtime.concurrency.DispatcherProvider
import kotlinx.coroutines.withContext
import java.io.IOException
import java.security.GeneralSecurityException

private const val PREFS_FILE_NAME = "dc_foundation_secure_prefs"

/**
 * `EncryptedSharedPreferences` + Keystore-backed [MasterKey] implementation (SEC-02). File I/O is
 * synchronous/blocking, so every call is dispatched onto [dispatcherProvider]'s `io` dispatcher
 * (THR-02, rule 7 — main-safe at this boundary, callers don't need to know that).
 *
 * [MasterKey]/[EncryptedSharedPreferences] construction can throw [GeneralSecurityException] (key
 * generation/Keystore failure) or [IOException] (prefs file corruption/access failure). Per
 * ERR-01, those two expected infra exception types are caught at [sharedPreferences] and
 * re-wrapped as [SecureStorageUnavailableException]; on the read path ([getString]) that maps to
 * `null` (safe default, mirrors `DataStorePreferencesDataSource.getInt`'s `IOException` ->
 * `emptyPreferences()` mapping) rather than crashing the caller. Anything unexpected propagates.
 */
class EncryptedSharedPreferencesSecureStorage(
    private val context: Context,
    private val dispatcherProvider: DispatcherProvider
) : SecureStorage {

    override suspend fun putString(key: String, value: String) {
        withContext(dispatcherProvider.io) {
            sharedPreferences().edit().putString(key, value).apply()
        }
    }

    override suspend fun getString(key: String): String? =
        withContext(dispatcherProvider.io) {
            try {
                sharedPreferences().getString(key, null)
            } catch (cause: SecureStorageUnavailableException) {
                null
            }
        }

    override suspend fun remove(key: String) {
        withContext(dispatcherProvider.io) {
            sharedPreferences().edit().remove(key).apply()
        }
    }

    private fun sharedPreferences(): SharedPreferences {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedSharedPreferences.create(
                context,
                PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (cause: GeneralSecurityException) {
            throw SecureStorageUnavailableException(cause)
        } catch (cause: IOException) {
            throw SecureStorageUnavailableException(cause)
        }
    }
}

/** Safe/explicit failure signal for SEC-02's ERR-01 boundary — see [EncryptedSharedPreferencesSecureStorage]. */
class SecureStorageUnavailableException(cause: Throwable) : Exception(cause)
