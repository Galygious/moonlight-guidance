package org.arcanaforge.app.ui.screens.decks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.arcanaforge.app.core.database.entity.DeckEntity
import org.arcanaforge.app.domain.deck.displayName
import org.arcanaforge.app.ui.components.EmptyState

@Composable
fun DeckLibraryScreen(
    viewModelFactory: ViewModelProvider.Factory,
    onOpenDeck: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: DeckLibraryViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DeckLibraryContent(
        uiState = uiState,
        onSearchChange = viewModel::updateSearchQuery,
        onCreateDeck = { viewModel.createDeck(onOpenDeck) },
        onToggleFavorite = viewModel::toggleFavorite,
        onOpenDeck = onOpenDeck,
        modifier = modifier,
    )
}

@Composable
private fun DeckLibraryContent(
    uiState: DeckLibraryUiState,
    onSearchChange: (String) -> Unit,
    onCreateDeck: () -> Unit,
    onToggleFavorite: (DeckEntity) -> Unit,
    onOpenDeck: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Deck Library",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Create, search, and favorite local decks.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FilledTonalButton(onClick = onCreateDeck) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                    )
                    Text(
                        text = "Create Deck",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                    )
                },
                singleLine = true,
                label = { Text("Search decks") },
            )
        }

        uiState.errorMessage?.let { message ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }

        if (uiState.isLoading) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        if (uiState.decks.isEmpty() && !uiState.isLoading) {
            item {
                EmptyState(
                    icon = Icons.AutoMirrored.Outlined.LibraryBooks,
                    title = "No matching decks",
                    body = "Create a deck or clear the search field.",
                )
            }
        } else {
            items(uiState.decks, key = { it.id }) { deck ->
                DeckRow(
                    deck = deck,
                    onOpen = { onOpenDeck(deck.id) },
                    onToggleFavorite = { onToggleFavorite(deck) },
                )
            }
        }
    }
}

@Composable
private fun DeckRow(
    deck: DeckEntity,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = deck.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = deck.deckType.displayName(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (deck.isFavorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (deck.isFavorite) "Remove favorite" else "Mark favorite",
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            if (deck.description.isNotBlank()) {
                Text(
                    text = deck.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (deck.tags.isNotEmpty() || deck.correspondenceSystems.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (deck.tags + deck.correspondenceSystems).take(3).forEach { label ->
                        AssistChip(
                            onClick = {},
                            label = { Text(label) },
                        )
                    }
                }
            }
        }
    }
}
