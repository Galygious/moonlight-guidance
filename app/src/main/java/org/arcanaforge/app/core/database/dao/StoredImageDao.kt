package org.arcanaforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import org.arcanaforge.app.core.database.entity.StoredImageEntity

@Dao
interface StoredImageDao {
    @Query("SELECT * FROM stored_images WHERE id = :id")
    suspend fun getImage(id: String): StoredImageEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(image: StoredImageEntity): Long

    @Upsert
    suspend fun upsert(image: StoredImageEntity)

    @Query("DELETE FROM stored_images WHERE id = :id")
    suspend fun deleteById(id: String)
}
