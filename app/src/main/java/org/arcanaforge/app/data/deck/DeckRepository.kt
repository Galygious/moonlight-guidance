package org.arcanaforge.app.data.deck

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.database.dao.CardDao
import org.arcanaforge.app.core.database.dao.DeckDao
import org.arcanaforge.app.core.database.entity.DeckEntity
import org.arcanaforge.app.domain.deck.DeckType

interface DeckRepository {
    fun observeDecks(): Flow<List<DeckEntity>>
    fun observeFavoriteDecks(limit: Int): Flow<List<DeckEntity>>
    suspend fun createDeck(name: String): DeckEntity
    suspend fun toggleFavorite(deck: DeckEntity)
    suspend fun deleteDeck(deck: DeckEntity)
    suspend fun countCards(deckId: String): Int
}

class OfflineDeckRepository(
    private val deckDao: DeckDao,
    private val cardDao: CardDao,
) : DeckRepository {
    override fun observeDecks(): Flow<List<DeckEntity>> = deckDao.observeDecks()

    override fun observeFavoriteDecks(limit: Int): Flow<List<DeckEntity>> =
        deckDao.observeFavoriteDecks(limit)

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

    override suspend fun countCards(deckId: String): Int = cardDao.countCardsForDeck(deckId)
}
