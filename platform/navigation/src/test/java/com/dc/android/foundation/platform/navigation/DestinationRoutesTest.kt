package com.dc.android.foundation.platform.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class DestinationRoutesTest {
    @Test fun route_isQualifiedName() {
        assertEquals(
            "com.dc.android.foundation.platform.navigation.Destination.Greeting",
            DestinationRoutes.of(Destination.Greeting)
        )
    }

    @Test fun routes_areDistinct() {
        assertNotEquals(DestinationRoutes.of(Destination.Greeting), DestinationRoutes.of(Destination.Articles))
    }

    @Test fun fromRoute_roundTrips() {
        assertSame(Destination.Articles, DestinationRoutes.fromRoute(DestinationRoutes.of(Destination.Articles)))
    }

    @Test fun fromRoute_unknownOrNull_returnsNull() {
        assertNull(DestinationRoutes.fromRoute("nope"))
        assertNull(DestinationRoutes.fromRoute(null))
    }

    @Test fun effects_areValueEquals() {
        assertEquals(NavigationEffect.Navigate(Destination.Greeting), NavigationEffect.Navigate(Destination.Greeting))
    }
}
