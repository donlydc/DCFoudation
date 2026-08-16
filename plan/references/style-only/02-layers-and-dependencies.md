# Layers & dependency rules

[Back to architecture index](../ARCHITECTURE.md)

## Layer map

```text
                       ┌──────────────────────────────────────────────┐
   PUBLIC API          │ sdk/connectivity/   unified facade (current) │
                       │ sdk/                legacy attach path       │
                       └──────────────────────────────────────────────┘
                                          │
                       ┌──────────────────────────────────────────────┐
   RPC + CONTRACTS     │ sdk/rpc/  sdk/service/  sdk/runtime/         │
                       │ sdk/control/                                 │
                       └──────────────────────────────────────────────┘
                                          │
                       ══════ ConnectionsTransport (bytes + lifecycle) ══════
                                          │
                       ┌──────────────────────────────────────────────┐
   PHYSICAL            │ core/ble/**   core/classic/**   core/session/ │
                       │ core/v2/dataplane/   core/stream/            │
                       └──────────────────────────────────────────────┘

   CROSS-CUTTING       transport/   tools/   utils/
   LEGACY RESIDUE      common/   mapping/
```

## Dependency rules

These are checkable review rules.

**D1 — `core/` must not depend on `sdk/` except for the transport SPI.**
`core/classic/*` legitimately imports `sdk.Connection*` types because `ConnectionsTransport` is a
port defined in the upper layer and implemented in the lower one. That is the only permitted upward
import. Imports from `core/` to `sdk/connectivity/`, `sdk/service/`, or `sdk/runtime/` violate the
boundary.

One tolerated exception exists: `core/classic/ClassicConnectionsTransport.kt` imports
`sdk.rpc.BleRpcExecutionContext`. Do not extend this pattern.

**D2 — Nothing in `core/` or `sdk/` may import business/domain types.** No Metering models and no
`EventProto`. See [Known deviations](06-known-deviations.md#actionflags-leaks-metering-ids-into-core)
for the existing breach.

**D3 — Consumers depend on `sdk/`, never on `core/`.** If a consumer needs something from `core/`,
expose it through the appropriate `sdk/` contract.

**D4 — `transport/`, `utils/`, and `tools/` are leaves.** They may not import `core/` or `sdk/`.

**D5 — Public API types must not leak transport specifics.** No `BluetoothGatt*`, Nordic types, or
`android.bluetooth.*` in a public `sdk/` signature.
