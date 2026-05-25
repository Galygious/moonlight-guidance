package org.arcanaforge.app.core.database

import android.content.Context
import android.graphics.BitmapFactory
import androidx.room.withTransaction
import java.io.File
import java.time.Instant
import org.arcanaforge.app.core.database.entity.CardEntity
import org.arcanaforge.app.core.database.entity.DeckEntity
import org.arcanaforge.app.core.database.entity.LayoutEntity
import org.arcanaforge.app.core.database.entity.LayoutSlotEntity
import org.arcanaforge.app.core.database.entity.StoredImageEntity
import org.arcanaforge.app.domain.correspondence.CardCorrespondences
import org.arcanaforge.app.domain.deck.DeckType
import org.arcanaforge.app.domain.image.ImageSource

class DatabaseSeeder(
    private val database: AppDatabase,
    private val context: Context? = null,
) {
    suspend fun seedIfNeeded() {
        database.withTransaction {
            seedSampleDeck()
            seedStandardTarotDeck()
            seedOneCardLayout()
            seedThreeCardLayout()
            seedSevenChakraLayout()
        }

        context?.let {
            val now = Instant.now()
            attachStandardTarotImages(
                context = it,
                cards = buildStandardTarotCards(deckId = STANDARD_TAROT_DECK_ID, now = now),
                now = now,
            )
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

    private suspend fun seedStandardTarotDeck() {
        val deckId = STANDARD_TAROT_DECK_ID

        val now = Instant.now()
        if (database.deckDao().getDeck(deckId) == null) {
            database.deckDao().insert(
                DeckEntity(
                    id = deckId,
                    name = "Standard Tarot",
                    description = "A complete 78-card Rider-Waite-Smith tarot starter deck with editable meanings.",
                    author = "Pamela Colman Smith / Arcana Forge",
                    deckType = DeckType.Tarot,
                    reversalsEnabled = true,
                    correspondenceSystems = listOf("elements", "zodiac", "planets", "colors"),
                    tags = listOf("default", "tarot", "starter"),
                    isFavorite = true,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }

        val standardTarotCards = buildStandardTarotCards(deckId = deckId, now = now)
        if (database.cardDao().countCardsForDeck(deckId) < standardTarotCards.size) {
            database.cardDao().insertAll(standardTarotCards)
        }
        fillMissingStandardTarotDetails(standardTarotCards, now)

    }

    private suspend fun fillMissingStandardTarotDetails(cards: List<CardEntity>, now: Instant) {
        cards.forEach { seededCard ->
            val currentCard = database.cardDao().getCard(seededCard.id) ?: return@forEach
            val updatedCard = currentCard.copy(
                subtitle = currentCard.subtitle.ifBlank { seededCard.subtitle },
                suit = currentCard.suit.ifBlank { seededCard.suit },
                group = currentCard.group.ifBlank { seededCard.group },
                keywords = currentCard.keywords.ifEmpty { seededCard.keywords },
                uprightMeaning = currentCard.uprightMeaning.ifBlank { seededCard.uprightMeaning },
                reversedMeaning = currentCard.reversedMeaning.ifBlank { seededCard.reversedMeaning },
            )
            if (updatedCard != currentCard) {
                database.cardDao().upsert(updatedCard.copy(updatedAt = now))
            }
        }
    }

    private fun buildStandardTarotCards(deckId: String, now: Instant): List<CardEntity> {
        val majorArcana = listOf(
            "The Fool",
            "The Magician",
            "The High Priestess",
            "The Empress",
            "The Emperor",
            "The Hierophant",
            "The Lovers",
            "The Chariot",
            "Strength",
            "The Hermit",
            "Wheel of Fortune",
            "Justice",
            "The Hanged Man",
            "Death",
            "Temperance",
            "The Devil",
            "The Tower",
            "The Star",
            "The Moon",
            "The Sun",
            "Judgement",
            "The World",
        )

        val ranks = listOf(
            "Ace",
            "Two",
            "Three",
            "Four",
            "Five",
            "Six",
            "Seven",
            "Eight",
            "Nine",
            "Ten",
            "Page",
            "Knight",
            "Queen",
            "King",
        )
        val suits = listOf("Wands", "Cups", "Swords", "Pentacles")

        val majorCards = majorArcana.mapIndexed { index, title ->
            val cardId = "standard-tarot-major-${index.toString().padStart(2, '0')}"
            val meaning = StandardTarotMeanings.forCard(cardId)
            CardEntity(
                id = cardId,
                deckId = deckId,
                title = title,
                subtitle = "Major Arcana",
                orderIndex = index,
                group = "Major Arcana",
                keywords = meaning?.keywords.orEmpty(),
                uprightMeaning = meaning?.uprightMeaning.orEmpty(),
                reversedMeaning = meaning?.reversedMeaning.orEmpty(),
                createdAt = now,
                updatedAt = now,
            )
        }

        val minorCards = suits.flatMapIndexed { suitIndex, suit ->
            ranks.mapIndexed { rankIndex, rank ->
                val orderIndex = majorArcana.size + suitIndex * ranks.size + rankIndex
                val cardId = "standard-tarot-${suit.lowercase()}-${rank.lowercase()}"
                val meaning = StandardTarotMeanings.forCard(cardId)
                CardEntity(
                    id = cardId,
                    deckId = deckId,
                    title = "$rank of $suit",
                    subtitle = suit,
                    orderIndex = orderIndex,
                    suit = suit,
                    group = "Minor Arcana",
                    keywords = meaning?.keywords.orEmpty(),
                    uprightMeaning = meaning?.uprightMeaning.orEmpty(),
                    reversedMeaning = meaning?.reversedMeaning.orEmpty(),
                    createdAt = now,
                    updatedAt = now,
                )
            }
        }

        return majorCards + minorCards
    }

    private suspend fun attachStandardTarotImages(
        context: Context,
        cards: List<CardEntity>,
        now: Instant,
    ) {
        cards.forEach { seededCard ->
            val image = ensureStandardTarotImage(context, seededCard.id, now)
            val currentCard = database.cardDao().getCard(seededCard.id)
            if (currentCard != null && currentCard.imageId != image.id) {
                database.cardDao().upsert(currentCard.copy(imageId = image.id, updatedAt = now))
            }
        }
    }

    private suspend fun ensureStandardTarotImage(
        context: Context,
        cardId: String,
        now: Instant,
    ): StoredImageEntity {
        val imageId = "seed-image-$cardId"
        val imageDir = File(context.filesDir, "images/cards").apply { mkdirs() }
        val imageFile = File(imageDir, "$imageId.jpg")
        val assetPath = "seed/tarot/rws1909/$cardId.jpg"

        if (!imageFile.exists() || imageFile.length() == 0L) {
            context.assets.open(assetPath).use { input ->
                imageFile.outputStream().use { output -> input.copyTo(output) }
            }
        }

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(imageFile.absolutePath, options)
        val storedImage = StoredImageEntity(
            id = imageId,
            localPath = imageFile.absolutePath,
            thumbnailPath = imageFile.absolutePath,
            mimeType = "image/jpeg",
            width = options.outWidth,
            height = options.outHeight,
            source = ImageSource.Imported,
            provider = "Rider-Waite-Smith public-domain seed asset",
            createdAt = now,
        )
        database.storedImageDao().upsert(storedImage)
        return storedImage
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
        const val STANDARD_TAROT_DECK_ID = "default-deck-standard-tarot"
    }
}
