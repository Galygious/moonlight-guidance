package org.arcanaforge.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import org.arcanaforge.app.AppContainer
import org.arcanaforge.app.ui.screens.cardeditor.CardEditorViewModel
import org.arcanaforge.app.ui.screens.charts.NatalChartDetailViewModel
import org.arcanaforge.app.ui.screens.charts.NatalChartLibraryViewModel
import org.arcanaforge.app.ui.screens.deckeditor.DeckEditorViewModel
import org.arcanaforge.app.ui.screens.decks.DeckLibraryViewModel
import org.arcanaforge.app.ui.screens.home.HomeViewModel
import org.arcanaforge.app.ui.screens.layouteditor.LayoutEditorViewModel
import org.arcanaforge.app.ui.screens.layoutlibrary.LayoutLibraryViewModel
import org.arcanaforge.app.ui.screens.readings.ReadingCreateViewModel
import org.arcanaforge.app.ui.screens.readings.ReadingDetailViewModel
import org.arcanaforge.app.ui.screens.readings.ReadingHistoryViewModel
import org.arcanaforge.app.ui.screens.schedule.ScheduleViewModel
import org.arcanaforge.app.ui.screens.settings.SettingsViewModel

class AppViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedStateHandle = extras.createSavedStateHandle()
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(
                deckRepository = container.deckRepository,
                readingRepository = container.readingRepository,
                scheduleRepository = container.scheduleRepository,
            ) as T

            modelClass.isAssignableFrom(DeckLibraryViewModel::class.java) -> DeckLibraryViewModel(
                deckRepository = container.deckRepository,
            ) as T

            modelClass.isAssignableFrom(DeckEditorViewModel::class.java) -> DeckEditorViewModel(
                savedStateHandle = savedStateHandle,
                deckRepository = container.deckRepository,
                imageRepository = container.imageRepository,
            ) as T

            modelClass.isAssignableFrom(CardEditorViewModel::class.java) -> CardEditorViewModel(
                savedStateHandle = savedStateHandle,
                deckRepository = container.deckRepository,
                imageRepository = container.imageRepository,
            ) as T

            modelClass.isAssignableFrom(ReadingHistoryViewModel::class.java) -> ReadingHistoryViewModel(
                readingRepository = container.readingRepository,
                deckRepository = container.deckRepository,
                layoutRepository = container.layoutRepository,
            ) as T

            modelClass.isAssignableFrom(ReadingCreateViewModel::class.java) -> ReadingCreateViewModel(
                savedStateHandle = savedStateHandle,
                readingRepository = container.readingRepository,
                deckRepository = container.deckRepository,
                layoutRepository = container.layoutRepository,
            ) as T

            modelClass.isAssignableFrom(ReadingDetailViewModel::class.java) -> ReadingDetailViewModel(
                savedStateHandle = savedStateHandle,
                readingRepository = container.readingRepository,
                deckRepository = container.deckRepository,
                layoutRepository = container.layoutRepository,
                imageRepository = container.imageRepository,
                readingAiService = container.readingAiService,
                readingAiChatRepository = container.readingAiChatRepository,
            ) as T

            modelClass.isAssignableFrom(LayoutLibraryViewModel::class.java) -> LayoutLibraryViewModel(
                layoutRepository = container.layoutRepository,
            ) as T

            modelClass.isAssignableFrom(LayoutEditorViewModel::class.java) -> LayoutEditorViewModel(
                savedStateHandle = savedStateHandle,
                layoutRepository = container.layoutRepository,
            ) as T

            modelClass.isAssignableFrom(NatalChartLibraryViewModel::class.java) -> NatalChartLibraryViewModel(
                natalChartRepository = container.natalChartRepository,
            ) as T

            modelClass.isAssignableFrom(NatalChartDetailViewModel::class.java) -> NatalChartDetailViewModel(
                savedStateHandle = savedStateHandle,
                natalChartRepository = container.natalChartRepository,
                natalChartAiChatRepository = container.natalChartAiChatRepository,
                readingAiService = container.readingAiService,
            ) as T

            modelClass.isAssignableFrom(ScheduleViewModel::class.java) -> ScheduleViewModel(
                scheduleRepository = container.scheduleRepository,
                deckRepository = container.deckRepository,
                layoutRepository = container.layoutRepository,
            ) as T

            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(
                aiProviderRepository = container.aiProviderRepository,
            ) as T

            else -> error("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
