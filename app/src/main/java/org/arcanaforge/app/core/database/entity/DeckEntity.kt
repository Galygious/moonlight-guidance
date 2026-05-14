package org.arcanaforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import org.arcanaforge.app.domain.deck.DeckType

@Entity(
    tableName = "decks",
    indices = [
        Index(value = ["name"]),
        Index(value = ["deck_type"]),
        Index(value = ["is_favorite"]),
    ],
)
data class DeckEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val author: String = "",
    @ColumnInfo(name = "deck_type") val deckType: DeckType = DeckType.Custom,
    @ColumnInfo(name = "card_back_image_id") val cardBackImageId: String? = null,
    @ColumnInfo(name = "reversals_enabled") val reversalsEnabled: Boolean = true,
    @ColumnInfo(name = "correspondence_systems") val correspondenceSystems: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
