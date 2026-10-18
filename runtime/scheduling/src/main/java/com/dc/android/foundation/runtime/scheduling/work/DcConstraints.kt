package com.dc.android.foundation.runtime.scheduling.work

enum class NetworkRequirement { None, Connected, Unmetered }

/** Compact, reusable WorkManager constraint policy. */
data class DcConstraints(
    val network: NetworkRequirement = NetworkRequirement.None,
    val requiresCharging: Boolean = false,
    val requiresBatteryNotLow: Boolean = false,
    val requiresStorageNotLow: Boolean = false,
    val requiresDeviceIdle: Boolean = false,
) {
    companion object {
        val None = DcConstraints()
        val Network = DcConstraints(network = NetworkRequirement.Connected)
        val Sync = DcConstraints(
            network = NetworkRequirement.Unmetered,
            requiresCharging = true,
        )
    }
}
