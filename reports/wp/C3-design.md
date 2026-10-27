# C3-design — D-05 publishing model, versioning, API compatibility (PROPOSED)
Status: PROPOSED (not a decision; no DEC entry written). Sources: MP D-05 (L1155), MP §11, MP:BLD-05. Language: English, matching other design reports.

## 1. As-is facts
- No `maven-publish`/`publishing` block anywhere; no artifact has been published. Only `build-logic` sets a `group` (`com.dc.android.foundation.buildlogic`). Modules carry no `version`.
- `build-logic` is consumed as an included build via `pluginManagement { includeBuild("build-logic") }` in `settings.gradle.kts`; plugin ids `dc.android.application|library`, `dc.kotlin.jvm`, `dc.compose`, `dc.hilt`, `dc.room`, `dc.testing`, `dc.feature.generator`.
- Versions come from `gradle/libs.versions.toml` (AGP 9.3.1, Kotlin 2.2.10, Hilt 2.60.1, coroutines 1.11.0). `build-logic/settings.gradle.kts` reads the same catalog by path, so `build-logic` is not relocatable alone.
- There is no independent consumer yet; `:app` is a reference sample. MP D-05 says to decide after an independent consumer exists, and MP §11.3 forbids official versions for modules that have not passed their quality gate (build verification is still NOT_RUN).

## 2. Options for D-05
| Option | How consumers use it | Pros | Cons |
|---|---|---|---|
| A. Composite build / git submodule (`includeBuild`) | Consumer app includes the foundation repo; project substitution by coordinates | No publish infra; instant source-level debugging; atomic changes with the consumer; matches today's `includeBuild("build-logic")` | Consumer builds foundation from source (build time, AGP/Kotlin must match exactly); no version pinning except git SHA/tag; weak isolation |
| B. Internal Maven repository (Artifactory/Nexus/GitHub Packages) | `implementation("com.dc.android:foundation-xxx:1.2.0")` plus a BOM; `build-logic` published as plugin marker artifacts | Real semver pinning, reproducible, parallel consumer cadence, binary-compat checking possible | Needs infra, credentials, signing/CI release job, version-skew management; publishing Android libs needs `singleVariant` config per module |
| C. Hybrid | Composite build during co-development; Maven for released tags | Lowest cost now, upgrade path later | Two consumption paths to document and test |

## 3. Recommendation (PROPOSED)
1. Now (no independent consumer): Option A. Document it in README; do not add publishing config (MP:BLD-05 "only when shipping artifacts"). Tag git releases as `vMAJOR.MINOR.PATCH` once a gate passes.
2. Trigger to move to Option B: first consumer that lives in another repo and cannot track foundation HEAD. At that point: publish `build-logic` plugins plus a `dc-foundation-bom` (constraints for every published module so versions move together), group `com.dc.android.foundation`, one repository URL from a Gradle property (no credentials in git; KD/secret rules).
3. Publish granularity: only `maturity >= beta` modules (MP §11.1). `:app` is never published. `testing` is published as a test-only artifact when a consumer needs it. `internal` modules are not published.
4. Toolchain compatibility list ships with each release (AGP, Kotlin, KSP, Compose compiler, Hilt, Room, compile/min SDK, JDK) and is derived from `libs.versions.toml`, so it cannot drift.

## 4. Versioning
- SemVer per MP §11.3: PATCH = fix without contract change; MINOR = additive public capability; MAJOR = breaking API/behavior with migration note. Pre-1.0 (`0.y.z`) for everything until modules pass gate and have multi-consumer evidence; MINOR may break while `0.x`, but must still carry a CHANGELOG migration note.
- One repository version for all published modules via the BOM (lock-step) is recommended over per-module versions while there is a single team; revisit if modules diverge.
- `build-logic` is versioned separately in practice (Gradle/AGP coupling, MP §11.3 exception): its CHANGELOG entry states the supported AGP/Gradle range. A Gradle/AGP major upgrade is a foundation MAJOR bump.
- Maturity (`experimental|beta|stable|internal`) is recorded per module in the module page; no module is stable by default.

## 5. API compatibility and deprecation policy
- Public API surface = non-`internal`, non-`@RestrictTo` Kotlin declarations of published modules. Use explicit API mode for newly published libraries (proposed, not enabled).
- Compatibility check: adopt the Kotlin binary-compatibility-validator (`apiDump`/`apiCheck`) per published module once Option B is triggered; `apiCheck` becomes a blocking CI step. Not added now (no published artifacts, build not runnable here).
- Deprecation: (1) annotate `@Deprecated(message, ReplaceWith(...), level = WARNING)` with a replacement and a CHANGELOG line (precedent: `DcSnackbarEffect(String?, ...)`, DEC-08, lane A4); (2) keep at least one MINOR release (or 30 days) at WARNING; (3) raise to ERROR for a later MINOR; (4) remove only in a MAJOR. Experimental APIs may skip steps 2-3 with a migration note.
- Rename migration: keep the old symbol as a `@Deprecated` typealias/delegating function with `ReplaceWith`; CHANGELOG "Migration" subsection shows before/after for each renamed public component (R7 "migration examples"). Example format:
  `DcSnackbarEffect(message, hostState)` -> `DcSnackbarMessage`-based overload (see A4 report).
- Behavior changes (threading, cancellation, error mapping) count as API changes (MP §11.2) and need a CHANGELOG entry at the matching level.
- Reviewer checklist = MP §11.2 list, referenced from PR template when it exists.

## 6. Open questions for the user
- Is there (or will there be) a consumer outside this repository, and which hosting is available (Artifactory/Nexus/GitHub Packages)?
- Lock-step BOM versioning acceptable?
- Is a 1.0 target date wanted? It requires build verification (currently NOT_RUN) and multi-consumer evidence.
- Doc delta: if accepted, record DEC-xx (D-05) in `architecture/23-decision-log.md` and update `architecture/19-ci-release-quality-gates.md` / `modules/build-logic.md`. Not edited here (lane scope).
