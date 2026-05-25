package org.arcanaforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.database.entity.NatalChartEntity

@Dao
interface NatalChartDao {
    @Query("SELECT * FROM natal_charts ORDER BY created_at DESC")
    fun observeCharts(): Flow<List<NatalChartEntity>>

    @Query("SELECT * FROM natal_charts WHERE id = :id")
    suspend fun getById(id: String): NatalChartEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(chart: NatalChartEntity): Long

    @Upsert
    suspend fun upsert(chart: NatalChartEntity)

    @Query("UPDATE natal_charts SET notes = :notes, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateNotes(id: String, notes: String, updatedAt: java.time.Instant)

    @Query("UPDATE natal_charts SET is_favorite = :isFavorite, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean, updatedAt: java.time.Instant)

    @Query("DELETE FROM natal_charts WHERE id = :id")
    suspend fun deleteById(id: String)
}

