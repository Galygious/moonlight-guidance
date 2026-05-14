package org.arcanaforge.app.core.database

import androidx.room.withTransaction
import java.time.Instant
import org.arcanaforge.app.core.database.entity.CardEntity
import org.arcanaforge.app.core.database.entity.DeckEntity
import org.arcanaforge.app.core.database.entity.LayoutEntity
import org.arcanaforge.app.core.database.entity.LayoutSlotEntity
import org.arcanaforge.app.domain.correspondence.CardCorrespondences
import org.arcanaforge.app.domain.deck.DeckType

class DatabaseSeeder(
    private val database: AppDatabase,
) {
    suspend fun seedIfNeeded() {
        database.withTransaction {
            seedSampleDeck()
            seedOneCardLayout()
            seedThreeCardLayout()
            seedSevenChakraLayout()
        }
    }

    private suspend fun seedSampleDeck() {
        val deckId = SAMPLE_ORACLE_DECK_ID
        if (database.deckDao().getDeck(deckId) != null) return

        val now = Instant.now()
        database.deckDao().insert(
            DeckEntity(
                id = deckId,
                name = "Moonlit Oracle",
                description = "A small sample oracle deck for proving local storage.",
                author = "Arcana Forge",
                deckType = DeckType.Oracle,
                reversalsEnabled = true,
                correspondenceSystems = listOf("chakras", "crystals", "elements", "colors"),
                tags = listOf("sample", "oracle"),
                isFavorite = true,
                createdAt = now,
                updatedAt = now,
            ),
        )

        database.cardDao().insertAll(
            listOf(
                CardEntity(
                    id = "sample-card-still-water",
                    deckId = deckId,
                    title = "Still Water",
                    subtitle = "Reflection",
                    orderIndex = 0,
                    keywords = listOf("calm", "listening", "depth"),
                    uprightMeaning = "Pause long enough for the truth beneath the surface to become visible.",
                    reversedMeaning = "A forced answer may be obscuring what quiet attention would reveal.",
                    correspondences = CardCorrespondences(
                        chakras = listOf("Third Eye"),
                        crystals = listOf("Amethyst"),
                        elements = listOf("Water"),
                        colors = listOf("Indigo", "Silver"),
                    ),
                    createdAt = now,
                    updatedAt = now,
                ),
                CardEntity(
                    id = "sample-card-threshold-flame",
                    deckId = deckId,
                    title = "Threshold Flame",
                    subtitle = "Choice",
                    orderIndex = 1,
                    keywords = listOf("courage", "agency", "beginning"),
                    uprightMeaning = "A small decisive act can turn intention into motion.",
                    reversedMeaning = "Name what is draining your will before demanding more from yourself.",
                    correspondences = CardCorrespondences(
                        chakras = listOf("Solar Plexus"),
                        crystals = listOf("Citrine"),
                        elements = listOf("Fire"),
                        colors = listOf("Gold"),
                    ),
                    createdAt = now,
                    updatedAt = now,
                ),
                CardEntity(
                    id = "sample-card-open-sky",
                    deckId = deckId,
                    title = "Open Sky",
                    subtitle = "Perspective",
                    orderIndex = 2,
                    keywords = listOf("space", "clarity", "release"),
                    uprightMeaning = "Widen the frame before deciding what the moment means.",
                    reversedMeaning = "Too much distance can become avoidance; return to the next honest step.",
                    correspondences = CardCorrespondences(
                        chakras = listOf("Crown"),
                        crystals = listOf("Clear Quartz"),
                        elements = listOf("Air"),
                        colors = listOf("White", "Blue"),
                    ),
                    createdAt = now,
                    updatedAt = now,
                ),
            ),
        )
    }

    private suspend fun seedOneCardLayout() {
        val layoutId = "layout-one-card"
        if (database.layoutDao().countLayoutById(layoutId) > 0) return

        val now = Instant.now()
        database.layoutDao().insert(
            LayoutEntity(
                id = layoutId,
                name = "One Card",
                description = "A single-card reflection.",
                slotCount = 1,
                canvasWidth = 360f,
                canvasHeight = 520f,
                tags = listOf("built-in", "daily"),
                isBuiltIn = true,
                createdAt = now,
                updatedAt = now,
            ),
        )
        database.layoutDao().insertSlots(
            listOf(
                LayoutSlotEntity(
                    id = "layout-one-card-slot-guidance",
                    layoutId = layoutId,
                    title = "Guidance",
                    description = "The central message or reflection for this reading.",
                    x = 120f,
                    y = 120f,
                    width = 120f,
                    height = 200f,
                    drawOrder = 0,
                ),
            ),
        )
    }

    private suspend fun seedThreeCardLayout() {
        val layoutId = "layout-three-card-past-present-future"
        if (database.layoutDao().countLayoutById(layoutId) > 0) return

        val now = Instant.now()
        database.layoutDao().insert(
            LayoutEntity(
                id = layoutId,
                name = "Past / Present / Future",
                description = "A three-card timeline spread.",
                slotCount = 3,
                canvasWidth = 720f,
                canvasHeight = 420f,
                tags = listOf("built-in", "three-card"),
                isBuiltIn = true,
                createdAt = now,
                updatedAt = now,
            ),
        )
        database.layoutDao().insertSlots(
            listOf(
                LayoutSlotEntity(
                    id = "layout-three-card-slot-past",
                    layoutId = layoutId,
                    title = "Past",
                    description = "The influence or pattern behind the current situation.",
                    x = 70f,
                    y = 90f,
                    width = 120f,
                    height = 200f,
                    drawOrder = 0,
                ),
                LayoutSlotEntity(
                    id = "layout-three-card-slot-present",
                    layoutId = layoutId,
                    title = "Present",
                    description = "What is most active or visible now.",
                    x = 300f,
                    y = 90f,
                    width = 120f,
                    height = 200f,
                    drawOrder = 1,
                ),
                LayoutSlotEntity(
                    id = "layout-three-card-slot-future",
                    layoutId = layoutId,
                    title = "Future",
                    description = "The direction suggested by the current pattern.",
                    x = 530f,
                    y = 90f,
                    width = 120f,
                    height = 200f,
                    drawOrder = 2,
                ),
            ),
        )
    }

    private suspend fun seedSevenChakraLayout() {
        val layoutId = "layout-seven-chakra-check-in"
        if (database.layoutDao().countLayoutById(layoutId) > 0) return

        val now = Instant.now()
        database.layoutDao().insert(
            LayoutEntity(
                id = layoutId,
                name = "Seven Chakra Check-In",
                description = "A symbolic energy reflection across seven chakra themes.",
                slotCount = 7,
                canvasWidth = 420f,
                canvasHeight = 980f,
                tags = listOf("built-in", "chakra"),
                isBuiltIn = true,
                createdAt = now,
                updatedAt = now,
            ),
        )

        val chakraSlots = listOf(
            "Root Chakra" to "Grounding, safety, body, and survival needs.",
            "Sacral Chakra" to "Creativity, pleasure, emotion, and desire.",
            "Solar Plexus Chakra" to "Confidence, willpower, agency, and boundaries.",
            "Heart Chakra" to "Care, connection, grief, and receiving.",
            "Throat Chakra" to "Voice, truth, communication, and expression.",
            "Third Eye Chakra" to "Intuition, imagination, insight, and pattern recognition.",
            "Crown Chakra" to "Meaning, mystery, devotion, and spiritual perspective.",
        )

        database.layoutDao().insertSlots(
            chakraSlots.mapIndexed { index, (title, description) ->
                LayoutSlotEntity(
                    id = "layout-seven-chakra-slot-$index",
                    layoutId = layoutId,
                    title = title,
                    description = description,
                    x = 150f,
                    y = 40f + index * 130f,
                    width = 120f,
                    height = 190f,
                    drawOrder = index,
                )
            },
        )
    }

    companion object {
        const val SAMPLE_ORACLE_DECK_ID = "sample-deck-moonlit-oracle"
    }
}
