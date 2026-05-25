package org.arcanaforge.app.ui.screens.readings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.io.File
import org.arcanaforge.app.domain.ai.AiChatMessage
import org.arcanaforge.app.domain.ai.AiChatRole
import org.arcanaforge.app.domain.reading.ReadingOrientation
import org.arcanaforge.app.ui.components.ConfirmDeleteDialog

@Composable
fun ReadingDetailScreen(
    viewModelFactory: ViewModelProvider.Factory,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ReadingDetailViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedItem by remember { mutableStateOf<ReadingDetailItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Delete reading",
            body = "This deletes the saved reading and its notes from this device.",
            confirmLabel = "Delete Reading",
            onConfirm = { viewModel.deleteReading(onDeleted) },
            onDismiss = { showDeleteDialog = false },
        )
    }

    selectedItem?.let { item ->
        ReadingCardDetailDialog(
            item = item,
            onSaveNote = { note ->
                viewModel.saveReadingCardNote(item.readingCard.id, note)
                selectedItem = null
            },
            onDismiss = { selectedItem = null },
        )
    }

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

        uiState.reading?.let { reading ->
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = reading.title,
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Text(
                                text = "${uiState.deckName} • ${uiState.layoutName}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row {
                            IconButton(
                                onClick = {
                                    runCatching {
                                        shareReadingImage(context, uiState)
                                    }.onFailure { throwable ->
                                        Toast.makeText(
                                            context,
                                            throwable.message ?: "Reading image could not be shared.",
                                            Toast.LENGTH_LONG,
                                        ).show()
                                    }
                                },
                                enabled = uiState.items.isNotEmpty(),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = "Share reading image",
                                )
                            }
                            IconButton(onClick = viewModel::toggleFavorite) {
                                Icon(
                                    imageVector = if (reading.isFavorite) {
                                        Icons.Outlined.Favorite
                                    } else {
                                        Icons.Outlined.FavoriteBorder
                                    },
                                    contentDescription = if (reading.isFavorite) {
                                        "Remove favorite"
                                    } else {
                                        "Mark favorite"
                                    },
                                    tint = if (reading.isFavorite) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete reading",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                    if (reading.question.isNotBlank()) {
                        Text(
                            text = reading.question,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }

        if (uiState.reading != null) {
            item {
                ReadingNotesEditor(
                    notes = uiState.readingNotes,
                    onNotesChange = viewModel::updateReadingNotes,
                    onSave = viewModel::saveReadingNotes,
                )
            }

            item {
                ReadingAiChatPanel(
                    messages = uiState.aiChatMessages,
                    questionDraft = uiState.aiQuestionDraft,
                    isAsking = uiState.isAskingAi,
                    readingTitle = uiState.reading?.title.orEmpty(),
                    onQuestionChange = viewModel::updateAiQuestionDraft,
                    onAsk = viewModel::askAiAboutReading,
                )
            }
        }

        items(uiState.items, key = { it.readingCard.id }) { item ->
            ReadingCardRow(
                item = item,
                onClick = { selectedItem = item },
            )
        }
    }
}

@Composable
private fun ReadingAiChatPanel(
    messages: List<AiChatMessage>,
    questionDraft: String,
    isAsking: Boolean,
    readingTitle: String,
    onQuestionChange: (String) -> Unit,
    onAsk: () -> Unit,
) {
    val context = LocalContext.current
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Ask About This Reading",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            if (messages.isEmpty()) {
                Text(
                    text = "Ask for a reflective interpretation, patterns between cards, or how a real-life event might connect symbolically.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    messages.forEach { message ->
                        AiChatBubble(message)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = {
                            copyChatTranscript(
                                context = context,
                                readingTitle = readingTitle,
                                messages = messages,
                            )
                        },
                    ) {
                        Text("Copy Chat")
                    }
                    TextButton(
                        onClick = {
                            shareChatTranscript(
                                context = context,
                                readingTitle = readingTitle,
                                messages = messages,
                            )
                        },
                    ) {
                        Text("Share Chat")
                    }
                }
            }
            if (isAsking) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = questionDraft,
                    onValueChange = onQuestionChange,
                    modifier = Modifier.weight(1f),
                    minLines = 2,
                    label = { Text("Question") },
                    placeholder = { Text("What do you think this could mean?") },
                    enabled = !isAsking,
                )
                IconButton(
                    onClick = onAsk,
                    enabled = questionDraft.isNotBlank() && !isAsking,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Ask AI",
                    )
                }
            }
        }
    }
}

@Composable
private fun AiChatBubble(
    message: AiChatMessage,
) {
    val isUser = message.role == AiChatRole.User
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
    ) {
        Text(
            text = if (isUser) "You" else "AI",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.86f else 0.96f)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isUser) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                )
                .padding(12.dp),
        ) {
            SelectionContainer {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private fun copyChatTranscript(
    context: Context,
    readingTitle: String,
    messages: List<AiChatMessage>,
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(
        ClipData.newPlainText(
            "Moonlight Guidance reading chat",
            formatChatTranscript(readingTitle, messages),
        ),
    )
    Toast.makeText(context, "Chat copied.", Toast.LENGTH_SHORT).show()
}

private fun shareChatTranscript(
    context: Context,
    readingTitle: String,
    messages: List<AiChatMessage>,
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Moonlight Guidance - $readingTitle")
        putExtra(Intent.EXTRA_TEXT, formatChatTranscript(readingTitle, messages))
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share reading chat"))
}

private fun formatChatTranscript(
    readingTitle: String,
    messages: List<AiChatMessage>,
): String = buildString {
    appendLine("Moonlight Guidance")
    if (readingTitle.isNotBlank()) {
        appendLine(readingTitle)
    }
    appendLine()
    messages.forEach { message ->
        appendLine(if (message.role == AiChatRole.User) "You:" else "AI:")
        appendLine(message.text)
        appendLine()
    }
}.trim()

@Composable
private fun ReadingCardRow(
    item: ReadingDetailItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ReadingCardImage(
                item = item,
                modifier = Modifier.size(width = 88.dp, height = 132.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = onClick,
                        label = { Text(item.slot.title) },
                    )
                    AssistChip(
                        onClick = onClick,
                        label = { Text(item.readingCard.orientation.displayName()) },
                    )
                }
                Text(
                    text = item.card.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                if (item.slot.description.isNotBlank()) {
                    Text(
                        text = item.slot.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val meaning = if (item.readingCard.orientation == ReadingOrientation.Reversed) {
                    item.card.reversedMeaning.ifBlank { item.card.uprightMeaning }
                } else {
                    item.card.uprightMeaning
                }
                if (meaning.isNotBlank()) {
                    Text(
                        text = meaning,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                if (item.card.correspondences.hasAnyValue()) {
                    Text(
                        text = item.card.correspondences.summary(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadingCardImage(
    item: ReadingDetailItem,
    modifier: Modifier,
) {
    val imagePath = item.image?.thumbnailPath ?: item.image?.localPath
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (imagePath != null) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = "Image for ${item.card.title}",
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        rotationZ = if (item.readingCard.orientation == ReadingOrientation.Reversed) {
                            180f
                        } else {
                            0f
                        }
                    },
                contentScale = ContentScale.Fit,
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
private fun ReadingCardDetailDialog(
    item: ReadingDetailItem,
    onSaveNote: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var noteDraft by remember(item.readingCard.id) { mutableStateOf(item.readingCard.userNote) }
    val meaning = if (item.readingCard.orientation == ReadingOrientation.Reversed) {
        item.card.reversedMeaning.ifBlank { item.card.uprightMeaning }
    } else {
        item.card.uprightMeaning
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = item.card.title)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    ReadingCardImage(
                        item = item,
                        modifier = Modifier.size(width = 180.dp, height = 270.dp),
                    )
                }
                DetailLine(label = "Slot", value = item.slot.title)
                DetailLine(label = "Orientation", value = item.readingCard.orientation.displayName())
                if (item.card.subtitle.isNotBlank()) {
                    DetailLine(label = "Subtitle", value = item.card.subtitle)
                }
                if (item.card.suit.isNotBlank()) {
                    DetailLine(label = "Suit", value = item.card.suit)
                }
                if (item.card.group.isNotBlank()) {
                    DetailLine(label = "Group", value = item.card.group)
                }
                if (item.card.keywords.isNotEmpty()) {
                    DetailLine(label = "Keywords", value = item.card.keywords.joinToString(", "))
                }
                if (item.slot.description.isNotBlank()) {
                    DetailSection(label = "Slot meaning", value = item.slot.description)
                }
                if (meaning.isNotBlank()) {
                    DetailSection(
                        label = "${item.readingCard.orientation.displayName()} meaning",
                        value = meaning,
                    )
                }
                if (item.card.correspondences.hasAnyValue()) {
                    DetailSection(label = "Correspondences", value = item.card.correspondences.summary())
                }
                if (item.readingCard.userNote.isNotBlank()) {
                    DetailSection(label = "Reading note", value = item.readingCard.userNote)
                }
                if (item.card.notes.isNotBlank()) {
                    DetailSection(label = "Card notes", value = item.card.notes)
                }
                if (!item.readingCard.aiInterpretation.isNullOrBlank()) {
                    DetailSection(label = "AI interpretation", value = item.readingCard.aiInterpretation)
                }
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = { noteDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text("Card note") },
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        confirmButton = {
            TextButton(onClick = { onSaveNote(noteDraft) }) {
                Text("Save Note")
            }
        },
    )
}

@Composable
private fun ReadingNotesEditor(
    notes: String,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Reading Notes",
            style = MaterialTheme.typography.titleLarge,
        )
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            label = { Text("Journal notes") },
        )
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save Reading Notes")
        }
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String,
) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun DetailSection(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
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

private fun ReadingOrientation.displayName(): String =
    name.lowercase().replaceFirstChar { it.titlecase() }

private fun org.arcanaforge.app.domain.correspondence.CardCorrespondences.hasAnyValue(): Boolean =
    chakras.isNotEmpty() ||
        crystals.isNotEmpty() ||
        elements.isNotEmpty() ||
        zodiacSigns.isNotEmpty() ||
        planets.isNotEmpty() ||
        colors.isNotEmpty() ||
        herbs.isNotEmpty() ||
        custom.isNotEmpty()

private fun org.arcanaforge.app.domain.correspondence.CardCorrespondences.summary(): String {
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
    return parts.joinToString(" • ")
}
