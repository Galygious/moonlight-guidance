package org.arcanaforge.app.data.reading

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.database.entity.ReadingCardEntity
import org.arcanaforge.app.core.database.dao.ReadingDao
import org.arcanaforge.app.core.database.entity.ReadingEntity

interface ReadingRepository {
    fun observeRecentReadings(limit: Int): Flow<List<ReadingEntity>>
    fun observeReadings(): Flow<List<ReadingEntity>>
    fun observeReadingCount(): Flow<Int>
    suspend fun getReading(readingId: String): ReadingEntity?
    suspend fun getReadingCards(readingId: String): List<ReadingCardEntity>
    suspend fun updateReadingNotes(readingId: String, notes: String)
    suspend fun updateReadingFavorite(readingId: String, isFavorite: Boolean)
    suspend fun updateReadingCardNote(readingCardId: String, note: String)
    suspend fun deleteReading(readingId: String)
    suspend fun createReading(
        title: String,
        question: String,
        deckId: String,
        layoutId: String,
        cards: List<ReadingCardEntity>,
    ): ReadingEntity
}

class OfflineReadingRepository(
    private val readingDao: ReadingDao,
) : ReadingRepository {
    override fun observeRecentReadings(limit: Int): Flow<List<ReadingEntity>> =
        readingDao.observeRecentReadings(limit)

    override fun observeReadings(): Flow<List<ReadingEntity>> = readingDao.observeReadings()

    override fun observeReadingCount(): Flow<Int> = readingDao.observeReadingCount()

    override suspend fun getReading(readingId: String): ReadingEntity? =
        readingDao.getReading(readingId)

    override suspend fun getReadingCards(readingId: String): List<ReadingCardEntity> =
        readingDao.getCardsForReading(readingId)

    override suspend fun updateReadingNotes(readingId: String, notes: String) {
        readingDao.updateNotes(readingId, notes)
    }

    override suspend fun updateReadingFavorite(readingId: String, isFavorite: Boolean) {
        readingDao.updateFavorite(readingId, isFavorite)
    }

    override suspend fun updateReadingCardNote(readingCardId: String, note: String) {
        readingDao.updateReadingCardNote(readingCardId, note)
    }

    override suspend fun deleteReading(readingId: String) {
        readingDao.deleteById(readingId)
    }

    override suspend fun createReading(
        title: String,
        question: String,
        deckId: String,
        layoutId: String,
        cards: List<ReadingCardEntity>,
    ): ReadingEntity {
        val now = Instant.now()
        val reading = ReadingEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            question = question,
            deckId = deckId,
            layoutId = layoutId,
            createdAt = now,
        )
        readingDao.insertReadingWithCards(
            reading = reading,
            cards = cards.map { it.copy(readingId = reading.id) },
        )
        return reading
    }
}
