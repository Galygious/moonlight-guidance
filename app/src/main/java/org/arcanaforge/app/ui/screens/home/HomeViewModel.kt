package org.arcanaforge.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.arcanaforge.app.core.database.entity.DeckEntity
import org.arcanaforge.app.core.database.entity.ReadingEntity
import org.arcanaforge.app.core.database.entity.ScheduledReadingEntity
import org.arcanaforge.app.data.deck.DeckRepository
import org.arcanaforge.app.data.reading.ReadingRepository
import org.arcanaforge.app.data.schedule.ScheduleRepository

data class HomeUiState(
    val decks: List<DeckEntity> = emptyList(),
    val favoriteDecks: List<DeckEntity> = emptyList(),
    val recentReadings: List<ReadingEntity> = emptyList(),
    val nextSchedule: ScheduledReadingEntity? = null,
    val isLoading: Boolean = true,
)

class HomeViewModel(
    deckRepository: DeckRepository,
    readingRepository: ReadingRepository,
    scheduleRepository: ScheduleRepository,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        deckRepository.observeDecks(),
        deckRepository.observeFavoriteDecks(limit = 3),
        readingRepository.observeRecentReadings(limit = 5),
        scheduleRepository.observeNextEnabledSchedule(),
    ) { decks, favoriteDecks, recentReadings, nextSchedule ->
        HomeUiState(
            decks = decks,
            favoriteDecks = favoriteDecks,
            recentReadings = recentReadings,
            nextSchedule = nextSchedule,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )
}
