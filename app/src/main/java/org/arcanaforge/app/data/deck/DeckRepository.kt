package org.arcanaforge.app.data.deck

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.database.dao.CardDao
import org.arcanaforge.app.core.database.dao.DeckDao
import org.arcanaforge.app.core.database.entity.CardEntity
import org.arcanaforge.app.core.database.entity.DeckEntity
import org.arcanaforge.app.domain.deck.DeckType

interface DeckRepository {
    fun observeDecks(): Flow<List<DeckEntity>>
    fun observeDeck(deckId: String): Flow<DeckEntity?>
    fun observeCards(deckId: String): Flow<List<CardEntity>>
    fun observeCard(cardId: String): Flow<CardEntity?>
    fun observeFavoriteDecks(limit: Int): Flow<List<DeckEntity>>
    suspend fun getDeck(deckId: String): DeckEntity?
    suspend fun getCards(deckId: String): List<CardEntity>
    suspend fun createDeck(name: String): DeckEntity
    suspend fun createCard(deckId: String): CardEntity
    suspend fun updateDeck(deck: DeckEntity)
    suspend fun updateCard(card: CardEntity)
    suspend fun toggleFavorite(deck: DeckEntity)
    suspend fun deleteDeck(deck: DeckEntity)
    suspend fun deleteCard(card: CardEntity)
    suspend fun countCards(deckId: String): Int
}

class OfflineDeckRepository(
    private val deckDao: DeckDao,
    private val cardDao: CardDao,
) : DeckRepository {
    override fun observeDecks(): Flow<List<DeckEntity>> = deckDao.observeDecks()

    override fun observeDeck(deckId: String): Flow<DeckEntity?> = deckDao.observeDeck(deckId)

    override fun observeCards(deckId: String): Flow<List<CardEntity>> =
        cardDao.observeCardsForDeck(deckId)

    override fun observeCard(cardId: String): Flow<CardEntity?> =
        cardDao.observeCard(cardId)

    override fun observeFavoriteDecks(limit: Int): Flow<List<DeckEntity>> =
        deckDao.observeFavoriteDecks(limit)

    override suspend fun getDeck(deckId: String): DeckEntity? = deckDao.getDeck(deckId)

    override suspend fun getCards(deckId: String): List<CardEntity> =
        cardDao.getCardsForDeck(deckId)

    override suspend fun createDeck(name: String): DeckEntity {
        val now = Instant.now()
        val deck = DeckEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            description = "",
            author = "",
            deckType = DeckType.Custom,
            reversalsEnabled = true,
            correspondenceSystems = emptyList(),
            tags = emptyList(),
            createdAt = now,
            updatedAt = now,
        )
        deckDao.insert(deck)
        return deck
    }

    override suspend fun createCard(deckId: String): CardEntity {
        val now = Instant.now()
        val orderIndex = cardDao.nextOrderIndex(deckId)
        val card = CardEntity(
            id = UUID.randomUUID().toString(),
            deckId = deckId,
            title = "New Card ${orderIndex + 1}",
            orderIndex = orderIndex,
            createdAt = now,
            updatedAt = now,
        )
        cardDao.insert(card)
        return card
    }

    override suspend fun updateDeck(deck: DeckEntity) {
        deckDao.upsert(deck.copy(updatedAt = Instant.now()))
    }

    override suspend fun updateCard(card: CardEntity) {
        cardDao.upsert(card.copy(updatedAt = Instant.now()))
    }

    override suspend fun toggleFavorite(deck: DeckEntity) {
        deckDao.updateFavorite(
            id = deck.id,
            isFavorite = !deck.isFavorite,
            updatedAt = Instant.now(),
        )
    }

    override suspend fun deleteDeck(deck: DeckEntity) {
        deckDao.delete(deck)
    }

    override suspend fun deleteCard(card: CardEntity) {
        cardDao.delete(card)
    }

    override suspend fun countCards(deckId: String): Int = cardDao.countCardsForDeck(deckId)
}
