# Known deviations

[Back to architecture index](00-index.md)

## Scope

Explicit intentional/current violations or non-yet-satisfied states of architecture rules. Each entry needs rule ID, evidence path, reason, risk, owner/exit condition.

## Entries

### KD-01 — Root `CLAUDE.md` previously stated the wrong primary namespace

**Rule affected:** STR-02 (`03-project-structure.md`)

**Evidence:** `app/build.gradle.kts` — `namespace = "com.dc.android.foundation"`; every `package` declaration under `app/src` matches. Root `CLAUDE.md` §1 previously read `**Primary namespace:** com.dc.foundation`.

**Reason:** `CLAUDE.md` §1 was written before the Android Studio project was generated; the generated project used `com.dc.android.foundation` instead. Per the project's own architecture-authority order (`CLAUDE.md` §6: verified source ranks above the high-level summary document), verified reality wins.

**Risk:** Low — documentation-only mismatch, no code impact.

**Resolution:** Corrected in Phase B — `CLAUDE.md` §1 updated to state `com.dc.android.foundation`. Not treated as an open deviation going forward; kept here as the audit trail. See `23-decision-log.md` DEC-01.

### KD-02 — No enforced module boundaries yet

**Rule affected:** DEP-01, DEP-04 (`02-layers-and-dependencies.md`)

**Evidence:** `settings.gradle.kts` — `include(":app")` only.

**Reason:** The project is pre-Phase-C; no capability has yet earned a dedicated Gradle module per the anti-overengineering constraint (`plan/CLAUDE_IMPLEMENTATION_PLAN.md` §7).

**Risk:** None currently — there is nothing to violate with a single module.

**Owner/exit condition:** Closes automatically as modules are introduced by Phase C onward; each new module must restate STR-01's one-line responsibility in `03-project-structure.md`.
