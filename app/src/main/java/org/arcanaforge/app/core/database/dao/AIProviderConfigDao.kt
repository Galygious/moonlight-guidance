package org.arcanaforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.database.entity.AIProviderConfigEntity

@Dao
interface AIProviderConfigDao {
    @Query("SELECT * FROM ai_provider_configs ORDER BY enabled DESC, name COLLATE NOCASE ASC")
    fun observeConfigs(): Flow<List<AIProviderConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(config: AIProviderConfigEntity): Long

    @Upsert
    suspend fun upsert(config: AIProviderConfigEntity)

    @Query("DELETE FROM ai_provider_configs WHERE id = :id")
    suspend fun deleteById(id: String)
}
