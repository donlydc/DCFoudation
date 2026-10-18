package com.dc.android.foundation.data.network

import com.dc.android.foundation.core.AppError
import java.io.IOException
import kotlinx.serialization.SerializationException
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
        assertEquals(AppError.Connectivity(cause), (result as NetworkResult.Failure).error)
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
        assertEquals(
            AppError.Http(code = 500, serverMessage = null, cause = cause),
            (result as NetworkResult.Failure).error
        )
    }

    @Test
    fun `safeApiCall uses an explicit error-body parser`() = runTest {
        val response = Response.error<String>(
            422,
            "display-safe message".toResponseBody("text/plain".toMediaType())
        )

        val result = safeApiCall<String>(
            errorBodyParser = HttpErrorBodyParser { body -> body?.string() }
        ) { throw HttpException(response) }

        assertEquals(
            "display-safe message",
            (result as NetworkResult.Failure).error.let { (it as AppError.Http).serverMessage }
        )
    }

    @Test
    fun `safeApiCall maps serialization failures`() = runTest {
        val cause = SerializationException("invalid payload")

        val result = safeApiCall<String> { throw cause }

        assertEquals(AppError.Serialization(cause), (result as NetworkResult.Failure).error)
    }

    @Test(expected = IllegalStateException::class)
    fun `safeApiCall rethrows unexpected exceptions`() = runTest {
        safeApiCall<String> { throw IllegalStateException("programming error") }
    }
}
