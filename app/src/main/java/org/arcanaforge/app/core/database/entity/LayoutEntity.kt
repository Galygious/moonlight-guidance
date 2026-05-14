package org.arcanaforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "layouts",
    indices = [
        Index(value = ["name"]),
        Index(value = ["is_built_in"]),
    ],
)
data class LayoutEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    @ColumnInfo(name = "slot_count") val slotCount: Int,
    @ColumnInfo(name = "canvas_width") val canvasWidth: Float,
    @ColumnInfo(name = "canvas_height") val canvasHeight: Float,
    val tags: List<String> = emptyList(),
    @ColumnInfo(name = "is_built_in") val isBuiltIn: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
