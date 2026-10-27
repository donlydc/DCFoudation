# CLAUDE.md — DC Android Foundation

## 1. Project context

**Project:** DC Android Foundation  
**Repository:** `dc-android-foundation`  
**Brand:** `DC` = Donly Conan  
**Primary namespace:** `com.dc.android.foundation` (verified against `app/build.gradle.kts` and source; see `architecture/22-known-deviations.md` KD-01)

This repository is a reusable Android foundation for future DC applications. It provides architecture, UI/design-system, platform, data, runtime, security, observability, testing, and build conventions that are intentionally business-neutral.

The goal is **not** to build one application. The goal is to create a stable, production-ready base that multiple Android projects can depend on and extend.

### High-level capabilities

```text
DC Android Foundation
│
├── foundation/        minimal shared primitives/contracts
├── architecture/      UDF, presentation contracts, architecture rules
│
├── ui/
│   ├── designsystem/  tokens, semantic theme, typography, spacing, shapes, motion
│   ├── components/    reusable Compose components
│   ├── adaptive/      responsive/adaptive UI
│   └── resources/     shared icons/resources
│
├── data/
│   ├── network/       HTTP/network infrastructure
│   ├── database/      Room infrastructure
│   ├── preferences/   DataStore infrastructure
│   └── cache/         cache policies/primitives
│
├── runtime/
│   ├── concurrency/   Coroutines, dispatchers, Flow utilities
│   ├── lifecycle/     application/process lifecycle
│   └── background/    WorkManager/background execution
│
├── platform/
│   ├── navigation/    navigation contracts/implementation
│   ├── permission/    permission handling
│   ├── device/        Android/device abstractions
│   └── configuration/ environment/feature configuration
│
├── security/          secure storage/session/security primitives
├── observability/     logging, analytics, crash, performance
├── testing/           shared test utilities/fakes/rules
└── build-logic/       DC Gradle convention plugins
```

Do not create all modules just because they appear in this target map. Create/split a module only when its boundary provides real dependency, ownership, reuse, testability, or build-value.

---

## 2. Baseline technical direction

Unless an architecture decision explicitly changes it, prefer:

- Kotlin
- Jetpack Compose + Material 3
- UDF / MVVM-MVI-lite presentation style
- ViewModel + `StateFlow`
- Hilt for dependency injection
- Coroutines + Flow
- Navigation abstraction over the selected Android navigation engine
- Room for structured persistence
- DataStore for preferences/configuration
- WorkManager for persistent background work
- Gradle Kotlin DSL
- Version Catalog
- Gradle convention plugins

### Hilt rules

- Prefer constructor injection.
- Use `@HiltViewModel` for ViewModels requiring dependencies.
- Keep bindings near the module/capability that owns them.
- Do not create one giant `AppModule`.
- Do not resolve repositories/services directly inside Composables.
- Use Hilt EntryPoints only when framework integration makes normal injection impractical.

### UI / Design System direction

Use this dependency direction:

```text
Primitive Tokens
      ↓
Semantic Theme
      ↓
Reusable DC Components
      ↓
Feature UI
```

Feature UI should prefer semantic design-system APIs over arbitrary colors, dimensions, typography, shapes, or raw Material styling.

---

## 3. Architecture rules

Prefer:

```text
UI
 ↓
Presentation
 ↓
Domain [optional]
 ↓
Repository
 ↓
DataSource
```

Non-negotiable defaults:

1. Composables do not directly access Retrofit, DAO, Room, DataStore, or repositories.
2. UI state flows down; UI actions/events flow up.
3. Infrastructure exceptions do not leak into UI contracts.
4. DTOs and database entities do not become UI models by accident.
5. Prefer composition over inheritance.
6. Do not use `GlobalScope`.
7. Blocking/expensive work must be main-safe at the appropriate lower-layer boundary.
8. Do not expose Android/framework implementation details through generic public contracts unless unavoidable and documented.
9. Core/foundation code stays business-neutral.
10. Hilt is an implementation mechanism, not a reason to create a `dc-hilt` capability module.

If the repository contains `architecture/`, those documents are more specific than this summary and must be followed.

---

## 3a. CodeGraph first (code lookup) — MANDATORY every session (see `SKILLS.md`)

This repository is indexed by CodeGraph (`.codegraph/`, local-only and git-ignored). For **any** question about code — where something is, how it works, what a change affects — use CodeGraph **before** `Grep`, `Glob`, `find`, or opening files:

- MCP tool (preferred): `codegraph_explore` with a natural-language question or a bag of symbol/file names, `projectPath` = repo root. It returns verbatim line-numbered source plus call paths and blast radius; treat that source as already read.
- Shell fallback: `codegraph explore "<symbols or question>"`.
- Refresh after large edits: `codegraph sync`. Re-run `codegraph init -y` only if the index is missing.

Order of tools: **CodeGraph → Graphify (architecture/community map, §4) → targeted Grep/Read**. Only fall back to Grep/Read for non-code files (docs, Gradle, XML) or when CodeGraph returns nothing relevant. Apply the same rule in every subagent prompt that explores code.

---

## 4. Using Graphify

Graphify is the preferred **repository map** for architecture discovery and cross-module impact analysis.

### Before broad repository search

If this exists:

```text
graphify-out/GRAPH_REPORT.md
```

read it **before** broad `Glob`, `Grep`, or large directory scans.

Use the graph to identify:

- important/god nodes;
- module/community boundaries;
- dependency directions;
- highly connected files;
- likely implementation entry points;
- cross-cutting dependencies;
- suspicious coupling;
- files that deserve direct source inspection.

Then inspect only the relevant raw source files needed to verify the graph and complete the task.

### Creating/updating the graph

Preferred from Claude Code when the Graphify skill is installed:

```text
/graphify .
```

CLI equivalent from project root:

```bash
graphify .
```

Do not regenerate the full graph on every task. Reuse an existing graph when it still represents the current repository. Update/regenerate it when major structural changes make the graph stale or when the task explicitly requires a fresh architecture map.

Typical Graphify output:

```text
graphify-out/
├── GRAPH_REPORT.md   human/agent-readable architecture summary
├── graph.json        machine/queryable graph data
└── graph.html        interactive visualization
```

### Graphify is a navigation aid, not source-of-truth

Never change code solely because Graphify says two things are connected.

For any implementation decision:

```text
Graphify
   ↓ locate likely area
Architecture docs
   ↓ establish rules
Source/build files
   ↓ verify reality
Implementation/tests
```

If graph data conflicts with current source/build configuration, trust verified current source/build configuration and refresh Graphify if appropriate.

---

## 5. Context-efficient workflow

For each task:

1. Read this `CLAUDE.md`, then `architecture/context-protocol.md` and look the task up in `architecture/context-map.yaml` (module/feature page, entry symbols, owner rules) before any code search. CodeGraph-first (§3a) still governs code lookup.
2. If available, read `architecture/00-index.md` and `architecture/01-overview-and-invariants.md`.
3. Use CodeGraph (`codegraph_explore`) first for code lookup (§3a); read `graphify-out/GRAPH_REPORT.md` before broad architecture-level exploration.
4. Identify the smallest relevant modules/files.
5. Read only task-specific architecture documents.
6. Verify relevant source/build/test files.
7. Implement the smallest coherent change.
8. Run focused tests/checks first, broader checks only when justified.
9. Update architecture docs only when a durable rule/decision/boundary changed.

### Token discipline

- Do not preload the entire repository.
- Do not preload the entire `architecture/` directory.
- Prefer CodeGraph → Graphify → targeted search → targeted reads.
- Prefer symbol/file-level context over full directory dumps.
- Summarize large tool output before carrying it forward.
- Avoid reopening unchanged files without a reason.
- For simple repository mapping or mechanical checks, use the cheapest suitable model/subagent when available.

---

## 6. Architecture authority

When instructions conflict, use this order:

1. Explicit current user/task requirement
2. Non-negotiable project architecture invariants
3. Task-specific architecture document
4. Accepted architecture/decision records
5. Verified build/source implementation
6. Nearby code conventions
7. This high-level summary

Do not silently invent a new architecture to resolve a conflict. Record or surface the decision explicitly.

---

## 7. Definition of done

A change is not complete until, as applicable:

- architecture boundaries remain valid;
- code compiles;
- relevant tests pass;
- lint/static checks pass where configured;
- Design System is used instead of duplicated feature styling where appropriate;
- Hilt bindings have correct ownership/scope;
- coroutine/thread ownership is explicit and safe;
- no secrets or local machine configuration are committed;
- durable architecture changes are documented;
- Graphify is refreshed if the change materially alters repository topology.

Keep implementations reusable, explicit, testable, and smaller than the problem they solve.

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
