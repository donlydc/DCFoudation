package com.dc.android.foundation.runtime.scheduling.work

import androidx.work.ListenableWorker
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlinx.serialization.KSerializer

/** A typed declaration of one unique WorkManager job. */
data class DcJobSpec<I : Any>(
    val name: String,
    val worker: KClass<out ListenableWorker>,
    val input: I? = null,
    val inputSerializer: KSerializer<I>? = null,
    val constraints: DcConstraints = DcConstraints.None,
    val retry: DcRetryPolicy = DcRetryPolicy.Default,
    val initialDelay: Duration = Duration.ZERO,
    val expedited: Boolean = false,
    val tags: Set<String> = emptySet(),
) {
    init {
        require(name.isNotBlank()) { "A scheduled job needs a non-blank name." }
        require(input == null || inputSerializer != null) {
            "A non-null job input requires its KSerializer."
        }
    }
}
