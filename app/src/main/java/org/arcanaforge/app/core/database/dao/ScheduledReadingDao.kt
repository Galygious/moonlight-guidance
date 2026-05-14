package org.arcanaforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.database.entity.ScheduledReadingEntity

@Dao
interface ScheduledReadingDao {
    @Query("SELECT * FROM scheduled_readings ORDER BY enabled DESC, updated_at DESC")
    fun observeSchedules(): Flow<List<ScheduledReadingEntity>>

    @Query("SELECT * FROM scheduled_readings WHERE enabled = 1 ORDER BY updated_at DESC LIMIT 1")
    fun observeNextEnabledSchedule(): Flow<ScheduledReadingEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(schedule: ScheduledReadingEntity): Long

    @Upsert
    suspend fun upsert(schedule: ScheduledReadingEntity)

    @Query("DELETE FROM scheduled_readings WHERE id = :id")
    suspend fun deleteById(id: String)
}
