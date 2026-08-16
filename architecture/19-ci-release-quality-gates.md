# CI, release & quality gates

[Back to architecture index](00-index.md)

## Scope

The canonical commands that gate a change, and what CI actually runs.

## Current implementation

`.github/workflows/ci.yml` — on every push to `main`/`master` and every PR: `./gradlew testDebugUnitTest`, `./gradlew lintDebug`, `./gradlew assembleDebug`, then uploads the JUnit XML results as an artifact. **Does not** run `connectedAndroidTest` (the `GreetingScreenTest` Compose UI test) — no emulator/device runner is configured in the workflow; that's a known gap, not an oversight to hide (`22-known-deviations.md`).

No release/versioning policy exists yet beyond `versionCode`/`versionName` in `app/build.gradle.kts` (both still at their template defaults, `1`/`"1.0"`) — this is a library-shaped foundation repo, not something released to a store, so a real release policy is deferred until a consuming app actually ships from it.

## Rules

### CI-01 — Canonical local validation commands

```bash
./gradlew testDebugUnitTest   # unit tests, all modules
./gradlew lintDebug           # Android Lint
./gradlew assembleDebug       # full compile + package
```

**Why:** Same three commands CI runs — no separate "what CI actually checks" tribal knowledge.

**Verification:** `.github/workflows/ci.yml` steps match this list.

## Testing / validation

See `PLAN_REPORT.md`'s consolidated-verification entry for the actual local run of these three commands and their results.

## Related documents

- [`17-testing.md`](17-testing.md), [`18-build-logic-and-dependencies.md`](18-build-logic-and-dependencies.md), [`22-known-deviations.md`](22-known-deviations.md)
