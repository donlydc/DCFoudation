# Error & result model

[Back to architecture index](00-index.md)

## Scope

How infrastructure exceptions get mapped before they cross into presentation.

## Current implementation

No application-wide generic `Resource<T>` wrapper exists — feature state remains more expressive than a universal loading/success/error envelope.

- `DataStorePreferencesDataSource` catches expected read `IOException` at its boundary and emits empty preferences, so callers receive their declared default.
- `safeApiCall` maps expected `IOException`/Retrofit `HttpException`/serialization failures into `NetworkResult.Failure(AppError)`; unexpected/programming failures are rethrown. HTTP payload parsing is opt-in and feature-owned, so an unknown server body is not retained by default.
- `ArticlesRepository` keeps Room as source of truth, records refresh failures, preserves cached rows, and returns a success flag. `ArticlesViewModel` converts that outcome into durable `UiState` (`isShowingStaleData` plus `userMessage`) rather than exposing infrastructure exceptions or a transient message channel.

`:foundation` owns the intentionally small shared `AppError` vocabulary (`Connectivity`, `Http`, `Serialization`, `Unknown`) and `Retryable`. Feature-specific localized messages remain in feature presentation state; do not invent a generic `UiMessage` mapping in the generator before a real cross-feature policy requires one.

## Rules

### ERR-01 — Infrastructure exceptions are caught at the data-source boundary, not the ViewModel

**Rule:** A `*DataSource` catches its own infrastructure exceptions (`IOException`, network failures, etc.) and maps them to a safe default or a domain-level error type; a ViewModel never catches `IOException`/`SQLException`/HTTP-client exceptions directly.

**Why:** Keeps infra exception types out of presentation-layer code (DEP-04, INV-07).

**Verification:** Inspect `DataStorePreferencesDataSource` and `safeApiCall`; presentation code has no Retrofit, Room, SQL, or I/O exception imports.

## Testing / validation

`NetworkResultTest` covers success, I/O, HTTP, serialization and unexpected exception propagation. `NetworkClientFactoryTest` verifies policy headers/request IDs and timeouts against MockWebServer. `DataStorePreferencesDataSourceTest` covers expected read-I/O fallback through a deterministic `DataStore` contract double. `ArticlesViewModelTest` covers durable refresh-success/failure messages and acknowledgement.

## Related documents

- [`10-data/preferences-datastore.md`](10-data/preferences-datastore.md)
