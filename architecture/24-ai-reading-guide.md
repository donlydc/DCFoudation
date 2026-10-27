# AI reading guide

[Back to architecture index](00-index.md)

## Goal

Minimize token usage and reduce accidental cross-domain assumptions by loading only architecture relevant to the task.

## Always read

- `00-index.md`
- `01-overview-and-invariants.md`
- `context-protocol.md` and `context-map.yaml`: after `CLAUDE.md`, find the module/feature entry and open its page under `modules/` or `features/` before the topic route below.

Then choose the narrowest route below. If the target document doesn't exist yet, check `00-index.md`'s status table and `../PLAN_REPORT.md` before assuming it's missing by mistake — it likely just belongs to a phase that hasn't run.

| Task signal | Read next | Usually avoid unless directly affected |
|---|---|---|
| Compose component/theme/style | `07-design-system/README.md` + one relevant DS file (Phase C) | network, database, CI |
| screen/ViewModel/state/effect | `05-presentation-udf.md`, `09-concurrency-and-threading.md`; add `11` if errors (Phase E/F/H) | Room/build logic |
| navigation/deep link | `06-navigation.md`, `05-presentation-udf.md` (Phase E) | network/database |
| add Hilt binding / inject dependency | `08-dependency-injection-hilt.md` (Phase D), `02-layers-and-dependencies.md` | full design system |
| coroutine/Flow/thread bug | `09-concurrency-and-threading.md` (Phase F); add owning layer doc | unrelated UI style docs |
| repository/API | `10-data/repositories.md`, `10-data/network.md`, `11-error-and-result-model.md` (Phase G/H); add `12` for auth | design-system details |
| Room/local persistence | `10-data/persistence-room.md`, `10-data/mapping-boundaries.md` (Phase G) | navigation |
| DataStore/preferences/theme preference | `10-data/preferences-datastore.md` (Phase G) + owner doc such as DS/config | network |
| caching/offline | `10-data/cache-and-offline.md` (Phase G), repositories/network/persistence as needed | unrelated observability |
| auth/session/security | `12-security-and-session.md` (Phase H), network/error docs as needed | all design-system subdocs |
| permission | `13-permissions.md` (Phase H), presentation doc | Room/network unless permission changes them |
| WorkManager/background | `14-background-work.md`, `09-concurrency-and-threading.md` (Phase F) | typography/components |
| logging/analytics/crash/performance | specific `15-observability/*` file (Phase H) | full data tree |
| Gradle/module/dependency update | `18-build-logic-and-dependencies.md` (Phase I), `02-layers-and-dependencies.md` | feature implementation docs unless dependency impacts them |
| CI/release | `19-ci-release-quality-gates.md`, `18-build-logic-and-dependencies.md` (Phase I) | presentation details |
| new feature | `20-feature-template.md` (Phase E), `05`, `06`, then exact platform/data docs it needs | every optional capability |

## Search-before-read protocol

1. Classify the task using the table.
2. CodeGraph first (CLAUDE.md section 3a, `context-protocol.md` step 5): `codegraph_explore` with the `entry_symbols` from `context-map.yaml` or a natural-language question (shell fallback `codegraph explore "<symbols>"`). Then, if `graphify-out/graph.json` exists and the question is broad ("how does X relate to Y"), `graphify query "<question>"`. Only then `rg`/`git grep`/`Glob`, and for non-code files (docs, Gradle, XML) or when CodeGraph returns nothing relevant.
3. Read only relevant architecture docs.
4. Read target source and immediate dependencies.
5. Expand context only when evidence shows a cross-cutting impact.

## Escalation

If documents conflict, do not read every document hoping to resolve it. Gather the conflicting rule IDs + source evidence and invoke the critical architecture auditor (Opus, per `plan/CLAUDE_IMPLEMENTATION_PLAN.md` §1.1) under the plan's escalation rules.

## Architecture update trigger

Update docs only when a change modifies a boundary, invariant, public contract, lifecycle/thread ownership, DI scope, design-system rule, data source-of-truth rule, or other durable architecture behavior. Ordinary local implementation details do not require architecture churn.
