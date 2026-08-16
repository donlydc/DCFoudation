package com.dc.android.foundation.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

/**
 * Generic, business-neutral Retrofit/OkHttp construction (DAT-NET-01). Owns no feature-specific
 * types (no DTOs/API interfaces here) — those live next to the feature that needs them, same as
 * `PreferencesDataSource` staying generic while `GreetingPreferencesRepository` adds vocabulary.
 */
object NetworkClientFactory {

    private val defaultJson = Json { ignoreUnknownKeys = true }

    /**
     * [isDebug] gates the logging interceptor entirely (never runs in release) and even in debug
     * only logs at [HttpLoggingInterceptor.Level.BASIC] (method/URL/response code) — never
     * `HEADERS` or `BODY`, so no auth header or request/response content is ever logged (SEC-01).
     */
    fun createOkHttpClient(isDebug: Boolean): OkHttpClient =
        OkHttpClient.Builder()
            .apply {
                if (isDebug) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
                    )
                }
            }
            .build()

    fun createRetrofit(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        json: Json = defaultJson
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
