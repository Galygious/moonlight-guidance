package org.arcanaforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.database.entity.ReadingAiMessageEntity

@Dao
interface ReadingAiMessageDao {
    @Query("SELECT * FROM reading_ai_messages WHERE reading_id = :readingId ORDER BY created_at ASC")
    fun observeMessages(readingId: String): Flow<List<ReadingAiMessageEntity>>

    @Query("SELECT * FROM reading_ai_messages WHERE reading_id = :readingId ORDER BY created_at ASC")
    suspend fun getMessages(readingId: String): List<ReadingAiMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ReadingAiMessageEntity)

    @Query("DELETE FROM reading_ai_messages WHERE reading_id = :readingId")
    suspend fun deleteForReading(readingId: String)
}
