# `:observability`

**Role:** vendor-neutral logging, analytics, and crash-reporting contracts/adapters.

**Owns:** `Logger`, `AnalyticsTracker`, `CrashReporter`, and logging-backed implementations.

## AI guidance

- Keep public contracts vendor-neutral; vendor SDK bindings belong at the app composition root.
- Enforce SEC-01 at every call site: no credentials, tokens, secrets, or PII.
- Event names/properties must be stable and business-appropriate; do not log raw payloads.
- Read `architecture/15-observability/` and `12-security-and-session.md` before API changes.
- Verify module compilation and affected feature tests.

Implementation guide: [`IMPLEMENTATION.md`](IMPLEMENTATION.md).
