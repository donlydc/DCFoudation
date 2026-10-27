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
 * ERR-01, those two expected infra exception types are caught in `createEncryptedPreferences` and
 * re-wrapped as [SecureStorageUnavailableException]; on the read path ([getString]) that maps to
 * `null` (safe default, mirrors `DataStorePreferencesDataSource.getInt`'s `IOException` ->
 * `emptyPreferences()` mapping) rather than crashing the caller. Anything unexpected propagates.
 */
class EncryptedSharedPreferencesSecureStorage internal constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val preferencesProvider: () -> SharedPreferences
) : SecureStorage {

    constructor(context: Context, dispatcherProvider: DispatcherProvider) :
        this(dispatcherProvider, { createEncryptedPreferences(context) })

    /** Failure contract: throws [SecureStorageUnavailableException] if the store cannot be opened. */
    override suspend fun putString(key: String, value: String) {
        withContext(dispatcherProvider.io) {
            preferencesProvider().edit().putString(key, value).apply()
        }
    }

    /**
     * Returns `null` when the store cannot be opened ([SecureStorageUnavailableException]) or the
     * stored value can no longer be decrypted (e.g. Keystore key invalidated -> `SecurityException`
     * from `EncryptedSharedPreferences`). The store is never silently cleared or re-keyed here
     * (no migration/backup plan, MP 4.9); "undecryptable" is surfaced as "absent".
     */
    override suspend fun getString(key: String): String? =
        withContext(dispatcherProvider.io) {
            try {
                preferencesProvider().getString(key, null)
            } catch (cause: SecureStorageUnavailableException) {
                null
            } catch (cause: SecurityException) {
                null
            }
        }

    /** Failure contract: throws [SecureStorageUnavailableException] if the store cannot be opened. */
    override suspend fun remove(key: String) {
        withContext(dispatcherProvider.io) {
            preferencesProvider().edit().remove(key).apply()
        }
    }

    private companion object {
        fun createEncryptedPreferences(context: Context): SharedPreferences {
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
}

/** Safe/explicit failure signal for SEC-02's ERR-01 boundary — see [EncryptedSharedPreferencesSecureStorage]. */
class SecureStorageUnavailableException(cause: Throwable) : Exception(cause)
