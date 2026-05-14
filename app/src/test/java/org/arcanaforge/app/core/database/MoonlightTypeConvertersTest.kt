package org.arcanaforge.app.core.database

import java.time.Instant
import org.arcanaforge.app.domain.correspondence.CardCorrespondences
import org.arcanaforge.app.domain.deck.DeckType
import org.junit.Assert.assertEquals
import org.junit.Test

class MoonlightTypeConvertersTest {
    private val converters = MoonlightTypeConverters()

    @Test
    fun stringListRoundTrip() {
        val original = listOf("chakras", "crystals", "custom")

        val restored = converters.jsonToStringList(converters.stringListToJson(original))

        assertEquals(original, restored)
    }

    @Test
    fun stringListMapRoundTrip() {
        val original = mapOf(
            "Affirmation" to listOf("I speak clearly."),
            "Body area" to listOf("Chest", "Throat"),
        )

        val restored = converters.jsonToStringListMap(converters.stringListMapToJson(original))

        assertEquals(original, restored)
    }

    @Test
    fun correspondencesRoundTrip() {
        val original = CardCorrespondences(
            chakras = listOf("Heart", "Throat"),
            crystals = listOf("Rose Quartz"),
            custom = mapOf("Reflection" to listOf("receiving support")),
        )

        val restored = converters.jsonToCorrespondences(converters.correspondencesToJson(original))

        assertEquals(original, restored)
    }

    @Test
    fun instantRoundTrip() {
        val original = Instant.parse("2026-05-13T17:00:00Z")

        val restored = converters.longToInstant(converters.instantToLong(original))

        assertEquals(original, restored)
    }

    @Test
    fun deckTypeRoundTrip() {
        val restored = converters.stringToDeckType(converters.deckTypeToString(DeckType.Oracle))

        assertEquals(DeckType.Oracle, restored)
    }
}
