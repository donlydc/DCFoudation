package com.dc.android.foundation.platform.navigation

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * MP:NAV-01 — contract tests for what [Destination] actually defines today: two `@Serializable`
 * `data object` destinations. Pure JVM; no `androidx.navigation` involved (INV-07).
 */
class DestinationTest {

    private val json = Json

    @Test
    fun greeting_roundTrips_toSameInstance() {
        val encoded = json.encodeToString(Destination.Greeting.serializer(), Destination.Greeting)

        val decoded = json.decodeFromString(Destination.Greeting.serializer(), encoded)

        assertSame(Destination.Greeting, decoded)
    }

    @Test
    fun articles_roundTrips_toSameInstance() {
        val encoded = json.encodeToString(Destination.Articles.serializer(), Destination.Articles)

        val decoded = json.decodeFromString(Destination.Articles.serializer(), encoded)

        assertSame(Destination.Articles, decoded)
    }

    @Test
    fun dataObjectDestinations_encodeToEmptyJsonObject() {
        assertEquals("{}", json.encodeToString(Destination.Greeting.serializer(), Destination.Greeting))
        assertEquals("{}", json.encodeToString(Destination.Articles.serializer(), Destination.Articles))
    }

    @Test
    fun serialNames_areStableAndDistinct() {
        // Navigation-Compose derives the type-safe route from the serial name, so changing it
        // (e.g. moving/renaming the class) silently changes the route and breaks deep links/restoration.
        val greeting = Destination.Greeting.serializer().descriptor.serialName
        val articles = Destination.Articles.serializer().descriptor.serialName

        assertEquals("com.dc.android.foundation.platform.navigation.Destination.Greeting", greeting)
        assertEquals("com.dc.android.foundation.platform.navigation.Destination.Articles", articles)
        assertNotEquals(greeting, articles)
    }

    @Test
    fun unknownArgument_isRejectedByStrictJson() {
        try {
            json.decodeFromString(Destination.Greeting.serializer(), """{"unexpected":"value"}""")
            fail("Expected SerializationException for an unknown argument")
        } catch (expected: SerializationException) {
            // Invalid/unknown arguments must not be silently accepted by the default decoder.
        }
    }

    @Test
    fun unknownArgument_isIgnoredWhenLenientJsonIsExplicitlyChosen() {
        val lenient = Json { ignoreUnknownKeys = true }

        val decoded = lenient.decodeFromString(
            Destination.Greeting.serializer(),
            """{"unexpected":"value"}"""
        )

        assertSame(Destination.Greeting, decoded)
    }

    @Test
    fun malformedPayload_isRejected() {
        try {
            json.decodeFromString(Destination.Articles.serializer(), "not-json")
            fail("Expected SerializationException for a malformed payload")
        } catch (expected: SerializationException) {
            // expected
        }
    }

    @Test
    fun destinations_areDistinctValuesAndSubtypesOfDestination() {
        val greeting: Destination = Destination.Greeting
        val articles: Destination = Destination.Articles

        assertNotEquals(greeting, articles)
        assertEquals(Destination.Greeting, greeting)
        assertTrue(greeting is Destination.Greeting)
        assertTrue(articles is Destination.Articles)
    }

    @Test
    fun exhaustiveWhen_coversEveryDestination() {
        // Compile-time guard: adding a Destination subtype without handling it here fails the build,
        // forcing the author to also add its route-registration/serialization test coverage.
        fun label(destination: Destination): String = when (destination) {
            Destination.Greeting -> "greeting"
            Destination.Articles -> "articles"
        }

        assertEquals("greeting", label(Destination.Greeting))
        assertEquals("articles", label(Destination.Articles))
    }
}
