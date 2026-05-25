package org.arcanaforge.app.ui.screens.deckeditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.arcanaforge.app.core.database.entity.CardEntity
import org.arcanaforge.app.core.database.entity.DeckEntity
import org.arcanaforge.app.core.database.entity.StoredImageEntity
import org.arcanaforge.app.data.deck.DeckRepository
import org.arcanaforge.app.data.image.ImageRepository
import org.arcanaforge.app.domain.deck.DeckType

data class CardListItem(
    val card: CardEntity,
    val image: StoredImageEntity?,
)

data class DeckEditorUiState(
    val deck: DeckEntity? = null,
    val cards: List<CardListItem> = emptyList(),
    val name: String = "",
    val description: String = "",
    val author: String = "",
    val deckType: DeckType = DeckType.Custom,
    val tagsText: String = "",
    val correspondenceSystemsText: String = "",
    val reversalsEnabled: Boolean = true,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
)

private data class DeckDraft(
    val name: String = "",
    val description: String = "",
    val author: String = "",
    val deckType: DeckType = DeckType.Custom,
    val tagsText: String = "",
    val correspondenceSystemsText: String = "",
    val reversalsEnabled: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class DeckEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val deckRepository: DeckRepository,
    private val imageRepository: ImageRepository,
) : ViewModel() {
    private val deckId: String = checkNotNull(savedStateHandle["deckId"])
    private val cardsFlow = deckRepository.observeCards(deckId)
    private val draft = MutableStateFlow(DeckDraft())
    private val errorMessage = MutableStateFlow<String?>(null)
    private val statusMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DeckEditorUiState> = combine(
        deckRepository.observeDeck(deckId),
        cardsFlow.flatMapLatest { cards ->
            val imageIds = cards.mapNotNull { it.imageId }.distinct()
            imageRepository.observeImages(imageIds).map { images ->
                val imagesById = images.associateBy { it.id }
                cards.map { card ->
                    CardListItem(
                        card = card,
                        image = card.imageId?.let(imagesById::get),
                    )
                }
            }
        },
        draft,
        errorMessage,
        statusMessage,
    ) { deck, cards, draft, error, status ->
        DeckEditorUiState(
            deck = deck,
            cards = cards,
            name = draft.name,
            description = draft.description,
            author = draft.author,
            deckType = draft.deckType,
            tagsText = draft.tagsText,
            correspondenceSystemsText = draft.correspondenceSystemsText,
            reversalsEnabled = draft.reversalsEnabled,
            isLoading = deck == null,
            errorMessage = error,
            statusMessage = status,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DeckEditorUiState(),
    )

    init {
        viewModelScope.launch {
            val deck = deckRepository.observeDeck(deckId).filterNotNull().first()
            draft.value = DeckDraft(
                name = deck.name,
                description = deck.description,
                author = deck.author,
                deckType = deck.deckType,
                tagsText = deck.tags.joinToString(", "),
                correspondenceSystemsText = deck.correspondenceSystems.joinToString(", "),
                reversalsEnabled = deck.reversalsEnabled,
            )
        }
    }

    fun updateName(value: String) {
        draft.value = draft.value.copy(name = value)
    }

    fun updateDescription(value: String) {
        draft.value = draft.value.copy(description = value)
    }

    fun updateAuthor(value: String) {
        draft.value = draft.value.copy(author = value)
    }

    fun updateDeckType(value: DeckType) {
        draft.value = draft.value.copy(deckType = value)
    }

    fun updateTags(value: String) {
        draft.value = draft.value.copy(tagsText = value)
    }

    fun updateCorrespondenceSystems(value: String) {
        draft.value = draft.value.copy(correspondenceSystemsText = value)
    }

    fun updateReversalsEnabled(value: Boolean) {
        draft.value = draft.value.copy(reversalsEnabled = value)
    }

    fun saveDeck() {
        viewModelScope.launch {
            val currentDeck = uiState.value.deck ?: return@launch
            runCatching {
                val currentDraft = draft.value
                deckRepository.updateDeck(
                    currentDeck.copy(
                        name = currentDraft.name.trim().ifBlank { "Untitled Deck" },
                        description = currentDraft.description.trim(),
                        author = currentDraft.author.trim(),
                        deckType = currentDraft.deckType,
                        tags = currentDraft.tagsText.toListValues(),
                        correspondenceSystems = currentDraft.correspondenceSystemsText.toListValues(),
                        reversalsEnabled = currentDraft.reversalsEnabled,
                        updatedAt = Instant.now(),
                    ),
                )
                statusMessage.value = "Deck saved"
                errorMessage.value = null
            }.onFailure { throwable ->
                errorMessage.value = throwable.message ?: "Deck could not be saved."
            }
        }
    }

    fun createCard(onCreated: (String) -> Unit) {
        viewModelScope.launch {
            runCatching {
                deckRepository.createCard(deckId)
            }.onSuccess { card ->
                onCreated(card.id)
            }.onFailure { throwable ->
                errorMessage.value = throwable.message ?: "Card could not be created."
            }
        }
    }

    fun deleteCard(card: CardEntity) {
        viewModelScope.launch {
            runCatching {
                deckRepository.deleteCard(card)
            }.onFailure { throwable ->
                errorMessage.value = throwable.message ?: "Card could not be deleted."
            }
        }
    }

    fun deleteDeck(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val deck = uiState.value.deck ?: return@launch
            runCatching {
                deckRepository.deleteDeck(deck)
            }.onSuccess {
                onDeleted()
            }.onFailure { throwable ->
                errorMessage.value = throwable.message ?: "Deck could not be deleted."
            }
        }
    }

    private fun String.toListValues(): List<String> =
        split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
}
