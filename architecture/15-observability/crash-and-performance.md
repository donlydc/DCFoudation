# Crash & performance

[Back to observability](README.md)

## Current implementation

`CrashReporter` interface (`observability/.../CrashReporter.kt` — `recordException(throwable)`, `log(message)` for breadcrumbs), bound in `app/.../di/ObservabilityModule.kt` via `provideCrashReporter`. Real call site: `ArticlesRepository.refresh()`'s existing `NetworkResult.Failure` branch (the ERR-01 boundary from Phase O) now also calls `crashReporter.recordException(result.throwable)` — an observability side-effect at an already-existing error boundary, not a new one.

No crash-reporting (Crashlytics, Sentry) or performance-tracing vendor has been chosen yet. Per `22-known-deviations.md` KD-05, the bound implementation is `LoggingCrashReporter`, a placeholder that forwards to `Logger.e`/`Logger.d` — matching `AndroidLogger.e()`'s own longstanding note that it "is where reporting would hook in." Swap it for a real vendor-backed implementation behind the same `CrashReporter` interface once a vendor decision is made — no call site should need to change.

## Rules

### OBS-CRASH-01 — Crash/error reporting goes through `CrashReporter`, never a vendor SDK directly

**Rule:** Feature code depends on the `CrashReporter` interface (`observability`), never on a crash-reporting vendor SDK directly. `recordException`/`log` arguments never contain a secret/token/PII or full content body (SEC-01) — the throwable and count/id/boolean/status context only.

**Why:** Keeps a future vendor swap confined to `ObservabilityModule`'s one `@Provides` function, mirroring `Logger`/`AndroidLogger` (OBS-01). Also gives an already-existing `NetworkResult.Failure`/ERR-01 boundary a genuine downstream consumer instead of the exception being silently dropped after `refresh()` returns `false`.

**Verification:** `grep` for a vendor crash-reporting import (e.g. `com.google.firebase.crashlytics`, `io.sentry`) outside a future vendor-specific `CrashReporter` implementation — should be zero hits today (KD-05: no vendor chosen). Manually re-check every new `recordException(`/`log(` call site, same discipline as SEC-01's log-call review.

**Exceptions/current status:** Per KD-05, the bound implementation (`LoggingCrashReporter`) is a placeholder with no real vendor backend — the contract and DI wiring are real, only the vendor is intentionally absent.

## Related documents

- [`README.md`](README.md), [`logging.md`](logging.md)
