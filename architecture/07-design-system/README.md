# Design System

[Back to architecture index](../00-index.md)

## Contract

```text
Primitive design tokens          ui/designsystem/.../tokens/*
        ↓
Semantic theme                   ui/designsystem/.../theme/DcTheme.kt
        ↓
Reusable components               ui/designsystem/.../components/*
        ↓
Feature UI                        app/.../MainActivity.kt
```

Feature UI expresses meaning (`MaterialTheme.colorScheme.primary`, `SpacingTokens.Medium`, `DcButton`) rather than brand implementation values (raw hex, raw `.dp` literals, Material `Button`/`Text` directly) when a semantic token/component exists.

## Current implementation (Phase C)

New Gradle module `:ui:designsystem` (namespace `com.dc.android.foundation.designsystem`), depended on by `:app`. Contains:

- `tokens/` — `ColorTokens`, `SpacingTokens`, `ShapeTokens`, `MotionTokens`, `AccessibilityTokens`.
- `theme/` — `DcTheme` (semantic entry point), `DcTypography`, `DcShapes`.
- `components/` — text/button primitives plus controlled text/password input, loading/empty/error/offline states, accessible list rows and a neutral key/value detail row.
- `catalog/DesignSystemCatalog.kt` — manual preview catalog (light + dark).

`DcTheme` is now the app-level customization seam: callers may override its Material `ColorScheme`, `Typography`, `Shapes`, extended `DcColors`, and `DcSpacing`; dynamic color is opt-in so branded apps and previews remain deterministic. `AccessibilityTokens.MinTouchTarget` remains non-overridable. `app`'s old `ui/theme/{Color,Type,Theme}.kt` were deleted; `MainActivity.kt` themes through `DcTheme`, and its debug build exposes the catalog destination.

Read only the subdocument for the concern being changed.

## Rules

### DS-01 — Feature UI themes through `DcTheme`, not raw tokens

**Rule:** Screens root their Compose tree in `DcTheme { ... }` and read colors/typography via `MaterialTheme.*`; they do not import `ColorTokens`/`DcTypography` directly.

**Why:** Keeps rebranding to a single file change (INV-08).

**Verification:** `grep` feature UI packages for direct `tokens.*` imports outside `ui/designsystem`.

### DS-02 — Interactive components enforce the accessibility floor once, centrally

**Rule:** Touch-target sizing (`AccessibilityTokens.MinTouchTarget`) is enforced inside the DS component (e.g. `DcButton`), not repeated at each call site.

**Why:** One place to get right instead of every screen remembering it.

**Verification:** Inspect `DcButton`/future interactive components for `defaultMinSize`.

### DS-03 — No raw spacing/shape literals in feature UI

**Rule:** Feature UI uses `SpacingTokens`/`ShapeTokens` (or the semantic `DcShapes` inside `MaterialTheme`), not raw `.dp`/`RoundedCornerShape(...)` literals.

**Why:** Keeps spacing/shape consistent and greppable.

**Verification:** `grep '\.dp'` / `RoundedCornerShape(` in `app/src` outside `ui/designsystem`; currently zero hits.

**Exceptions:** `MainActivity.kt`'s `Modifier.padding(innerPadding)` uses `Scaffold`'s own inner padding value, not a literal — not a violation.

## Testing / validation

- `./gradlew :ui:designsystem:compileDebugKotlin :app:compileDebugKotlin` compiles.
- `DesignSystemCatalog` previews (light/dark) compile and render every current token/component.

## Related documents

- [`tokens.md`](tokens.md), [`semantic-theme.md`](semantic-theme.md), [`typography.md`](typography.md), [`spacing-shapes-elevation.md`](spacing-shapes-elevation.md), [`components.md`](components.md), [`common-screen-patterns.md`](common-screen-patterns.md), [`motion.md`](motion.md), [`adaptive-accessibility.md`](adaptive-accessibility.md), [`previews-and-catalog.md`](previews-and-catalog.md)
- [`component-roadmap.md`](component-roadmap.md) — widget roadmap; W0–W3 implemented, W4 deferred
- [`../01-overview-and-invariants.md`](../01-overview-and-invariants.md) (INV-08)
