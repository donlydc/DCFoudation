package com.dc.android.foundation.testing

import com.dc.android.foundation.runtime.scheduling.DcScheduleOutcome
import com.dc.android.foundation.runtime.scheduling.DcScheduleRequest
import com.dc.android.foundation.runtime.scheduling.DcScheduler
import com.dc.android.foundation.runtime.scheduling.DcScheduledItem
import com.dc.android.foundation.runtime.scheduling.DcTrigger
import com.dc.android.foundation.runtime.scheduling.work.DcJobState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Deterministic scheduler fake for feature ViewModel tests; it never touches Android framework APIs. */
class FakeScheduler : DcScheduler {
    data class ScheduleCommand(
        val id: String,
        val trigger: DcTrigger,
        val request: DcScheduleRequest,
    )

    val scheduleCommands = mutableListOf<ScheduleCommand>()
    val cancelledIds = mutableListOf<String>()
    var nextOutcome: DcScheduleOutcome = DcScheduleOutcome.Scheduled
    private val states = mutableMapOf<String, MutableStateFlow<DcJobState>>()

    override suspend fun schedule(
        id: String,
        trigger: DcTrigger,
        request: DcScheduleRequest,
    ): DcScheduleOutcome {
        scheduleCommands += ScheduleCommand(id, trigger, request)
        return nextOutcome
    }

    override suspend fun cancel(id: String) {
        cancelledIds += id
    }

    override suspend fun scheduled(): List<DcScheduledItem> = emptyList()

    override fun observe(id: String): Flow<DcJobState> =
        states.getOrPut(id) { MutableStateFlow(DcJobState.Enqueued) }.asStateFlow()

    fun emitState(id: String, state: DcJobState) {
        states.getOrPut(id) { MutableStateFlow(DcJobState.Enqueued) }.value = state
    }
}
