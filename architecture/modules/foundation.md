# MODULE: :foundation — core contracts
Status: DRAFT
Verified commit: UNKNOWN (source read at working-tree HEAD 2c0ae05; page not yet reviewed)
Last source audit: NOT_RUN
Test status: NOT_RUN (evidence: reports/baseline-verification.md; Part B1/B2 blocked Gradle)

Legends for `Status:` / `Test status:` and per-fact tags: [`TEMPLATE.md`](TEMPLATE.md). Tags used below: CONFIRMED_IN_CODE (read in source at 2c0ae05), PROPOSED (backlog, not implemented).

## 1. Identity and responsibility
- Owns: the shared infrastructure-error vocabulary `AppError` and its retryability policy `AppError.isRetryable`. CONFIRMED_IN_CODE
- Does not own: error-to-text/UI mapping (CORE-03, PROPOSED), `NetworkResult`/`safeApiCall` (`:data:network`), Android or HTTP-library types, DTOs, navigation events, locale strings (MP 4.1 "do not put here").
- Public package/API: `com.dc.android.foundation.core` - `AppError` (sealed interface: `Connectivity`, `Http(code, serverMessage)`, `Serialization`, `Storage`, `Unknown`; each has nullable `cause`), `AppError.isRetryable` (extension, file `Retryable.kt`; there is no `Retryable` type).
- Consumer(s): `:data:network` (`NetworkResult.Failure(AppError)`), `:runtime:scheduling` (`DcJobResult.Failure`, `DcJobState.Failed`, `DcWorker`), `:app` (`ArticlesSyncWorker`; `ArticlesViewModelTest` builds `AppError.Connectivity`).

## 2. Context and discovery
- Read first: [`00-index.md`](../00-index.md), [`11-error-and-result-model.md`](../11-error-and-result-model.md) (ERR-01), [`dependency-rules.md`](../dependency-rules.md).
- Main code: `foundation/src/main/kotlin/com/dc/android/foundation/core/AppError.kt`, `.../core/Retryable.kt`.
- Tests: `foundation/src/test/kotlin/com/dc/android/foundation/core/AppErrorTest.kt` (1 test method).
- Build config: `foundation/build.gradle.kts` (`dc.kotlin.jvm`, `dc.testing`; no `dependencies {}` block, so no project or library deps).
- CodeGraph entry symbols: `AppError`, `isRetryable`; callers: `NetworkResult.kt` (creates Connectivity/Http/Serialization), `DcWorker.kt` (creates Serialization/Unknown, maps all variants), `DcJobState.kt`, `ArticlesSyncWorker.kt` (creates Unknown).

## 3. Dependencies and boundaries
- Allowed: none (current edge table in [`dependency-rules.md`](../dependency-rules.md): `:foundation -> (none)`). Inbound edges from `:data:network`, `:runtime:scheduling`, `:app` are allowed there.
- Forbidden: any edge to `:app` (DEP-01); PROPOSED (not yet a rule): `:foundation` stays Android-free, based on the build fact that it applies `dc.kotlin.jvm`.
- Exceptions (DEC/KD): none for this module. KD-02 (boundary enforcement not automated) applies to every edge: [`22-known-deviations.md`](../22-known-deviations.md).

## 4. Data/control flow
```mermaid
flowchart LR
  IO[IOException / HttpException / SerializationException] --> Net[":data:network NetworkResult mapping"]
  Net --> AE[AppError]
  Thr[Throwable in DcWorker.run] --> W[":runtime:scheduling DcWorker.doWork"]
  W --> AE
  AE --> Retry[isRetryable]
  AE --> App[":app consumer: when(AppError)"]
  AE --> Kind["DcWorker kind string -> DcJobState.Failed"]
```
Refs: `NetworkResult.kt:21` (`Failure(error: AppError)`), `DcWorker.kt:37-68,87-93` (creation and `kind()` mapping), `DcJobState.kt:9`. In current source `isRetryable` is referenced only by its declaration and `AppErrorTest` (repo-wide grep): no production code uses it yet.

## 5. Contracts
- `AppError`: sealed, exhaustive `when` is possible; `cause: Throwable?` is diagnostic-only (KDoc) and must not be rendered or logged raw by presentation. No Android, HTTP-engine or UI types in signatures. CONFIRMED_IN_CODE
- `AppError.isRetryable`: `Connectivity` true; `Http` true for 408, 429, >=500; `Serialization`, `Storage`, `Unknown` false.
- Errors/cancellation: the module throws nothing and has no coroutine or Android dependency; cancellation is the callers' concern (`DcWorker` rethrows `CancellationException` before mapping).
- Threading/lifecycle: pure immutable data classes; no state, no lifecycle.
- Versioning/API policy: none written (CORE-04, PROPOSED).

## 6. DI, configuration and ownership
Not bound in any Hilt module: values are constructed at failure sites. No configuration or variants. `:app/.../di/` contains no `AppError` binding (ConcurrencyModule, DataStoreModule, DatabaseModule, NetworkModule, ObservabilityModule, SecurityModule).

## 7. Edge cases, security and performance
- `Http.serverMessage` and `cause` can carry server/infrastructure text; do not log or show them raw (SEC-01 intent, `AppError` KDoc). No policy enforces this beyond the KDoc (CORE-04, PROPOSED).
- `DcWorker.appErrorFrom` rebuilds errors from a kind string only: `Http` comes back as `code = 0` and `cause`/`serverMessage` are lost, so `isRetryable` on a restored `Http` is false.
- `AppError.Storage` has no production creation site in the repo outside `DcWorker`'s mapping (only `appErrorFrom` instantiates it); the data/storage modules do not emit it yet.
- Negligible size/perf cost.

## 8. Tests and evidence
- Unit: `AppErrorTest` - `isRetryable` for Connectivity, Http 408/429/500/404, Serialization. Not covered: Storage/Unknown, `cause` handling, `Http` 499/599 boundaries.
- Integration: none in-module. Consumer mapping is exercised indirectly by `NetworkResultTest` (`:data:network`).
- Instrumented: none.
- Commands: `./gradlew :foundation:test` (JVM module). CI runs `testDebugUnitTest`, which does not exist for JVM modules (baseline Part A drift D2).
- Test status: NOT_RUN (evidence: reports/baseline-verification.md, Part B: Gradle blocked at daemon-toolchain provisioning)
- Last verified result: none for the current HEAD.

## 9. Known debt / decisions
- KD-02 (edges unenforced). No module-specific KD/DEC.
- Backlog, all PROPOSED (MP 4.1): CORE-01 `AppError` exhaustiveness audit across app/network/storage/Worker (P0); CORE-02 unify `Outcome`/`Result` only if `NetworkResult` duplicates a domain result (P1, avoid a third result type); CORE-03 error-presentation mapping lives in presentation, test raw error -> UI message (P0); CORE-04 public-API policy, compatibility, KDoc (P1).
- Doc drift to resolve: see report R1.1a section D (11-error-and-result-model.md lists four variants and a `Retryable` type).

## 10. Safe extension guide
- Add a variant only with every exhaustive `when` updated in the same change: `DcWorker.kind()` / `appErrorFrom`, `NetworkResult` mapping, any consumer `when` (CodeGraph the `AppError` blast radius first).
- Keep the module free of strings, Android types and vendor exceptions. Sample consumer: `NetworkResult.Failure` in `:data:network`.
- Adding a `project(...)` dependency requires updating `dependency-rules.md`, `context-map.yaml`, `module-map.md` together.

## 11. AI task playbook
- Read -> Graph -> Propose -> Edit -> Test -> Update docs -> Handover.
- Read: this page, `11-error-and-result-model.md`, `AppError.kt`, `Retryable.kt`.
- Graph: `codegraph_explore "AppError isRetryable DcWorker NetworkResult DcJobState"`.
- Recipe (CORE-01): list every `when (error)` and constructor call, add missing-variant tests per consumer, then run `./gradlew :foundation:test :data:network:testDebugUnitTest :runtime:scheduling:testDebugUnitTest`.
- Done: exhaustive mappings, tests added with evidence under `reports/logs/`, this page and the 11-doc updated.

## 12. Change log and links
- 2026-10-27 - R1.1a - first DRAFT from source at 2c0ae05 ([`reports/wp/R1.1a.md`](../../reports/wp/R1.1a.md)).
- Links: [`context-map.yaml`](../context-map.yaml) entry `foundation`; rules INV-07, ERR-01; [`module-map.md`](../module-map.md).
