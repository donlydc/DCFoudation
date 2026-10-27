# MODULE: :platform:navigation — platform-navigation
Status: DRAFT
Verified commit: UNKNOWN (drafted against working tree at HEAD 2c0ae05b; not audited)
Last source audit: NOT_RUN
Test status: NOT_RUN (evidence: reports/baseline-verification.md)

> Doc status (`DRAFT|VERIFIED|NEEDS_REFRESH`) and test status (`NOT_RUN|TESTED|FAILED|BLOCKED`) are separate vocabularies; see `TEMPLATE.md`.

## 1. Identity and responsibility
- Owns: the engine-neutral, `@Serializable` top-level destination contract `Destination` (`Destination.Greeting`, `Destination.Articles`). Nothing else: the module has 1 main file and 0 tests.
- Does not own: `NavHost`/`NavController`/back stack (in `:app`, `MainActivity.kt` `DcNavHost`); any `Navigator`/action wrapper (deliberately absent per the KDoc of `Destination`); argument encoding, deep links, state restoration (none implemented here).
- Public package/API: `com.dc.android.foundation.platform.navigation` -> `Destination` (sealed interface).
- Consumer(s): `:app` only (`MainActivity.kt`: 2 caller sites per CodeGraph, `DcNavHost`). Feature packages do not import it today.

## 2. Context and discovery
- Read first: `architecture/00-index.md`, `architecture/06-navigation.md` (NAV-01), `architecture/dependency-rules.md`.
- Main code: `platform/navigation/src/main/java/com/dc/android/foundation/platform/navigation/Destination.kt`.
- Tests: none (`context-map.yaml` `test_roots: []`; baseline A2: 0 test files).
- Build config: `platform/navigation/build.gradle.kts` (`dc.android.library` + `kotlin.serialization`; `implementation(libs.kotlinx.serialization.json)`; no `androidx.navigation`).
- CodeGraph entry symbols/callers/callees: `Destination` <- `DcNavHost` (`MainActivity.kt`: `Destination.Greeting::class.qualifiedName`, `navController.navigate(Destination.X, options)`, `startDestination`, `composable<Destination.X>`). No callees.

## 3. Dependencies and boundaries
- Allowed: no project dependencies (`dependency-rules.md` allowed-edge table: `:platform:navigation -> (none)`); `kotlinx-serialization-json` only.
- Forbidden: any `androidx.navigation.*` import (NAV-01, INV-07; module STRUCTURE.md); edges to `:app` (DEP-01) or to features; production edge to `:testing` (BLD-03).
- Exceptions (DEC/KD): none recorded. KD-02 (module edge rules not machine-enforced) applies.

## 4. Data/control flow
```mermaid
flowchart LR
  Nav[DcNavigationSuite onSelect] --> Host[DcNavHost in MainActivity]
  Host -->|navigate Destination.X| NC[NavController]
  Dest[Destination sealed] -.route type.-> Host
  NC --> Comp[composable Destination.X -> GreetingRoute / ArticlesRoute]
```
Source refs: `MainActivity.kt` `DcNavHost` (selection by `Destination.*::class.qualifiedName`, `popUpTo ... saveState`, `launchSingleTop`, `restoreState`). Routes `more`, `design-system-catalog`, `trip-showcase` are raw string constants in `:app`, not `Destination` members (CONFIRMED_IN_CODE).

## 5. Contracts
- API: `sealed interface Destination` with two `@Serializable data object`s. Inputs/outputs: none (no arguments, no encoding/decoding code, no route-builder).
- Errors: none; no `AppError` mapping (no I/O). Cancellation/threading/lifecycle: not applicable (pure types).
- Stability: the type-safe route string is derived by Navigation-Compose from the serializer; no test pins it (PROPOSED NAV-01 round-trip test).

## 6. DI, configuration and ownership
- No Hilt module; nothing to construct. `:app` owns the navigation engine and the graph. No variants or defaults.

## 7. Edge cases, security and performance
- No secrets/PII (SEC-01/02 n/a). Adding a destination requires a matching `composable<...>` in `DcNavHost`; there is no compile-time check that every `Destination` member is registered (CONFIRMED_IN_CODE: `Destination` is not exhaustively consumed by a `when`).
- Back/deep link/state restoration beyond `saveState`/`restoreState` options in `:app`: not implemented in this module (UNKNOWN whether configured elsewhere; no `<deep-link>` audit done here).

## 8. Tests and evidence
- Unit: none. Integration: none. Instrumented: none for this module.
- Commands (once Gradle runs): `./gradlew :platform:navigation:testDebugUnitTest` (no test sources exist, so this would be a no-op); `./gradlew :app:assembleDebug` to verify the consumer.
- Test status: NOT_RUN (evidence: reports/baseline-verification.md)
- Last verified result: none. No test exists, so there is nothing to run for this module.

## 9. Known debt / decisions
- Gap, not completion: only `Destination.kt` exists; the master plan (MP 4.7) calls this a clear functional gap. Do not describe navigation as a finished framework.
- PROPOSED backlog (MP 4.7, none implemented): NAV-01 destination/argument contract, encoding/decoding, stable route + round-trip/invalid-argument tests (P0); NAV-02 minimal `Navigator`/effect adapter testable at ViewModel/Route boundary (P1); NAV-03 back/deep link/state restoration rules + nested graph example (P1, instrumented); NAV-04 greeting <-> articles/showcase sample integration (P1).
- Note: the rule ID NAV-01 in `06-navigation.md` (engine isolation) is not the same item as backlog NAV-01 (route contract); resolve the numbering when the backlog is adopted (docs drift, see report D).
- Related: KD-02 ([22-known-deviations.md](../22-known-deviations.md)). No DEC.

## 10. Safe extension guide
- Add a destination only with a real screen and a `composable<Destination.X>` in `:app`'s `DcNavHost` (module STRUCTURE.md). Keep destinations immutable and free of mutable UI state (MP 4.7). Do not add a `Navigator` until a feature needs to trigger navigation itself (KDoc of `Destination`). Sample consumer: `DcNavHost` in `MainActivity.kt`.
- Generated features (`createFeature -Pnavigation=true`) emit a per-feature `<Name>Destination` data object that is not a `Destination` subtype (`FeatureScaffoldGenerator.destination`); see `build-logic.md`.

## 11. AI task playbook
- Read -> Graph -> Propose -> Edit -> Test -> Update docs -> Handover.
- Read: `06-navigation.md`, `Destination.kt`, `MainActivity.kt`. Graph: `codegraph_explore "Destination DcNavHost MainActivity"`.
- Recipe: add the member, register the route in `:app`, add a route test when NAV-01/02 land. DoD: `:app:assembleDebug` compiles; no `androidx.navigation` in this module; `context-map.yaml`/this page updated if the public API changed.

## 12. Change log and links
- 2026-10-27 — R1.1c — first draft from source and CodeGraph. Report: `reports/wp/R1.1c.md`.
- Links: `context-map.yaml` entry `platform-navigation`; rules `06-navigation.md`; `dependency-rules.md`.
