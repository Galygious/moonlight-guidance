package org.arcanaforge.app

import android.content.Context
import org.arcanaforge.app.core.ai.OpenAiCodexOAuthClient
import org.arcanaforge.app.core.database.AppDatabase
import org.arcanaforge.app.core.database.DatabaseSeeder
import org.arcanaforge.app.core.datastore.SettingsRepository
import org.arcanaforge.app.core.security.SecureStringStore
import org.arcanaforge.app.data.astrology.AstronomyEngineNatalChartCalculator
import org.arcanaforge.app.data.astrology.NatalChartAiChatRepository
import org.arcanaforge.app.data.astrology.NatalChartRepository
import org.arcanaforge.app.data.astrology.OfflineNatalChartAiChatRepository
import org.arcanaforge.app.data.astrology.OfflineNatalChartRepository
import org.arcanaforge.app.data.ai.AiProviderRepository
import org.arcanaforge.app.data.ai.OfflineAiProviderRepository
import org.arcanaforge.app.data.ai.OpenAiReadingAiService
import org.arcanaforge.app.data.ai.OfflineReadingAiChatRepository
import org.arcanaforge.app.data.ai.ReadingAiChatRepository
import org.arcanaforge.app.data.ai.ReadingAiService
import org.arcanaforge.app.data.deck.DeckRepository
import org.arcanaforge.app.data.deck.OfflineDeckRepository
import org.arcanaforge.app.data.image.ImageRepository
import org.arcanaforge.app.data.image.LocalImageRepository
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
    val imageRepository: ImageRepository
    val layoutRepository: LayoutRepository
    val readingRepository: ReadingRepository
    val scheduleRepository: ScheduleRepository
    val aiProviderRepository: AiProviderRepository
    val readingAiService: ReadingAiService
    val readingAiChatRepository: ReadingAiChatRepository
    val natalChartRepository: NatalChartRepository
    val natalChartAiChatRepository: NatalChartAiChatRepository
    val databaseSeeder: DatabaseSeeder
}

class DefaultAppContainer(context: Context) : AppContainer {
    override val database: AppDatabase = AppDatabase.build(context)
    override val settingsRepository: SettingsRepository = SettingsRepository(context.applicationContext)
    override val deckRepository: DeckRepository = OfflineDeckRepository(
        deckDao = database.deckDao(),
        cardDao = database.cardDao(),
    )
    override val imageRepository: ImageRepository = LocalImageRepository(
        context = context.applicationContext,
        storedImageDao = database.storedImageDao(),
    )
    override val layoutRepository: LayoutRepository = OfflineLayoutRepository(database.layoutDao())
    override val readingRepository: ReadingRepository = OfflineReadingRepository(database.readingDao())
    override val scheduleRepository: ScheduleRepository = OfflineScheduleRepository(
        context = context.applicationContext,
        scheduledReadingDao = database.scheduledReadingDao(),
    )
    override val aiProviderRepository: AiProviderRepository = OfflineAiProviderRepository(
        aiProviderConfigDao = database.aiProviderConfigDao(),
        secureStringStore = SecureStringStore(),
        openAiCodexOAuthClient = OpenAiCodexOAuthClient(),
    )
    override val readingAiService: ReadingAiService = OpenAiReadingAiService(
        aiProviderConfigDao = database.aiProviderConfigDao(),
        secureStringStore = SecureStringStore(),
        openAiCodexOAuthClient = OpenAiCodexOAuthClient(),
    )
    override val readingAiChatRepository: ReadingAiChatRepository = OfflineReadingAiChatRepository(
        readingAiMessageDao = database.readingAiMessageDao(),
    )
    override val natalChartRepository: NatalChartRepository = OfflineNatalChartRepository(
        natalChartDao = database.natalChartDao(),
        natalChartCalculator = AstronomyEngineNatalChartCalculator(),
    )
    override val natalChartAiChatRepository: NatalChartAiChatRepository = OfflineNatalChartAiChatRepository(
        natalChartAiMessageDao = database.natalChartAiMessageDao(),
    )
    override val databaseSeeder: DatabaseSeeder = DatabaseSeeder(
        database = database,
        context = context.applicationContext,
    )
}
