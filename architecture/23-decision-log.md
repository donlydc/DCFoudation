# Architecture decision log

[Back to architecture index](00-index.md)

## Scope

Durable decisions with date, context, decision, alternatives, consequences, affected rules/files. Keep concise; not a meeting log.

## Entries

### DEC-07 — 2026-09-05 — Separate appearance persistence from Android locale ownership

**Context:** Applications need common controls for theme, interface size, dynamic color and language. Persisting all four in custom DataStore state would duplicate Android's per-app locale source of truth; globally overriding Compose density would also interfere with accessibility and responsive breakpoints.

**Decision:** Persist typed appearance values through `AppConfigurationRepository`; apply `DcUiScale` to semantic typography/spacing at the root `DcTheme`, retaining the fixed touch-target floor and system font scale. Manage language only through `AppLanguageController`/`AppCompatDelegate`, with generated locale config and AndroidX compatibility storage. Keep these application-level classes in `:app` until another module consumer earns a separate `:platform:configuration` boundary.

**Alternatives considered:** Store locale twice in DataStore, override `LocalDensity`, let each screen apply configuration, or pre-create a configuration module with one consumer.

**Consequences:** Changes are immediately observable and survive restart; locale stays synchronized with Android App Languages; feature UI receives no raw DataStore or AppCompat APIs. UI scale changes semantic layout/text without weakening accessibility minimums.

**Affected:** `app/config/*`, `MainActivity.kt`, `ui/designsystem/theme/{DcTheme,DcUiScale}.kt`, CFG-02/03, DAT-01, INV-02/08.

### DEC-06 — 2026-09-05 — Promote stable UI primitives, not domain-shaped screens

**Context:** The Trip showcase contained a reusable label/value row and a record-card layout, but copying a Trip-shaped "service screen" into the design system would introduce business assumptions and a large, inflexible API.

**Decision:** Promote the stable label/value policy as business-neutral `DcKeyValueRow`; compose record cards from `DcCard` + `DcListRow` + `DcStatusChip`; keep screen orchestration, filters, formatting, domain status mapping and models in the feature/showcase layer. Product states come from Route/ViewModel and are rendered through `DcContentState`, not through developer-only state controls in the product UI.

**Alternatives considered:** Add a generic `DcServiceScreen`/`DcServiceRecordCard`, or keep duplicating the label/value styling in each feature.

**Consequences:** The same primitives cover trips, orders, bookings, transactions and profiles without leaking their models into `:ui:designsystem`. Feature screens retain layout freedom, while typography, spacing, status and interaction policy stay centralized.

**Affected:** `ui/designsystem/components/DcListComponents.kt`, `app/showcase/trip/*`, `architecture/07-design-system/common-screen-patterns.md`, INV-03, INV-08, DS-04.

### DEC-05 — 2026-09-05 — Apply the UI style guide as a delta over the existing DC design system

**Context:** `:ui:designsystem` already exposed a broad `Dc*` API and a five-layer `DcTheme` override seam, while the requested guide assumed a new `core/design-system` module and `App*` APIs.

**Decision:** Keep `:ui:designsystem` and the `Dc` prefix; install the clean blue/neutral visual language as overridable defaults; keep dynamic color opt-in; preserve adaptive navigation; keep caller-owned dialog visibility; and place business-specific Trip reference screens in debug `:app` showcase code.

**Alternatives considered:** Create the module and API names from the generic prompt, force phone-only `NavigationBar`, or put Trip models in the reusable UI module.

**Consequences:** Existing consumers retain their architecture and can rebrand through `DcTheme`. Public component parameters are additive. Shape defaults intentionally move to 4/8/12/16/20dp, so all shape call sites are audited. Status chips remain visually compact because they are labels; Material owns the touch target of interactive filter chips.

**Affected:** `ui/designsystem/**`, `app/src/main/**`, DS-01..04, INV-03, INV-08.

### DEC-01 — 2026-08-16 — Correct `CLAUDE.md` namespace instead of renaming the codebase

**Context:** Phase B reconnaissance found root `CLAUDE.md` claimed primary namespace `com.dc.foundation`, but the real Android Studio-generated project uses `com.dc.android.foundation` throughout (`app/build.gradle.kts`, every `package` declaration).

**Decision:** Update `CLAUDE.md` to state the verified namespace rather than renaming the package across the codebase.

**Alternatives considered:** Rename `com.dc.android.foundation` → `com.dc.foundation` to match the original intent in `CLAUDE.md`.

**Consequences:** Zero code churn; `CLAUDE.md` now matches reality. If a future decision wants the shorter namespace, it is a deliberate rename with its own decision entry, not a silent fix.

**Affected:** `CLAUDE.md` §1, `architecture/03-project-structure.md` (STR-02), `architecture/22-known-deviations.md` (KD-01).

### DEC-02 — 2026-08-16 — Do not pre-create empty seed docs for unstarted phases

**Context:** `plan/architecture/*` ships placeholder files for all 24 target documents (04-21, `07-design-system/*`, `10-data/*`, `15-observability/*`). Phase B's explicit scope is only `01`, `02`, `03` (plus the always-read `00-index`/`24-ai-reading-guide`).

**Decision:** Only create a root `architecture/*` document when the phase that owns it actually runs. `00-index.md` carries a status table so an AI reader knows a missing file means "not built yet," not "forgotten."

**Alternatives considered:** Copy the full `plan/architecture/` tree into root now as placeholders.

**Consequences:** Keeps `architecture/` honest (INV-10) and avoids the plan's own anti-overengineering constraint (§7) applied to documentation itself — no doc describes an unimplemented capability as if it were designed.

**Affected:** `architecture/00-index.md`, all future phase docs.

### DEC-03 — 2026-08-16 — Look up exact library versions via Maven metadata instead of guessing

**Context:** Phase D needed Hilt/KSP/androidx.hilt versions. An initial `WebSearch`/`WebFetch` pass produced inconsistent numbers (2.56.2 vs 2.59.2 vs 2.60.1) — search-engine-summarized results were not reliable for exact version pins.

**Decision:** For any dependency version that must be exact (build-breaking if wrong), fetch the authoritative `maven-metadata.xml` directly (`repo1.maven.org` via `curl`, or `dl.google.com/dl/android/maven2/.../group-index.xml` for AndroidX/Google artifacts) rather than trusting a search summary.

**Consequences:** Slower than trusting the first search result, but the Gate C/D builds only needed one retry each (both for real compatibility issues, not version mistakes) once this was adopted.

**Affected:** `gradle/libs.versions.toml` (hilt, ksp, androidxHiltCompose), any future phase adding a new dependency.

### DEC-04 — 2026-08-16 — Add a second reference feature (`articles`) instead of building the Reserved capabilities as speculative wrappers

**Context:** Nine architecture areas are still `Reserved`/not-implemented: navigation (NAV-01), network (DAT-NET-01), Room (DAT-ROOM-01), cache (DAT-CACHE-01), background work (BG-01), permissions (PERM-01), and the build-logic convention plugins. Each of those docs already states its own trigger condition for when the capability should move from "Reserved" to "real." The user has asked for all remaining capabilities to be implemented, but the plan's own anti-overengineering constraint (`plan/CLAUDE_IMPLEMENTATION_PLAN.md` §7) and DEC-02 both reject adding a capability with no real call site.

**Decision:** Rather than building the nine Reserved capabilities as speculative wrappers with no real call site, this build cycle adds a second reference feature, `articles` — it fetches from a real remote API, caches offline-first in Room, and is synced periodically via a background worker — specifically so each Reserved capability gets a genuine, non-speculative use case that satisfies its own documented trigger condition:

- NAV-01 triggers on "a second destination exists" → `articles` is that second destination.
- DAT-NET-01 triggers on "a real endpoint exists to call" → `articles` calls one.
- DAT-ROOM-01 triggers on "structured/relational data exists" → the articles cache table.
- DAT-CACHE-01 triggers on "a repository combines RemoteDataSource + LocalDataSource" → `ArticlesRepository`.
- BG-01 triggers on "work must survive app closure/process death with retry guarantees" → the periodic articles sync worker.
- PERM-01 triggers on "a real `<uses-permission>` entry/call site exists" → `POST_NOTIFICATIONS` for the worker.
- convention-plugins trigger on "a 4th or 5th Android module" → this build adds enough new Android modules to cross that threshold.

This decision does **not** cover analytics, crash reporting, or secure storage — those are addressed separately (see `22-known-deviations.md` KD-04/KD-05) because they cannot get a genuine use case within a business-neutral foundation repo without picking a vendor or building fake auth.

**Alternatives considered:** Build the nine capabilities directly as unused/demo wrappers with no real call site; skip the capabilities entirely and leave them `Reserved` despite the user's explicit request.

**Rationale:** This satisfies `plan/CLAUDE_IMPLEMENTATION_PLAN.md` §7's anti-overengineering constraint (no capability added without a genuine near-term use case) while still fulfilling the user's explicit request to implement everything — genuine use cases are manufactured deliberately rather than the constraint being silently ignored.

**Consequences:** NAV-01, DAT-NET-01, DAT-ROOM-01, DAT-CACHE-01, BG-01, PERM-01 rule IDs will be filled in with real rules (not "Reserved") in the phases that follow, once code backs them.

**Affected:** `architecture/06-navigation.md` (NAV-01), `architecture/10-data/network.md` (DAT-NET-01), `architecture/10-data/persistence-room.md` (DAT-ROOM-01), `architecture/10-data/cache-and-offline.md` (DAT-CACHE-01), `architecture/14-background-work.md` (BG-01), `architecture/13-permissions.md` (PERM-01), `build-logic/` convention plugins, all Phase M–T implementation work.
