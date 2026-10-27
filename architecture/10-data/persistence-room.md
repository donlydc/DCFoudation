# Persistence (Room)

[Back to data](README.md)

## Current implementation

`:data:database` contains `RoomDatabaseFactory`, `RoomTransactionRunner` and `StalenessPolicy` as reusable persistence policy, plus `DcDatabase`, `ArticleDao` and `ArticleEntity` for the real structured articles cache. The factory routes Room query and transaction work through `DispatcherProvider.io`; schemas are exported by Room. DAO reads expose `Flow`; writes are suspending. `DatabaseModule` creates the singleton database/DAO at the app composition root. The articles schema remains a reference implementation and is not a business-neutral public model for downstream apps; moving it fully into an app-owned schema is the next boundary refinement when feature modules are split.

## Ownership proposal (D-01, PROPOSED — pending Opus final review)

Lane B3 (`c2e7331`) labelled `DcDatabase`, `ArticleEntity` and `ArticleDao` as SAMPLE and the factory/transaction/staleness types as GENERIC in KDoc, and added `StalenessPolicy` boundary tests (NOT_RUN). Moving the sample schema to `:app` is proposed as DEC-B3-1 (`reports/wp/B3-design.md`); it is not performed. `RoomTransactionRunner` transaction semantics tests (MP:DB-02) need a Room test stack that the version catalog lacks.

## Rules

**DAT-ROOM-01 — DAO/entity types do not cross the repository mapping boundary.** Feature presentation consumes feature/domain models; only the repository/local data source sees Room types.

## Related documents

- [`README.md`](README.md), [`preferences-datastore.md`](preferences-datastore.md)
