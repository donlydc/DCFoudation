# Cache & offline

[Back to data](README.md)

## Current implementation

`ArticlesRepository` is the reference offline-first policy. Room is the observable source of truth, so cached rows render immediately. A refresh fetches remote data and upserts it into Room; failures leave cached rows untouched and return a failure status so presentation can show stale/offline feedback.

## Rules

**DAT-CACHE-01 — One repository owns remote/local coordination and stale policy.** UI/ViewModel do not merge sources or apply network responses directly; observing the local source reflects successful upserts automatically.

## Related documents

- [`README.md`](README.md), [`repositories.md`](repositories.md)
