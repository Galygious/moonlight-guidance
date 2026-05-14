package org.arcanaforge.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import org.arcanaforge.app.core.database.dao.AIProviderConfigDao
import org.arcanaforge.app.core.database.dao.CardDao
import org.arcanaforge.app.core.database.dao.DeckDao
import org.arcanaforge.app.core.database.dao.LayoutDao
import org.arcanaforge.app.core.database.dao.ReadingDao
import org.arcanaforge.app.core.database.dao.ScheduledReadingDao
import org.arcanaforge.app.core.database.dao.StoredImageDao
import org.arcanaforge.app.core.database.entity.AIProviderConfigEntity
import org.arcanaforge.app.core.database.entity.CardEntity
import org.arcanaforge.app.core.database.entity.DeckEntity
import org.arcanaforge.app.core.database.entity.LayoutEntity
import org.arcanaforge.app.core.database.entity.LayoutSlotEntity
import org.arcanaforge.app.core.database.entity.ReadingCardEntity
import org.arcanaforge.app.core.database.entity.ReadingEntity
import org.arcanaforge.app.core.database.entity.ScheduledReadingEntity
import org.arcanaforge.app.core.database.entity.StoredImageEntity

@Database(
    entities = [
        DeckEntity::class,
        CardEntity::class,
        StoredImageEntity::class,
        LayoutEntity::class,
        LayoutSlotEntity::class,
        ReadingEntity::class,
        ReadingCardEntity::class,
        ScheduledReadingEntity::class,
        AIProviderConfigEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(MoonlightTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun cardDao(): CardDao
    abstract fun storedImageDao(): StoredImageDao
    abstract fun layoutDao(): LayoutDao
    abstract fun readingDao(): ReadingDao
    abstract fun scheduledReadingDao(): ScheduledReadingDao
    abstract fun aiProviderConfigDao(): AIProviderConfigDao

    companion object {
        const val DATABASE_NAME = "moonlight_guidance.db"

        val MIGRATIONS: Array<Migration> = emptyArray()

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME,
            )
                .addMigrations(*MIGRATIONS)
                .build()
    }
}
