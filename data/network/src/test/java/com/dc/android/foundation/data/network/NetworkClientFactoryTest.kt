package com.dc.android.foundation.data.network

import kotlin.time.Duration.Companion.milliseconds
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NetworkClientFactoryTest {

    private val server = MockWebServer()

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `client adds shared headers and a request id without overriding feature headers`() {
        server.enqueue(MockResponse().setBody("ok"))
        val client = NetworkClientFactory.createOkHttpClient(
            NetworkClientConfig(
                isDebug = false,
                headers = mapOf("X-App-Version" to "1", "X-Region" to "default"),
                requestIdProvider = RequestIdProvider { "request-123" }
            )
        )

        client.newCall(
            Request.Builder()
                .url(server.url("/articles"))
                .header("X-Region", "feature")
                .build()
        ).execute().use { response -> assertEquals(200, response.code) }

        val request = requireNotNull(server.takeRequest())
        assertEquals("1", request.getHeader("X-App-Version"))
        assertEquals("feature", request.getHeader("X-Region"))
        assertEquals("request-123", request.getHeader("X-Request-Id"))
    }

    @Test
    fun `client applies configured timeouts`() {
        val client = NetworkClientFactory.createOkHttpClient(
            NetworkClientConfig(
                isDebug = false,
                connectTimeout = 1200.milliseconds,
                readTimeout = 2300.milliseconds,
                writeTimeout = 3400.milliseconds
            )
        )

        assertEquals(1200, client.connectTimeoutMillis)
        assertEquals(2300, client.readTimeoutMillis)
        assertEquals(3400, client.writeTimeoutMillis)
    }
}
