# Android Base Project — Claude implementation plan

## 0. Mission

Build a reusable Android Base Project and a maintainable `architecture/` knowledge system that lets humans and AI agents understand **only the part of the architecture relevant to the current task**.

The deliverable is not a giant framework. It is a compact, production-ready foundation with strict dependency direction, reusable UI/design primitives, Hilt-based DI, deterministic concurrency/testing rules, and documented boundaries.

### Definition of done

The work is complete only when:

- architecture documents reflect the real repository;
- module/layer dependency rules are explicit and checkable;
- Design System is structured as tokens → semantic theme → components;
- Hilt is integrated with module-local bindings and constructor injection by default;
- threading/coroutine ownership is documented and applied;
- the selected base capabilities compile and tests pass;
- architecture deviations are recorded instead of hidden;
- AI reading/routing rules are present and usable;
- no secrets/build caches are included in the final archive;
- a final ZIP is produced after all quality gates pass.

---

# 1. Model routing and token budget policy

## 1.1 Pinned models

Use explicit model IDs. Do not use `inherit` in project subagents.

| Work type | Agent | Model | Effort | Purpose |
|---|---|---|---|---|
| Repository discovery / file routing | `architecture-reader` | `claude-haiku-4-5-20251001` | low | Cheap grep/glob/read and small context packets |
| Architecture documentation | `architecture-writer` | `claude-sonnet-5` | medium | Design boundaries and write verified docs |
| Normal implementation | `implementation-worker` | `claude-sonnet-5` | medium | Production coding and tests |
| Mechanical consistency / rule checks | `consistency-checker` | `claude-haiku-4-5-20251001` | low | Fast validation without redesign |
| Critical architecture escalation only | `critical-architecture-auditor` | `claude-opus-5` | medium | Resolve high-cost ambiguity once, read-only |

### Opus escalation conditions

Invoke Opus **only** when one of these is true:

1. two architecture rules conflict and source inspection cannot resolve it;
2. a decision changes public/core API compatibility across modules;
3. a security, process-lifecycle, or concurrency decision has materially different safe designs;
4. a new dependency direction appears to violate a non-negotiable invariant;
5. the same architecture-related implementation approach failed validation twice;
6. an irreversible architectural migration is about to be committed.

Do **not** use Opus for boilerplate, formatting, ordinary Compose code, routine tests, Gradle cleanup, documentation indexing, or straightforward refactors.

## 1.2 Context budget rules

- `architecture-reader` returns a **context packet**, not raw file dumps.
- Target context packet: <= 1,200 tokens unless a critical invariant requires more.
- A normal implementation task should read `00-index`, invariants, and at most 2–4 specific architecture docs before source files.
- Never recursively read the entire repository or architecture folder merely “for context.”
- Prefer `rg`, `git grep`, Gradle project listing, and focused reads.
- Do not paste long build logs into another agent; summarize the failing task, first root-cause evidence, and relevant lines.

## 1.3 Model configuration

Project model policy is encoded in `.claude/settings.json` and `.claude/agents/*.md`.

Important limitation: project-level settings are a practical repository policy, but higher-precedence organization/managed settings or explicit runtime overrides can supersede parts of local configuration. If runtime reports model substitution, record it in the final execution report rather than silently assuming the requested model ran.

---

# 2. Documentation style to preserve

The supplied architecture references establish a useful documentation pattern:

- one file owns one architectural concern;
- top-level invariants are numbered and include a reason;
- dependency rules are written as review-checkable statements;
- layer maps visualize direction;
- exceptions/deviations are explicit rather than normalized into the rule;
- index/backlinks make the knowledge base navigable.

Use that style, but do not copy BLE/connectivity-specific rules into this project.

---

# 3. Target architecture knowledge tree

Claude must create/finalize this structure based on the actual repository:

```text
architecture/
├── README.md
├── 00-index.md
├── 01-overview-and-invariants.md
├── 02-layers-and-dependencies.md
├── 03-project-structure.md
├── 04-app-bootstrap-and-lifecycle.md
├── 05-presentation-udf.md
├── 06-navigation.md
├── 07-design-system/
│   ├── README.md
│   ├── tokens.md
│   ├── semantic-theme.md
│   ├── typography.md
│   ├── spacing-shapes-elevation.md
│   ├── components.md
│   ├── motion.md
│   ├── adaptive-accessibility.md
│   └── previews-and-catalog.md
├── 08-dependency-injection-hilt.md
├── 09-concurrency-and-threading.md
├── 10-data/
│   ├── README.md
│   ├── repositories.md
│   ├── network.md
│   ├── persistence-room.md
│   ├── preferences-datastore.md
│   ├── cache-and-offline.md
│   └── mapping-boundaries.md
├── 11-error-and-result-model.md
├── 12-security-and-session.md
├── 13-permissions.md
├── 14-background-work.md
├── 15-observability/
│   ├── README.md
│   ├── logging.md
│   ├── analytics.md
│   └── crash-and-performance.md
├── 16-config-feature-flags-environments.md
├── 17-testing.md
├── 18-build-logic-and-dependencies.md
├── 19-ci-release-quality-gates.md
├── 20-feature-template.md
├── 21-coding-rules.md
├── 22-known-deviations.md
├── 23-decision-log.md
├── 24-ai-reading-guide.md
└── _templates/
    └── architecture-document-template.md
```

If the actual project does not use a capability, mark the document as **Not implemented / optional** rather than inventing an implementation.

---

# 4. Phase plan

## Phase A — Repository reconnaissance

**Agent:** `architecture-reader` (Haiku)

Tasks:

1. Identify root Gradle files, version catalog, build logic, modules, source sets, CI config, and existing architecture docs.
2. Detect actual Android/Kotlin/Compose/Hilt/coroutines/navigation/network/database/testing dependencies.
3. Produce a compact repository map with evidence paths.
4. Identify existing base classes, service locators, dependency inversions, direct infrastructure access from UI, hard-coded design constants, and obvious thread ownership patterns.
5. Do not edit production code.

Output:

- a context packet for Phase B;
- evidence list: file paths + concise finding;
- unresolved questions only when the repository cannot answer them.

### Gate A

Do not proceed until the repository/module map is source-backed.

---

## Phase B — Establish invariants and dependency boundaries

**Agent:** `architecture-writer` (Sonnet)

Create/finalize:

- `01-overview-and-invariants.md`
- `02-layers-and-dependencies.md`
- `03-project-structure.md`

Minimum invariants to evaluate and adapt:

- one-directional dependency flow;
- no feature implementation imported by core;
- no direct infrastructure objects exposed to UI/public contracts;
- Compose UI receives state/actions, not repositories/data sources;
- business models do not pollute reusable base/platform layers;
- one owner for long-lived mutable orchestration state;
- no lock held across callback/business/suspension boundaries;
- no secret/PII logging;
- design-system semantic tokens/components are the visual contract;
- architecture docs distinguish current implementation from target state.

Each invariant must contain **Rule + Why + Verification**.

### Gate B

Run `consistency-checker` against imports/module graph. Any deliberate mismatch goes to `22-known-deviations.md`.

---

## Phase C — Design System foundation

**Agent:** `implementation-worker` (Sonnet)

Build/finalize a reusable design system. Prefer a dedicated `core:designsystem` module if supported by the repository structure.

### Required layers

```text
Primitive tokens
    ↓
Semantic theme
    ↓
Reusable components
    ↓
Feature UI
```

Cover:

- color primitives and semantic colors;
- typography scale / semantic text roles;
- spacing scale;
- shapes/radius;
- elevation where applicable;
- component dimensions;
- motion durations/easing policy;
- light/dark/system theme modes;
- dynamic color policy (optional, explicit);
- adaptive layouts/window size policy;
- accessibility, font scale, touch target, semantics, RTL;
- component previews/catalog.

Feature code must not proliferate raw brand colors, arbitrary spacing, or ad-hoc component styling when a semantic token/component exists.

Document all rules in `07-design-system/*`.

### Gate C

- Compose compile passes.
- Design-system previews/sample screen compile where supported.
- Search for obvious hard-coded design values in migrated sample code and record remaining deviations.

---

## Phase D — Hilt dependency injection

**Agent:** `implementation-worker` (Sonnet)

Hilt is the default DI mechanism.

Rules:

- constructor injection first;
- `@HiltViewModel` for ViewModels that require injected dependencies;
- bindings live near the module/capability that owns them;
- avoid a monolithic multi-thousand-line `AppModule`;
- use qualifiers for dispatcher/environment distinctions;
- field injection only where Android/framework construction requires it;
- Hilt entry points are escape hatches, not ordinary dependency access;
- Compose content must not use DI to reach repositories/data sources directly.

Document component/scope ownership in `08-dependency-injection-hilt.md`.

### Gate D

- no duplicate/conflicting bindings;
- app starts/compiles with Hilt processing;
- sample ViewModel injection works;
- dependency direction remains valid.

---

## Phase E — Presentation, navigation, lifecycle

**Agent:** `implementation-worker` (Sonnet)

Establish feature contract template:

```text
Route
ViewModel
UiState
UiAction
UiEffect (only true one-shot effects)
Screen/Content
```

Rules:

- UDF: state down, actions up;
- screen content should be stateless where practical;
- navigation engine hidden behind destination/navigation contracts when useful;
- process death/restorable state policy is explicit;
- do not use a universal `BaseViewModel` as a dumping ground;
- prefer small reusable delegates/helpers over inheritance.

Document in `04`, `05`, `06`, and `20`.

### Gate E

Create one representative sample feature or migrate one existing feature to prove the pattern. Tests must cover its state transitions.

---

## Phase F — Concurrency and background execution

**Agent:** `implementation-worker` (Sonnet)

Define and implement only the necessary runtime contracts:

- app dispatcher abstraction/qualifiers;
- main-safe suspend boundary policy;
- `viewModelScope` ownership;
- lifecycle-aware UI collection;
- injected external/application scope only for work that must outlive callers;
- WorkManager for persistent/retriable background work;
- foreground service only for Android-valid user-visible long-running cases;
- no `GlobalScope`;
- no lock held while invoking external/business callbacks or resuming/suspending in unsafe ways.

Document in `09-concurrency-and-threading.md` and `14-background-work.md`.

### Gate F

Run concurrency/unit tests where applicable and grep for forbidden patterns (`GlobalScope`, hard-coded dispatchers where abstraction is required, unmanaged scopes).

---

## Phase G — Data platform

**Agent:** `implementation-worker` (Sonnet)

Implement/document reusable boundaries, not business-specific data models.

Target structure:

```text
Repository contract
  ├─ RemoteDataSource
  ├─ LocalDataSource
  └─ Cache/offline policy
```

Define mapping boundaries:

```text
DTO ↔ data model ↔ domain model ↔ UI model
Room entity ↔ data/domain model
```

No Retrofit DTO or Room entity should become an accidental app-wide public model.

Cover if present/required:

- network client/interceptors/serialization/timeouts;
- auth/session integration contract;
- Room/migrations/transactions;
- DataStore for small preference/config state;
- cache freshness/source-of-truth policy;
- network connectivity observation only when it changes behavior.

Document in `10-data/*`.

### Gate G

Repository/API tests pass. Mappers are tested where behavior is non-trivial. No infrastructure types leak into presentation APIs.

---

## Phase H — Error, security, permissions, config, observability

**Agent:** `implementation-worker` (Sonnet)

### Error/result

Create a small, useful error taxonomy. Do not force every operation into one generic `Resource<T>` if feature state is more expressive.

Infrastructure exceptions are mapped before crossing presentation boundaries.

### Security/session

Define:

- secure storage contract if needed;
- token/session ownership;
- sensitive logging policy;
- credential refresh/expiry boundary;
- no secrets in source, generated docs, or final ZIP.

### Permissions

Provide consistent permission state/flow conventions without creating an abstraction larger than Android's actual needs.

### Config

Define environments and configuration ownership. Feature flags are optional and provider-neutral.

### Observability

Separate contracts for:

- logging;
- analytics;
- crash reporting;
- performance tracing.

Do not make feature code depend directly on a vendor SDK unless intentionally accepted.

Document `11`–`16`.

### Gate H

Search for token/password/secret logging and accidental vendor coupling. Record legitimate deviations.

---

## Phase I — Testing, build logic, CI, quality

**Agent:** `implementation-worker` (Sonnet), followed by `consistency-checker` (Haiku)

Cover:

- unit tests for ViewModels/use cases/mappers;
- coroutine test utilities and main dispatcher rule;
- fakes/test data builders;
- Compose UI tests for reusable components/critical screens;
- convention plugins/build logic where duplication justifies them;
- version catalog/dependency ownership;
- lint/static analysis/formatting policy already supported by the project;
- CI build/test/lint gates;
- release/versioning policy appropriate to app vs reusable library.

Do not add tools solely because they are popular; each dependency must earn its maintenance cost.

Document `17`, `18`, `19`, `21`.

### Gate I

Run the repository's canonical validation commands. If no canonical commands exist, establish and document the smallest deterministic set.

---

## Phase J — Architecture documentation finalization

**Agent:** `architecture-writer` (Sonnet)

1. Re-read actual implementation changes, not prior summaries.
2. Finalize every applicable architecture file.
3. Mark unused optional areas explicitly.
4. Populate `22-known-deviations.md` with unresolved intentional breaches.
5. Populate `23-decision-log.md` with durable decisions and rationale.
6. Finalize `24-ai-reading-guide.md` routing table.
7. Ensure every document has Scope, Rules, Why, Verification, Related docs, and Known deviations where relevant.
8. Ensure `00-index.md` is enough for an agent to pick documents without reading all of them.

### Gate J

Use `consistency-checker` to verify:

- links exist;
- no duplicate/contradictory rule IDs;
- module names match Gradle/source reality;
- docs do not claim unimplemented behavior;
- known deviations are linked from the relevant rule docs.

---

## Phase K — Critical audit (conditional)

**Agent:** `critical-architecture-auditor` (Opus)

Run **only if an escalation condition from §1.1 occurred**.

Input must be concise:

- exact conflicting rule/decision;
- relevant architecture excerpts;
- relevant source/build paths;
- observed validation evidence;
- 2–3 candidate resolutions maximum.

Output:

- selected resolution;
- trade-off;
- invariant/dependency impact;
- exact docs/code that need updates.

No broad repository rewrite.

---

## Phase L — Final validation and sanitized ZIP

**Agents:** `consistency-checker` (Haiku) then main/Sonnet for packaging

### Required validation

Run the project's documented canonical commands, for example where applicable:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

Use the repository's actual tasks rather than blindly assuming these exact names.

Then inspect:

```bash
git status --short
git diff --check
```

### Secret/sensitive-file exclusion

The final ZIP must exclude at minimum:

```text
.git/
.gradle/
**/build/
.idea/ caches where not needed
local.properties
.env
.env.*
*.jks
*.keystore
*.p12
*.pfx
signing.properties
secrets.properties
credentials files
previous *.zip outputs
```

Do not delete these files from the developer machine; simply exclude them from the archive.

### ZIP deliverable

Create a deterministic, clearly named archive such as:

```text
deliverables/android-base-project-final.zip
```

The archive should contain source/configuration/documentation needed to open/build the project, excluding generated output, caches, VCS internals, and secrets.

After creating it:

1. list ZIP contents;
2. verify forbidden paths are absent;
3. report archive path and size;
4. provide final validation summary and unresolved deviations.

---

# 5. Architecture rule format

Use stable IDs so code review and AI can cite rules.

Example:

```markdown
## UI-01 — Composables do not access repositories

**Rule:** Feature Composables receive state and callbacks/actions; they do not directly resolve or call repositories, Retrofit services, DAOs, or DataStore.

**Why:** Keeps UI previewable/testable and prevents lifecycle/infrastructure coupling.

**Verification:** Search feature UI packages for repository/DAO/service imports and direct DI lookup.

**Exceptions:** Link to `22-known-deviations.md` if one exists.
```

Recommended prefixes:

```text
INV  global invariant
DEP  dependency
UI   presentation/Compose
DS   design system
DI   Hilt/dependency injection
NAV  navigation
THR  concurrency/threading
DAT  data
ERR  error/result
SEC  security/session
PERM permissions
BG   background work
OBS  observability
BLD  build/release
TST  testing
AI   agent/document routing
```

---

# 6. Change protocol for future tasks

For every future coding task:

1. Haiku reader determines relevant architecture docs.
2. Worker reads only those docs + target source files.
3. Worker states which rule IDs constrain the change in its internal task context.
4. Implement smallest coherent change.
5. Run focused tests first, broader gates second.
6. Haiku checker verifies architecture consistency.
7. Update docs only if architecture/contract changed.
8. Escalate to Opus only under the explicit escalation conditions.

This is the primary token-saving loop.

---

# 7. Anti-overengineering constraints

Claude must not create abstractions merely to complete the architecture checklist.

Do not add a capability when:

- the project has no current or near-term use case;
- Android/framework API is already simpler than the proposed wrapper;
- the wrapper exposes the same API without adding policy/testability/isolation;
- it creates a cross-module dependency only to satisfy a diagram;
- the only justification is “future-proofing.”

In those cases, document the policy and mark implementation optional.

---

# 8. Final execution report

At completion produce `IMPLEMENTATION_REPORT.md` containing:

- phases completed;
- files/modules created or materially changed;
- architecture invariants established;
- Hilt status;
- design-system status;
- tests/build/lint commands and result;
- known deviations;
- Opus escalation count and reason (target: 0–1);
- actual model substitutions reported by Claude Code, if any;
- final ZIP path and size.
