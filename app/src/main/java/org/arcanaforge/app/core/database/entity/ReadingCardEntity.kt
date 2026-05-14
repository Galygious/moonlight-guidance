package org.arcanaforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.arcanaforge.app.domain.reading.ReadingOrientation

@Entity(
    tableName = "reading_cards",
    foreignKeys = [
        ForeignKey(
            entity = ReadingEntity::class,
            parentColumns = ["id"],
            childColumns = ["reading_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["reading_id"]),
        Index(value = ["slot_id"]),
        Index(value = ["card_id"]),
    ],
)
data class ReadingCardEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "reading_id") val readingId: String,
    @ColumnInfo(name = "slot_id") val slotId: String,
    @ColumnInfo(name = "card_id") val cardId: String,
    val orientation: ReadingOrientation,
    @ColumnInfo(name = "user_note") val userNote: String = "",
    @ColumnInfo(name = "ai_interpretation") val aiInterpretation: String? = null,
)
