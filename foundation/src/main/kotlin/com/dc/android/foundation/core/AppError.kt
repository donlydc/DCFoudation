package com.dc.android.foundation.core

/**
 * Stable infrastructure-error vocabulary shared by data and presentation boundaries.
 *
 * [cause] is diagnostic-only: presentation maps this type to a durable user message and must
 * never render or log the raw exception message. Keeping it nullable allows errors created from
 * policy without retaining an infrastructure exception.
 *
 * Contract notes:
 * - `when (error)` over this type should stay exhaustive (no `else`) so a new variant is a
 *   compile-time event for every consumer.
 * - [cause] is held by reference and takes part in `equals`/`hashCode`; the generated `toString`
 *   prints it, so do not log `"$error"` — log the variant and [Throwable] separately instead.
 * - Retry classification lives in [isRetryable]; consumers must not re-derive it from [cause].
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
