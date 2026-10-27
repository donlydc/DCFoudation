package com.dc.android.foundation.data.network

import com.dc.android.foundation.core.AppError
import java.io.IOException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

/**
 * Success/failure network result (DAT-NET-01, ERR-01).
 *
 * The existing `PreferencesDataSource` pattern maps an infra failure to a safe *empty default*
 * and swallows it. That shape does not fit network calls: a repository combining a remote source
 * with a local cache (DAT-CACHE-01) needs to distinguish "the network call actually returned
 * fresh data" from "the network call failed, keep showing what's cached" so the UI can surface a
 * stale-data indicator. A silently-substituted empty value would make a failed refresh
 * indistinguishable from "the server legitimately returned nothing," so a minimal sealed
 * success/failure wrapper is used here instead.
 */
sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Failure(val error: AppError) : NetworkResult<Nothing>
}

/**
 * Runs [block] and maps it to a [NetworkResult] at the data-source boundary (ERR-01).
 *
 * Error-response contract (NET-01/NET-05):
 *
 * | Thrown by [block]                              | Result                                                    |
 * |------------------------------------------------|-----------------------------------------------------------|
 * | none                                           | [NetworkResult.Success]                                   |
 * | [IOException] (DNS, connect, TLS, timeout, reset) | `Failure(AppError.Connectivity(cause))`                |
 * | [HttpException] (any non-2xx status)           | `Failure(AppError.Http(code, serverMessage, cause))`      |
 * | [SerializationException] (malformed 2xx body)  | `Failure(AppError.Serialization(cause))`                  |
 * | `CancellationException`                        | rethrown unchanged (structured concurrency is preserved)  |
 * | anything else                                  | rethrown: programming error, never silently swallowed     |
 *
 * `HttpException` is matched by type, never by status: the HTTP code is carried verbatim in
 * [AppError.Http.code] and retry classification belongs to [AppError], not to this function.
 *
 * [errorBodyParser] is the only place a server error payload is read. Its result becomes
 * [AppError.Http.serverMessage]; if the parser returns `null` or throws, `serverMessage` is `null`
 * (a faulty parser can never turn an HTTP failure into a crash). The raw body is never retained
 * or logged. The default [NoOpHttpErrorBodyParser] discards the payload.
 *
 * Client configuration (timeouts, shared headers, request id) is declared in [NetworkClientConfig];
 * retry is intentionally not part of either type.
 *
 * Sample (feature-owned data source):
 * ```
 * suspend fun fetchArticles(): NetworkResult<List<ArticleDto>> =
 *     safeApiCall(errorBodyParser = ArticlesErrorParser) { api.getPosts() }
 * ```
 */
suspend fun <T> safeApiCall(
    errorBodyParser: HttpErrorBodyParser = NoOpHttpErrorBodyParser,
    block: suspend () -> T
): NetworkResult<T> = try {
    NetworkResult.Success(block())
} catch (cause: IOException) {
    NetworkResult.Failure(AppError.Connectivity(cause))
} catch (cause: HttpException) {
    NetworkResult.Failure(
        AppError.Http(
            code = cause.code(),
            serverMessage = runCatching {
                errorBodyParser.parse(cause.response()?.errorBody())
            }.getOrNull(),
            cause = cause
        )
    )
} catch (cause: SerializationException) {
    NetworkResult.Failure(AppError.Serialization(cause))
}
