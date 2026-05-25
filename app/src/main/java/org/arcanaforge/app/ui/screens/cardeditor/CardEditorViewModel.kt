package org.arcanaforge.app.ui.screens.cardeditor

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.arcanaforge.app.core.database.entity.CardEntity
import org.arcanaforge.app.core.database.entity.StoredImageEntity
import org.arcanaforge.app.data.deck.DeckRepository
import org.arcanaforge.app.data.image.ImageRepository
import org.arcanaforge.app.domain.correspondence.CardCorrespondences

data class CardEditorUiState(
    val card: CardEntity? = null,
    val image: StoredImageEntity? = null,
    val title: String = "",
    val subtitle: String = "",
    val suit: String = "",
    val group: String = "",
    val keywordsText: String = "",
    val uprightMeaning: String = "",
    val reversedMeaning: String = "",
    val notes: String = "",
    val chakrasText: String = "",
    val crystalsText: String = "",
    val elementsText: String = "",
    val zodiacSignsText: String = "",
    val planetsText: String = "",
    val colorsText: String = "",
    val herbsText: String = "",
    val customCorrespondencesText: String = "",
    val aiPrompt: String = "",
    val isLoading: Boolean = true,
    val isImportingImage: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
)

private data class CardDraft(
    val title: String = "",
    val subtitle: String = "",
    val suit: String = "",
    val group: String = "",
    val keywordsText: String = "",
    val uprightMeaning: String = "",
    val reversedMeaning: String = "",
    val notes: String = "",
    val chakrasText: String = "",
    val crystalsText: String = "",
    val elementsText: String = "",
    val zodiacSignsText: String = "",
    val planetsText: String = "",
    val colorsText: String = "",
    val herbsText: String = "",
    val customCorrespondencesText: String = "",
    val aiPrompt: String = "",
)

@OptIn(ExperimentalCoroutinesApi::class)
class CardEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val deckRepository: DeckRepository,
    private val imageRepository: ImageRepository,
) : ViewModel() {
    private val cardId: String = checkNotNull(savedStateHandle["cardId"])
    private val cardFlow = deckRepository.observeCard(cardId)
    private val draft = MutableStateFlow(CardDraft())
    private val isImportingImage = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val statusMessage = MutableStateFlow<String?>(null)
    private val messages = combine(errorMessage, statusMessage) { error, status -> error to status }

    val uiState: StateFlow<CardEditorUiState> = combine(
        cardFlow,
        cardFlow.flatMapLatest { card ->
            card?.imageId?.let(imageRepository::observeImage) ?: flowOf(null)
        },
        draft,
        isImportingImage,
        messages,
    ) { card, image, draft, importing, messages ->
        CardEditorUiState(
            card = card,
            image = image,
            title = draft.title,
            subtitle = draft.subtitle,
            suit = draft.suit,
            group = draft.group,
            keywordsText = draft.keywordsText,
            uprightMeaning = draft.uprightMeaning,
            reversedMeaning = draft.reversedMeaning,
            notes = draft.notes,
            chakrasText = draft.chakrasText,
            crystalsText = draft.crystalsText,
            elementsText = draft.elementsText,
            zodiacSignsText = draft.zodiacSignsText,
            planetsText = draft.planetsText,
            colorsText = draft.colorsText,
            herbsText = draft.herbsText,
            customCorrespondencesText = draft.customCorrespondencesText,
            aiPrompt = draft.aiPrompt,
            isLoading = card == null,
            isImportingImage = importing,
            errorMessage = messages.first,
            statusMessage = messages.second,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CardEditorUiState(),
    )

    init {
        viewModelScope.launch {
            val card = cardFlow.filterNotNull().first()
            draft.value = CardDraft(
                title = card.title,
                subtitle = card.subtitle,
                suit = card.suit,
                group = card.group,
                keywordsText = card.keywords.joinToString(", "),
                uprightMeaning = card.uprightMeaning,
                reversedMeaning = card.reversedMeaning,
                notes = card.notes,
                chakrasText = card.correspondences.chakras.joinToString(", "),
                crystalsText = card.correspondences.crystals.joinToString(", "),
                elementsText = card.correspondences.elements.joinToString(", "),
                zodiacSignsText = card.correspondences.zodiacSigns.joinToString(", "),
                planetsText = card.correspondences.planets.joinToString(", "),
                colorsText = card.correspondences.colors.joinToString(", "),
                herbsText = card.correspondences.herbs.joinToString(", "),
                customCorrespondencesText = card.correspondences.custom.toTextValue(),
                aiPrompt = card.aiPrompt.orEmpty(),
            )
        }
    }

    fun updateTitle(value: String) {
        draft.value = draft.value.copy(title = value)
    }

    fun updateSubtitle(value: String) {
        draft.value = draft.value.copy(subtitle = value)
    }

    fun updateSuit(value: String) {
        draft.value = draft.value.copy(suit = value)
    }

    fun updateGroup(value: String) {
        draft.value = draft.value.copy(group = value)
    }

    fun updateKeywords(value: String) {
        draft.value = draft.value.copy(keywordsText = value)
    }

    fun updateUprightMeaning(value: String) {
        draft.value = draft.value.copy(uprightMeaning = value)
    }

    fun updateReversedMeaning(value: String) {
        draft.value = draft.value.copy(reversedMeaning = value)
    }

    fun updateNotes(value: String) {
        draft.value = draft.value.copy(notes = value)
    }

    fun updateChakras(value: String) {
        draft.value = draft.value.copy(chakrasText = value)
    }

    fun updateCrystals(value: String) {
        draft.value = draft.value.copy(crystalsText = value)
    }

    fun updateElements(value: String) {
        draft.value = draft.value.copy(elementsText = value)
    }

    fun updateZodiacSigns(value: String) {
        draft.value = draft.value.copy(zodiacSignsText = value)
    }

    fun updatePlanets(value: String) {
        draft.value = draft.value.copy(planetsText = value)
    }

    fun updateColors(value: String) {
        draft.value = draft.value.copy(colorsText = value)
    }

    fun updateHerbs(value: String) {
        draft.value = draft.value.copy(herbsText = value)
    }

    fun updateCustomCorrespondences(value: String) {
        draft.value = draft.value.copy(customCorrespondencesText = value)
    }

    fun updateAiPrompt(value: String) {
        draft.value = draft.value.copy(aiPrompt = value)
    }

    fun saveCard() {
        viewModelScope.launch {
            val currentCard = uiState.value.card ?: return@launch
            runCatching {
                deckRepository.updateCard(currentCard.withDraft())
                statusMessage.value = "Card saved"
                errorMessage.value = null
            }.onFailure { throwable ->
                errorMessage.value = throwable.message ?: "Card could not be saved."
            }
        }
    }

    fun attachImage(uri: Uri) {
        viewModelScope.launch {
            val currentCard = uiState.value.card ?: return@launch
            isImportingImage.value = true
            runCatching {
                val image = imageRepository.importPickedImage(uri)
                deckRepository.updateCard(currentCard.withDraft().copy(imageId = image.id))
                statusMessage.value = "Image attached"
                errorMessage.value = null
            }.onFailure { throwable ->
                errorMessage.value = throwable.message ?: "Image could not be imported."
            }
            isImportingImage.value = false
        }
    }

    private fun CardEntity.withDraft(): CardEntity {
        val currentDraft = draft.value
        return copy(
            title = currentDraft.title.trim().ifBlank { "Untitled Card" },
            subtitle = currentDraft.subtitle.trim(),
            suit = currentDraft.suit.trim(),
            group = currentDraft.group.trim(),
            keywords = currentDraft.keywordsText.toListValues(),
            uprightMeaning = currentDraft.uprightMeaning.trim(),
            reversedMeaning = currentDraft.reversedMeaning.trim(),
            notes = currentDraft.notes.trim(),
            correspondences = CardCorrespondences(
                chakras = currentDraft.chakrasText.toListValues(),
                crystals = currentDraft.crystalsText.toListValues(),
                elements = currentDraft.elementsText.toListValues(),
                zodiacSigns = currentDraft.zodiacSignsText.toListValues(),
                planets = currentDraft.planetsText.toListValues(),
                colors = currentDraft.colorsText.toListValues(),
                herbs = currentDraft.herbsText.toListValues(),
                custom = currentDraft.customCorrespondencesText.toCustomCorrespondences(),
            ),
            aiPrompt = currentDraft.aiPrompt.trim().ifBlank { null },
            updatedAt = Instant.now(),
        )
    }

    private fun String.toListValues(): List<String> =
        split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

    private fun String.toCustomCorrespondences(): Map<String, List<String>> =
        lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() && it.contains(":") }
            .map { line ->
                val key = line.substringBefore(":").trim()
                val values = line.substringAfter(":").toListValues()
                key to values
            }
            .filter { (key, values) -> key.isNotBlank() && values.isNotEmpty() }
            .toMap()

    private fun Map<String, List<String>>.toTextValue(): String =
        entries.joinToString("\n") { (key, values) ->
            "$key: ${values.joinToString(", ")}"
        }
}
