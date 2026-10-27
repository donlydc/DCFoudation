# `:runtime:scheduling`

**Role:** business-neutral scheduling facade which routes durable background jobs to WorkManager
and exact user-facing triggers to AlarmManager.

**Owns:** typed work specifications/worker policy, alarm persistence and restoration, DST-safe
occurrence calculations, and the `DcScheduler` facade.

## AI guidance

- Read `architecture/14-background-work.md` before changing scheduling policy.
- Feature-specific `@HiltWorker` and `BroadcastReceiver` implementations remain in `:app`.
- Use `DcScheduler`; app/feature code must not call `WorkManager` or `AlarmManager` directly.
- WorkManager is for durable/retriable work, never a precise alarm. Exact alarms return an
  explicit permission outcome rather than silently downgrading.
- This module has no Hilt dependency. Its Android framework receivers use the module-owned
  coroutine scope only to finish short framework callbacks; business work must hand off to a job.
- Verify with `:runtime:scheduling:testDebugUnitTest` and app assembly after integration changes.

Implementation guide: [`IMPLEMENTATION.md`](IMPLEMENTATION.md).
