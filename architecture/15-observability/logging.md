# Logging

[Back to observability](README.md)

## Current implementation

`app/.../observability/Logger.kt` — `d(tag, message)` / `e(tag, message, throwable?)`. `AndroidLogger` (real impl) gates `d()` on `AppEnvironment.DEBUG` (`16-config-feature-flags-environments.md`) so a release build's logcat isn't spammed; `e()` always logs. Bound via `di/ObservabilityModule.kt`. Used by `GreetingViewModel.refresh()` — one real call site, not just plumbing.

## Rules

### OBS-01 — Feature code depends on `Logger`, never `android.util.Log` directly

**Rule:** `import android.util.Log` only appears inside `AndroidLogger.kt`.

**Why:** Keeps a future vendor logger (Timber, a crash-reporting SDK's logger) a one-file swap.

**Verification:** `grep 'android.util.Log'` across `app/src` outside `observability/AndroidLogger.kt` — zero hits.

See also SEC-01 (`12-security-and-session.md`) — never log a secret/token/PII through this interface.

### Redaction safety net (lane B2, MP:SEC-04 / MP:OBS-01)

`observability/.../Redactor.kt` (pure Kotlin) masks sensitive-looking map keys and secret-shaped text (Bearer/Basic, JWT, e-mail, inline `key=value`). `LoggingAnalyticsTracker` and `LoggingCrashReporter.log` apply it. It is heuristic and defence in depth only: callers still own SEC-01, `AndroidLogger` does not redact, and the Bearer/Basic pattern can produce false positives on ordinary text. Tests: `RedactorTest`, `SecretLeakAndIsolationTest` (written, NOT_RUN).

### OBS-02 isolation (PROPOSED — pending Opus final review; DEC-B2-2)

Telemetry is best-effort: `Logging*` swallow `Exception` thrown by the `Logger` so a failing sink never crashes the caller (`Error`s propagate). Future logger/vendor adapters follow the same rule.

## Testing / validation

Lane B2 added JVM tests in `:observability` (`RedactorTest`, `SecretLeakAndIsolationTest`, `ThrowingLogger`); status NOT_RUN. `GreetingViewModelTest` passes a `NoOpLogger` fake — proves `Logger` is a real constructor dependency, not a global singleton reached via `Log.d` directly.

## Related documents

- [`../12-security-and-session.md`](../12-security-and-session.md) (SEC-01)
- [`../16-config-feature-flags-environments.md`](../16-config-feature-flags-environments.md)
