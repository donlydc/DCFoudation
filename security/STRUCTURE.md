# `:security`

**Role:** secure, business-neutral key/value storage.

**Owns:** `SecureStorage`, encrypted Android implementation, and storage-specific failures.

## AI guidance

- Never log or persist secrets outside the encrypted boundary.
- Keep blocking crypto/storage operations main-safe through `DispatcherProvider`.
- Do not add fake authentication/session flows merely to consume this module.
- Read `architecture/12-security-and-session.md` and `11-error-and-result-model.md` before contract changes.
- Verify with `:security:testDebugUnitTest` plus instrumentation when Android Keystore behavior changes.

Implementation guide: [`IMPLEMENTATION.md`](IMPLEMENTATION.md).
