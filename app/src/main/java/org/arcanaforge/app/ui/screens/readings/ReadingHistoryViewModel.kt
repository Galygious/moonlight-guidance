package org.arcanaforge.app.ui.screens.readings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.arcanaforge.app.data.deck.DeckRepository
import org.arcanaforge.app.data.layout.LayoutRepository
import org.arcanaforge.app.data.reading.ReadingRepository

data class ReadingHistoryItem(
    val id: String,
    val title: String,
    val question: String,
    val deckName: String,
    val layoutName: String,
    val createdAt: String,
)

data class ReadingHistoryUiState(
    val readings: List<ReadingHistoryItem> = emptyList(),
)

class ReadingHistoryViewModel(
    readingRepository: ReadingRepository,
    deckRepository: DeckRepository,
    layoutRepository: LayoutRepository,
) : ViewModel() {
    val uiState: StateFlow<ReadingHistoryUiState> = combine(
        readingRepository.observeReadings(),
        deckRepository.observeDecks(),
        layoutRepository.observeLayouts(),
    ) { readings, decks, layouts ->
        val deckNames = decks.associate { it.id to it.name }
        val layoutNames = layouts.associate { it.id to it.name }
        ReadingHistoryUiState(
            readings = readings.map { reading ->
                ReadingHistoryItem(
                    id = reading.id,
                    title = reading.title,
                    question = reading.question,
                    deckName = deckNames[reading.deckId] ?: "Deleted deck",
                    layoutName = layoutNames[reading.layoutId] ?: "Deleted layout",
                    createdAt = DateTimeFormatter.ISO_LOCAL_DATE.format(
                        reading.createdAt.atZone(java.time.ZoneId.systemDefault()),
                    ),
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReadingHistoryUiState(),
    )
}
