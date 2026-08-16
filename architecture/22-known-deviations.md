# Known deviations

[Back to architecture index](00-index.md)

## Scope

Explicit intentional/current violations or non-yet-satisfied states of architecture rules. Each entry needs rule ID, evidence path, reason, risk, owner/exit condition.

## Entries

### KD-01 — Root `CLAUDE.md` previously stated the wrong primary namespace

**Rule affected:** STR-02 (`03-project-structure.md`)

**Evidence:** `app/build.gradle.kts` — `namespace = "com.dc.android.foundation"`; every `package` declaration under `app/src` matches. Root `CLAUDE.md` §1 previously read `**Primary namespace:** com.dc.foundation`.

**Reason:** `CLAUDE.md` §1 was written before the Android Studio project was generated; the generated project used `com.dc.android.foundation` instead. Per the project's own architecture-authority order (`CLAUDE.md` §6: verified source ranks above the high-level summary document), verified reality wins.

**Risk:** Low — documentation-only mismatch, no code impact.

**Resolution:** Corrected in Phase B — `CLAUDE.md` §1 updated to state `com.dc.android.foundation`. Not treated as an open deviation going forward; kept here as the audit trail. See `23-decision-log.md` DEC-01.

### KD-02 — No enforced module boundaries yet

**Rule affected:** DEP-01, DEP-04 (`02-layers-and-dependencies.md`)

**Evidence:** `settings.gradle.kts` — `include(":app")` only.

**Reason:** The project is pre-Phase-C; no capability has yet earned a dedicated Gradle module per the anti-overengineering constraint (`plan/CLAUDE_IMPLEMENTATION_PLAN.md` §7).

**Risk:** None currently — there is nothing to violate with a single module.

**Status:** **Resolved in Phase C** — `:ui:designsystem` now exists (`settings.gradle.kts`: `include(":app")`, `include(":ui:designsystem")`), and `:app` depends on it one-directionally (`implementation(project(":ui:designsystem"))`). DEP-01/04 are now verifiable via `./gradlew :app:dependencies`. Kept here as history; not an open deviation.

### KD-03 — `android.disallowKotlinSourceSets=false` required for KSP on AGP 9

**Rule affected:** none (build-tooling compatibility, not an architecture rule)

**Evidence:** `gradle.properties`. `./gradlew :app:assembleDebug` failed configuration with `Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin` before this flag was added.

**Reason:** AGP 9.3.1's new built-in-Kotlin compilation pipeline rejects the classic `kotlin.sourceSets` DSL. The KSP Gradle plugin (`2.2.10-2.0.2`, the version matching this project's Kotlin `2.2.10`) still registers its generated-sources directory through that classic DSL — it has not yet published an `android.sourceSets`-based update. `android.disallowKotlinSourceSets=false` is the flag Android's own error message names as the fix, not a project-invented workaround.

**Risk:** Low — a build-tooling compatibility flag, not an architecture/behavior change. Revisit when KSP ships an AGP-9-native release.

**Owner/exit condition:** Remove the flag (and this entry) once a KSP release adds `android.sourceSets` support and the build still succeeds without it.
