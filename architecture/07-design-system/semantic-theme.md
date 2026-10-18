# Semantic theme

[Back to design system](README.md)

## Scope

How primitive tokens become the Material3 `MaterialTheme` that feature UI actually reads.

## Current implementation

`theme/DcTheme.kt` — the single Compose theme entry point:

- `darkTheme: Boolean = isSystemInDarkTheme()` — follows system by default.
- `dynamicColor: Boolean = false` — branded apps opt in on API 31+ when they intentionally want wallpaper-derived colors; previews therefore stay deterministic by default.
- `colorScheme`, `typography`, `shapes`, `extendedColors` (`DcColors`), and `spacing` (`DcSpacing`) are parameters. `LocalDcColors`/`LocalDcSpacing` make the extended semantic values available to DC components without hard-coding them.
- `uiScale: DcUiScale` provides `Compact` (0.90x), `Standard` (1.00x), and `Comfortable` (1.10x). It scales semantic typography and spacing but never shrinks the 48dp accessibility touch floor; Android font scale still applies on top.
- Wires `MaterialTheme(colorScheme = ..., typography = ..., shapes = ..., content = ...)` inside those composition locals.

The default light scheme explicitly defines primary/container, secondary/container, tertiary/container, background/surface/container, outline, error and inverse roles. The dark scheme defines the same roles against the Ink surface ramp; neither mode falls back to Material defaults. `DcColors` adds success, warning and info foreground/container pairs. These are defaults, not locks: an application can override every theme layer through `DcTheme(...)` while `dynamicColor` remains opt-in and `false` by default for deterministic branding.

`MainActivity.kt` roots its tree in `DcTheme { Scaffold { Greeting(...) } }` — the only consumer today.

## Rules

Governed by [`README.md`](README.md) DS-01. No new rule IDs here.

## Testing / validation

`DesignSystemCatalog`'s light/dark previews (`dynamicColor = false`, so the preview is deterministic regardless of host OS version) exercise both color schemes.

## Related documents

- [`tokens.md`](tokens.md)
- [`typography.md`](typography.md)
