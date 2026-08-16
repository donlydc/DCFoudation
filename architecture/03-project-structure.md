# Project structure

[Back to architecture index](00-index.md)

## Scope

Actual Gradle modules, package ownership, naming, and public vs internal surfaces. Does not own dependency-direction rules (see [`02-layers-and-dependencies.md`](02-layers-and-dependencies.md)).

## Current implementation (verified, Phase A)

Single Gradle module:

```text
DCAndroidFoundation/
├── app/                                    the only Gradle module (settings.gradle.kts: include(":app"))
│   └── src/
│       ├── main/java/com/dc/android/foundation/
│       │   ├── MainActivity.kt             ComponentActivity, Greeting(), GreetingPreview()
│       │   └── ui/theme/{Color,Type,Theme}.kt
│       ├── main/res/                       standard drawable/mipmap/values/xml
│       ├── test/java/.../ExampleUnitTest.kt
│       └── androidTest/java/.../ExampleInstrumentedTest.kt
├── architecture/                           this directory
├── plan/                                   execution-plan seed package (plan/README_FIRST.md)
├── graphify-out/                           generated, gitignored
├── build.gradle.kts, settings.gradle.kts, gradle/
└── CLAUDE.md, README.md, PLAN_REPORT.md
```

- `namespace` / `applicationId`: `com.dc.android.foundation` (from `app/build.gradle.kts`).
- Root project name: `DC Android Foundation` (`settings.gradle.kts`; a stray `(` typo here was fixed in Phase A).
- `compileSdk 37`, `minSdk 24`, `targetSdk 37`, Java 11 source/target compatibility, Compose enabled, no `kapt`/`ksp` plugin.

## Target structure (not yet implemented)

Root [`CLAUDE.md`](../CLAUDE.md) §1 describes a target capability map (`foundation/`, `architecture/`, `ui/{designsystem,components,adaptive,resources}`, `data/{network,database,preferences,cache}`, `runtime/{concurrency,lifecycle,background}`, `platform/{navigation,permission,device,configuration}`, `security/`, `observability/`, `testing/`, `build-logic/`). Per that same document: *"Do not create all modules just because they appear in this target map. Create/split a module only when its boundary provides real dependency, ownership, reuse, testability, or build-value."* No module beyond `:app` exists today; modules will be introduced by the phase that actually needs them (Phase C onward), not created preemptively here.

## Rules

### STR-01 — One Gradle module owns one clearly stated responsibility

**Rule:** When a new Gradle module is introduced, it must be introduced by the phase that needs its boundary (design system, data, etc.), with a one-line stated responsibility in this document.

**Why:** Prevents speculative modularization (anti-overengineering constraint, `plan/CLAUDE_IMPLEMENTATION_PLAN.md` §7).

**Verification:** Every entry under `settings.gradle.kts` `include(...)` has a corresponding row in this document's structure tree.

### STR-02 — Package root is `com.dc.android.foundation`

**Rule:** All application code lives under the `com.dc.android.foundation` namespace (verified: `app/build.gradle.kts` `namespace`/`applicationId`, and every `package` declaration in `app/src`).

**Why:** Matches what Android Studio actually generated and what the manifest/build config declare; renaming now would be a purely cosmetic, non-trivial refactor (touches `applicationId`, manifest, generated `R`/`BuildConfig` references) with no functional benefit.

**Verification:** `grep` package declarations under `app/src` and `namespace`/`applicationId` in `app/build.gradle.kts`.

**Exceptions:** See `KD-01` in [`22-known-deviations.md`](22-known-deviations.md) — root `CLAUDE.md` previously stated the primary namespace as `com.dc.foundation`; that line has been corrected to match verified reality instead of renaming the codebase.

## Testing / validation

- `settings.gradle.kts` `include(...)` list vs. the structure tree above must match on every phase.
- `grep -r "^package "` under `app/src` must only ever show `com.dc.android.foundation*`.

## Related documents

- [`02-layers-and-dependencies.md`](02-layers-and-dependencies.md)
- [`22-known-deviations.md`](22-known-deviations.md)
- [`23-decision-log.md`](23-decision-log.md)
