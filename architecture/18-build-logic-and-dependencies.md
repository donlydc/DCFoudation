# Build logic & dependencies

[Back to architecture index](00-index.md)

## Scope

Version catalog usage and whether/when Gradle convention plugins earn their keep.

## Current implementation

- **Version catalog** (`gradle/libs.versions.toml`) — dependency and plugin versions are declared once and referenced through `libs.*` or convention plugins.
- **Included build** (`build-logic/`) — `settings.gradle.kts` loads the convention plugins with `includeBuild("build-logic")`.
- **Convention plugins:** `dc.android.application`, `dc.android.library`, `dc.kotlin.jvm`, `dc.compose`, `dc.hilt`, `dc.room`, and `dc.testing`. They centralize SDK/Java compatibility, Compose defaults, Hilt/KSP, Room schemas, and shared test dependencies. Module build files compose only the capabilities they need.
- **Scaffolding plugin:** `dc.feature.generator` registers `createFeature`; it is separate from build conventions because generation is an opt-in developer action, not build configuration.
- **Testing boundary:** `:testing` is an Android library because its reusable rule/fakes are consumed by Android unit tests; production modules never depend on it through `implementation`/`api`.

## Rules

### BLD-01 — No dependency version outside the version catalog

**Rule:** A module's `build.gradle.kts` never writes a literal version string for a dependency/plugin that has a `libs.versions.toml` entry.

**Why:** One place to bump a version; avoids silent drift between modules.

**Verification:** `grep '".*[0-9]\.[0-9].*"'`-style scan of `*/build.gradle.kts` for inline version literals outside `libs.versions.toml` itself — the only accepted exception found is `javax.inject:javax.inject:1`'s literal `"1"` (an ancient, effectively frozen artifact with no meaningful version to track; even that is routed through a `libs.versions.toml` alias, just with the version inlined there instead of a separate `[versions]` key).

### BLD-02 — Convention plugins are composable capabilities

**Rule:** A module applies only the DC convention plugins matching its real capabilities. Android application/library, Compose, Hilt, Room, and testing concerns remain separate plugins rather than one all-inclusive plugin.

**Why:** Keeps dependency ownership visible while removing repeated build configuration; a non-Compose or non-Hilt module does not inherit those dependencies accidentally.

**Verification:** Inspect module `plugins` blocks and compare them with imports/dependencies in that module.

### BLD-03 — Test utilities never enter production runtime graphs

**Rule:** `:testing` may be referenced only from test configurations such as `testImplementation` or `androidTestImplementation`.

**Why:** Avoids shipping JUnit/coroutines-test and mutable recording fakes in production artifacts.

**Verification:** Search build files for `project(":testing")`; every match must be attached to a test configuration.

## Testing / validation

`./gradlew help` verifies the included build and project configuration. Focused module compilation plus `./gradlew testDebugUnitTest` verify that convention-provided dependencies and generated KSP/Room/Hilt tasks remain wired correctly. No automated catalog-lint exists yet.

## Related documents

- [`19-ci-release-quality-gates.md`](19-ci-release-quality-gates.md)
