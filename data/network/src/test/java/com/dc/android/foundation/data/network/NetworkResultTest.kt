package com.dc.android.foundation.data.network

import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/** No real network call here — pure logic around the ERR-01 boundary mapping (DAT-NET-01). */
class NetworkResultTest {

    @Test
    fun `safeApiCall wraps a successful result`() = runTest {
        val result = safeApiCall { "ok" }

        assertTrue(result is NetworkResult.Success)
        assertEquals("ok", (result as NetworkResult.Success).data)
    }

    @Test
    fun `safeApiCall maps IOException to Failure`() = runTest {
        val cause = IOException("no connectivity")

        val result = safeApiCall<String> { throw cause }

        assertTrue(result is NetworkResult.Failure)
        assertEquals(cause, (result as NetworkResult.Failure).throwable)
    }

    @Test
    fun `safeApiCall maps HttpException to Failure`() = runTest {
        val response = Response.error<String>(
            500,
            "error".toResponseBody("text/plain".toMediaType())
        )
        val cause = HttpException(response)

        val result = safeApiCall<String> { throw cause }

        assertTrue(result is NetworkResult.Failure)
        assertEquals(cause, (result as NetworkResult.Failure).throwable)
    }

    @Test(expected = IllegalStateException::class)
    fun `safeApiCall rethrows unexpected exceptions`() = runTest {
        safeApiCall<String> { throw IllegalStateException("programming error") }
    }
}
