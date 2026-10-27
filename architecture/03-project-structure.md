# Project structure

[Back to architecture index](00-index.md)

## Scope

Actual Gradle modules, package ownership, naming, and public vs internal surfaces. Does not own dependency-direction rules (see [`02-layers-and-dependencies.md`](02-layers-and-dependencies.md)).

## Current implementation (verified against source and `settings.gradle.kts`)

```text
DCAndroidFoundation/
├── foundation/                   Android-free shared contracts and primitives
├── app/                         composition root, app shell, greeting/articles reference features
├── ui/designsystem/             semantic Compose theme, tokens, components and catalog
├── runtime/concurrency/         dispatcher contracts and implementations (plain Kotlin/JVM)
├── runtime/scheduling/          WorkManager + AlarmManager scheduling policy
├── data/preferences/            DataStore-backed generic preferences boundary
├── data/network/                Retrofit/OkHttp construction and network result boundary
├── data/database/               Room database plus the reference articles schema
├── platform/navigation/         DC-owned destination contracts
├── platform/permission/         DC-owned permission state and notification permission adapter
├── security/                    secure key/value storage contract and encrypted implementation
├── observability/               logger, analytics and crash-reporting contracts/adapters
├── testing/                     reusable dispatcher rules and infrastructure fakes for tests
├── build-logic/                 included build with DC Gradle convention plugins
├── architecture/                architecture authority and decision records
├── plan/                        original execution-plan seed package
└── graphify-out/                generated repository graph (gitignored)
```

- Application `namespace` / `applicationId`: `com.dc.android.foundation`.
- Root project name: `DC Android Foundation`.
- Android modules use compile/target SDK 37, min SDK 24 and Java 11 compatibility; KSP is used for Hilt/Room where needed.
- `:app` depends one-way on reusable capability modules. Feature packages do not import one another's private implementation.

Per-folder architecture map: [`../STRUCTURE.md`](../STRUCTURE.md); each module also carries a compact `STRUCTURE.md` routing card.

## Target structure and split policy

Root [`CLAUDE.md`](../CLAUDE.md) §1 describes a larger capability map. `:testing` now has a real reuse boundary, while `build-logic` is an included build rather than an application module. `ui/components`, `ui/adaptive`, `data/cache`, `runtime/lifecycle`, `platform/device`, and `platform/configuration` remain logical capabilities rather than automatic module requirements. Create another module only when it adds real dependency, ownership, reuse, testability, or build value.

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
