package com.dc.android.foundation.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AppErrorTest {

    /**
     * Compile-time exhaustiveness guard (MP:CORE-01): there is deliberately no `else` branch, so
     * adding an [AppError] variant breaks compilation of this test until every consumer list in
     * this file (and the `when` sites named in architecture/modules/foundation.md) is revisited.
     */
    private fun AppError.variantName(): String = when (this) {
        is AppError.Connectivity -> "Connectivity"
        is AppError.Http -> "Http"
        is AppError.Serialization -> "Serialization"
        is AppError.Storage -> "Storage"
        is AppError.Unknown -> "Unknown"
    }

    private val oneOfEachVariant: List<AppError> = listOf(
        AppError.Connectivity(),
        AppError.Http(code = 500),
        AppError.Serialization(),
        AppError.Storage(),
        AppError.Unknown(),
    )

    @Test
    fun `retryability follows the shared transport policy`() {
        assertTrue(AppError.Connectivity().isRetryable)
        assertTrue(AppError.Http(code = 408).isRetryable)
        assertTrue(AppError.Http(code = 429).isRetryable)
        assertTrue(AppError.Http(code = 500).isRetryable)
        assertFalse(AppError.Http(code = 404).isRetryable)
        assertFalse(AppError.Serialization().isRetryable)
    }

    @Test
    fun `every variant is covered by the exhaustive mapping and has a distinct name`() {
        val names = oneOfEachVariant.map { it.variantName() }

        assertEquals(5, names.size)
        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun `storage and unknown are never retryable`() {
        assertFalse(AppError.Storage().isRetryable)
        assertFalse(AppError.Unknown().isRetryable)
        assertFalse(AppError.Storage(IOException("disk")).isRetryable)
        assertFalse(AppError.Unknown(IllegalStateException("bug")).isRetryable)
    }

    @Test
    fun `serialization is not retryable even with a transport-looking cause`() {
        assertFalse(AppError.Serialization(IOException("truncated body")).isRetryable)
    }

    @Test
    fun `connectivity is retryable regardless of cause`() {
        assertTrue(AppError.Connectivity(null).isRetryable)
        assertTrue(AppError.Connectivity(IOException("offline")).isRetryable)
    }

    @Test
    fun `http retryability at status boundaries`() {
        val retryable = listOf(408, 429, 500, 501, 502, 503, 504, 599, 600)
        val notRetryable = listOf(Int.MIN_VALUE, -1, 0, 100, 200, 301, 400, 401, 403, 404, 407, 409, 410, 422, 428, 430, 499)

        retryable.forEach { code ->
            assertTrue("HTTP $code should be retryable", AppError.Http(code = code).isRetryable)
        }
        notRetryable.forEach { code ->
            assertFalse("HTTP $code should not be retryable", AppError.Http(code = code).isRetryable)
        }
    }

    @Test
    fun `http retryability ignores server message and cause`() {
        assertFalse(AppError.Http(code = 404, serverMessage = "try again later", cause = IOException()).isRetryable)
        assertTrue(AppError.Http(code = 503, serverMessage = null, cause = null).isRetryable)
    }

    @Test
    fun `cause defaults to null for every variant`() {
        oneOfEachVariant.forEach { error ->
            assertNull("${error.variantName()} must allow policy-created errors without a cause", error.cause)
        }
        assertNull(AppError.Http(code = 404).serverMessage)
    }

    @Test
    fun `cause is retained by reference for every variant`() {
        val failure = IOException("diagnostic only")

        val errors = listOf(
            AppError.Connectivity(failure),
            AppError.Http(code = 500, cause = failure),
            AppError.Serialization(failure),
            AppError.Storage(failure),
            AppError.Unknown(failure),
        )

        errors.forEach { assertSame(it.variantName(), failure, it.cause) }
    }

    @Test
    fun `http keeps the server message separate from the cause`() {
        val failure = IllegalStateException("cause text")
        val error = AppError.Http(code = 422, serverMessage = "Name is required", cause = failure)

        assertEquals(422, error.code)
        assertEquals("Name is required", error.serverMessage)
        assertSame(failure, error.cause)
    }

    @Test
    fun `equality includes the cause so identical policy errors are equal`() {
        val failure = IOException("x")

        assertEquals(AppError.Connectivity(), AppError.Connectivity())
        assertEquals(AppError.Connectivity(failure), AppError.Connectivity(failure))
        assertNotEquals(AppError.Connectivity(failure), AppError.Connectivity())
        assertNotEquals(AppError.Http(code = 500), AppError.Http(code = 503))
        assertNotEquals(AppError.Storage(), AppError.Unknown())
    }

    @Test
    fun `retryability is determined by the variant not by instance identity`() {
        val first = AppError.Http(code = 503)
        val second = AppError.Http(code = 503)

        assertEquals(first.isRetryable, second.isRetryable)
        assertTrue(first.isRetryable)
    }
}
