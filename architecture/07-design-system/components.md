# Components

[Back to design system](README.md)

## Scope

Reusable Compose components exposed by `:ui:designsystem`.

## Current implementation

The component set covers policies repeated by both current and expected reference features:

- **`DcText`** (`components/DcText.kt`) — wraps Material3 `Text`; defaults `style` to `MaterialTheme.typography.bodyLarge`.
- **`DcButton`** — semantic primary action with the accessibility touch-target floor.
- **`DcTextField` / `DcPasswordTextField`** — controlled inputs with error semantics, keyboard hooks and caller-supplied localized visibility labels.
- **`DcLoadingState`, `DcEmptyState`, `DcErrorState`** — consistent screen/content feedback; retry remains caller-owned.
- **`DcOfflineBanner`** — semantic error-container connectivity feedback with optional action content.
- **`DcListRow`** — standard list item; when clickable it applies button semantics and the touch-target floor.

New components still require a real shared policy (theme, spacing, accessibility or interaction). Do not wrap a Material component only to rename it.

## Rules

Governed by [`README.md`](README.md) DS-01/DS-02. No new rule IDs here.

## Testing / validation

Every public component renders in `DesignSystemCatalog`'s light/dark previews.

## Related documents

- [`previews-and-catalog.md`](previews-and-catalog.md)
- [`adaptive-accessibility.md`](adaptive-accessibility.md)
