# Overview & non-negotiable invariants

[Back to architecture index](00-index.md)

## What this project is

A reusable Android base (DC Android Foundation) intended to provide architecture, runtime/platform abstractions, engineering conventions, and a coherent Compose design system without embedding application-specific business models. The goal is not one app — it is a stable base other DC Android projects can depend on and extend.

## Current implementation state (verified — see `PLAN_REPORT.md` Phase A)

As of Phase F/G (see `settings.gradle.kts`), the repository has a 4-module split: `:app`, `:ui:designsystem`, `:runtime:concurrency`, and `:data:preferences`. Hilt (constructor injection + `@Module`/`@InstallIn` bindings, see `app/src/main/java/com/dc/android/foundation/di/*.kt`) and DataStore (`DataStoreModule.kt`) are both in real use; Room/Retrofit/WorkManager/Navigation are not yet present. Every invariant below states the **target** rule; several still describe behavior that is only partially realized and will only become fully checkable once the corresponding later phase has run. This document will be re-verified, not rewritten, as each phase lands.

## Invariants

| ID | Invariant | Why | Verification |
|---|---|---|---|
| INV-01 | Dependency direction must follow the documented layer/module graph; core must not depend on feature implementation. | Prevents cyclic coupling and makes the base reusable. | Inspect Gradle dependencies and imports. |
| INV-02 | Composables must not directly call repositories, Retrofit services, DAOs, or DataStore. | Keeps UI lifecycle-safe, previewable, and testable. | Search UI packages for infrastructure imports/access. |
| INV-03 | Reusable base/core APIs must not depend on project-specific business/domain models unless explicitly approved. | Keeps the base reusable across projects. | Inspect public signatures and module dependencies. |
| INV-04 | Hilt constructor injection is the default dependency acquisition mechanism; service-locator patterns require a documented exception. | Makes ownership and testing explicit. | Search EntryPoint/service-locator/global singleton patterns. |
| INV-05 | Do not hold locks while invoking external/business callbacks or across unsafe suspension/resumption boundaries. | Avoids deadlocks and concurrency bugs. | Review synchronized/Mutex critical sections. |
| INV-06 | Long-lived mutable orchestration has one clear owner. | Avoids races and conflicting lifecycle decisions. | Identify coordinators/managers and mutation sites. |
| INV-07 | Infrastructure exceptions and vendor-specific types do not leak into presentation/public contracts. | Prevents cross-layer coupling. | Inspect UI/public API signatures. |
| INV-08 | Design-system consumers use semantic theme tokens/components instead of duplicating brand constants. | Maintains consistent theming and makes rebranding feasible. | Search migrated UI for raw colors/dimensions/text styles. |
| INV-09 | Secrets, credentials, tokens, and sensitive user data must not be logged or packaged in deliverables. | Prevents security/privacy incidents. | Secret/log scan and ZIP content inspection. |
| INV-10 | Architecture documentation must distinguish implemented behavior from target/optional behavior. | Prevents AI and developers from coding against fiction. | Cross-check claims against source/build files. |

Any intentional violation belongs in [`22-known-deviations.md`](22-known-deviations.md). Durable rationale for a rule belongs in [`23-decision-log.md`](23-decision-log.md).

## Related documents

- [`02-layers-and-dependencies.md`](02-layers-and-dependencies.md)
- [`03-project-structure.md`](03-project-structure.md)
- [`24-ai-reading-guide.md`](24-ai-reading-guide.md)
