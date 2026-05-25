package org.arcanaforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import org.arcanaforge.app.domain.ai.AiChatRole

@Entity(
    tableName = "reading_ai_messages",
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
        Index(value = ["created_at"]),
    ],
)
data class ReadingAiMessageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "reading_id") val readingId: String,
    val role: AiChatRole,
    val text: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
)
