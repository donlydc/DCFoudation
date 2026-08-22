package com.dc.android.foundation.data.network

import com.dc.android.foundation.core.AppError
import java.io.IOException
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
 * Runs [block] and maps it to a [NetworkResult] at the data-source boundary (ERR-01):
 * [IOException] and [HttpException] are the expected/anticipated failure modes for a network call
 * and are mapped to the stable [AppError] vocabulary here. Anything else is an
 * unexpected/programming-error condition and is rethrown rather than silently swallowed.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): NetworkResult<T> = try {
    NetworkResult.Success(block())
} catch (cause: IOException) {
    NetworkResult.Failure(AppError.Connectivity(cause))
} catch (cause: HttpException) {
    NetworkResult.Failure(
        AppError.Http(
            code = cause.code(),
            serverMessage = cause.message(),
            cause = cause
        )
    )
}
