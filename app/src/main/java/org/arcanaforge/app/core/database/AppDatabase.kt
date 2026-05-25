package org.arcanaforge.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.arcanaforge.app.core.database.dao.AIProviderConfigDao
import org.arcanaforge.app.core.database.dao.CardDao
import org.arcanaforge.app.core.database.dao.DeckDao
import org.arcanaforge.app.core.database.dao.LayoutDao
import org.arcanaforge.app.core.database.dao.ReadingAiMessageDao
import org.arcanaforge.app.core.database.dao.ReadingDao
import org.arcanaforge.app.core.database.dao.ScheduledReadingDao
import org.arcanaforge.app.core.database.dao.StoredImageDao
import org.arcanaforge.app.core.database.entity.AIProviderConfigEntity
import org.arcanaforge.app.core.database.entity.CardEntity
import org.arcanaforge.app.core.database.entity.DeckEntity
import org.arcanaforge.app.core.database.entity.LayoutEntity
import org.arcanaforge.app.core.database.entity.LayoutSlotEntity
import org.arcanaforge.app.core.database.entity.ReadingAiMessageEntity
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
        ReadingAiMessageEntity::class,
        ReadingCardEntity::class,
        ScheduledReadingEntity::class,
        AIProviderConfigEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(MoonlightTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun cardDao(): CardDao
    abstract fun storedImageDao(): StoredImageDao
    abstract fun layoutDao(): LayoutDao
    abstract fun readingDao(): ReadingDao
    abstract fun readingAiMessageDao(): ReadingAiMessageDao
    abstract fun scheduledReadingDao(): ScheduledReadingDao
    abstract fun aiProviderConfigDao(): AIProviderConfigDao

    companion object {
        const val DATABASE_NAME = "moonlight_guidance.db"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ai_provider_configs ADD COLUMN auth_mode TEXT NOT NULL DEFAULT 'ApiKey'")
                db.execSQL("ALTER TABLE ai_provider_configs ADD COLUMN oauth_access_token_encrypted TEXT")
                db.execSQL("ALTER TABLE ai_provider_configs ADD COLUMN oauth_refresh_token_encrypted TEXT")
                db.execSQL("ALTER TABLE ai_provider_configs ADD COLUMN oauth_expires_at INTEGER")
                db.execSQL("ALTER TABLE ai_provider_configs ADD COLUMN oauth_account_id TEXT")
                db.execSQL("ALTER TABLE ai_provider_configs ADD COLUMN oauth_account_label TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS reading_ai_messages (
                        id TEXT NOT NULL PRIMARY KEY,
                        reading_id TEXT NOT NULL,
                        role TEXT NOT NULL,
                        text TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        FOREIGN KEY(reading_id) REFERENCES readings(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reading_ai_messages_reading_id ON reading_ai_messages(reading_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reading_ai_messages_created_at ON reading_ai_messages(created_at)")
            }
        }

        val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)

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
