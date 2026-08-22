# Persistence (Room)

[Back to data](README.md)

## Current implementation

`:data:database` contains `DcDatabase`, `ArticleDao` and `ArticleEntity` for the real structured articles cache. DAO reads expose `Flow`; writes are suspending. `DatabaseModule` creates the singleton database/DAO at the app composition root. The articles schema is a reference implementation, not a business-neutral public model for downstream apps.

## Rules

**DAT-ROOM-01 — DAO/entity types do not cross the repository mapping boundary.** Feature presentation consumes feature/domain models; only the repository/local data source sees Room types.

## Related documents

- [`README.md`](README.md), [`preferences-datastore.md`](preferences-datastore.md)
