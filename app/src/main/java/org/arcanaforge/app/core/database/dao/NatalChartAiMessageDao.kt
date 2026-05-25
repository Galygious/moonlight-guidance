package org.arcanaforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.database.entity.NatalChartAiMessageEntity

@Dao
interface NatalChartAiMessageDao {
    @Query("SELECT * FROM natal_chart_ai_messages WHERE chart_id = :chartId ORDER BY created_at ASC")
    fun observeMessages(chartId: String): Flow<List<NatalChartAiMessageEntity>>

    @Query("SELECT * FROM natal_chart_ai_messages WHERE chart_id = :chartId ORDER BY created_at ASC")
    suspend fun getMessages(chartId: String): List<NatalChartAiMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: NatalChartAiMessageEntity)

    @Query("DELETE FROM natal_chart_ai_messages WHERE chart_id = :chartId")
    suspend fun deleteForChart(chartId: String)
}

