package org.arcanaforge.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.arcanaforge.app.data.deck.OfflineDeckRepository
import org.arcanaforge.app.data.layout.OfflineLayoutRepository
import org.arcanaforge.app.domain.correspondence.CardCorrespondences
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var database: AppDatabase

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun seedDataCreatesSampleDeckAndLayouts() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        DatabaseSeeder(database, context).seedIfNeeded()

        assertEquals(2, database.deckDao().countDecks())
        assertEquals(
            78,
            database.cardDao().countCardsForDeck(DatabaseSeeder.STANDARD_TAROT_DECK_ID),
        )
        val firstTarotCard = database.cardDao()
            .getCardsForDeck(DatabaseSeeder.STANDARD_TAROT_DECK_ID)
            .first()
        assertEquals(
            "The Fool",
            firstTarotCard.title,
        )
        assertTrue(firstTarotCard.keywords.isNotEmpty())
        assertTrue(firstTarotCard.uprightMeaning.isNotBlank())
        assertTrue(firstTarotCard.reversedMeaning.isNotBlank())
        assertEquals("seed-image-standard-tarot-major-00", firstTarotCard.imageId)
        val firstTarotImage = database.storedImageDao().getImage(firstTarotCard.imageId.orEmpty())
        assertNotNull(firstTarotImage)
        assertTrue(File(firstTarotImage!!.localPath).exists())
        assertTrue(firstTarotImage.width > 0)
        assertTrue(firstTarotImage.height > 0)
        assertTrue(database.layoutDao().countLayoutById("layout-one-card") > 0)
        assertTrue(database.layoutDao().countLayoutById("layout-three-card-past-present-future") > 0)
        assertTrue(database.layoutDao().countLayoutById("layout-seven-chakra-check-in") > 0)
    }

    @Test
    fun deckRepositoryCreatesDeckAndCard() = runTest {
        val repository = OfflineDeckRepository(
            deckDao = database.deckDao(),
            cardDao = database.cardDao(),
        )

        val deck = repository.createDeck("Test Deck")
        val card = repository.createCard(deck.id)
        repository.updateCard(
            card.copy(
                correspondences = CardCorrespondences(
                    chakras = listOf("Heart"),
                    crystals = listOf("Rose Quartz"),
                ),
            ),
        )

        assertEquals("Test Deck", database.deckDao().getDeck(deck.id)?.name)
        assertEquals(1, database.cardDao().countCardsForDeck(deck.id))
        val savedCard = database.cardDao().getCard(card.id)
        assertEquals("New Card 1", savedCard?.title)
        assertEquals(listOf("Heart"), savedCard?.correspondences?.chakras)
        assertEquals(listOf("Rose Quartz"), savedCard?.correspondences?.crystals)
    }

    @Test
    fun layoutRepositoryDuplicatesSlotsAndRefreshesSlotCount() = runTest {
        val repository = OfflineLayoutRepository(database.layoutDao())

        val layout = repository.createCustomLayout("Test Layout")
        val slot = repository.addSlot(layout.id)
        val duplicate = repository.duplicateSlot(slot.id)

        assertNotEquals(slot.id, duplicate.id)
        assertEquals("${slot.title} Copy", duplicate.title)
        assertEquals(1, duplicate.drawOrder)
        assertEquals(2, database.layoutDao().countSlotsForLayout(layout.id))
        assertEquals(2, database.layoutDao().getLayout(layout.id)?.slotCount)
    }
}
