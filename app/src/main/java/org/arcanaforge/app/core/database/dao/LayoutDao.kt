package org.arcanaforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.database.entity.LayoutEntity
import org.arcanaforge.app.core.database.entity.LayoutSlotEntity

@Dao
interface LayoutDao {
    @Query("SELECT * FROM layouts ORDER BY is_built_in DESC, slot_count ASC, name COLLATE NOCASE ASC")
    fun observeLayouts(): Flow<List<LayoutEntity>>

    @Query("SELECT * FROM layouts WHERE id = :id")
    suspend fun getLayout(id: String): LayoutEntity?

    @Query("SELECT * FROM layout_slots WHERE layout_id = :layoutId ORDER BY draw_order ASC")
    suspend fun getSlotsForLayout(layoutId: String): List<LayoutSlotEntity>

    @Query("SELECT COUNT(*) FROM layouts WHERE id = :id")
    suspend fun countLayoutById(id: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(layout: LayoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(layouts: List<LayoutEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSlots(slots: List<LayoutSlotEntity>)

    @Upsert
    suspend fun upsert(layout: LayoutEntity)

    @Upsert
    suspend fun upsertSlot(slot: LayoutSlotEntity)

    @Query("DELETE FROM layouts WHERE id = :id")
    suspend fun deleteLayoutById(id: String)
}
