# Semantic theme

[Back to design system](README.md)

## Scope

How primitive tokens become the Material3 `MaterialTheme` that feature UI actually reads.

## Current implementation

`theme/DcTheme.kt` — the single Compose theme entry point:

- `darkTheme: Boolean = isSystemInDarkTheme()` — follows system by default.
- `dynamicColor: Boolean = true` — on API 31+ (`Build.VERSION_CODES.S`), uses `dynamicLightColorScheme`/`dynamicDarkColorScheme` from `LocalContext.current`; below API 31, or when `dynamicColor = false`, falls back to the token-built `LightColorScheme`/`DarkColorScheme`.
- Wires `MaterialTheme(colorScheme = ..., typography = DcTypography, shapes = DcShapes, content = ...)`.

`MainActivity.kt` roots its tree in `DcTheme { Scaffold { Greeting(...) } }` — the only consumer today.

## Rules

Governed by [`README.md`](README.md) DS-01. No new rule IDs here.

## Testing / validation

`DesignSystemCatalog`'s light/dark previews (`dynamicColor = false`, so the preview is deterministic regardless of host OS version) exercise both color schemes.

## Related documents

- [`tokens.md`](tokens.md)
- [`typography.md`](typography.md)
