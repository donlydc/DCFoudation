package com.dc.android.foundation.security

import com.dc.android.foundation.runtime.concurrency.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * MP:SEC-01 read/write/remove/overwrite/failure tests for [EncryptedSharedPreferencesSecureStorage]
 * via its internal preferences-provider seam (fake [android.content.SharedPreferences]; the real
 * Keystore-backed construction remains instrumentation-only). Run status: NOT_RUN.
 * There is intentionally no `clear()` test: the [SecureStorage] contract has no clear (see B2 report).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EncryptedSharedPreferencesSecureStorageTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val dispatchers = object : DispatcherProvider {
        override val main: CoroutineDispatcher = dispatcher
        override val mainImmediate: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
    }

    private fun storage(prefs: FakeSharedPreferences) =
        EncryptedSharedPreferencesSecureStorage(dispatchers) { prefs }

    private fun unavailable(cause: Throwable) =
        EncryptedSharedPreferencesSecureStorage(dispatchers) { throw SecureStorageUnavailableException(cause) }

    @Test
    fun putThenGet_roundTrips() = runTest {
        val s = storage(FakeSharedPreferences())
        s.putString("token", "v1")
        assertEquals("v1", s.getString("token"))
    }

    @Test
    fun get_missingKey_returnsNull() = runTest {
        assertNull(storage(FakeSharedPreferences()).getString("absent"))
    }

    @Test
    fun put_existingKey_overwrites() = runTest {
        val s = storage(FakeSharedPreferences())
        s.putString("k", "old")
        s.putString("k", "new")
        assertEquals("new", s.getString("k"))
    }

    @Test
    fun remove_deletesOnlyThatKey() = runTest {
        val s = storage(FakeSharedPreferences())
        s.putString("a", "1")
        s.putString("b", "2")
        s.remove("a")
        assertNull(s.getString("a"))
        assertEquals("2", s.getString("b"))
    }

    @Test
    fun remove_missingKey_isNoOp() = runTest {
        storage(FakeSharedPreferences()).remove("absent")
    }

    @Test
    fun get_whenStoreUnavailable_returnsNull_forGeneralSecurityAndIo() = runTest {
        assertNull(unavailable(GeneralSecurityException("keystore")).getString("k"))
        assertNull(unavailable(IOException("corrupt")).getString("k"))
    }

    @Test
    fun get_whenValueUndecryptable_returnsNull() = runTest {
        val prefs = FakeSharedPreferences(failOnGet = SecurityException("Could not decrypt value"))
        assertNull(storage(prefs).getString("k"))
    }

    @Test
    fun get_unexpectedException_propagates() = runTest {
        val prefs = FakeSharedPreferences(failOnGet = IllegalStateException("bug"))
        try {
            storage(prefs).getString("k")
            fail("expected IllegalStateException")
        } catch (expected: IllegalStateException) {
            assertEquals("bug", expected.message)
        }
    }

    @Test
    fun put_whenStoreUnavailable_throwsSecureStorageUnavailable() = runTest {
        val cause = GeneralSecurityException("keystore")
        try {
            unavailable(cause).putString("k", "v")
            fail("expected SecureStorageUnavailableException")
        } catch (expected: SecureStorageUnavailableException) {
            assertTrue(expected.cause === cause)
        }
    }

    @Test
    fun remove_whenStoreUnavailable_throwsSecureStorageUnavailable() = runTest {
        try {
            unavailable(IOException("corrupt")).remove("k")
            fail("expected SecureStorageUnavailableException")
        } catch (expected: SecureStorageUnavailableException) {
            // contract: writes surface the failure explicitly
        }
    }
}
