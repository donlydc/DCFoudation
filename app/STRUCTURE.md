# `:app`

**Role:** composition root, application shell, and reference features.

**Owns:** `DcApplication`, `MainActivity`, Hilt wiring in `di/`, environment config, and the `greeting`/`articles` feature packages.

## AI guidance

- Follow each feature's Route → ViewModel/UDF → repository shape; Composables receive state and callbacks only.
- Keep vendor/framework wiring here when it requires the app Hilt graph; reusable policy belongs in an owning library module.
- Features must not import another feature's private implementation.
- Read `architecture/04-app-bootstrap-and-lifecycle.md`, `05-presentation-udf.md`, and `20-feature-template.md` before structural changes.
- Verify with focused `:app` tests, then `:app:assembleDebug`.
