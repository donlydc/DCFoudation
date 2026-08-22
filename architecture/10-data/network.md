# Network

[Back to data](README.md)

## Current implementation

`:data:network` owns business-neutral Retrofit/OkHttp construction, Kotlin serialization configuration, debug-only BASIC HTTP logging, and `NetworkResult`/`safeApiCall`. The `articles` feature owns `ArticlesApiService`, DTOs and `ArticlesNetworkDataSource`; expected I/O/HTTP failures are mapped before they reach the repository or ViewModel.

## Rules

**DAT-NET-01 — Feature API/DTO types stay feature-owned; generic client policy stays in `:data:network`.** A feature data source is the only feature type that calls its Retrofit service and maps expected infrastructure failures to `NetworkResult`.

## Related documents

- [`README.md`](README.md)
