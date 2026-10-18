# Task Handover — DC Android Foundation

> **Current-state note (2026-08-25):** the historical phase snapshot below is retained for
> delivery context, but is no longer a current topology report. The repository now has 13 Gradle
> modules, including `:runtime:scheduling`, `:data:{network,database}`, `:platform:{navigation,
> permission}`, `:security`, `:observability` and `:testing`, plus both `greeting` and `articles`
> reference features. Use `architecture/00-index.md`, `settings.gradle.kts` and current source as
> technical authority; this handover must be re-authored before the next phase handoff.

**Date:** 2026-08-16
**Handing over at:** all 12 phases (A–L) complete. This build is finished; see "Open gaps" below for the one thing still worth doing before treating it as production-verified.

## Where to start reading

1. [`CLAUDE.md`](CLAUDE.md) — project conventions, architecture authority order.
2. [`architecture/00-index.md`](architecture/00-index.md) — routing table into the rest of `architecture/`; its status column tells you exactly what exists vs. what's still pending its phase.
3. [`PLAN_REPORT.md`](PLAN_REPORT.md) — the full phase-by-phase execution log (what was built, every real bug hit and how it was fixed, every Gate's actual result). This handover is a *summary*; PLAN_REPORT.md is the source of truth for detail.
4. [`plan/CLAUDE_IMPLEMENTATION_PLAN.md`](plan/CLAUDE_IMPLEMENTATION_PLAN.md) — the 12-phase plan (A–L) this whole build follows, including the exact Phase L spec.

## State: Phases A–K done, Phase L not started

| Phase | Status | Commit(s) |
|---|---|---|
| A — Repository reconnaissance | done | `21973b1` |
| B — Invariants & dependency boundaries | done | `ec3d9ad` |
| C — Design System foundation | done | `523c161` |
| D — Hilt dependency injection | done | `3f2dc8c` |
| E — Presentation, navigation, lifecycle | done | `3b2d215` |
| F — Concurrency & background execution | done | `adfc80d` |
| G — Data platform | done | `adfc80d` |
| H — Error, security, permissions, config, observability | done | `8d2538b` |
| I — Testing, build logic, CI, quality | done | `144902e` |
| J — Architecture documentation finalization (Gate: PASS) | done | `595a089` |
| K — Critical audit | **skipped, justified** — no Opus escalation condition fired | `595a089` |
| L — Final validation & sanitized ZIP | done | (uncommitted as of this edit) |

## What actually exists (verified, not aspirational)

Four Gradle modules: `:app`, `:ui:designsystem`, `:runtime:concurrency`, `:data:preferences` (see `settings.gradle.kts`). One reference feature (`app/.../greeting/`) built to the full canonical shape — Route/ViewModel/UiState/UiAction/UiEffect/Screen, Hilt DI, `DispatcherProvider`-mediated concurrency, DataStore-backed persistence layered under `SavedStateHandle`, `Logger`-based observability — documented in `architecture/20-feature-template.md` as the pattern for any future feature.

Everything **not** built (Navigation, Room, network, WorkManager, security/session, permissions, analytics, crash reporting) is explicitly marked "not implemented" in its architecture doc rather than stubbed out speculatively — see the "not implemented" rows in `architecture/00-index.md`'s topic-routing table.

## Last known-good verification (already run, not projected)

```bash
./gradlew testDebugUnitTest assembleDebug   # BUILD SUCCESSFUL, 4 modules, 7/7 tests passing
./gradlew lintDebug                          # BUILD SUCCESSFUL, 12 warnings, 0 errors
```

## Open gaps for whoever continues

- **`GreetingScreenTest` (Compose UI test) has never actually run** — compiles (`compileDebugAndroidTestKotlin` verified) but this environment has no emulator/device, and CI (`.github/workflows/ci.yml`) doesn't run `connectedAndroidTest` either. Run it on a real device/emulator or wire an emulator into CI before trusting it.
- CI has never executed for real (no push to a remote yet) — the workflow file exists and is believed correct but is unverified against actual GitHub Actions infrastructure.
- 12 Android Lint warnings exist (`app/build/reports/lint-results-debug.html`, regenerate via `./gradlew lintDebug`) — not triaged individually; none are blocking (0 errors).
- Known deviations (`architecture/22-known-deviations.md`): KD-01 (namespace doc fixed, no longer open), KD-02 (resolved — module boundary now exists), KD-03 (`android.disallowKotlinSourceSets=false` still required — remove once KSP ships an AGP-9-native release; check on any KSP upgrade).

## Phase L result (2026-08-16)

Ran fresh (not reused): `./gradlew test lint assembleDebug` → BUILD SUCCESSFUL, 7/7 tests passing, 0 lint errors, matching the last known-good state exactly. Tree was clean (`git status --short` / `git diff --check` both empty) before archiving.

Built `deliverables/android-base-project-final.zip` (227,748 bytes) from the 186 `git ls-files`-tracked files, staged into a temp directory to preserve directory structure (plain `Compress-Archive -Path $files` flattens everything into one directory and silently collides same-named files — hit this, fixed it, see `PLAN_REPORT.md`'s Phase L entry for detail). Verified by enumerating the archive: 225 entries, structure intact, zero forbidden paths (`.git/`, `.gradle/`, `build/`, `.idea/`, `local.properties`, `.env*`, keystores, secrets). Added `deliverables/` to `.gitignore` — the ZIP is a generated artifact and is not committed to the repo; regenerate it from `git ls-files` at any commit if needed.

Full detail and the final unresolved-deviations summary are in `PLAN_REPORT.md`'s Phase L entry (the last entry in that file).

## Model routing (for whoever continues to follow it)

Per `plan/CLAUDE_IMPLEMENTATION_PLAN.md` §1.1: Haiku for reconnaissance/mechanical consistency checks, Sonnet for implementation/architecture-writing (used for essentially all of Phases A–K directly), Opus only under the six explicit escalation conditions (none fired so far — 0 escalations, within the plan's 0–1 target).
