package org.arcanaforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "layout_slots",
    foreignKeys = [
        ForeignKey(
            entity = LayoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["layout_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["layout_id", "draw_order"]),
    ],
)
data class LayoutSlotEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "layout_id") val layoutId: String,
    val title: String,
    val description: String = "",
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val rotation: Float = 0f,
    @ColumnInfo(name = "draw_order") val drawOrder: Int,
    @ColumnInfo(name = "reversed_allowed") val reversedAllowed: Boolean = true,
)
