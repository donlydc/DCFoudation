# Dependency injection — Hilt

[Back to architecture index](00-index.md)

## Scope

How Hilt is wired, where bindings live, and how Compose reaches injected dependencies. Does not own presentation/UDF shape (see [`05-presentation-udf.md`](05-presentation-udf.md), Phase E).

## Current implementation (Phase D)

Versions (verified via Maven Central `maven-metadata.xml`, not guessed): `com.google.dagger:hilt-android:2.60.1` + `hilt-compiler:2.60.1` via KSP `2.2.10-2.0.2` (matches the project's Kotlin `2.2.10`), `androidx.hilt:hilt-lifecycle-viewmodel-compose:1.4.0` — the navigation-independent `hiltViewModel()` (the `androidx.hilt:hilt-navigation-compose` one is now deprecated in favor of this; using the lean artifact also avoids pulling in Navigation before Phase E actually needs it).

- `app/src/main/java/.../DcApplication.kt` — `@HiltAndroidApp`, registered as `android:name=".DcApplication"` in `AndroidManifest.xml`. This is the Hilt entry point for the whole app.
- `MainActivity.kt` — `@AndroidEntryPoint` (field injection only where Android construction requires it, per the plan's DI rules — an `Activity` can't be constructor-injected).
- `greeting/GreetingProvider.kt` — `GreetingProvider` interface + `DefaultGreetingProvider` with `@Inject constructor()`. A deliberately trivial collaborator: proves constructor injection without inventing a fake repository/network/database (Phase G doesn't exist yet).
- `greeting/GreetingModule.kt` — `@Module @InstallIn(SingletonComponent::class)` with one `@Binds`. Lives next to `GreetingProvider` in the same `greeting` package, not in a shared `AppModule`.
- `greeting/GreetingViewModel.kt` — `@HiltViewModel` with `@Inject constructor(greetingProvider: GreetingProvider)`. Obtained in `MainActivity` via `hiltViewModel()` (`androidx.hilt.lifecycle.viewmodel.compose`), state read via `collectAsStateWithLifecycle()` (`androidx.lifecycle.compose`, requires the `androidx.compose.runtime.getValue` import for the `by` delegate to resolve on `State<T>`).

This `greeting` feature is intentionally minimal — Phase E replaces `GreetingViewModel`'s raw `StateFlow<String>` with the full Route/UiState/UiAction/UiEffect contract; the DI wiring proven here (module-local `@Binds`, `@HiltViewModel`, `hiltViewModel()`) carries forward unchanged.

## Rules

### DI-01 — Constructor injection is the default acquisition mechanism

**Rule:** Classes declare dependencies via `@Inject constructor(...)`. Field injection is used only where the Android framework instantiates the class (`Activity`/`Fragment`/`Application` via `@AndroidEntryPoint`/`@HiltAndroidApp`).

**Why:** Explicit, testable dependency graphs (INV-04).

**Verification:** `grep` for `@Inject` on fields vs constructors; every non-framework class should use the constructor form.

### DI-02 — Bindings live next to the interface/impl they wire

**Rule:** A `@Module` binding an interface to an implementation is colocated with that interface/impl's package, not collected into one shared module file.

**Why:** Avoids a monolithic multi-thousand-line `AppModule` (plan's explicit anti-pattern).

**Verification:** `GreetingModule.kt` sits in `greeting/` next to `GreetingProvider.kt` — check every future `@Module` the same way.

**Exceptions:** None yet.

### DI-03 — Compose content does not resolve dependencies itself

**Rule:** Composables receive a ViewModel via `hiltViewModel()` at the screen root (or state/callbacks from a parent), never a `@Module`-bound type or Hilt `EntryPoint` directly.

**Why:** Keeps UI previewable/testable and prevents infrastructure coupling in Compose (INV-02, DEP-03).

**Verification:** `Greeting()` (the reusable composable) takes a plain `String`, not a `GreetingProvider` or `GreetingViewModel` — only `MainActivity`'s `setContent` block calls `hiltViewModel()`.

### DI-04 — Hilt `EntryPoint`s are an escape hatch, not ordinary access

**Rule:** Use `@EntryPoint`/`EntryPointAccessors` only where a non-Hilt-managed class (e.g. a `ContentProvider`, or a third-party framework callback) needs a dependency and cannot be constructor-injected.

**Why:** Keeps the graph legible through normal injection instead of manual lookups scattered around.

**Verification:** None exist yet — flag any future `@EntryPoint` for a documented reason here.

## Testing / validation

- `./gradlew :app:assembleDebug` — exercises full Hilt/KSP component generation (Gate D).
- `grep '@Inject'` — confirm every hit is on a constructor, not a field, outside `MainActivity`/`DcApplication`.

## Related documents

- [`02-layers-and-dependencies.md`](02-layers-and-dependencies.md) (DEP-03)
- [`01-overview-and-invariants.md`](01-overview-and-invariants.md) (INV-04)
- [`05-presentation-udf.md`](05-presentation-udf.md) (Phase E)
