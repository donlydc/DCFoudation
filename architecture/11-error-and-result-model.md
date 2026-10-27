# Error & result model

[Back to architecture index](00-index.md)

## Scope

How infrastructure exceptions get mapped before they cross into presentation.

## Current implementation

No application-wide generic `Resource<T>` wrapper exists — feature state remains more expressive than a universal loading/success/error envelope.

- `DataStorePreferencesDataSource` catches expected read `IOException` at its boundary and emits empty preferences, so callers receive their declared default.
- `safeApiCall` maps expected `IOException`/Retrofit `HttpException`/serialization failures into `NetworkResult.Failure(AppError)`; unexpected/programming failures are rethrown. HTTP payload parsing is opt-in and feature-owned, so an unknown server body is not retained by default.
- `ArticlesRepository` keeps Room as source of truth, records refresh failures, preserves cached rows, and returns a success flag. `ArticlesViewModel` converts that outcome into durable `UiState` (`isShowingStaleData` plus `userMessage`) rather than exposing infrastructure exceptions or a transient message channel.

`:foundation` owns the intentionally small shared `AppError` vocabulary (`Connectivity`, `Http`, `Serialization`, `Storage`, `Unknown`) and the retry classification `AppError.isRetryable` (extension property in `Retryable.kt`; there is no `Retryable` type): true for `Connectivity` and for `Http` 408/429/>=500, false for `Serialization`, `Storage`, `Unknown`. `AppError.cause` is diagnostic only, kept by reference and part of `equals`; the data-class `toString` prints it, so never log `"$error"` (redacting `toString` is a follow-up). Exhaustiveness policy (PROPOSED — pending Opus final review, DEC-A3-8): `when (error)` has no `else` branch; today's two sites are `Retryable.kt` and `DcWorker.workKind` (`:runtime:scheduling`), and adding a variant updates both in the same change. Across WorkManager only a kind string survives (`workKind`/`errorFromKind`): `Http` returns with code 0 and no `cause`/`serverMessage`, so a restored `Http` is not retryable; this lossiness is by design and pinned per variant by `DcWorkContractTest`. `Storage` has no producer yet. MP:CORE-03 (raw error -> semantic UI message tests in app consumers) is still open; `ArticlesRepository` collapses errors to a Boolean today. Feature-specific localized messages remain in feature presentation state; do not invent a generic `UiMessage` mapping in the generator before a real cross-feature policy requires one.

## Rules

### ERR-01 — Infrastructure exceptions are caught at the data-source boundary, not the ViewModel

**Rule:** A `*DataSource` catches its own infrastructure exceptions (`IOException`, network failures, etc.) and maps them to a safe default or a domain-level error type; a ViewModel never catches `IOException`/`SQLException`/HTTP-client exceptions directly.

**Why:** Keeps infra exception types out of presentation-layer code (DEP-04, INV-07).

**Verification:** Inspect `DataStorePreferencesDataSource` and `safeApiCall`; presentation code has no Retrofit, Room, SQL, or I/O exception imports.

## Testing / validation

`AppErrorTest` (12 tests after lane A3, Gradle NOT_RUN) pins variants, retry boundaries and cause handling. `NetworkErrorMappingTest` (lane B1, 14 `@Test`, NOT_RUN) pins the full transport/HTTP/serialization/cancellation mapping, and `safeApiCall` does not classify retries (`AppError.isRetryable` in `:foundation` does). Lane C2 added `AppError.toMessageRes()` (`app/.../errors/AppErrorMessages.kt`, MP:CORE-03) mapping each variant to a localized message, with `AppErrorMessagesTest`. `NetworkResultTest` covers success, I/O, HTTP, serialization and unexpected exception propagation. `NetworkClientFactoryTest` verifies policy headers/request IDs and timeouts against MockWebServer. `DataStorePreferencesDataSourceTest` covers expected read-I/O fallback through a deterministic `DataStore` contract double. `ArticlesViewModelTest` covers durable refresh-success/failure messages and acknowledgement.

## Related documents

- [`10-data/preferences-datastore.md`](10-data/preferences-datastore.md)
