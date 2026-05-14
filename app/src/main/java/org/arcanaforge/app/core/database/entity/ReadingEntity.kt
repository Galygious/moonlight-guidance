package org.arcanaforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "readings",
    indices = [
        Index(value = ["created_at"]),
        Index(value = ["deck_id"]),
        Index(value = ["layout_id"]),
        Index(value = ["is_favorite"]),
    ],
)
data class ReadingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val question: String = "",
    @ColumnInfo(name = "deck_id") val deckId: String,
    @ColumnInfo(name = "layout_id") val layoutId: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    val tags: List<String> = emptyList(),
    val notes: String = "",
    @ColumnInfo(name = "ai_summary") val aiSummary: String? = null,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
)
