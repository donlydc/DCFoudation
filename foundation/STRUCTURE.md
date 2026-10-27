# `:foundation`

**Role:** Android- and DI-free application primitives shared across capabilities.

**Owns:** `AppError` and retry policy contracts.

## AI guidance

- Keep public APIs business-neutral and free of Android, Hilt, Retrofit, Room, and UI types.
- Add a primitive only when at least two capability modules need the same policy.
- Presentation maps `AppError` to user text; this module never owns resource IDs or messages.
- Verify with `:foundation:test` and focused dependent-module tests.

Implementation guide: [`IMPLEMENTATION.md`](IMPLEMENTATION.md).
