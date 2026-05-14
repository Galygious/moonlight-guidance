package org.arcanaforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.database.entity.CardEntity

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE deck_id = :deckId ORDER BY order_index ASC, title COLLATE NOCASE ASC")
    fun observeCardsForDeck(deckId: String): Flow<List<CardEntity>>

    @Query("SELECT COUNT(*) FROM cards WHERE deck_id = :deckId")
    suspend fun countCardsForDeck(deckId: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(card: CardEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(cards: List<CardEntity>)

    @Upsert
    suspend fun upsert(card: CardEntity)

    @Delete
    suspend fun delete(card: CardEntity)

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteById(id: String)
}
