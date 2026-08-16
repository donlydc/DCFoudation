# Background work

[Back to architecture index](00-index.md)

## Scope

Persistent/retriable background work policy (WorkManager vs. a plain coroutine).

## Current implementation (Phase Q)

`articles` gets the real BG-01 use case DEC-04 set out to manufacture: `ArticlesSyncWorker` periodically calls `ArticlesRepository.refresh()` so cached data stays fresh even when the app isn't in the foreground, surviving app closure/process death with WorkManager's own retry/backoff guarantees.

**Module split — `:runtime:background` vs `:app`:**

- `:runtime:background` owns `PeriodicSyncScheduler`, a generic, business-neutral helper (`enqueue(context, workerClass, uniqueWorkName, repeatIntervalMinutes)`) that wraps `PeriodicWorkRequest` + `WorkManager.enqueueUniquePeriodicWork` with this repo's standard policy: `NetworkType.CONNECTED` constraint, `ExistingPeriodicWorkPolicy.KEEP` so re-enqueuing on every process start doesn't reset the schedule. It has no Hilt import and no knowledge of `articles` — same "framework-agnostic library, `:app` does the DI wiring" split as `:runtime:concurrency`/`:data:preferences`.
- `ArticlesSyncWorker` (the actual `@HiltWorker class : CoroutineWorker`) lives in `app/src/main/java/com/dc/android/foundation/articles/`, not `:runtime:background`. Reason: `@HiltWorker`/`@AssistedInject` need the same Hilt component graph as `ArticlesRepository`, and `ArticlesRepository` is itself an `:app`-only type (it was never extracted to a library module). Hilt-DI-annotated classes live only in `:app` per this repo's established convention — forcing the real worker into `:runtime:background` would mean either duplicating `ArticlesRepository`'s fetch-and-upsert logic or making `:runtime:background` depend on `:app`-only application code, both worse than the current split.
- `DcApplication` implements `Configuration.Provider`, providing a Hilt-injected `HiltWorkerFactory` (`androidx.hilt:hilt-work` standard pattern). `AndroidManifest.xml`'s default `androidx.work.WorkManagerInitializer` startup entry is removed (`tools:node="remove"`) so WorkManager only ever sees this `Configuration`.
- `DcApplication.onCreate()` calls `PeriodicSyncScheduler.enqueue(...)` to schedule `ArticlesSyncWorker` as unique periodic work named `"articles-sync"`, repeating every **15 minutes** — WorkManager's own minimum periodic interval; chosen because this is a reference/demo sync job, not a latency-sensitive one.
- On success, `ArticlesSyncWorker` posts a best-effort "Articles synced" notification (skipped silently, never a crash, if `POST_NOTIFICATIONS` isn't granted — see `13-permissions.md` PERM-01). On network failure, `doWork()` returns `Result.retry()` so WorkManager's backoff policy is the retry guarantee; on success it returns `Result.success()`.

## Rules

### BG-01 — Persistent/retriable work uses WorkManager via a `CoroutineWorker`; screen-scoped work stays a `viewModelScope` coroutine

**Rule:** Work that must survive app closure/process death with retry guarantees is a `CoroutineWorker` (optionally `@HiltWorker` for DI), scheduled through `PeriodicSyncScheduler` (`:runtime:background`) or an equivalent thin WorkManager wrapper — never a raw `GlobalScope`/detached coroutine. Work that only needs to live as long as a screen (e.g. `GreetingViewModel`'s refresh, `ArticlesViewModel`'s manual `Refresh` action) stays a `viewModelScope` coroutine; it does not get promoted to a `Worker` just because a `Worker` also exists in the app. `@HiltWorker`/`@AssistedInject` Worker classes that need Hilt-DI-annotated application types (repositories, etc.) live in `:app`; only framework-agnostic scheduling/policy helpers live in `:runtime:background`.

**Why:** WorkManager exists for guarantees a coroutine scope can't give (survives process death, persisted retry/backoff, constraint-gated execution) — using it for screen-scoped work is unnecessary overhead and indirection; not using it for the sync job would silently drop syncs whenever the app isn't running, defeating the whole point of DEC-04's `articles` sync use case.

**Verification:** `grep -r "GlobalScope" app/src runtime/background/src` is empty. `ArticlesSyncWorker` is the only `CoroutineWorker` in the repo and lives under `app/src/main/java/com/dc/android/foundation/articles/`, not `runtime/background/src`. `runtime/background/src/main/java/.../PeriodicSyncScheduler.kt` has no `dagger`/`javax.inject` import beyond what's already a transitive dependency declaration (none currently used). `DcApplication.kt` implements `Configuration.Provider` and `AndroidManifest.xml` has the `tools:node="remove"` block for `WorkManagerInitializer`.

**Exceptions:** None yet.

## Testing / validation

`ArticlesSyncWorker.doWork()` is not unit- or instrumented-tested here: exercising it for real requires either a `WorkManager` `TestDriver`/instrumented test (needs an emulator/device, unavailable in this environment) or hand-rolling a fake that bypasses `CoroutineWorker`'s framework wiring, which would test the fake more than the worker. This is a known, documented gap — same honesty pattern as `GreetingScreenTest`'s instrumented tests never having run here. `ArticlesRepository.refresh()` (the logic the worker calls) is already covered by `ArticlesViewModelTest`, so the worker's one genuinely untested seam is the thin `doWork()` → `Result` mapping and the best-effort notification post.

## Related documents

- [`09-concurrency-and-threading.md`](09-concurrency-and-threading.md)
- [`13-permissions.md`](13-permissions.md) (PERM-01 — the notification this worker posts)
- [`23-decision-log.md`](23-decision-log.md) (DEC-04 — why this use case exists)
