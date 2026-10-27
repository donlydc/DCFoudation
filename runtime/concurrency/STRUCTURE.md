# `:runtime:concurrency`

**Role:** business-neutral coroutine dispatcher contracts and reusable concurrency policies.

**Owns:**
- `DispatcherProvider` / `DefaultDispatcherProvider` — dispatcher access (THR-02).
- `AppCoroutineScope` — the process-lifetime scope that replaces `GlobalScope` (THR-01).
- `onIo` / `onDefault` / `flowOnIo` — `DispatcherProvider` shorthands for `withContext`/`flowOn`.
- `RetryPolicy` + `retrying` (suspend) + `Flow.retrying` — one backoff policy, two surfaces.
- `SingleFlight` — collapses concurrent calls sharing a key into one execution.
- `JobRegistry` — named `launchLatest`/`launchIfIdle` jobs scoped to one `CoroutineScope`.
- `stateInWhileSubscribed` / `WhileUiSubscribed` / `throttleFirst` — Flow-to-UDF sharing helpers.

## AI guidance

- Keep this module plain Kotlin/JVM and free of Android, Hilt, feature, and data dependencies.
- Every helper here must be optional at the call site — no type a feature is forced to adopt.
  Deleting this module should still leave call sites compilable against raw `kotlinx.coroutines`.
- Never swallow `CancellationException`: `retrying`, `SingleFlight`, and `JobRegistry` all rethrow
  or propagate it untouched — verify this stays true for any new helper added here.
- `SingleFlight`'s and `JobRegistry`'s shared work runs on the `CoroutineScope` passed to the
  constructor, never on an individual caller's coroutine — that's what lets one caller cancel
  without cancelling the shared execution or other waiters. Preserve that when extending either.
- Test utilities (fakes) belong in `:testing`; only real production types live here.
- Read `architecture/09-concurrency-and-threading.md` before changing dispatcher contracts.
- Verify with `:runtime:concurrency:test`.

Implementation guide: [`IMPLEMENTATION.md`](IMPLEMENTATION.md).
