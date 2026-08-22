package com.dc.android.foundation.core

/** Central retry policy so features do not infer retryability from transport exceptions. */
val AppError.isRetryable: Boolean
    get() = when (this) {
        is AppError.Connectivity -> true
        is AppError.Http -> code == 408 || code == 429 || code >= 500
        is AppError.Serialization,
        is AppError.Storage,
        is AppError.Unknown -> false
    }
