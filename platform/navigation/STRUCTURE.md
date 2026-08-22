# `:platform:navigation`

**Role:** DC-owned destination contracts independent of the navigation engine.

**Owns:** the serializable `Destination` model.

## AI guidance

- Do not add `androidx.navigation` here; the real `NavController`/`NavHost` stays in `:app`.
- Add a destination only with a real screen and composition-root route registration.
- Feature packages may depend on destination contracts, never engine objects.
- Read `architecture/06-navigation.md` before changing route contracts.
- Verify module compilation and `:app:assembleDebug`.
