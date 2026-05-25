package org.arcanaforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import org.arcanaforge.app.domain.ai.AiChatRole

@Entity(
    tableName = "natal_chart_ai_messages",
    foreignKeys = [
        ForeignKey(
            entity = NatalChartEntity::class,
            parentColumns = ["id"],
            childColumns = ["chart_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["chart_id"]),
        Index(value = ["created_at"]),
    ],
)
data class NatalChartAiMessageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "chart_id") val chartId: String,
    val role: AiChatRole,
    val text: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
)

