package com.dc.android.foundation.core

/**
 * Stable infrastructure-error vocabulary shared by data and presentation boundaries.
 *
 * [cause] is diagnostic-only: presentation maps this type to a durable user message and must
 * never render or log the raw exception message. Keeping it nullable allows errors created from
 * policy without retaining an infrastructure exception.
 */
sealed interface AppError {
    val cause: Throwable?

    data class Connectivity(override val cause: Throwable? = null) : AppError

    data class Http(
        val code: Int,
        val serverMessage: String? = null,
        override val cause: Throwable? = null
    ) : AppError

    data class Serialization(override val cause: Throwable? = null) : AppError

    data class Storage(override val cause: Throwable? = null) : AppError

    data class Unknown(override val cause: Throwable? = null) : AppError
}
