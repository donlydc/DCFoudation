# Concurrency & threading

[Back to architecture index](00-index.md)

## Scope

Dispatcher ownership policy and how ViewModels/repositories move work off the main thread.

## Current implementation

`:runtime:concurrency` (plain Kotlin/JVM module, no Android/DI dependency — INV-03) owns `DispatcherProvider` (`main`/`mainImmediate`/`io`/`default`) and `DefaultDispatcherProvider`. It also provides optional process/screen concurrency policies: `AppCoroutineScope`, dispatcher shortcuts, `RetryPolicy`, `SingleFlight`, atomic `JobRegistry` launch policies, `stateInWhileSubscribed`, and `throttleFirst`. The P0 `:testing` module owns `FakeDispatcherProvider`, keeping a test-only type out of production artifacts. `:app`'s `di/ConcurrencyModule.kt` provides the Hilt singleton dispatcher binding, while `DcApplication` owns the process-lifetime scope used for startup scheduling (ownership/shutdown contract: see THR-03 below) — see the cross-module-binding note in [`08-dependency-injection-hilt.md`](08-dependency-injection-hilt.md).

`GreetingViewModel` uses `dispatcherProvider.default` via `withContext(...)` around `greetingProvider.greetingFor(...)` — the computation itself is trivial today, but the pattern is what matters: no `GreetingViewModel` code references `kotlinx.coroutines.Dispatchers` directly. `viewModelScope` owns every coroutine the ViewModel launches; `DcApplication` is the only creator of the `AppCoroutineScope` (never cancelled in production; tests/embedders stop it with `cancel()`, after which `launch` is silently dropped; an uncaught `launch` failure goes to `onUncaughtException`, which must not throw, while siblings survive via `SupervisorJob`; `async` failures surface only through `await`); features use `viewModelScope` or receive this scope and never build their own. Receiver scopes in `:runtime:scheduling` (`DcAlarmReceiver`/`DcAlarmRestorer`) are static and class-owned because receivers have no DI; they are bounded by `goAsync` plus a 9 s timeout (PROPOSED deviation, DEC-A3-2). Nothing uses `GlobalScope`. Shared jobs preserve cancellation: retries never retry `CancellationException`, workers rethrow it, and one cancelled `SingleFlight` waiter does not cancel other waiters or the application-owned execution, a failing `SingleFlight` block does not cancel the caller's scope, and a cancelled flight scope yields `CancellationException`.

## Rules

### THR-01 — No `GlobalScope`

**Rule:** Coroutines are launched from `viewModelScope`, a lifecycle-aware scope, or an explicitly injected/documented application scope — never `GlobalScope`.

**Why:** Untracked coroutines outlive their owner and leak (INV/plan §3 rule 6).

**Verification:** `grep GlobalScope` across `app/src`, `runtime/`, `data/` — zero hits.

### THR-02 — Dispatcher access goes through `DispatcherProvider`

**Rule:** Code that needs a specific dispatcher takes a `DispatcherProvider`, not a hard-coded `Dispatchers.IO`/`Dispatchers.Default` reference.

**Why:** Makes dispatcher usage swappable/deterministic in tests (`FakeDispatcherProvider`) without `Dispatchers.setMain`-style global mutation for every dispatcher, not just `Main`.

**Verification:** Production `Dispatchers.*` use stays inside `DefaultDispatcherProvider`; `:testing` may use `Dispatchers.Main`/test dispatchers to install deterministic virtual time.

### THR-03 — `AppCoroutineScope` has one creator and is not cancelled in production (PROPOSED — pending Opus final review, DEC-A3-2)

**Rule:** The composition root (`DcApplication`) is the single creator of `AppCoroutineScope`; production code never cancels it; tests and embedders stop it with `cancel()`; the `onUncaughtException` handler must not throw.

**Why:** No Android callback guarantees process shutdown, so a production `close()` would be dead code, while a second creator breaks INV-06 (one owner for long-lived orchestration).

**Verification:** NOT_RUN as a mechanical check; reading shows one construction site (`DcApplication`). `AppCoroutineScopeTest` (5 tests, Gradle NOT_RUN) pins the cancel/handler behaviour.

## Testing / validation

`GreetingViewModelTest` uses `FakeDispatcherProvider(Dispatchers.Main)` (with `Dispatchers.Main` already swapped to a `StandardTestDispatcher` by `MainDispatcherRule`) so every coroutine the ViewModel launches — including the `withContext(dispatcherProvider.default)` hop — runs on the same virtual-time scheduler `runTest`/`advanceUntilIdle()` controls. `:runtime:concurrency:test` additionally covers retry ceilings/backoff, Flow throttling, keyed job cancellation/atomic idle launch, `SingleFlight` coalescing, immediate completion cleanup, and waiter cancellation (all Gradle NOT_RUN). Since lane A3 (`bb5ce19`) the contract tests use seeded interleavings (`kotlin.random.Random`, 100+ deterministic runs) and the `VirtualTimeScope` helper (`runTest` plus a scope on `StandardTestDispatcher(testScheduler)` cancelled in `finally`), with no real sleeps; `:testing` cannot be used inside `:runtime:concurrency` tests (dependency direction), so dispatcher fakes are declared in-test.

## Related documents

- [`14-background-work.md`](14-background-work.md)
- [`08-dependency-injection-hilt.md`](08-dependency-injection-hilt.md)
