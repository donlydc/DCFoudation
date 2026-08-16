# Claude Android Base — execution package

This package is a **plan + architecture documentation seed + Claude Code routing configuration** for building a reusable Android Base Project.

## Goal

Build a production-oriented, reusable Android foundation with:

- Kotlin + Jetpack Compose + Material 3
- a real design system (tokens → semantic theme → components)
- UDF / MVVM-MVI-lite presentation contracts
- Hilt dependency injection
- Coroutines / Flow / explicit dispatcher policy
- Navigation abstraction
- repository/data-source boundaries
- network, Room, DataStore, cache/offline conventions
- standardized errors/results
- security/session/permissions/background work
- logging/analytics/crash/performance abstractions
- testing/build logic/CI quality gates
- AI-readable architecture documentation

## How to use

1. Extract this package into the repository root.
2. Start Claude Code from the repository root.
3. Give Claude the contents of `START_CLAUDE.md`, or simply instruct it to execute `CLAUDE_IMPLEMENTATION_PLAN.md`.
4. Claude must follow `CLAUDE.md` and the architecture routing rules.
5. The final phase creates a sanitized ZIP after all quality gates pass.

## Important

The files under `references/style-only/` are **writing-structure references only**. They describe a BLE/connectivity library and must **not** be treated as requirements for the Android Base Project.

The architecture folder included here is a **seed/index and scope map**. Claude must inspect the actual repository before finalizing factual content. It must not invent modules, dependencies, SDK versions, or implemented behavior that the repository does not contain.
