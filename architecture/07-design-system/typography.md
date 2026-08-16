# Typography

[Back to design system](README.md)

## Scope

Semantic text-style scale wired into `MaterialTheme.typography`.

## Current implementation

`theme/DcTypography.kt` overrides only `bodyLarge` (`FontFamily.Default`, `FontWeight.Normal`, 16sp/24sp line height/0.5sp letter spacing, carried over unchanged from the stock template's `Type.kt`). Every other Material3 type role (`titleLarge`, `labelSmall`, etc.) falls back to the Material3 default scale — not yet customized, since nothing in the app uses them.

`components/DcText.kt` defaults its `style` param to `MaterialTheme.typography.bodyLarge`.

## Rules

No new rule IDs — governed by [`README.md`](README.md) DS-01.

## Testing / validation

`DesignSystemCatalog` preview renders `DcText` with the default style.

## Related documents

- [`semantic-theme.md`](semantic-theme.md)
- [`components.md`](components.md)
