# Context protocol (mandatory AI discovery sequence)

[Back to architecture index](00-index.md) · Origin: Master Plan 3.5 and 10.4, adapted to this repository.

Goal: any AI agent starting a task in any module reads the right docs, code, graph and tests, instead of guessing from class names. Architecture docs are navigation and rules; **verified source/build files stay the truth** (CLAUDE.md section 6).

## A. Discovery sequence (in order; do not skip)

| Step | Action | Output |
|---|---|---|
| 0 | Classify the task: bug / feature / refactor / API change / test / docs. Note `git rev-parse HEAD` and `git status --short`. | task type, base commit |
| 1 | Read `CLAUDE.md` and `SKILLS.md` (tool order and rules). | tool order, hard rules |
| 2 | Read `architecture/00-index.md`, then `01-overview-and-invariants.md` (INV-xx). | invariants |
| 3 | Open `architecture/context-map.yaml`; find the module or feature entry by `gradle_path`, `id` or symbol. | `doc`, `source_roots`, `test_roots`, `entry_symbols`, `depends_on`, `owners_rules` |
| 4 | Read that module/feature page (`architecture/modules/<m>.md` or `features/<f>.md`) and only the rule docs named in `owners_rules` (e.g. `13-permissions.md` for PERM-01). Check `22-known-deviations.md` / `23-decision-log.md` for KD/DEC on the area. | contracts, boundaries, debt |
| 5 | Run CodeGraph first: `codegraph_explore` with `projectPath` = repo root and the `entry_symbols` (shell fallback: `codegraph explore "<symbols>"`). Use its blast radius (callers, tests). If the index looks stale run `codegraph sync`; if it fails, say so and fall back to Grep/Glob. | verbatim source, callers, impact |
| 6 | Optional: Graphify (`graphify query ...`) only if `graphify-out/graph.json` exists (currently absent: `reports/wp/R0.1.md`). Skip silently otherwise. | architecture map |
| 7 | Read targeted source and build files (`build.gradle.kts`, Hilt module in `:app/.../di/`) to confirm what the docs claim; read `dependency-rules.md` before touching any `project(...)` edge. | verified facts |
| 8 | Read existing tests under `test_roots` and the test kit in `:testing`. | test baseline, fakes |
| 9 | Plan: as-is -> target -> affected modules -> risks -> test strategy. Preserve public API unless a migration is proposed. | plan |
| 10 | Implement the smallest coherent change; run focused tests, then broader checks if justified. | evidence |
| 11 | Refresh docs (section B), then hand over (section C). | docs delta, report |

Rules of the protocol:
- A conflict between docs and source is recorded as drift (report section D) and never silently "fixed" by changing architecture; resolve per CLAUDE.md section 6.
- Two architecture rules in conflict that source cannot resolve: stop and report for `critical-architecture-auditor`.
- Doc status (`DRAFT | VERIFIED | NEEDS_REFRESH`) and test status (`NOT_RUN | TESTED | FAILED | BLOCKED`) are separate fields (legends in `modules/TEMPLATE.md`). Mark a page `VERIFIED` only with a commit SHA and audit date; mark tests `TESTED`/`FAILED` only with an evidence path to logged command output, otherwise `NOT_RUN`.
- State explicitly in the final report whether CodeGraph was queried and whether the index matches the commit (MP 10.4).

## A2. ID convention (rule IDs vs master-plan backlog)

Rule IDs from `architecture/01..24` and the topical docs (for example `PERM-01`, `NAV-01`, `OBS-01`, `SEC-01`, `TST-01`, `UI-01`, `BLD-01..03`) and the master-plan backlog (`plan/FOUNDATION_MASTER_PLAN.md` section 4) reuse the same prefixes with different meaning. Convention:

- **A bare ID is an architecture rule**: `PERM-01` = `13-permissions.md`; `TST-01` = `17-testing.md`. `owners_rules` in `context-map.yaml` holds bare rule IDs only.
- **A master-plan backlog item is written `MP:<ID>`**: `MP:PERM-01` = "state machine + notification contract" in MP 4.8; `MP:TST-01` = fake parity in MP 4.11. Applies to every `architecture/modules/*.md` and `architecture/features/*.md` page and to reports, work-package titles and commit messages. `DEC-xx` and `KD-xx` are not affected.
- A module page that cites a rule ID whose text lives outside `modules/` ends with a one-line "Rule glosses" bullet (gloss only; the rule doc stays authoritative).

## B. Refresh rules (MP 10.4)

| Change in the PR | Must update |
|---|---|
| Public API, dependency, state machine, lifecycle, DI, system behavior of a module | `architecture/modules/<m>.md`; plus `module-map.md` and `context-map.yaml` if pointers, deps or ownership changed |
| Any `project(...)` edge | `dependency-rules.md` tables, `context-map.yaml` `depends_on`, `module-map.md` |
| Feature flow | `architecture/features/<f>.md` |
| Principle-level decision | `23-decision-log.md` (alternatives, trade-offs, migration) |
| Open or close a known deviation | `22-known-deviations.md` with evidence |
| UI component | design-system registry/catalog, previews, tests, usage guide (`07-design-system/`, DS-04) |
| New module or feature skeleton | `settings.gradle.kts`, `context-map.yaml` entry, doc page from TEMPLATE; the generator emits the skeleton, AI fills it from source; an empty template is never VERIFIED |
| Large edits | `codegraph sync`; `graphify update .` only when topology changed and a graph exists |

Keep `context-map.yaml` pointers-only (no copied code). Doc-drift checking: `python3 tools/check_architecture.py` (R1.3): module coverage, links, headings, `entry_symbols`, edges vs `dependency-rules.md`, and no `TESTED` without an evidence path.

## C. Handoff report convention

Every work package ends with `reports/wp/<WP-ID>.md` (max 120 lines) using the template in `plan/AGENT_EXECUTION_PLAN.md` section 6:

```text
# <WP-ID> - <title>
- Agent / model:
- Base commit -> result commit:
- CodeGraph: queried? index matches commit?
## A. As-is evidence     (file:line, symbol, main callers)
## B. Changes            (files, API delta, related DEC/KD)
## C. Verification       (command -> result; NOT_RUN + reason)
## D. Docs drift         (docs != source)
## E. Risks / next steps
```

Reports hold evidence and stay short: command + result, with long logs stored under `reports/logs/` and referenced by path. A change that touches files outside the work package scope stops and is escalated to the main session.
