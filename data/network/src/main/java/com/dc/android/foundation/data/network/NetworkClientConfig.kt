package com.dc.android.foundation.data.network

import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Supplies a request identifier without coupling the network module to an observability vendor. */
fun interface RequestIdProvider {
    fun nextRequestId(): String
}

/** Default request-id source for clients that do not receive one from an app-level correlation scope. */
data object UuidRequestIdProvider : RequestIdProvider {
    override fun nextRequestId(): String = UUID.randomUUID().toString()
}

/**
 * Explicit HTTP-client policy. Retry behavior is deliberately absent: only a feature knows
 * whether a request is safe to repeat.
 */
data class NetworkClientConfig(
    val isDebug: Boolean,
    val connectTimeout: Duration = 15.seconds,
    val readTimeout: Duration = 30.seconds,
    val writeTimeout: Duration = 30.seconds,
    val headers: Map<String, String> = emptyMap(),
    val requestIdHeader: String = "X-Request-Id",
    val requestIdProvider: RequestIdProvider = UuidRequestIdProvider
) {
    init {
        require(connectTimeout.inWholeMilliseconds > 0) {
            "connectTimeout must be at least one millisecond."
        }
        require(readTimeout.inWholeMilliseconds > 0) {
            "readTimeout must be at least one millisecond."
        }
        require(writeTimeout.inWholeMilliseconds > 0) {
            "writeTimeout must be at least one millisecond."
        }
        require(requestIdHeader.isNotBlank()) { "requestIdHeader must not be blank." }
        require(headers.keys.all(String::isNotBlank)) { "HTTP header names must not be blank." }
    }
}
