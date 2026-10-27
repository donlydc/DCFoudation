# Review DOC-A + DOC-B — APPROVE
## Blocking (must fix)
- None.
## Non-blocking
- Fixed by reviewer: bare backlog IDs introduced by DOC-B (SEC-03, PERM-02, NET-02, NET-05, DB-02; 20+ places across modules/*, 10-data/*, 13, 17, 22, 23, HANDOVER F18) now `MP:`-prefixed (DEC-C1-2 heading, DEC-B2-1 text, F18, etc.). Bare SEC-01/SEC-02/OBS-01/OBS-02/PERM-01/NAV-01 are architecture rule IDs and left bare.
- 3 checker WARNs (ambiguous entry symbols DispatcherProvider, DcJobSpec, PreferenceKey) pre-existing.
- Docs still DRAFT; DOC-A/B reports state CodeGraph was not queried (grep/read used) — acceptable, claims verified below.
## Spot-checks performed
- check_architecture.py: ERROR 0 WARN 3; unittest 16 OK; --boundaries-only ERROR 0 (13 modules, 20 edges). Artifacts regenerated after fixes.
- SingleFlight per-execution SupervisorJob completed with execution (SingleFlight.kt:42-57) → matches DEC-A3-1.
- scheduling `api(project(":foundation"))` (build.gradle.kts:13) → matches F2/DEC-A3-7.
- :data:network no :observability edge (only :foundation); :platform:permission build has no compose → F3/F4 RESOLVED correct.
- DcScheduler id≠name → Rejected (identityMismatch) → matches DEC-A3-3.
- F19 verified: AndroidDcAlarmScheduler.restore() calls scheduleAt inside forEach without try/catch; a throw aborts remaining alarms (DcAlarmRestorer only wraps with timeout/finally).
- F20 verified: DcScheduler.observe returns Flow, DcJobSpec has KSerializer, factory takes DispatcherProvider; coroutines/serialization are `implementation`.
- NavigationEffect {Navigate, Back}, DestinationRoutes private hand-maintained `all` → matches 06/platform-navigation.
- PermissionStatus 5 cases; toState lossy; Redactor BEARER matches "Basic <word>" (F14); ArticlesRepository.refresh(): AppError?; AppError.toMessageRes in app tests.
- @Test counts match docs: foundation 12, concurrency 64, scheduling 79, testing 5, network 22, prefs 15, db 12, security 13, observability 27, navigation 14, permission 18, build-logic 6, app unit 26 + 3 androidTest, DS unit 30 (+21 androidTest).
- All 8 lane commit hashes exist (git cat-file). 
- All new DEC entries (DEC-A3-1..8, A4-1, B3-1, B2-1/2, C1-1/2, C3-1) labelled PROPOSED — pending Opus final review; none accepted. KD-02 "Open (partially mitigated by lane A1)". KD-06 stays Open.
- grep PASS/TESTED: no unevidenced claim; all test status NOT_RUN.
