package com.dc.android.foundation.platform.navigation

import kotlin.reflect.KClass

/**
 * Engine-neutral navigation intent a ViewModel may emit as a one-shot effect (MP:NAV-02, D-02
 * minimal option). The Route/composition root maps it onto the real navigation engine; no
 * `androidx.navigation` type appears here (INV-07). Additive: no current feature emits it yet.
 */
sealed interface NavigationEffect {
    /** Navigate to [destination]. */
    data class Navigate(val destination: Destination) : NavigationEffect

    /** Pop the current entry. */
    data object Back : NavigationEffect
}

/**
 * Stable route naming for [Destination] (MP:NAV-01). Navigation-Compose's type-safe API uses the
 * destination class's qualified name as the route for `data object`s; this centralises that
 * convention so `:app` need not repeat `X::class.qualifiedName` comparisons.
 */
object DestinationRoutes {
    /** Route string for [type]; throws if the class is anonymous/local (no qualified name). */
    fun of(type: KClass<out Destination>): String =
        requireNotNull(type.qualifiedName) { "Destination must be a named class: $type" }

    fun of(destination: Destination): String = of(destination::class)

    /** The registered [Destination] whose route equals [route], or null for unknown/null routes. */
    fun fromRoute(route: String?): Destination? =
        if (route == null) null else all.firstOrNull { of(it) == route }

    private val all: List<Destination> = listOf(Destination.Greeting, Destination.Articles)
}
