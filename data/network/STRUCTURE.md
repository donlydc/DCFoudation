# `:data:network`

**Role:** reusable HTTP client and network-result infrastructure.

**Owns:** `NetworkClientFactory`, explicit client/request-id policy, `NetworkResult`, and `safeApiCall`.

## AI guidance

- Keep endpoint DTOs/services feature-owned unless multiple features genuinely share them.
- Translate expected transport failures here; never leak Retrofit/OkHttp exceptions into UI contracts.
- Logging must satisfy SEC-01: no secrets, tokens, response bodies, or PII.
- Read `architecture/10-data/network.md` and `11-error-and-result-model.md` before boundary changes.
- Verify with `:data:network:testDebugUnitTest`.

Implementation guide: [`IMPLEMENTATION.md`](IMPLEMENTATION.md).
