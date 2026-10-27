# B3-design — D-01: ownership of DcDatabase / ArticleEntity / ArticleDao
- Status: PROPOSED (Opus finalizes; no move performed). Agent: architecture-writer (Sonnet 5.5).
- Evidence (CodeGraph + source, base 2c0ae05): `DcDatabase.kt:7` `@Database(entities=[ArticleEntity], version=1, exportSchema=true)`; schema `data/database/schemas/com.dc.android.foundation.data.database.DcDatabase/1.json`. Consumers: `:app` only — `di/DatabaseModule.kt` (name `dc_foundation_db`, `RoomDatabaseFactory.create(DcDatabase::class.java, ...)`, `provideArticleDao`), `articles/ArticlesRepository.kt` (5 refs to entity, DAO), `ArticlesViewModelTest` `FakeArticleDao`. `RoomDatabaseFactory` is already schema-generic (`<T : RoomDatabase>`). `TransactionRunner`/`StalenessPolicy` have no production caller.
- Problem: a generic module ships a sample `articles` schema; any consumer importing `DcDatabase` gets it (MP 4.5).

## Options
1. **Keep in `:data:database` as annotated sample** (KDoc "SAMPLE", done in B3). Zero schema/DB risk; no file moves. Cost: module stays non-neutral (CLAUDE.md invariant 9); a second consumer must build its own DB class anyway.
2. **Move `Article*` + `DcDatabase` to `:app`** (package kept or changed), `:data:database` retains only factory/transaction/staleness. Best boundary; `:app` owns schema (the factory doc already says so). Cost: move `schemas/` dir (Room `schemaDirectory` is per-module via `dc.room`; `:app` must apply `dc.room`/KSP), update Hilt `DatabaseModule` imports, test fakes, docs, context-map.
3. **Move to a new `:features:articles-data` (or sample) module.** Cleanest if articles splits into a feature module later. Cost: new module without current build/ownership value (CLAUDE.md "do not create modules speculatively").

## Recommendation
Option 2, executed as a separate refactor after the R5.0 ADR, because the module is generic infra and `:app` is sole consumer. Until then Option 1 stands (KDoc applied). Do not do Option 3 yet.

## Migration constraints (must be preserved)
- Keep `version = 1`, table `articles`, column names/types, DB file name `dc_foundation_db`, and identity hash: Room identity hash derives from the schema, not the class's package/module, so installed DBs open unchanged. Verify by diffing regenerated `1.json` vs the committed one (only `identityHash` equality and `entities` must match; no new `version`).
- Move `schemas/.../1.json` to `app/schemas/<new FQCN>/1.json`. If the class FQCN/package changes, the schema directory name changes; keep the FQCN of `DcDatabase` identical (preferred) to avoid churn, or accept a renamed directory with identical content.
- No migration needed (no schema change); no destructive flag.
- Steps: (a) ADR in `23-decision-log.md`; (b) add `dc.room` to `:app`; (c) `git mv` the 3 sources + schema; (d) fix imports in `DatabaseModule`, `ArticlesRepository`, test; (e) drop `ArticleDao` etc. from `:data:database`; (f) update module docs, `persistence-room.md`, context-map, graphify; (g) `:app:kspDebugKotlin`, `:app:testDebugUnitTest`, `:data:database:testDebugUnitTest`, diff schema JSON.
- Rollback: single revert commit (pure move, no data change; installed DBs unaffected either direction). Land as its own commit, separate from UI work (MP 4.5 note).

## Open questions for Opus
- Can `:app` host Room KSP in its current build (kapt/ksp/Hilt interplay) — NOT verified (Gradle cannot run here).
- Keep FQCN `com.dc.android.foundation.data.database.DcDatabase` inside `:app`, or rename package to `...articles.data`?
