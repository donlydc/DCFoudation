# Navigation

[Back to architecture index](00-index.md)

## Scope

How the app moves between destinations, and how far the navigation engine is abstracted behind a contract.

## Current implementation (Phase P)

The app has two peer top-level destinations — `GreetingRoute` and `ArticlesRoute` — hosted by a real `androidx.navigation` Compose `NavHost` built directly in `app/src/main/java/com/dc/android/foundation/MainActivity.kt`, with a `NavigationBar` letting the user switch between them. `:platform:navigation` (`Destination.kt`) defines a DC-owned, engine-neutral `sealed interface Destination` with `Destination.Greeting`/`Destination.Articles` as `@Serializable data object`s, used with Navigation-Compose's type-safe route API (`composable<Destination.Greeting>`).

There is deliberately **no** `Navigator`/`DcNavController` action-wrapper: the two destinations are peers that never navigate to one another — only `:app`'s `NavHost` ever calls real `androidx.navigation` APIs. Neither `GreetingRoute` nor `ArticlesRoute` changed signature; neither imports navigation of any kind. If a future feature needs to *trigger* navigation itself (not just be a destination), that is the trigger to add a thin navigation-action contract to `:platform:navigation` — not before.

## Rules

### NAV-01 — Feature code does not depend on the navigation engine; only `:app` does

**Rule:** No package under a feature (`app/.../greeting/**`, `app/.../articles/**`) or under any non-`:app` module imports `androidx.navigation.*`. Destinations are described only through `com.dc.android.foundation.platform.navigation.Destination` (or a future navigation-action contract in the same module). Only `:app`'s `MainActivity.kt` (the composition root and sole `NavHost` owner) depends on `androidx.navigation-compose` and builds/drives the real `NavController`.

**Why:** Keeps features independently replaceable/testable and prevents a vendor navigation type from leaking into presentation/public contracts (DEP-02, DEP-04, INV-07).

**Verification:** `grep -r "androidx.navigation" app/src/main/java/com/dc/android/foundation/{greeting,articles}` and across `ui/`, `runtime/`, `data/`, `security/`, `observability/`, `platform/permission`, `platform/device`, `platform/configuration` should be empty; only `app/src/main/java/com/dc/android/foundation/MainActivity.kt` and `:platform:navigation` (via `kotlinx.serialization`, not `androidx.navigation`, in `Destination.kt`) reference navigation types. `platform/navigation/build.gradle.kts` has no `androidx.navigation.compose` dependency.

**Exceptions:** None.

## Testing / validation

Tests: `DestinationTest` (lane A2) and `DestinationRoutesTest` (lane C1), both written and NOT_RUN.

### D-02 — navigation depth (PROPOSED — pending Opus final review; DEC-C1-1)

Lane C1 (`aca166d`) added, additively, `NavigationEffect` (`Navigate(destination)`, `Back`) and `DestinationRoutes` (`of`, `fromRoute`) to `:platform:navigation`. They are engine-neutral (NAV-01 holds: no `androidx.navigation` type). No `Navigator`/`NavController` wrapper and no deep-link table exist; argument encoding (MP:NAV-01) is deferred because no destination has arguments. No feature emits `NavigationEffect` yet and `MainActivity` still compares `X::class.qualifiedName`, which equals `DestinationRoutes.of` today. `DestinationRoutes.fromRoute` relies on a hand-maintained list of destinations.

## Related documents

- [`05-presentation-udf.md`](05-presentation-udf.md)
- [`02-layers-and-dependencies.md`](02-layers-and-dependencies.md) (DEP-02, DEP-04)
- [`01-overview-and-invariants.md`](01-overview-and-invariants.md) (INV-07)
