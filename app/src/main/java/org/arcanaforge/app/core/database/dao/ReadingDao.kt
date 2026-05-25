package org.arcanaforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.database.entity.ReadingCardEntity
import org.arcanaforge.app.core.database.entity.ReadingEntity

@Dao
interface ReadingDao {
    @Query("SELECT * FROM readings ORDER BY created_at DESC LIMIT :limit")
    fun observeRecentReadings(limit: Int): Flow<List<ReadingEntity>>

    @Query("SELECT * FROM readings ORDER BY created_at DESC")
    fun observeReadings(): Flow<List<ReadingEntity>>

    @Query("SELECT * FROM readings WHERE id = :id")
    suspend fun getReading(id: String): ReadingEntity?

    @Query("SELECT * FROM reading_cards WHERE reading_id = :readingId")
    suspend fun getCardsForReading(readingId: String): List<ReadingCardEntity>

    @Query("SELECT * FROM reading_cards WHERE id = :id")
    suspend fun getReadingCard(id: String): ReadingCardEntity?

    @Query("SELECT COUNT(*) FROM readings")
    fun observeReadingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(reading: ReadingEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCards(cards: List<ReadingCardEntity>)

    @Transaction
    suspend fun insertReadingWithCards(reading: ReadingEntity, cards: List<ReadingCardEntity>) {
        insert(reading)
        insertCards(cards)
    }

    @Upsert
    suspend fun upsert(reading: ReadingEntity)

    @Upsert
    suspend fun upsertReadingCard(readingCard: ReadingCardEntity)

    @Query("UPDATE readings SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: String, notes: String)

    @Query("UPDATE readings SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE reading_cards SET user_note = :note WHERE id = :id")
    suspend fun updateReadingCardNote(id: String, note: String)

    @Query("DELETE FROM readings WHERE id = :id")
    suspend fun deleteById(id: String)
}
