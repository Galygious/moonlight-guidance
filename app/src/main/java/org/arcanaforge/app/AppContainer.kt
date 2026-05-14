package org.arcanaforge.app

import android.content.Context
import org.arcanaforge.app.core.database.AppDatabase
import org.arcanaforge.app.core.database.DatabaseSeeder
import org.arcanaforge.app.core.datastore.SettingsRepository
import org.arcanaforge.app.data.deck.DeckRepository
import org.arcanaforge.app.data.deck.OfflineDeckRepository
import org.arcanaforge.app.data.layout.LayoutRepository
import org.arcanaforge.app.data.layout.OfflineLayoutRepository
import org.arcanaforge.app.data.reading.OfflineReadingRepository
import org.arcanaforge.app.data.reading.ReadingRepository
import org.arcanaforge.app.data.schedule.OfflineScheduleRepository
import org.arcanaforge.app.data.schedule.ScheduleRepository

interface AppContainer {
    val database: AppDatabase
    val settingsRepository: SettingsRepository
    val deckRepository: DeckRepository
    val layoutRepository: LayoutRepository
    val readingRepository: ReadingRepository
    val scheduleRepository: ScheduleRepository
    val databaseSeeder: DatabaseSeeder
}

class DefaultAppContainer(context: Context) : AppContainer {
    override val database: AppDatabase = AppDatabase.build(context)
    override val settingsRepository: SettingsRepository = SettingsRepository(context.applicationContext)
    override val deckRepository: DeckRepository = OfflineDeckRepository(
        deckDao = database.deckDao(),
        cardDao = database.cardDao(),
    )
    override val layoutRepository: LayoutRepository = OfflineLayoutRepository(database.layoutDao())
    override val readingRepository: ReadingRepository = OfflineReadingRepository(database.readingDao())
    override val scheduleRepository: ScheduleRepository = OfflineScheduleRepository(database.scheduledReadingDao())
    override val databaseSeeder: DatabaseSeeder = DatabaseSeeder(database)
}
