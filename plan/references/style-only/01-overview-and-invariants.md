# Overview & non-negotiable invariants

[Back to architecture index](../ARCHITECTURE.md)

## What this module is

A generic device-to-device connectivity SDK for Android. It owns everything from the GATT/RFCOMM
radio up to a transport-neutral, typed RPC surface — and nothing above it.

- **Namespace:** `com.dc.peerlink.connectivity`
- **minSdk 23 · compileSdk 37 · JDK 17 · Kotlin 2.2.10**
- **Published as:** `com.dc.peerlink:ble-connectivity` (AAR + sources)
- **Language mix:** Kotlin throughout; Java survives only in `core/` for the Nordic BLE session,
  GATT plumbing, and legacy tools.

### The one idea

Both endpoints share a single contract that exposes **immutable bytes and lifecycle only**
(`ConnectionsTransport`, `sdk/ConnectionContracts.kt`). GATT types, protocol frames, and business
models never cross that boundary. Everything above it — protocol decode, RPC correlation, service
dispatch — is symmetric between the two roles.

Roles are `INITIATOR` / `ACCEPTOR` (`ConnectionEndpointRole`), deliberately not client/server,
because a device may initiate one session and accept the next.

## Non-negotiable invariants

These rules are ranked by how expensive the resulting bug is to find.

| # | Invariant | Why |
|---|---|---|
| I1 | **A receiver must never validate an inbound REQUEST against its own `connectionEpoch`/`connectionGeneration`.** Those counters are local, per-device, originator-scoped, and echoed on the wire. | Validating them on the receive path rejects legitimate calls from a peer whose counters differ. |
| I2 | **Reject stale async callbacks and late packets when generation/epoch no longer matches.** | Prevents replies from a previous session matching requests from the current one. |
| I3 | **Exactly one owner coordinates scan, connect, reconnect, and teardown.** | Multiple owners create hardware-only races. |
| I4 | **Never hold a lock while invoking business code, resuming a continuation, or sending a frame.** | Prevents deadlocks under concurrent RPC load. |
| I5 | **Handle RPC `RESPONSE` immediately on the ingress path**, not through the service queue. | Queueing responses behind inbound dispatch can deadlock the in-flight cap. |
| I6 | **Admission is not delivery.** `Accepted` / `ConnectionSendResult.Accepted` mean local admission only. | Treating admission as acknowledgement or remote processing causes silent data loss. |
| I7 | **Wire contracts are frozen once released:** service ids, method ids, handshake prefix bytes, and frame layouts. | Changing them breaks already-deployed peers. |
| I8 | **BLE GATT never carries stream or file-transfer payload.** It is control/RPC only in every mode. | Bulk data belongs to local Wi-Fi or capability-gated Classic. |
