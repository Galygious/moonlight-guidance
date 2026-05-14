package org.arcanaforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Query("SELECT COUNT(*) FROM readings")
    fun observeReadingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(reading: ReadingEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCards(cards: List<ReadingCardEntity>)

    @Upsert
    suspend fun upsert(reading: ReadingEntity)

    @Query("DELETE FROM readings WHERE id = :id")
    suspend fun deleteById(id: String)
}
