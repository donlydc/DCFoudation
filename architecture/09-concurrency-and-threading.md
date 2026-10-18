# Concurrency & threading

[Back to architecture index](00-index.md)

## Scope

Dispatcher ownership policy and how ViewModels/repositories move work off the main thread.

## Current implementation

`:runtime:concurrency` (plain Kotlin/JVM module, no Android/DI dependency — INV-03) owns `DispatcherProvider` (`main`/`mainImmediate`/`io`/`default`) and `DefaultDispatcherProvider`. It also provides optional process/screen concurrency policies: `AppCoroutineScope`, dispatcher shortcuts, `RetryPolicy`, `SingleFlight`, atomic `JobRegistry` launch policies, `stateInWhileSubscribed`, and `throttleFirst`. The P0 `:testing` module owns `FakeDispatcherProvider`, keeping a test-only type out of production artifacts. `:app`'s `di/ConcurrencyModule.kt` provides the Hilt singleton dispatcher binding, while `DcApplication` owns the process-lifetime scope used for startup scheduling — see the cross-module-binding note in [`08-dependency-injection-hilt.md`](08-dependency-injection-hilt.md).

`GreetingViewModel` uses `dispatcherProvider.default` via `withContext(...)` around `greetingProvider.greetingFor(...)` — the computation itself is trivial today, but the pattern is what matters: no `GreetingViewModel` code references `kotlinx.coroutines.Dispatchers` directly. `viewModelScope` owns every coroutine the ViewModel launches; `DcApplication` owns its explicit `AppCoroutineScope`; nothing uses `GlobalScope`. Shared jobs preserve cancellation: retries never retry `CancellationException`, workers rethrow it, and one cancelled `SingleFlight` waiter does not cancel other waiters or the application-owned execution.

## Rules

### THR-01 — No `GlobalScope`

**Rule:** Coroutines are launched from `viewModelScope`, a lifecycle-aware scope, or an explicitly injected/documented application scope — never `GlobalScope`.

**Why:** Untracked coroutines outlive their owner and leak (INV/plan §3 rule 6).

**Verification:** `grep GlobalScope` across `app/src`, `runtime/`, `data/` — zero hits.

### THR-02 — Dispatcher access goes through `DispatcherProvider`

**Rule:** Code that needs a specific dispatcher takes a `DispatcherProvider`, not a hard-coded `Dispatchers.IO`/`Dispatchers.Default` reference.

**Why:** Makes dispatcher usage swappable/deterministic in tests (`FakeDispatcherProvider`) without `Dispatchers.setMain`-style global mutation for every dispatcher, not just `Main`.

**Verification:** Production `Dispatchers.*` use stays inside `DefaultDispatcherProvider`; `:testing` may use `Dispatchers.Main`/test dispatchers to install deterministic virtual time.

## Testing / validation

`GreetingViewModelTest` uses `FakeDispatcherProvider(Dispatchers.Main)` (with `Dispatchers.Main` already swapped to a `StandardTestDispatcher` by `MainDispatcherRule`) so every coroutine the ViewModel launches — including the `withContext(dispatcherProvider.default)` hop — runs on the same virtual-time scheduler `runTest`/`advanceUntilIdle()` controls. `:runtime:concurrency:test` additionally covers retry ceilings/backoff, Flow throttling, keyed job cancellation/atomic idle launch, `SingleFlight` coalescing, immediate completion cleanup, and waiter cancellation.

## Related documents

- [`14-background-work.md`](14-background-work.md)
- [`08-dependency-injection-hilt.md`](08-dependency-injection-hilt.md)
