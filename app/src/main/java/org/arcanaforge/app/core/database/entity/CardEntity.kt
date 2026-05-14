package org.arcanaforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import org.arcanaforge.app.domain.correspondence.CardCorrespondences

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deck_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["deck_id", "order_index"]),
        Index(value = ["title"]),
    ],
)
data class CardEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "deck_id") val deckId: String,
    val title: String,
    val subtitle: String = "",
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    val suit: String = "",
    val group: String = "",
    @ColumnInfo(name = "image_id") val imageId: String? = null,
    val keywords: List<String> = emptyList(),
    @ColumnInfo(name = "upright_meaning") val uprightMeaning: String = "",
    @ColumnInfo(name = "reversed_meaning") val reversedMeaning: String = "",
    val notes: String = "",
    @ColumnInfo(name = "reversals_enabled") val reversalsEnabled: Boolean? = null,
    @ColumnInfo(name = "correspondences_json") val correspondences: CardCorrespondences = CardCorrespondences(),
    @ColumnInfo(name = "ai_prompt") val aiPrompt: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
