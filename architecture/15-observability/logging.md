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

## Testing / validation

`GreetingViewModelTest` passes a `NoOpLogger` fake — proves `Logger` is a real constructor dependency, not a global singleton reached via `Log.d` directly.

## Related documents

- [`../12-security-and-session.md`](../12-security-and-session.md) (SEC-01)
- [`../16-config-feature-flags-environments.md`](../16-config-feature-flags-environments.md)
