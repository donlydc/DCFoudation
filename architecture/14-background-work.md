# Background work

[Back to architecture index](00-index.md)

## Scope

Persistent/retriable background work policy (WorkManager vs. a plain coroutine).

## Current implementation

**Not implemented.** No feature has work that must survive app closure or process death with retry guarantees — `GreetingViewModel`'s work is a `viewModelScope` coroutine, correctly scoped to the screen's lifecycle, not a `Worker`. No `androidx.work` dependency exists. Per the anti-overengineering constraint, WorkManager is added when a real persistent-work use case appears (e.g. a sync job), not preemptively.

## Rules

**BG-01 — Reserved.** No rule yet — nothing to enforce with zero background jobs.

## Testing / validation

N/A yet.

## Related documents

- [`09-concurrency-and-threading.md`](09-concurrency-and-threading.md)
