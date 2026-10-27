# Background work and scheduling

[Back to architecture index](00-index.md)

## Scope

Persistent/retriable WorkManager jobs and user-visible, wall-clock alarm/reminder scheduling.

## Current implementation

`:runtime:scheduling` is the business-neutral scheduling boundary. `DcScheduler` is the only
public entry point for application scheduling: it routes `After` and `Every` triggers to
WorkManager, and `ExactAt`, `DailyAt`, and `WeeklyAt` triggers to AlarmManager. This distinction
is deliberate: WorkManager's delay and periodic APIs are timing-minimum/best-effort mechanisms,
not a replacement for an alarm that must fire at a precise wall-clock time.

- `AndroidDcWorkScheduler` owns typed `DcJobSpec`, constraints, WorkInfo-to-`DcJobState` mapping,
  early rejection of periodic intervals below 15 minutes, and bounded retries through
  `DcRetryPolicy`. `DcWorker` rethrows coroutine cancellation and converts other uncaught errors
  to a terminal `AppError` rather than allowing them to retry forever.
- `AndroidDcAlarmScheduler` persists each `DcAlarm` as a small JSON file, uses immutable,
  explicit PendingIntents, and computes recurring daily/weekly occurrences with `java.time`.
  `DcAlarmRestorer` is manifest-merged and restores stored alarms after boot, package replacement,
  time-zone, and wall-clock changes. The app supplies the required permissions because choosing
  `SCHEDULE_EXACT_ALARM` versus `USE_EXACT_ALARM` is a product/Play-policy decision.
- Exact schedule requests check `canScheduleExactAlarms()` on every API 31+ device before calling
  `setExactAndAllowWhileIdle()` or `setAlarmClock()`. A missing grant returns
  `DcScheduleOutcome.NeedsExactAlarmPermission`; it is never silently downgraded.
- `ArticlesSyncWorker` remains an `@HiltWorker` in `:app`, where its `ArticlesRepository` lives,
  but now extends `DcWorker<Unit>`. `DcApplication` creates the facade from its explicit
  process-lifetime `AppCoroutineScope` to enqueue the 15-minute, connected unique sync work without
  blocking the main thread. The scheduling module imports no Hilt.

Contract details added by lane A3 (`bb5ce19`; decisions PROPOSED — pending Opus final review, DEC-A3-3..6; Gradle NOT_RUN):

- Work identity: `id` must equal `DcJobSpec.name`, else `DcScheduleOutcome.Rejected` (cancel/observe address the unique work by name).
- Idempotency: `After` and `Every` use KEEP (first request wins; a later call still returns `Scheduled`; a changed `Every` interval/constraints is not applied); alarms are upserted by id.
- Retry layers: `DcRetryPolicy` is OS-driven, survives process death, backoff is WorkManager's (>= 10 s, no jitter) and its ceiling is enforced in `DcWorker`; `RetryPolicy` in `:runtime:concurrency` is in-process and dies with the process. Both count `maxAttempts` as total executions; do not nest `retrying` around a `DcJobResult.Retry` path (multiplicative attempts).
- Missed policy: `DcMissedPolicy` only affects `ExactAt`; repeating `DailyAt`/`WeeklyAt` alarms always re-arm the next occurrence on restore (pure `restoreDecision`).
- Corrupt entries: the stored alarm list is decoded per entry; one stale entry (removed receiver class, unknown zone, bad enum) is dropped without discarding the rest.
- Open (device/Robolectric needed): `restore()` aborts remaining alarms if one `scheduleAt` throws; no restore when exact-alarm permission is granted later (MP:SCH-04); orphan platform alarm if the store is lost.

## Rules

### BG-01 — Select the engine from the required delivery guarantee

**Rule:** Use `DcScheduler` instead of direct WorkManager/AlarmManager calls. Persistent,
retriable work that may run late uses `DcTrigger.After` or `DcTrigger.Every` and a `DcJobSpec`.
User-visible actions that must fire at a wall-clock time use `ExactAt`, `DailyAt`, or `WeeklyAt`
with an alarm request. Screen-scoped work stays in `viewModelScope`.

**Why:** A WorkManager periodic interval is at least 15 minutes and may be delayed by constraints
or Doze. Conversely, using an exact alarm for cache cleanup wastes restricted platform resources.
The trigger type makes the delivery guarantee explicit at the call site.

**Verification:** Search production app/feature code for direct `WorkManager` and `AlarmManager`
usage; only runtime scheduling infrastructure and WorkManager configuration/Hilt worker wiring may
use the framework types. `grep GlobalScope` across `app/src`, `runtime/`, and `data/` is empty.

### BG-02 — Exact alarms are opt-in and restoreable

**Rule:** Apps declare the appropriate exact-alarm and `RECEIVE_BOOT_COMPLETED` permissions,
handle `NeedsExactAlarmPermission` in UI, and use an app-specific `DcAlarmReceiver` for delivery.
The receiver must do only short user-visible work or return `HandOff` for WorkManager.

**Why:** Exact-alarm grants are special app access and may be denied/revoked. AlarmManager clears
scheduled alarms during reboot and can remove them when exact-alarm access is revoked; durable
storage plus restore is necessary for a reminder product to retain user intent.

**Verification:** Unit tests cover normal, DST gap/overlap, and weekly occurrence calculations;
the module test suite verifies retry/interval and alarm-codec policy. Instrumented tests for
`WorkManagerTestInitHelper` and real boot broadcasts remain device/emulator work.

## Testing / validation

The JVM suite verifies date/time and policy logic. Android framework integrations (actual alarm
delivery and WorkManager `TestDriver`) require an emulator/device and are intentionally not claimed
as executed by host-only Gradle tasks.

## Related documents

- [`09-concurrency-and-threading.md`](09-concurrency-and-threading.md)
- [`13-permissions.md`](13-permissions.md)
- [`23-decision-log.md`](23-decision-log.md)
