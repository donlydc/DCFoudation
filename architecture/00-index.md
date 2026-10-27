# Architecture index

## Mandatory reads for any implementation task

1. [`context-protocol.md`](context-protocol.md) (discovery sequence) and [`context-map.yaml`](context-map.yaml) (find the module/feature entry, then its page)
2. [`01-overview-and-invariants.md`](01-overview-and-invariants.md)
3. [`24-ai-reading-guide.md`](24-ai-reading-guide.md)

## Architecture-as-Context

Navigation aids for AI agents and humans (test status is NOT_RUN everywhere until evidence exists; all module and feature pages exist as `DRAFT`, written in R1.1/R1.2 and not yet audited to `VERIFIED`):

- [`context-protocol.md`](context-protocol.md) - mandatory discovery sequence, refresh rules, handoff report convention
- [`context-map.yaml`](context-map.yaml) - machine-readable pointers per module and feature
- [`module-map.md`](module-map.md) - module table: kind, responsibility, direct deps, doc, test status
- [`dependency-rules.md`](dependency-rules.md) - allowed/forbidden module edges (KD-02 (Open, partially mitigated: static `tools/check_architecture.py --boundaries-only`; CI run NOT_RUN))
- [`modules/`](modules/app.md) (14 module pages: `app`, `build-logic`, `data-database`, `data-network`, `data-preferences`, `foundation`, `observability`, `platform-navigation`, `platform-permission`, `runtime-concurrency`, `runtime-scheduling`, `security`, `testing`, `ui-designsystem`) and [`features/`](features/articles.md) (`greeting`, `articles`, `showcase`) - one page per context-map entry
- [`modules/TEMPLATE.md`](modules/TEMPLATE.md), [`features/TEMPLATE.md`](features/TEMPLATE.md) - page templates
- Component registry (A4): [`../ui/designsystem/COMPONENTS.md`](../ui/designsystem/COMPONENTS.md) - status, tests, previews and findings per design-system component (DRAFT, NOT_RUN)
- Lane reports and design notes: `../reports/wp/` (`B2-design.md`, `B3-design.md`, `C1-design.md`, `C3-design.md` back the PROPOSED decisions)
- Checker: `python3 tools/check_architecture.py` (module coverage, links, headings, entry symbols, edges vs `dependency-rules.md`)
- ID convention: bare IDs (`PERM-01`) are architecture rules; master-plan backlog items are `MP:<ID>` ([`context-protocol.md`](context-protocol.md) section A2)

## Status

This tree began as a phase-by-phase implementation plan and is now a living description of the verified source. Check [`../PLAN_REPORT.md`](../PLAN_REPORT.md) and [`../BASE_PROJECT_ROADMAP.md`](../BASE_PROJECT_ROADMAP.md) for historical progress and the current P0–P2 backlog. A document must distinguish implemented behavior from optional/target behavior.

| Document | Status |
|---|---|
| `01-overview-and-invariants.md` | done (Phase B) |
| `02-layers-and-dependencies.md` | done (Phase B) |
| `03-project-structure.md` | done (Phase B) |
| `04-app-bootstrap-and-lifecycle.md` | done (Phase E) |
| `05-presentation-udf.md` | done (Phase E) |
| `06-navigation.md` | implemented (type-safe top-level Navigation Compose) |
| `07-design-system/*` (11 files, incl. `component-roadmap.md` (W0–W3 implemented)) | implemented tokens/theme, reusable components and common screen patterns |
| `08-dependency-injection-hilt.md` | done (Phase D) |
| `09-concurrency-and-threading.md` | done (Phase F) |
| `10-data/*` (7 files) | implemented reference paths for DataStore, Retrofit, Room and offline-first cache |
| `11-error-and-result-model.md` | done (Phase H) |
| `12-security-and-session.md` | secure storage implemented; auth/session flow remains optional |
| `13-permissions.md` | notification permission path implemented |
| `14-background-work.md` | periodic WorkManager sync reference implemented |
| `15-observability/*` (4 files) | logging/analytics/crash contracts implemented; vendor/performance tracing optional |
| `16-config-feature-flags-environments.md` | implemented environment + persisted appearance + per-app language configuration |
| `17-testing.md` | implemented shared `:testing` kit plus feature/module tests; per-module test inventory, all NOT_RUN |
| `18-build-logic-and-dependencies.md` | implemented conventions and safe feature generator |
| `19-ci-release-quality-gates.md` | done (Phase I) |
| `20-feature-template.md` | implemented template plus `createFeature` generator |
| `21-coding-rules.md` | done (Phase I) |
| `22-known-deviations.md` | living document, updated every phase (KD-06 proposed fix recorded; resolved-defect notes) |
| `23-decision-log.md` | living document, updated every phase; PROPOSED lane entries (`DEC-A3-n`, `DEC-A4-1`, `DEC-B2-n`, `DEC-B3-1`, `DEC-C1-n`, `DEC-C3-1`) await Opus final review |
| `24-ai-reading-guide.md` | done (Phase B) |
| `context-protocol.md`, `context-map.yaml`, `module-map.md`, `dependency-rules.md`, `modules/*`, `features/*` | R1 (DRAFT, not audited) |

## Topic routing

| Topic | Primary documents |
|---|---|
| where does X live / which module or feature owns it | `context-map.yaml` (lookup by `gradle_path`, id or symbol) -> `modules/<m>.md` or `features/<f>.md`; overview in `module-map.md` |
| module boundaries / imports | `02-layers-and-dependencies.md`, `03-project-structure.md`, `dependency-rules.md` |
| app startup / process lifecycle | `04-app-bootstrap-and-lifecycle.md` (Phase E) |
| ViewModel / state / events | `05-presentation-udf.md`, `11-error-and-result-model.md`, `09-concurrency-and-threading.md` (Phase E/F/H) |
| navigation / deep links | `06-navigation.md` (Phase E) |
| colors / typography / spacing / components | `07-design-system/README.md` + the specific subdocument (Phase C) |
| Hilt / scopes / bindings | `08-dependency-injection-hilt.md` (Phase D), `02-layers-and-dependencies.md` |
| coroutine / Flow / threads | `09-concurrency-and-threading.md` (Phase F) |
| repository / API / DB / cache | `10-data/README.md` + specific subdocument (Phase G) |
| errors | `11-error-and-result-model.md` (Phase H) |
| auth / secure data / session | `12-security-and-session.md` (Phase H) |
| runtime permissions | `13-permissions.md` (Phase H) |
| WorkManager / long work | `14-background-work.md` (Phase F) |
| logs / analytics / crashes / performance | `15-observability/README.md` + specific subdocument (Phase H) |
| environments / feature flags | `16-config-feature-flags-environments.md` (Phase H) |
| tests | `17-testing.md` (Phase I) |
| Gradle / versions / build logic | `18-build-logic-and-dependencies.md` (Phase I) |
| CI / release | `19-ci-release-quality-gates.md` (Phase I) |
| new feature | `20-feature-template.md` + topic-specific docs (Phase E) |
| general coding rules | `21-coding-rules.md` (Phase I) |
| intentional exceptions | `22-known-deviations.md` |
| why an architecture choice exists | `23-decision-log.md` |
