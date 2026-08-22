# `:ui:designsystem`

**Role:** reusable Compose visual language.

**Owns:** primitive tokens, semantic theme, DC components, previews, and the design-system catalog.

## AI guidance

- Preserve Tokens → Theme → Components → Feature UI dependency direction.
- Add business-neutral APIs only; do not import feature or data-layer code.
- Prefer extending a DC component/token over introducing raw feature styling.
- Read the relevant file under `architecture/07-design-system/` before changing public UI APIs.
- Verify previews/compilation and `:ui:designsystem:assembleDebug`.
