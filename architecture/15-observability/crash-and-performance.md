# Crash & performance

[Back to observability](README.md)

## Current implementation

**Not implemented.** No crash-reporting (Crashlytics, Sentry) or performance-tracing vendor has been chosen. `AndroidLogger.e()` writes to logcat only — it does not report anywhere. Will follow the same DC-owned-interface pattern as [`logging.md`](logging.md) once a vendor is decided.

## Rules

**OBS-CRASH-01 — Reserved.** No rule yet.

## Related documents

- [`README.md`](README.md), [`logging.md`](logging.md)
