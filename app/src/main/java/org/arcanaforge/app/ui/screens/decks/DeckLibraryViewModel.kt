package org.arcanaforge.app.ui.screens.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.arcanaforge.app.core.database.entity.DeckEntity
import org.arcanaforge.app.data.deck.DeckRepository

data class DeckLibraryUiState(
    val decks: List<DeckEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class DeckLibraryViewModel(
    private val deckRepository: DeckRepository,
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DeckLibraryUiState> = combine(
        deckRepository.observeDecks(),
        searchQuery,
        errorMessage,
    ) { decks, query, error ->
        val filtered = if (query.isBlank()) {
            decks
        } else {
            decks.filter { deck ->
                deck.name.contains(query, ignoreCase = true) ||
                    deck.description.contains(query, ignoreCase = true) ||
                    deck.tags.any { it.contains(query, ignoreCase = true) }
            }
        }

        DeckLibraryUiState(
            decks = filtered,
            searchQuery = query,
            isLoading = false,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DeckLibraryUiState(),
    )

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun createDeck(onCreated: (String) -> Unit) {
        viewModelScope.launch {
            runCatching {
                val number = uiState.value.decks.count { it.name.startsWith("Custom Deck") } + 1
                deckRepository.createDeck("Custom Deck $number")
            }.onSuccess { deck ->
                onCreated(deck.id)
            }.onFailure { throwable ->
                errorMessage.value = throwable.message ?: "Deck could not be created."
            }
        }
    }

    fun toggleFavorite(deck: DeckEntity) {
        viewModelScope.launch {
            runCatching {
                deckRepository.toggleFavorite(deck)
            }.onFailure { throwable ->
                errorMessage.value = throwable.message ?: "Favorite could not be updated."
            }
        }
    }
}
