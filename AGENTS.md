# AGENTS.md — DC Android Foundation

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

## 4. Using Graphify

Graphify is the preferred **repository map** for architecture discovery and cross-module impact analysis.

Use Graphify when the task requires one or more of the following:

- broad architecture discovery in an unfamiliar area;
- dependency or call-path tracing across several modules;
- cross-cutting impact analysis;
- identifying likely entry points before targeted source inspection.

Do **not** use Graphify for a small local edit when the exact file/symbol is already known. In that case, use targeted `rg`/`rg --files` and direct reads; querying the graph would add context without reducing uncertainty.

### Before broad repository search

If this exists and still represents the current repository topology:

```text
graphify-out/GRAPH_REPORT.md
```

query the graph **before** broad `Glob`, `Grep`, or large directory scans. Prefer a bounded query such as:

```bash
graphify query "<task-specific architecture question>" --budget 1200
```

Use `graphify path` for a relationship between two known concepts and `graphify explain` for one focused concept. Read `GRAPH_REPORT.md` only for broad architecture review or when query/path/explain is insufficient.

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

Preferred from Codex when the Graphify skill is installed:

```text
/graphify .
```

CLI equivalent from project root:

```bash
graphify .
```

Do not regenerate the full graph on every task. Reuse an existing graph when it still represents the current repository. Run `graphify update .` after material topology changes (modules, dependency direction, major feature boundaries) or when the graph is known to be stale. If the CLI/interpreter is unavailable, report that fact and fall back to architecture docs plus targeted source inspection; do not treat stale graph output as current truth.

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

## 5. Using AgentMemory

AgentMemory is the **cross-session decision memory**, not a replacement for repository documentation or source inspection.

For every non-trivial task:

1. Before broad exploration, run one project-scoped `memory_smart_search` using the task topic and repository name.
2. Use at most the few most relevant hits; verify any code-related claim against current architecture docs and source because memories may be stale.
3. Save a memory immediately when a non-obvious decision or debugging constraint is settled. Include the conclusion, the reason, 2–5 specific concepts, and real file paths.
4. When the user corrects an approach or states an always/never rule, save it as a lesson rather than an ordinary memory. Recall applicable lessons before repeating that task type.
5. At session end, do not batch-save a recap; automatic hooks own session summaries.

Save only information that is expensive to rediscover:

- architecture decisions and why they were chosen;
- hard-won debugging findings;
- environment/tooling constraints not derivable from the repository;
- explicit user corrections and durable preferences.

Do **not** save:

- facts directly readable from code or architecture docs;
- transient task state, command logs, or step-by-step narration;
- secrets, credentials, tokens, or personal data;
- duplicate handover/report content.

Authority and routing:

```text
HANDOVER.md / PLAN_REPORT.md    current phase and delivery state
architecture/ + source/build   current technical truth
Graphify                       repository relationships and navigation
AgentMemory                    prior decisions, rationale, and hard-won context
```

If memory conflicts with current repository evidence, trust the repository and update/save the corrected decision only when it remains useful across sessions.

---

## 6. Context-efficient workflow

For each task:

1. Read this `AGENTS.md`.
2. For a non-trivial task, run one project-scoped AgentMemory recall before code exploration.
3. If available and relevant, use a bounded Graphify query before broad repository exploration.
4. Read `architecture/00-index.md`, `architecture/01-overview-and-invariants.md`, and only the task-specific architecture documents.
5. After identifying the target module, read that module's `STRUCTURE.md` as its compact AI routing card.
6. Identify and verify the smallest relevant files in current source/build configuration.
7. Implement the smallest coherent change.
8. Run focused tests/checks first, broader checks only when justified.
9. Save settled non-obvious decisions/gotchas to AgentMemory at the moment they are resolved.
10. Update architecture docs only when a durable rule/decision/boundary changed.

### Token discipline

- Do not preload the entire repository.
- Do not preload the entire `architecture/` directory.
- Prefer AgentMemory recall → bounded Graphify query when useful → targeted search → targeted reads.
- Skip AgentMemory/Graphify when the task is trivial and the exact target is already known.
- Prefer symbol/file-level context over full directory dumps.
- Summarize large tool output before carrying it forward.
- Avoid reopening unchanged files without a reason.
- For simple repository mapping or mechanical checks, use the cheapest suitable model/subagent when available.

---

## 7. Architecture authority

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

## 8. Definition of done

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
- non-obvious reusable decisions/gotchas are saved to AgentMemory when applicable;
- Graphify is refreshed if the change materially alters repository topology.

Keep implementations reusable, explicit, testable, and smaller than the problem they solve.
