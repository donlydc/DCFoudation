# DOC-A - Merge lane A3 + A4 doc deltas into architecture/**
- Agent / model: architecture-writer, Sonnet 5.5
- Commit gốc -> commit kết quả: HEAD bb5ce19 (A3) + 17b4703 (A4) -> uncommitted working tree (architecture/**, HANDOVER.md, reports only)
- CodeGraph: not queried. Facts were checked with grep/read against committed source: SingleFlight `putIfAbsent`/`SupervisorJob`/`invokeOnCompletion` (`SingleFlight.kt:42-57`), `identityMismatch` (`DcScheduler.kt:138,149,174`), `delayFor` (`RetryPolicy.kt:63`), `cancelChildren` (`FlowSharing.kt:51`), `api(project(":foundation"))` (`runtime/scheduling/build.gradle.kts:13`), `restoreDecision` (`DcAlarm.kt:66`), `ElevationTokens`/`StrokeTokens` in `SurfaceTokens.kt`, and `@Test` counts per file (foundation 12, concurrency 64, scheduling 79, testing 5, DS unit 30).

## A. As-is evidence
- Deltas taken from `reports/wp/A3.md` section D, `A3-design.md` (11 decisions) and `A4.md` section D. All numbers above matched source.

## B. Thay đổi
- Module pages: `foundation`, `runtime-concurrency`, `runtime-scheduling`, `testing`, `ui-designsystem` (contracts, edge cases, tests, backlog status, change-log row). Still DRAFT; Test status NOT_RUN; the A3 scratch run is cited as "scratch evidence only, not a Gradle gate".
- Rule docs: `09` (ownership paragraph, new THR-03 labelled PROPOSED, testing approach), `11` (cause/toString, exhaustiveness, lossy kind), `14` (id==name, KEEP idempotency, retry layers, missed policy, corrupt entries, open defects), `07-design-system/{README,components,previews-and-catalog}.md` (COMPONENTS.md pointer, tokens/theme lists, DS-03 reworded to `LocalDcSpacing.current`, snackbar deprecation, catalog counts).
- `dependency-rules.md`, `module-map.md`, `context-map.yaml`: scheduling -> foundation noted as `api` (edge set unchanged); `registry:` key added for `ui-designsystem`; notes on new tests (test_roots already cover the new files).
- `23-decision-log.md`: DEC-A3-1..8 and DEC-A4-1 (screenshot stack), each labelled "PROPOSED - pending Opus final review", placeholder numbers. Static receiver scopes recorded as a proposed deviation inside DEC-A3-2; `22-known-deviations.md` NOT edited (no KD number allocated).
- `HANDOVER.md`: F2 marked RESOLVED by `bb5ce19` (Gradle NOT_RUN, same-class leaks still open); F8 and F10 annotated with A4 `17b4703` / A3 `bb5ce19`.

## C. Kiểm chứng
- `python3 tools/check_architecture.py --md reports/architecture-check.md --json reports/architecture-check.json` -> ERROR 1, WARN 3. The one ERROR is NOT from this lane: `[edges] :data:network: context-map depends_on != build file (map-only: [':observability'])`, because another lane's uncommitted change removed `implementation(project(":observability"))` from `data/network/build.gradle.kts` (HANDOVER F3). It clears when that lane's doc delta drops the edge from `context-map.yaml`/`dependency-rules.md`/`module-map.md`. WARNs (ambiguous entry symbols DispatcherProvider, DcJobSpec, PreferenceKey) are not from this lane. My edits introduced no new ERROR/WARN.
- Gradle and all tests: NOT_RUN.

## D. Drift tài liệu
- `07-design-system/component-roadmap.md` W-status vs A4 additions was not read (A4 also did not read it): unchecked.
- `17-testing.md` and `00-index.md` not updated (no link to COMPONENTS.md in the index; DEC-08 text itself left unchanged).

## E. Rủi ro / việc tiếp theo
- Opus: rule on DEC-A3-1..8, DEC-A4-1, THR-03, STABLE/EXPERIMENTAL proposals; allocate final DEC/KD numbers; consider a KD for static receiver scopes.
- After the other lane lands the `:data:network` doc delta, re-run the checker for ERROR 0.
- Run `codegraph sync` / `graphify update .`; Gradle gates for foundation, concurrency, scheduling, testing, designsystem, `:app`.
