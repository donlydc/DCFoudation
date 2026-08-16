package com.dc.android.foundation.security

/**
 * Business-neutral encrypted key-value storage contract (SEC-02). Feature code depends on this,
 * never on `EncryptedSharedPreferences`/`MasterKey` directly — keeps the encryption mechanism
 * swappable behind the contract.
 *
 * This is explicitly NOT `PreferencesDataSource` (`:data:preferences`) — that contract is for
 * non-sensitive app preferences (see `12-security-and-session.md` SEC-02). Use [SecureStorage]
 * only for a credential/token/session id, once one exists to store.
 */
interface SecureStorage {
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String): String?
    suspend fun remove(key: String)
}
