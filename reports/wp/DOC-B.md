# DOC-B - Merge lane B1, B2, B3, C1, C2, C3 doc deltas into architecture/**
- Agent / model: architecture-writer, Sonnet 5.5
- Commit gốc → commit kết quả: HEAD (lane commits A3 bb5ce19, B3 c2e7331, C1 aca166d, C3 b7487f0, C2 64da185, B1 7a8a804, B2 8b6a6fe) → uncommitted working tree on top of the uncommitted DOC-A edits (kept)
- CodeGraph: not queried; facts were checked by reading committed source (Redactor, Logging*, EncryptedSharedPreferencesSecureStorage, NavigationEffect, PermissionStatus/Requester, AppErrorMessages, ArticlesRepository/ViewModel/Route, FeatureScaffoldGenerator) and `@Test` counts via grep.

## A. As-is evidence
- All `reports/wp/{B1,B2,B3,C1,C2,C3}.md` section D plus `B2-design`, `B3-design`, `C1-design`, `C3-design` were applied; each claim re-checked against source, notably: B2 read path maps `SecurityException` to null; `Redactor.BEARER` matches `Basic <word>` (false positive); `LoggingCrashReporter` forwards the throwable to `Logger.e`; `DestinationRoutes.all` is a private hand-maintained list; `FakePermissionRequester` is in main source; `ArticlesRepository.refresh(): AppError?`; manifest has `INTERNET` + `POST_NOTIFICATIONS`; `ArticlesViewModel` has no `SavedStateHandle`; Greeting still hard-codes "Refreshed"; `DcAlarmReceiver/Restorer` use plain `CoroutineScope(SupervisorJob()+...)`.
- Test counts (`@Test`): network 22, preferences 15, database 12, security 13, observability 27, navigation 14, permission 18, build-logic 6, app unit 26 (+3 instrumented).

## B. Thay đổi (docs only; no code, no commit)
- Edge fix (checker ERROR 0): removed `:observability` from `:data:network` in `context-map.yaml`, `dependency-rules.md` (edge table + actual-edge block), `module-map.md`; added entry symbols for navigation/permission in `context-map.yaml`.
- Module pages: `data-network`, `data-preferences`, `data-database`, `security`, `observability`, `platform-navigation`, `platform-permission`, `app`, `build-logic` (contracts, tests, backlog statuses, change-log rows).
- Feature pages: `articles` (AppError?, @StringRes, INTERNET fixed, SavedStateHandle removed, 12 tests, journey notes), `greeting` (typed snackbar, "Refreshed" follow-up); `TEMPLATE.md` unchanged (C3 headings match).
- Rule docs: `06-navigation` (D-02 section), `10-data/{network,persistence-room,preferences-datastore}`, `11-error-and-result-model`, `12-security-and-session` (testing), `13-permissions`, `15-observability/{logging,analytics,crash-and-performance}`, `17-testing` (inventory table, NOT_RUN), `20-feature-template`, `00-index`.
- `22-known-deviations.md`: KD-06 proposed fix added (stays Open); new "Resolved defects and notes" section: missing INTERNET fixed by C2, DEC-A3-2 static receiver scopes (no KD: contradicts neither THR-01 nor THR-03), KD-04/KD-05 stay Open.
- `23-decision-log.md`: PROPOSED (pending Opus final review) DEC-B3-1 (D-01), DEC-B2-1 (D-04), DEC-B2-2 (OBS-02 + Redactor), DEC-C1-1 (D-02), DEC-C1-2 (PERM-02 shape), DEC-C3-1 (D-05).
- `HANDOVER.md`: F1, F3, F4, F5 resolved with lane commits; F8, F9, F10 annotated; stale "no unit tests" bullet and KD-06 row updated; lane commit table; new follow-ups F12-F21 (FakePermissionRequester in main, DestinationRoutes list, Redactor false positives, SecureStorage no clear(), crash reporter throwable, Greeting "Refreshed", DB-02 test stack, restore() abort, scheduling api exposure + deprecated snackbar overload, unregistered generated pages/STRUCTURE.md drift).

## C. Kiểm chứng
- `python3 tools/check_architecture.py --md reports/architecture-check.md --json reports/architecture-check.json` → ERROR 0, WARN 3 (pre-existing ambiguous entry symbols), 14 modules, 3 features, 20 edges.
- `python3 -m unittest discover -s tools -p 'test_*.py'` → 16 tests OK.
- Gradle and all Kotlin tests: NOT_RUN (dl.google.com, api.foojay.io blocked). Doc status DRAFT everywhere; no TESTED/PASS claims.

## D. Drift tài liệu
- Not edited (outside allowed writes): `platform/navigation/STRUCTURE.md`, `platform/permission/STRUCTURE.md`, `data/network/STRUCTURE.md` (points at `architecture/10-data/network.md`).
- `modules/app.md` section 6 intro (F6) and `07-design-system/component-roadmap.md` (DOC-A) not re-audited.
- F19/F20 (restore() abort, scheduling `implementation` exposure) are carried from lane reports (A3.md line 34, task brief), not re-read in source.

## E. Rủi ro / việc tiếp theo
- Opus: rule on all PROPOSED DEC entries and allocate final numbers; decide D-01 move, D-04 provider, D-05 publishing, `SecureStorage.clear()`, DB-02 test stack.
- Run `codegraph sync` and `graphify update .`; then the Gradle gates once the network is available.
