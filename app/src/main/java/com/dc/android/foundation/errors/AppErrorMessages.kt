package com.dc.android.foundation.errors

import androidx.annotation.StringRes
import com.dc.android.foundation.R
import com.dc.android.foundation.core.AppError

/**
 * MP:CORE-03 presentation mapping: [AppError] -> durable, localized user message. Exhaustive
 * (no `else`) so a new [AppError] variant is a compile-time event here. Never renders
 * [AppError.cause] or [AppError.Http.serverMessage] (diagnostic-only, may contain sensitive data).
 */
@StringRes
fun AppError.toMessageRes(): Int = when (this) {
    is AppError.Connectivity -> R.string.error_connectivity
    is AppError.Http -> R.string.error_http
    is AppError.Serialization -> R.string.error_serialization
    is AppError.Storage -> R.string.error_storage
    is AppError.Unknown -> R.string.error_unknown
}
