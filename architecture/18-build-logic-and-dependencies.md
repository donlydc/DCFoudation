# Build logic & dependencies

[Back to architecture index](00-index.md)

## Scope

Version catalog usage and whether/when Gradle convention plugins earn their keep.

## Current implementation

- **Version catalog** (`gradle/libs.versions.toml`) — every dependency/plugin version is declared once and referenced via `libs.*`/`alias(libs.plugins.*)`; no module hard-codes a version string. Every version added this session was verified against live `maven-metadata.xml`/`group-index.xml` (curl against `repo1.maven.org`/`dl.google.com`), not guessed (`23-decision-log.md` DEC-03) — see the table in `PLAN_REPORT.md`'s header for the full pinned list.
- **Modules today:** `:app` (Android application), `:ui:designsystem` (Android library), `:data:preferences` (Android library), `:runtime:concurrency` (plain Kotlin/JVM — no Android dependency, per INV-03).
- **Convention plugins:** **not implemented.** Each module's `build.gradle.kts` repeats a small amount of boilerplate (`compileSdk`, `minSdk`, `compileOptions`, `buildFeatures`) directly rather than through a shared `build-logic` convention plugin. With 3 Android modules the duplication is small enough that a convention-plugin module would currently cost more (a whole `build-logic` included build to maintain) than it saves — revisit once a 4th or 5th Android module makes the duplication actually painful (anti-overengineering constraint).

## Rules

### BLD-01 — No dependency version outside the version catalog

**Rule:** A module's `build.gradle.kts` never writes a literal version string for a dependency/plugin that has a `libs.versions.toml` entry.

**Why:** One place to bump a version; avoids silent drift between modules.

**Verification:** `grep '".*[0-9]\.[0-9].*"'`-style scan of `*/build.gradle.kts` for inline version literals outside `libs.versions.toml` itself — the only accepted exception found is `javax.inject:javax.inject:1`'s literal `"1"` (an ancient, effectively frozen artifact with no meaningful version to track; even that is routed through a `libs.versions.toml` alias, just with the version inlined there instead of a separate `[versions]` key).

## Testing / validation

`./gradlew build` resolving without a version-conflict error is the practical check; no automated catalog-lint exists yet.

## Related documents

- [`19-ci-release-quality-gates.md`](19-ci-release-quality-gates.md)
