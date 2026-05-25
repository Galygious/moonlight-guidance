package org.arcanaforge.app.ui.screens.readings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.arcanaforge.app.core.database.entity.DeckEntity
import org.arcanaforge.app.core.database.entity.CardEntity
import org.arcanaforge.app.core.database.entity.LayoutEntity
import org.arcanaforge.app.core.database.entity.LayoutSlotEntity
import org.arcanaforge.app.core.database.entity.ReadingCardEntity
import org.arcanaforge.app.data.deck.DeckRepository
import org.arcanaforge.app.data.layout.LayoutRepository
import org.arcanaforge.app.data.reading.ReadingRepository
import org.arcanaforge.app.domain.reading.ReadingDrawEngine
import org.arcanaforge.app.domain.reading.ReadingOrientation

enum class ReadingCreateMode {
    Random,
    Manual,
}

data class ReadingCreateUiState(
    val decks: List<DeckEntity> = emptyList(),
    val layouts: List<LayoutEntity> = emptyList(),
    val cards: List<CardEntity> = emptyList(),
    val slots: List<LayoutSlotEntity> = emptyList(),
    val selectedDeckId: String = "",
    val selectedLayoutId: String = "",
    val manualCardIds: Map<String, String> = emptyMap(),
    val manualOrientations: Map<String, ReadingOrientation> = emptyMap(),
    val question: String = "",
    val mode: ReadingCreateMode = ReadingCreateMode.Random,
    val reversalsEnabled: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ReadingCreateViewModel(
    savedStateHandle: SavedStateHandle,
    private val readingRepository: ReadingRepository,
    private val deckRepository: DeckRepository,
    private val layoutRepository: LayoutRepository,
) : ViewModel() {
    private val formState = MutableStateFlow(
        ReadingCreateUiState(
            selectedDeckId = savedStateHandle["deckId"] ?: "",
            selectedLayoutId = savedStateHandle["layoutId"] ?: "",
            question = savedStateHandle["question"] ?: "",
        ),
    )
    private val selectedDeckIdFlow = formState.map { it.selectedDeckId }.distinctUntilChanged()
    private val selectedLayoutIdFlow = formState.map { it.selectedLayoutId }.distinctUntilChanged()
    private val cardsFlow = selectedDeckIdFlow.flatMapLatest { deckId ->
        if (deckId.isBlank()) flowOf(emptyList()) else deckRepository.observeCards(deckId)
    }
    private val slotsFlow = selectedLayoutIdFlow.flatMapLatest { layoutId ->
        if (layoutId.isBlank()) flowOf(emptyList()) else layoutRepository.observeSlots(layoutId)
    }

    val uiState: StateFlow<ReadingCreateUiState> = combine(
        formState,
        deckRepository.observeDecks(),
        layoutRepository.observeLayouts(),
        cardsFlow,
        slotsFlow,
    ) { form, decks, layouts, cards, slots ->
        form.copy(
            decks = decks,
            layouts = layouts,
            cards = cards,
            slots = slots,
            selectedDeckId = form.selectedDeckId.ifBlank { decks.firstOrNull()?.id.orEmpty() },
            selectedLayoutId = form.selectedLayoutId.ifBlank { layouts.firstOrNull()?.id.orEmpty() },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReadingCreateUiState(),
    )

    init {
        viewModelScope.launch {
            deckRepository.observeDecks().collect { decks ->
                if (formState.value.selectedDeckId.isBlank()) {
                    decks.firstOrNull()?.let { deck ->
                        formState.update { it.copy(selectedDeckId = deck.id) }
                    }
                }
            }
        }
        viewModelScope.launch {
            layoutRepository.observeLayouts().collect { layouts ->
                if (formState.value.selectedLayoutId.isBlank()) {
                    layouts.firstOrNull()?.let { layout ->
                        formState.update { it.copy(selectedLayoutId = layout.id) }
                    }
                }
            }
        }
    }

    fun updateDeck(deckId: String) {
        formState.update {
            it.copy(
                selectedDeckId = deckId,
                manualCardIds = emptyMap(),
                errorMessage = null,
            )
        }
    }

    fun updateLayout(layoutId: String) {
        formState.update {
            it.copy(
                selectedLayoutId = layoutId,
                manualCardIds = emptyMap(),
                manualOrientations = emptyMap(),
                errorMessage = null,
            )
        }
    }

    fun updateQuestion(question: String) {
        formState.update { it.copy(question = question, errorMessage = null) }
    }

    fun updateReversalsEnabled(enabled: Boolean) {
        formState.update { it.copy(reversalsEnabled = enabled, errorMessage = null) }
    }

    fun updateMode(mode: ReadingCreateMode) {
        formState.update { it.copy(mode = mode, errorMessage = null) }
    }

    fun updateManualCard(slotId: String, cardId: String) {
        formState.update {
            it.copy(
                manualCardIds = it.manualCardIds + (slotId to cardId),
                errorMessage = null,
            )
        }
    }

    fun updateManualOrientation(slotId: String, orientation: ReadingOrientation) {
        formState.update {
            it.copy(
                manualOrientations = it.manualOrientations + (slotId to orientation),
                errorMessage = null,
            )
        }
    }

    fun drawAndSave(onCreated: (String) -> Unit) {
        val current = uiState.value
        if (current.selectedDeckId.isBlank() || current.selectedLayoutId.isBlank()) {
            formState.update { it.copy(errorMessage = "Choose a deck and layout first.") }
            return
        }

        viewModelScope.launch {
            formState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                val deck = deckRepository.getDeck(current.selectedDeckId)
                    ?: error("The selected deck no longer exists.")
                val layout = layoutRepository.getLayout(current.selectedLayoutId)
                    ?: error("The selected layout no longer exists.")
                val cards = deckRepository.getCards(deck.id)
                val slots = layoutRepository.getSlots(layout.id)
                if (cards.isEmpty()) {
                    error("This deck has no cards yet.")
                }
                val drawnCards = if (current.mode == ReadingCreateMode.Manual) {
                    createManualReadingCards(
                        slots = slots,
                        selectedCardIds = current.manualCardIds,
                        orientations = current.manualOrientations,
                    )
                } else {
                    ReadingDrawEngine.draw(
                        cards = cards,
                        slots = slots,
                        reversalsEnabled = current.reversalsEnabled,
                    )
                }
                val reading = readingRepository.createReading(
                    title = current.question.ifBlank { "${layout.name} - ${deck.name}" },
                    question = current.question,
                    deckId = deck.id,
                    layoutId = layout.id,
                    cards = drawnCards,
                )
                reading.id
            }.onSuccess { readingId ->
                formState.update { it.copy(isSaving = false) }
                onCreated(readingId)
            }.onFailure { throwable ->
                formState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "Could not create reading.",
                    )
                }
            }
        }
    }

    private fun createManualReadingCards(
        slots: List<LayoutSlotEntity>,
        selectedCardIds: Map<String, String>,
        orientations: Map<String, ReadingOrientation>,
    ): List<ReadingCardEntity> {
        val orderedSlots = slots.sortedBy { it.drawOrder }
        if (orderedSlots.isEmpty()) {
            error("This layout has no slots yet.")
        }
        val selectedIds = orderedSlots.map { slot ->
            selectedCardIds[slot.id].orEmpty().ifBlank {
                error("Choose a card for ${slot.title}.")
            }
        }
        if (selectedIds.distinct().size != selectedIds.size) {
            error("Each physical draw slot needs a different card.")
        }
        return orderedSlots.map { slot ->
            ReadingCardEntity(
                id = UUID.randomUUID().toString(),
                readingId = "",
                slotId = slot.id,
                cardId = checkNotNull(selectedCardIds[slot.id]),
                orientation = orientations[slot.id] ?: ReadingOrientation.Upright,
            )
        }
    }
}
