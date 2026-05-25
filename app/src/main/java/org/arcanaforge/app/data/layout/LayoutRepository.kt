package org.arcanaforge.app.data.layout

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.database.dao.LayoutDao
import org.arcanaforge.app.core.database.entity.LayoutEntity
import org.arcanaforge.app.core.database.entity.LayoutSlotEntity

interface LayoutRepository {
    fun observeLayouts(): Flow<List<LayoutEntity>>
    fun observeSlots(layoutId: String): Flow<List<LayoutSlotEntity>>
    suspend fun getLayout(layoutId: String): LayoutEntity?
    suspend fun getSlots(layoutId: String): List<LayoutSlotEntity>
    suspend fun createCustomLayout(name: String): LayoutEntity
    suspend fun updateLayout(layout: LayoutEntity)
    suspend fun addSlot(layoutId: String): LayoutSlotEntity
    suspend fun duplicateSlot(slotId: String): LayoutSlotEntity
    suspend fun updateSlot(slot: LayoutSlotEntity)
    suspend fun deleteSlot(slotId: String)
    suspend fun deleteLayout(layoutId: String)
}

class OfflineLayoutRepository(
    private val layoutDao: LayoutDao,
) : LayoutRepository {
    override fun observeLayouts(): Flow<List<LayoutEntity>> = layoutDao.observeLayouts()

    override fun observeSlots(layoutId: String): Flow<List<LayoutSlotEntity>> =
        layoutDao.observeSlotsForLayout(layoutId)

    override suspend fun getLayout(layoutId: String): LayoutEntity? = layoutDao.getLayout(layoutId)

    override suspend fun getSlots(layoutId: String): List<LayoutSlotEntity> =
        layoutDao.getSlotsForLayout(layoutId)

    override suspend fun createCustomLayout(name: String): LayoutEntity {
        val now = Instant.now()
        val layout = LayoutEntity(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Custom Layout" },
            description = "",
            slotCount = 0,
            canvasWidth = 720f,
            canvasHeight = 520f,
            tags = listOf("custom"),
            isBuiltIn = false,
            createdAt = now,
            updatedAt = now,
        )
        layoutDao.insert(layout)
        return layout
    }

    override suspend fun updateLayout(layout: LayoutEntity) {
        layoutDao.upsert(layout.copy(updatedAt = Instant.now()))
    }

    override suspend fun addSlot(layoutId: String): LayoutSlotEntity {
        val index = layoutDao.countSlotsForLayout(layoutId)
        val slot = LayoutSlotEntity(
            id = UUID.randomUUID().toString(),
            layoutId = layoutId,
            title = "Slot ${index + 1}",
            description = "",
            x = 80f + (index % 3) * 170f,
            y = 80f + (index / 3) * 220f,
            width = 120f,
            height = 190f,
            drawOrder = index,
        )
        layoutDao.upsertSlot(slot)
        refreshSlotCount(layoutId)
        return slot
    }

    override suspend fun duplicateSlot(slotId: String): LayoutSlotEntity {
        val source = layoutDao.getSlot(slotId) ?: error("Slot not found.")
        val index = layoutDao.countSlotsForLayout(source.layoutId)
        val copy = source.copy(
            id = UUID.randomUUID().toString(),
            title = "${source.title} Copy",
            x = source.x + 24f,
            y = source.y + 24f,
            drawOrder = index,
        )
        layoutDao.upsertSlot(copy)
        refreshSlotCount(source.layoutId)
        return copy
    }

    override suspend fun updateSlot(slot: LayoutSlotEntity) {
        layoutDao.upsertSlot(slot)
        refreshSlotCount(slot.layoutId)
    }

    override suspend fun deleteSlot(slotId: String) {
        val layoutId = layoutDao.getSlot(slotId)?.layoutId
        layoutDao.deleteSlotById(slotId)
        if (layoutId != null) {
            refreshSlotCount(layoutId)
        }
    }

    override suspend fun deleteLayout(layoutId: String) {
        layoutDao.deleteLayoutById(layoutId)
    }

    private suspend fun refreshSlotCount(layoutId: String) {
        val layout = layoutDao.getLayout(layoutId) ?: return
        val count = layoutDao.countSlotsForLayout(layoutId)
        layoutDao.upsert(layout.copy(slotCount = count, updatedAt = Instant.now()))
    }
}
