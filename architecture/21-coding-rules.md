# Coding rules

[Back to architecture index](00-index.md)

## Scope

General conventions that don't have a more specific home in another document.

## Rules already established elsewhere (indexed here for discoverability)

- Composition over inheritance / no `BaseViewModel` — UI-03 (`05-presentation-udf.md`).
- Constructor injection by default — DI-01 (`08-dependency-injection-hilt.md`).
- No hard-coded dispatcher — THR-02 (`09-concurrency-and-threading.md`).
- No dependency version outside the catalog — BLD-01 (`18-build-logic-and-dependencies.md`).

## New rules

### CODE-01 — Package-private-by-default is not enforced by Kotlin, so keep classes `internal`/small-surface where nothing outside the module needs them

**Rule:** A class only the owning module needs (e.g. `DataStorePreferencesDataSource`, `AndroidLogger`) is not marked `internal` yet because these modules don't currently distinguish public API from implementation detail at the Kotlin visibility level — flagged as a real gap, not silently ignored. Revisit once a module has an actual external consumer whose API surface needs locking down (a Kotlin `explicitApi()` mode is the natural next step for `:runtime:concurrency`/`:data:preferences` at that point).

**Why:** Premature `internal` visibility on a 4-file module with one consumer (`:app`) adds friction without a real payoff yet.

**Verification:** N/A — documented intent, not a mechanically checked rule today.

## Related documents

- [`05-presentation-udf.md`](05-presentation-udf.md), [`08-dependency-injection-hilt.md`](08-dependency-injection-hilt.md), [`09-concurrency-and-threading.md`](09-concurrency-and-threading.md), [`18-build-logic-and-dependencies.md`](18-build-logic-and-dependencies.md)
