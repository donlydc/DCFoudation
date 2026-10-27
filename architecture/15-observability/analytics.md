# Analytics

[Back to observability](README.md)

## Current implementation

`AnalyticsTracker` interface (`observability/.../AnalyticsTracker.kt` — `trackEvent(name, properties = emptyMap())`), bound in `app/.../di/ObservabilityModule.kt` via `provideAnalyticsTracker`. Real call site: `ArticlesViewModel.refresh()` tracks `"articles_refreshed"` on a successful refresh.

No analytics vendor has been chosen yet (Firebase Analytics, Amplitude, etc.) — per `22-known-deviations.md` KD-05, the bound implementation is `LoggingAnalyticsTracker`, a placeholder that forwards every event to `Logger.d` (so it is visible in logcat during development) rather than a vendor SDK or a pure no-op. Swap it for a real vendor-backed implementation behind the same `AnalyticsTracker` interface once a vendor decision is made — no call site should need to change.

## Rules

### OBS-ANALYTICS-01 — Analytics events go through `AnalyticsTracker`, never a vendor SDK directly

**Rule:** Feature code depends on the `AnalyticsTracker` interface (`observability`), never on an analytics vendor SDK directly. `trackEvent`'s `properties` map never contains a secret/token/PII or full content body (SEC-01) — count/id/boolean/status values only.

**Why:** Keeps a future vendor swap confined to `ObservabilityModule`'s one `@Provides` function, mirroring `Logger`/`AndroidLogger` (OBS-01).

**Verification:** `grep` for a vendor analytics import (e.g. `com.google.firebase.analytics`, `com.amplitude`) outside a future vendor-specific `AnalyticsTracker` implementation — should be zero hits today (KD-05: no vendor chosen). Manually re-check every new `trackEvent(` call site's `properties` argument, same discipline as SEC-01's log-call review.

**Exceptions/current status:** Per KD-05 (still Open), the bound implementation (`LoggingAnalyticsTracker`) is a placeholder with no real vendor backend — the contract and DI wiring are real, only the vendor is intentionally absent. Since lane B2 it passes the event name and properties through `Redactor` and swallows `Logger` failures (OBS-02, PROPOSED DEC-B2-2, pending Opus final review); any vendor adapter must keep that isolation. Redaction is a safety net, not permission to pass sensitive properties.

## Related documents

- [`README.md`](README.md), [`logging.md`](logging.md)
