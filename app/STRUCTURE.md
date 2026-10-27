# `:app`

**Role:** composition root, application shell, and reference features.

**Owns:** `DcApplication`, `MainActivity`, Hilt wiring in `di/`, environment/user configuration, the `greeting`/`articles` feature packages, and the `showcase/trip` design-system demo screens (not a real feature).

## AI guidance

- Follow each feature's Route → ViewModel/UDF → repository shape; Composables receive state and callbacks only.
- Keep vendor/framework wiring here when it requires the app Hilt graph; reusable policy belongs in an owning library module.
- Apply user appearance configuration only at `MainActivity`'s `DcTheme`; language changes go through `AppLanguageController`.
- Features must not import another feature's private implementation.
- Read `architecture/04-app-bootstrap-and-lifecycle.md`, `05-presentation-udf.md`, and `20-feature-template.md` before structural changes.
- Verify with focused `:app` tests, then `:app:assembleDebug`.
