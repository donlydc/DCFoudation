# `:testing`

**Role:** reusable test-only rules and fakes shared across modules.

**Owns:** dispatcher rules/providers and observability fakes.

## AI guidance

- Keep utilities deterministic, minimal, and free of production behavior.
- Expose dependencies with `api` only when public fake/rule signatures require their types.
- Production modules may consume this module only from test configurations.
- Read `architecture/17-testing.md` before adding shared test abstractions.
- Verify with `:testing:testDebugUnitTest` and at least one consuming module test.
