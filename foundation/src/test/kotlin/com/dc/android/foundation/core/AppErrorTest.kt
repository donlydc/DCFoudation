package com.dc.android.foundation.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppErrorTest {

    @Test
    fun `retryability follows the shared transport policy`() {
        assertTrue(AppError.Connectivity().isRetryable)
        assertTrue(AppError.Http(code = 408).isRetryable)
        assertTrue(AppError.Http(code = 429).isRetryable)
        assertTrue(AppError.Http(code = 500).isRetryable)
        assertFalse(AppError.Http(code = 404).isRetryable)
        assertFalse(AppError.Serialization().isRetryable)
    }
}
