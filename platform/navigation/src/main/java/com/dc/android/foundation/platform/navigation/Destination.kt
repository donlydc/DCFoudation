package com.dc.android.foundation.platform.navigation

import kotlinx.serialization.Serializable

/**
 * DC-owned, engine-neutral description of the app's top-level destinations (06-navigation.md
 * NAV-01, DEP-04, INV-07). Feature modules and `:app` share this type instead of feature code
 * importing `androidx.navigation.*` directly — only `:app` (the composition root/navigation
 * host) is allowed to touch the real navigation engine.
 *
 * `@Serializable` is required so `:app`'s `NavHost` can use Navigation-Compose's type-safe route
 * API (`composable<Destination.Greeting>`); this is a `kotlinx.serialization` dependency, not an
 * `androidx.navigation` one, so it does not violate INV-07.
 *
 * Deliberately does not include a `Navigator`/`DcNavController` action wrapper: today's two
 * destinations are peers that never navigate to one another (only the composition root switches
 * between them), so there is no navigation *action* for feature code to invoke. Adding such a
 * wrapper now would be indirection with no policy/testability/isolation value it doesn't already
 * have (`plan/CLAUDE_IMPLEMENTATION_PLAN.md` §7). Add one the day a feature actually needs to
 * trigger navigation itself.
 */
sealed interface Destination {

    @Serializable
    data object Greeting : Destination

    @Serializable
    data object Articles : Destination
}
