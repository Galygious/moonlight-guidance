package org.arcanaforge.app.ui.screens.readings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.arcanaforge.app.core.database.entity.CardEntity
import org.arcanaforge.app.core.database.entity.LayoutSlotEntity
import org.arcanaforge.app.core.database.entity.ReadingCardEntity
import org.arcanaforge.app.core.database.entity.ReadingEntity
import org.arcanaforge.app.core.database.entity.StoredImageEntity
import org.arcanaforge.app.data.ai.ReadingAiChatRepository
import org.arcanaforge.app.data.ai.ReadingAiService
import org.arcanaforge.app.data.deck.DeckRepository
import org.arcanaforge.app.data.image.ImageRepository
import org.arcanaforge.app.data.layout.LayoutRepository
import org.arcanaforge.app.data.reading.ReadingRepository
import org.arcanaforge.app.domain.ai.AiChatMessage
import org.arcanaforge.app.domain.ai.AiChatRole
import org.arcanaforge.app.domain.reading.ReadingOrientation

data class ReadingDetailItem(
    val slot: LayoutSlotEntity,
    val card: CardEntity,
    val readingCard: ReadingCardEntity,
    val image: StoredImageEntity?,
)

data class ReadingDetailUiState(
    val isLoading: Boolean = true,
    val reading: ReadingEntity? = null,
    val deckName: String = "",
    val layoutName: String = "",
    val readingNotes: String = "",
    val statusMessage: String? = null,
    val items: List<ReadingDetailItem> = emptyList(),
    val aiChatMessages: List<AiChatMessage> = emptyList(),
    val aiQuestionDraft: String = "",
    val isAskingAi: Boolean = false,
    val errorMessage: String? = null,
)

class ReadingDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val readingRepository: ReadingRepository,
    private val deckRepository: DeckRepository,
    private val layoutRepository: LayoutRepository,
    private val imageRepository: ImageRepository,
    private val readingAiService: ReadingAiService,
    private val readingAiChatRepository: ReadingAiChatRepository,
) : ViewModel() {
    private val readingId: String = checkNotNull(savedStateHandle["readingId"])
    private val _uiState = MutableStateFlow(ReadingDetailUiState())
    val uiState: StateFlow<ReadingDetailUiState> = _uiState

    init {
        loadReading()
    }

    fun updateReadingNotes(value: String) {
        _uiState.update { it.copy(readingNotes = value, statusMessage = null) }
    }

    fun updateAiQuestionDraft(value: String) {
        _uiState.update { it.copy(aiQuestionDraft = value, statusMessage = null) }
    }

    fun saveReadingNotes() {
        val readingId = _uiState.value.reading?.id ?: return
        val notes = _uiState.value.readingNotes.trim()
        viewModelScope.launch {
            runCatching {
                readingRepository.updateReadingNotes(readingId, notes)
                val currentReading = _uiState.value.reading
                _uiState.update {
                    it.copy(
                        reading = currentReading?.copy(notes = notes),
                        readingNotes = notes,
                        statusMessage = "Reading notes saved.",
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "Reading notes could not be saved.")
                }
            }
        }
    }

    fun toggleFavorite() {
        val reading = _uiState.value.reading ?: return
        val nextFavorite = !reading.isFavorite
        viewModelScope.launch {
            runCatching {
                readingRepository.updateReadingFavorite(reading.id, nextFavorite)
                _uiState.update {
                    it.copy(
                        reading = reading.copy(isFavorite = nextFavorite),
                        statusMessage = if (nextFavorite) {
                            "Reading marked favorite."
                        } else {
                            "Reading removed from favorites."
                        },
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "Favorite status could not be saved.")
                }
            }
        }
    }

    fun saveReadingCardNote(
        readingCardId: String,
        note: String,
    ) {
        val trimmedNote = note.trim()
        viewModelScope.launch {
            runCatching {
                readingRepository.updateReadingCardNote(readingCardId, trimmedNote)
                _uiState.update { state ->
                    state.copy(
                        items = state.items.map { item ->
                            if (item.readingCard.id == readingCardId) {
                                item.copy(readingCard = item.readingCard.copy(userNote = trimmedNote))
                            } else {
                                item
                            }
                        },
                        statusMessage = "Card note saved.",
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "Card note could not be saved.")
                }
            }
        }
    }

    fun deleteReading(onDeleted: () -> Unit) {
        val readingId = _uiState.value.reading?.id ?: return
        viewModelScope.launch {
            runCatching {
                readingRepository.deleteReading(readingId)
            }.onSuccess {
                onDeleted()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "Reading could not be deleted.")
                }
            }
        }
    }

    fun askAiAboutReading() {
        val state = _uiState.value
        val readingId = state.reading?.id ?: return
        val question = state.aiQuestionDraft.trim()
        if (question.isBlank() || state.isAskingAi) {
            return
        }
        viewModelScope.launch {
            val userMessage = readingAiChatRepository.addMessage(
                readingId = readingId,
                role = AiChatRole.User,
                text = question,
            )
            _uiState.update {
                it.copy(
                    aiChatMessages = it.aiChatMessages + userMessage,
                    aiQuestionDraft = "",
                    isAskingAi = true,
                    errorMessage = null,
                )
            }
            runCatching {
                readingAiService.askAboutReading(
                    readingContext = buildReadingAiContext(_uiState.value),
                    history = state.aiChatMessages,
                    question = question,
                )
            }.onSuccess { answer ->
                val assistantMessage = readingAiChatRepository.addMessage(
                    readingId = readingId,
                    role = AiChatRole.Assistant,
                    text = answer,
                )
                _uiState.update {
                    it.copy(
                        aiChatMessages = it.aiChatMessages + assistantMessage,
                        isAskingAi = false,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isAskingAi = false,
                        errorMessage = throwable.message ?: "AI could not answer this reading question.",
                    )
                }
            }
        }
    }

    private fun loadReading() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val reading = readingRepository.getReading(readingId)
                    ?: error("Reading not found.")
                val deck = deckRepository.getDeck(reading.deckId)
                val layout = layoutRepository.getLayout(reading.layoutId)
                val slots = layoutRepository.getSlots(reading.layoutId).associateBy { it.id }
                val cards = deckRepository.getCards(reading.deckId).associateBy { it.id }
                val images = cards.values
                    .mapNotNull { it.imageId }
                    .distinct()
                    .associateWith { imageRepository.getImage(it) }
                val readingCards = readingRepository.getReadingCards(reading.id)
                val aiMessages = readingAiChatRepository.getMessages(reading.id)
                val items = readingCards.mapNotNull { readingCard ->
                    val slot = slots[readingCard.slotId]
                    val card = cards[readingCard.cardId]
                    if (slot != null && card != null) {
                        ReadingDetailItem(
                            slot = slot,
                            card = card,
                            readingCard = readingCard,
                            image = card.imageId?.let(images::get),
                        )
                    } else {
                        null
                    }
                }.sortedBy { it.slot.drawOrder }

                ReadingDetailUiState(
                    isLoading = false,
                    reading = reading,
                    deckName = deck?.name ?: "Deleted deck",
                    layoutName = layout?.name ?: "Deleted layout",
                    readingNotes = reading.notes,
                    items = items,
                    aiChatMessages = aiMessages,
                )
            }.onSuccess { state ->
                _uiState.value = state
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Could not load reading.",
                    )
                }
            }
        }
    }

    private fun buildReadingAiContext(state: ReadingDetailUiState): String {
        val reading = state.reading ?: return "No reading is loaded."
        return buildString {
            appendLine("Reading title: ${reading.title}")
            appendLine("Deck: ${state.deckName}")
            appendLine("Layout: ${state.layoutName}")
            if (reading.question.isNotBlank()) {
                appendLine("Question: ${reading.question}")
            }
            if (state.readingNotes.isNotBlank()) {
                appendLine("Reading notes: ${state.readingNotes}")
            }
            appendLine()
            appendLine("Cards:")
            state.items.forEach { item ->
                val meaning = if (item.readingCard.orientation == ReadingOrientation.Reversed) {
                    item.card.reversedMeaning.ifBlank { item.card.uprightMeaning }
                } else {
                    item.card.uprightMeaning
                }
                appendLine("- ${item.slot.title}: ${item.card.title} (${item.readingCard.orientation.name.lowercase()})")
                if (item.slot.description.isNotBlank()) {
                    appendLine("  Slot meaning: ${item.slot.description}")
                }
                if (meaning.isNotBlank()) {
                    appendLine("  Card meaning: $meaning")
                }
                if (item.card.keywords.isNotEmpty()) {
                    appendLine("  Keywords: ${item.card.keywords.joinToString(", ")}")
                }
                if (item.card.correspondences.hasAnyValue()) {
                    appendLine("  Correspondences: ${item.card.correspondences.summaryForAi()}")
                }
                if (item.readingCard.userNote.isNotBlank()) {
                    appendLine("  User note: ${item.readingCard.userNote}")
                }
            }
        }
    }
}

private fun org.arcanaforge.app.domain.correspondence.CardCorrespondences.hasAnyValue(): Boolean =
    chakras.isNotEmpty() ||
        crystals.isNotEmpty() ||
        elements.isNotEmpty() ||
        zodiacSigns.isNotEmpty() ||
        planets.isNotEmpty() ||
        colors.isNotEmpty() ||
        herbs.isNotEmpty() ||
        custom.isNotEmpty()

private fun org.arcanaforge.app.domain.correspondence.CardCorrespondences.summaryForAi(): String {
    val parts = buildList {
        if (chakras.isNotEmpty()) add("Chakras: ${chakras.joinToString(", ")}")
        if (crystals.isNotEmpty()) add("Crystals: ${crystals.joinToString(", ")}")
        if (elements.isNotEmpty()) add("Elements: ${elements.joinToString(", ")}")
        if (zodiacSigns.isNotEmpty()) add("Zodiac: ${zodiacSigns.joinToString(", ")}")
        if (planets.isNotEmpty()) add("Planets: ${planets.joinToString(", ")}")
        if (colors.isNotEmpty()) add("Colors: ${colors.joinToString(", ")}")
        if (herbs.isNotEmpty()) add("Herbs: ${herbs.joinToString(", ")}")
        custom.forEach { (key, values) ->
            if (values.isNotEmpty()) add("$key: ${values.joinToString(", ")}")
        }
    }
    return parts.joinToString(" | ")
}
