# Network

[Back to data](README.md)

## Current implementation

`:data:network` owns business-neutral Retrofit/OkHttp construction, Kotlin serialization configuration, debug-only BASIC HTTP logging, and `NetworkResult`/`safeApiCall`. `NetworkClientConfig` centralizes timeouts, static headers and request-id injection without adding implicit retries; an optional feature-owned `HttpErrorBodyParser` may map a known error payload to a display-safe message. Expected I/O/HTTP/serialization failures are mapped before they reach the repository or ViewModel. The `articles` feature owns `ArticlesApiService`, DTOs and `ArticlesNetworkDataSource`.

## Error-response contract (MP:NET-05, lane B1 `7a8a804`)

`safeApiCall` maps `IOException` (any subclass) to `AppError.Connectivity`, `HttpException` (any non-2xx) to `AppError.Http(code, serverMessage, cause)` where `serverMessage` is the opt-in `HttpErrorBodyParser` result (null when the parser returns null or throws), `SerializationException` to `AppError.Serialization`, and rethrows `CancellationException` and every other throwable. Full table and sample: `modules/data-network.md` section 5 and the KDoc in `NetworkResult.kt`. Since lane C2 the `articles` repository propagates the `AppError` to the ViewModel (`AppError?`) instead of collapsing it to a Boolean. Retry (MP:NET-03) and auth (MP:NET-04) do not exist. `:data:network` no longer depends on `:observability` (lane B1). Tests: `NetworkErrorMappingTest` (NOT_RUN). `data/network/STRUCTURE.md` still points at `architecture/10-data/network.md`; the module page is `modules/data-network.md` (path drift, not edited here).

## Rules

**DAT-NET-01 — Feature API/DTO types stay feature-owned; generic client policy stays in `:data:network`.** A feature data source is the only feature type that calls its Retrofit service and maps expected infrastructure failures to `NetworkResult`.

## Related documents

- [`README.md`](README.md)
