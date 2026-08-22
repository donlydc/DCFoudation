# `:data:database`

**Role:** Room persistence infrastructure and the reference article schema.

**Owns:** `DcDatabase`, `ArticleEntity`, and `ArticleDao`.

## AI guidance

- DAO/entity types stop at repository boundaries; never expose them to feature UI state.
- Add migrations and schema verification when changing persisted structure.
- Keep network DTO mapping outside this module.
- Read `architecture/10-data/persistence-room.md`, `cache-and-offline.md`, and `mapping-boundaries.md` before schema changes.
- Verify Room compilation and `:data:database:testDebugUnitTest`.
