package org.arcanaforge.app.ui.screens.deckeditor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.arcanaforge.app.core.database.entity.CardEntity
import org.arcanaforge.app.domain.deck.DeckType
import org.arcanaforge.app.domain.deck.displayName
import org.arcanaforge.app.ui.components.ConfirmDeleteDialog
import org.arcanaforge.app.ui.components.EmptyState
import coil.compose.AsyncImage
import java.io.File

@Composable
fun DeckEditorScreen(
    viewModelFactory: ViewModelProvider.Factory,
    onOpenCard: (String) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: DeckEditorViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Delete deck",
            body = "This deletes the deck and its cards from this device. Existing readings may lose their card details.",
            confirmLabel = "Delete Deck",
            onConfirm = { viewModel.deleteDeck(onDeleted) },
            onDismiss = { showDeleteDialog = false },
        )
    }

    DeckEditorContent(
        uiState = uiState,
        onNameChange = viewModel::updateName,
        onDescriptionChange = viewModel::updateDescription,
        onAuthorChange = viewModel::updateAuthor,
        onDeckTypeChange = viewModel::updateDeckType,
        onTagsChange = viewModel::updateTags,
        onCorrespondenceSystemsChange = viewModel::updateCorrespondenceSystems,
        onReversalsChange = viewModel::updateReversalsEnabled,
        onSave = viewModel::saveDeck,
        onCreateCard = { viewModel.createCard(onOpenCard) },
        onOpenCard = onOpenCard,
        onDeleteCard = viewModel::deleteCard,
        onRequestDeleteDeck = { showDeleteDialog = true },
        modifier = modifier,
    )
}

@Composable
private fun DeckEditorContent(
    uiState: DeckEditorUiState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onDeckTypeChange: (DeckType) -> Unit,
    onTagsChange: (String) -> Unit,
    onCorrespondenceSystemsChange: (String) -> Unit,
    onReversalsChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onCreateCard: () -> Unit,
    onOpenCard: (String) -> Unit,
    onDeleteCard: (CardEntity) -> Unit,
    onRequestDeleteDeck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (uiState.isLoading) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        item {
            DeckForm(
                uiState = uiState,
                onNameChange = onNameChange,
                onDescriptionChange = onDescriptionChange,
                onAuthorChange = onAuthorChange,
                onDeckTypeChange = onDeckTypeChange,
                onTagsChange = onTagsChange,
                onCorrespondenceSystemsChange = onCorrespondenceSystemsChange,
                onReversalsChange = onReversalsChange,
                onSave = onSave,
                onRequestDeleteDeck = onRequestDeleteDeck,
            )
        }

        uiState.errorMessage?.let { message ->
            item {
                MessageCard(message = message, isError = true)
            }
        }

        uiState.statusMessage?.let { message ->
            item {
                MessageCard(message = message, isError = false)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Cards",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Button(onClick = onCreateCard) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                    Text(text = "Add Card", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        if (uiState.cards.isEmpty() && !uiState.isLoading) {
            item {
                EmptyState(
                    icon = Icons.Outlined.Style,
                    title = "No cards yet",
                    body = "Add cards to build this deck.",
                )
            }
        } else {
            items(uiState.cards, key = { it.card.id }) { item ->
                CardRow(
                    item = item,
                    onOpen = { onOpenCard(item.card.id) },
                    onDelete = { onDeleteCard(item.card) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckForm(
    uiState: DeckEditorUiState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onDeckTypeChange: (DeckType) -> Unit,
    onTagsChange: (String) -> Unit,
    onCorrespondenceSystemsChange: (String) -> Unit,
    onReversalsChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onRequestDeleteDeck: () -> Unit,
) {
    var deckTypeExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = uiState.deck?.name ?: "Deck",
            style = MaterialTheme.typography.headlineMedium,
        )
        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Name") },
        )
        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            label = { Text("Description") },
        )
        OutlinedTextField(
            value = uiState.author,
            onValueChange = onAuthorChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Author") },
        )
        ExposedDropdownMenuBox(
            expanded = deckTypeExpanded,
            onExpandedChange = { deckTypeExpanded = !deckTypeExpanded },
        ) {
            OutlinedTextField(
                value = uiState.deckType.displayName(),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                label = { Text("Deck type") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = deckTypeExpanded)
                },
            )
            DropdownMenu(
                expanded = deckTypeExpanded,
                onDismissRequest = { deckTypeExpanded = false },
            ) {
                DeckType.entries.forEach { deckType ->
                    DropdownMenuItem(
                        text = { Text(deckType.displayName()) },
                        onClick = {
                            onDeckTypeChange(deckType)
                            deckTypeExpanded = false
                        },
                    )
                }
            }
        }
        OutlinedTextField(
            value = uiState.tagsText,
            onValueChange = onTagsChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Tags") },
            supportingText = { Text("Comma-separated") },
        )
        OutlinedTextField(
            value = uiState.correspondenceSystemsText,
            onValueChange = onCorrespondenceSystemsChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Correspondence systems") },
            supportingText = { Text("Example: chakras, crystals, elements") },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Reversals enabled",
                style = MaterialTheme.typography.bodyLarge,
            )
            Switch(
                checked = uiState.reversalsEnabled,
                onCheckedChange = onReversalsChange,
            )
        }
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text("Save Deck")
        }
        Button(
            onClick = onRequestDeleteDeck,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
            Text("Delete Deck", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun CardRow(
    item: CardListItem,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    val card = item.card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (card.subtitle.isNotBlank()) {
                    Text(
                        text = card.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (card.keywords.isNotEmpty()) {
                        AssistChip(
                            onClick = {},
                            label = { Text(card.keywords.take(2).joinToString(", ")) },
                        )
                    }
                    if (card.imageId != null) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Image") },
                        )
                    }
                }
            }
            if (card.imageId != null) {
                CardThumbnail(item = item)
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete card",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun CardThumbnail(item: CardListItem) {
    val imagePath = item.image?.thumbnailPath ?: item.image?.localPath
    Box(
        modifier = Modifier
            .padding(start = 12.dp)
            .size(width = 56.dp, height = 80.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (imagePath != null) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = "Thumbnail for ${item.card.title}",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.ImageNotSupported,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MessageCard(
    message: String,
    isError: Boolean,
) {
    Card {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        )
    }
}
