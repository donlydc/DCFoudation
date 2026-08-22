# `:runtime:background`

**Role:** business-neutral persistent-work scheduling policy.

**Owns:** `PeriodicSyncScheduler` and WorkManager request/deduplication policy.

## AI guidance

- Keep feature-specific `Worker` implementations and app-Hilt integration in `:app`.
- Use WorkManager only for work that must survive process death or retry persistently.
- Do not depend on feature implementations or expose unnecessary WorkManager details upstream.
- Read `architecture/14-background-work.md` before changing scheduling behavior.
- Verify with `:runtime:background:testDebugUnitTest` and app assembly when integration changes.
