package com.dc.android.foundation.data.network

import com.dc.android.foundation.core.AppError
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.GET

/**
 * NET-01 mapping contract: HTTP codes, transport failures, serialization, cancellation and
 * error-body parsing, first as pure mapping and then through real Retrofit + MockWebServer.
 */
class NetworkErrorMappingTest {

    @Serializable
    internal data class Item(val id: Int)

    internal interface ItemApi {
        @GET("items")
        suspend fun items(): List<Item>
    }

    private val server = MockWebServer()

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun httpException(code: Int, body: String = "error"): HttpException =
        HttpException(Response.error<String>(code, body.toResponseBody("text/plain".toMediaType())))

    private suspend fun errorOf(block: suspend () -> Unit): AppError =
        (safeApiCall(block = block) as NetworkResult.Failure).error

    // --- pure mapping -----------------------------------------------------------------------

    @Test
    fun `every non-success HTTP status maps to AppError Http with the same code`() = runTest {
        listOf(400, 401, 403, 404, 408, 409, 422, 429, 500, 502, 503, 504).forEach { code ->
            val cause = httpException(code)

            val error = errorOf { throw cause }

            assertEquals(AppError.Http(code = code, serverMessage = null, cause = cause), error)
        }
    }

    @Test
    fun `transport failures map to Connectivity`() = runTest {
        listOf(
            UnknownHostException("dns"),
            SocketTimeoutException("timeout"),
            IOException("reset")
        ).forEach { cause ->
            assertEquals(AppError.Connectivity(cause), errorOf { throw cause })
        }
    }

    @Test
    fun `serialization failure maps to Serialization`() = runTest {
        val cause = SerializationException("bad payload")

        assertEquals(AppError.Serialization(cause), errorOf { throw cause })
    }

    @Test
    fun `cancellation is rethrown and never mapped`() = runTest {
        var rethrown: Throwable? = null
        try {
            safeApiCall<String> { throw CancellationException("cancelled") }
        } catch (cause: CancellationException) {
            rethrown = cause
        }

        assertTrue(rethrown is CancellationException)
    }

    @Test
    fun `parser result becomes serverMessage and parser sees the error body`() = runTest {
        val error = errorOf2(
            parser = HttpErrorBodyParser { body -> body?.string()?.uppercase() },
            cause = httpException(422, "bad name")
        ) as AppError.Http

        assertEquals("BAD NAME", error.serverMessage)
        assertEquals(422, error.code)
    }

    @Test
    fun `parser returning null leaves serverMessage null`() = runTest {
        val error = errorOf2(HttpErrorBodyParser { null }, httpException(400)) as AppError.Http

        assertNull(error.serverMessage)
    }

    @Test
    fun `throwing parser never escapes and leaves serverMessage null`() = runTest {
        val error = errorOf2(
            HttpErrorBodyParser { throw IllegalStateException("parser bug") },
            httpException(500)
        ) as AppError.Http

        assertEquals(500, error.code)
        assertNull(error.serverMessage)
    }

    @Test
    fun `default parser discards the payload`() = runTest {
        val error = errorOf { throw httpException(500, "secret-token-123") } as AppError.Http

        assertNull(error.serverMessage)
    }

    private suspend fun errorOf2(parser: HttpErrorBodyParser, cause: HttpException): AppError =
        (safeApiCall<String>(errorBodyParser = parser) { throw cause } as NetworkResult.Failure).error

    // --- Retrofit + MockWebServer -----------------------------------------------------------

    private fun api(config: NetworkClientConfig = NetworkClientConfig(isDebug = false)): ItemApi {
        val client = NetworkClientFactory.createOkHttpClient(config)
            .newBuilder()
            .retryOnConnectionFailure(false)
            .build()
        return NetworkClientFactory.createRetrofit(server.url("/").toString(), client)
            .create(ItemApi::class.java)
    }

    @Test
    fun `mock 200 returns Success`() = runBlocking {
        server.enqueue(MockResponse().setBody("""[{"id":1},{"id":2}]"""))

        val result = safeApiCall { api().items() }

        assertEquals(NetworkResult.Success(listOf(Item(1), Item(2))), result)
    }

    @Test
    fun `mock 404 with JSON body returns Http failure with parsed message`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"message":"not found"}"""))

        val result = safeApiCall(
            errorBodyParser = HttpErrorBodyParser { body ->
                body?.string()?.substringAfter("\"message\":\"")?.substringBefore("\"")
            }
        ) { api().items() }

        val error = (result as NetworkResult.Failure).error as AppError.Http
        assertEquals(404, error.code)
        assertEquals("not found", error.serverMessage)
    }

    @Test
    fun `mock 503 without parser returns Http failure`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(503).setBody("maintenance"))

        val error = (safeApiCall { api().items() } as NetworkResult.Failure).error

        assertTrue(error is AppError.Http)
        assertEquals(503, (error as AppError.Http).code)
        assertNull(error.serverMessage)
    }

    @Test
    fun `mock malformed 200 body returns Serialization failure`() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"not":"a list"}"""))

        val error = (safeApiCall { api().items() } as NetworkResult.Failure).error

        assertTrue(error is AppError.Serialization)
    }

    @Test
    fun `mock dropped connection returns Connectivity failure`() = runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val error = (safeApiCall { api().items() } as NetworkResult.Failure).error

        assertTrue(error is AppError.Connectivity)
    }

    @Test
    fun `mock read timeout returns Connectivity failure`() = runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val error = (
            safeApiCall { api(NetworkClientConfig(isDebug = false, readTimeout = 150.milliseconds)).items() }
                as NetworkResult.Failure
            ).error

        assertTrue(error is AppError.Connectivity)
        assertTrue((error as AppError.Connectivity).cause is IOException)
    }
}
