package com.dc.android.foundation.platform.permission

/**
 * Lifecycle-free requester contract (MP:PERM-02). The framework-glue layer (Route/Activity) owns
 * the real launcher and implements this; ViewModels/features depend only on the interface.
 */
interface PermissionRequester {
    /** Current status without prompting. */
    suspend fun check(permission: String): PermissionStatus

    /** Launches the OS request (user-initiated contexts only) and returns the resulting status. */
    suspend fun request(permission: String): PermissionStatus
}

/**
 * Guards against double request (e.g. rapid taps, rotation re-entry). State lives in the owner
 * (ViewModel) so it survives configuration change; not thread-safe, call from one dispatcher.
 */
class PermissionRequestTracker {
    private val inFlight = mutableSetOf<String>()

    /** true if the caller should launch the request; false if one is already in flight. */
    fun tryBegin(permission: String): Boolean = inFlight.add(permission)

    fun complete(permission: String) {
        inFlight.remove(permission)
    }

    fun isInFlight(permission: String): Boolean = permission in inFlight
}

/** Scriptable [PermissionRequester] for tests and previews. */
class FakePermissionRequester(
    private val initial: Map<String, PermissionStatus> = emptyMap(),
    private val onRequest: (String, PermissionStatus) -> PermissionStatus = { _, _ -> PermissionStatus.Granted }
) : PermissionRequester {
    private val statuses = initial.toMutableMap()
    val requests = mutableListOf<String>()

    override suspend fun check(permission: String): PermissionStatus =
        statuses[permission] ?: PermissionStatus.NotRequested

    override suspend fun request(permission: String): PermissionStatus {
        requests += permission
        val result = onRequest(permission, check(permission))
        statuses[permission] = result
        return result
    }
}
