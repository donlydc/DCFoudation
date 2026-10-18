package com.dc.android.foundation.runtime.scheduling.work

import com.dc.android.foundation.core.AppError

sealed interface DcJobState {
    data object Enqueued : DcJobState
    data class Running(val progress: Float?) : DcJobState
    data object Succeeded : DcJobState
    data class Failed(val error: AppError?) : DcJobState
    data object Cancelled : DcJobState
}
