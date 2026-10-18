# Typography

[Back to design system](README.md)

## Scope

Semantic text-style scale wired into `MaterialTheme.typography`.

## Current implementation

`theme/DcTypography.kt` defines the full foundation scale with the system sans-serif family: display 32/40 bold, headline 24/32 semibold, title 20/28, 16/24 and 14/20 semibold, body 16/24, 14/20 and 12/16 regular, and label 14/20 semibold, 12/16 and 11/16 medium. No role falls back to a stock purple-template default.

`components/DcText.kt` defaults its `style` param to `MaterialTheme.typography.bodyLarge`.

Use display/headline roles sparingly for page-level emphasis; title roles for app bars, cards and sections; body roles for content/supporting text; and label roles for controls, navigation and compact status labels.

## Rules

No new rule IDs — governed by [`README.md`](README.md) DS-01.

## Testing / validation

`DesignSystemCatalog` renders every customized role with its name and size in the light/dark and window-width preview matrix.

## Related documents

- [`semantic-theme.md`](semantic-theme.md)
- [`components.md`](components.md)
