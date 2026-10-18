# `:ui:designsystem`

**Role:** reusable Compose visual language.

**Owns:** overridable semantic theme/tokens, DC components, preview annotations, and the design-system catalog.

## AI guidance

- Preserve Tokens → Theme → Components → Feature UI dependency direction.
- Apply `DcUiScale` once through the app-level `DcTheme`; do not scale individual feature screens.
- Add business-neutral APIs only; do not import feature or data-layer code.
- Prefer extending a DC component/token over introducing raw feature styling.
- Coil is isolated to `components/DcAsyncImage.kt`; feature modules must not import Coil directly.
- Adaptive navigation is exposed only through `DcNavigationSuite`; do not leak Material adaptive types.
- Read the relevant file under `architecture/07-design-system/` before changing public UI APIs.
- Verify previews/compilation and `:ui:designsystem:assembleDebug`.
