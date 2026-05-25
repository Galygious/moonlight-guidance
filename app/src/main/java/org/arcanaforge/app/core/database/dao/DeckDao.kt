package org.arcanaforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.database.entity.DeckEntity

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks ORDER BY is_favorite DESC, updated_at DESC, name COLLATE NOCASE ASC")
    fun observeDecks(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE id = :id")
    fun observeDeck(id: String): Flow<DeckEntity?>

    @Query("SELECT * FROM decks WHERE is_favorite = 1 ORDER BY updated_at DESC LIMIT :limit")
    fun observeFavoriteDecks(limit: Int): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getDeck(id: String): DeckEntity?

    @Query("SELECT COUNT(*) FROM decks")
    suspend fun countDecks(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(deck: DeckEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(decks: List<DeckEntity>)

    @Upsert
    suspend fun upsert(deck: DeckEntity)

    @Update
    suspend fun update(deck: DeckEntity)

    @Delete
    suspend fun delete(deck: DeckEntity)

    @Query("DELETE FROM decks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE decks SET is_favorite = :isFavorite, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean, updatedAt: Instant)
}
