# `build-logic`

**Role:** included Gradle build holding the DC convention plugins (not an application module).

**Owns:** plugin IDs `dc.android.application`, `dc.android.library`, `dc.kotlin.jvm`, `dc.compose`, `dc.hilt`, `dc.room`, `dc.testing`, `dc.feature.generator`, plus the `createFeature` scaffold task (`CreateFeatureTask`, `FeatureScaffoldGenerator`).

## AI guidance

- Modules apply `dc.*` plugins instead of repeating Android/Kotlin/Compose/Hilt/Room setup; change shared build behavior here, not per module.
- Versions come from `gradle/libs.versions.toml`; do not hard-code them in plugins.
- `dc.room` exports schemas to each module's `schemas/` directory; keep them committed.
- Generated feature scaffolding must stay inside the target module and respect `architecture/20-feature-template.md`.
- Read `architecture/18-build-logic-and-dependencies.md` before changing a convention.
- Verify with `./gradlew -p build-logic test` and a full `assembleDebug`.
