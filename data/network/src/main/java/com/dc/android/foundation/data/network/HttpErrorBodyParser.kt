package com.dc.android.foundation.data.network

import okhttp3.ResponseBody

/**
 * Feature-provided decoder for an HTTP error payload. The returned message must be short,
 * display-safe and must never be logged as a raw server body.
 */
fun interface HttpErrorBodyParser {
    fun parse(errorBody: ResponseBody?): String?
}

/** Default policy: do not retain an unknown server payload. */
data object NoOpHttpErrorBodyParser : HttpErrorBodyParser {
    override fun parse(errorBody: ResponseBody?): String? = null
}
