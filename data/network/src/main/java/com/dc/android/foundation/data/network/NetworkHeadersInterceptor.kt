package com.dc.android.foundation.data.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds app-wide headers and a request id only when a feature has not supplied its own value.
 * This keeps feature-specific protocol requirements authoritative while making correlation
 * available consistently at the transport boundary.
 */
internal class NetworkHeadersInterceptor(
    private val headers: Map<String, String>,
    private val requestIdHeader: String,
    private val requestIdProvider: RequestIdProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()

        headers.forEach { (name, value) ->
            if (request.header(name) == null) builder.header(name, value)
        }
        if (request.header(requestIdHeader) == null) {
            builder.header(requestIdHeader, requestIdProvider.nextRequestId())
        }

        return chain.proceed(builder.build())
    }
}
