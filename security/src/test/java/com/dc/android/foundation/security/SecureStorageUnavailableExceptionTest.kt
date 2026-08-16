package com.dc.android.foundation.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * [EncryptedSharedPreferencesSecureStorage] itself needs a real Android `Context`/Keystore to
 * construct a `MasterKey`/`EncryptedSharedPreferences`, so it is not feasibly unit-testable
 * outside instrumentation (same known gap class as the Phase Q worker-execution gap — see
 * `HANDOVER.md`). This covers the one piece of pure, Android-independent logic the class
 * introduces: wrapping an expected infra exception into the safe failure signal.
 */
class SecureStorageUnavailableExceptionTest {

    @Test
    fun `wraps a GeneralSecurityException cause`() {
        val cause = GeneralSecurityException("keystore unavailable")

        val exception = SecureStorageUnavailableException(cause)

        assertSame(cause, exception.cause)
    }

    @Test
    fun `wraps an IOException cause`() {
        val cause = IOException("prefs file corrupted")

        val exception = SecureStorageUnavailableException(cause)

        assertSame(cause, exception.cause)
    }

    @Test
    fun `message is not lost when wrapping`() {
        val cause = IOException("prefs file corrupted")

        val exception = SecureStorageUnavailableException(cause)

        assertEquals("prefs file corrupted", exception.cause?.message)
    }
}
