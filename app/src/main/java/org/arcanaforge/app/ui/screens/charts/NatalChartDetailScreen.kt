package org.arcanaforge.app.ui.screens.charts

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.arcanaforge.app.data.astrology.NatalChartRecord
import org.arcanaforge.app.domain.ai.AiChatMessage
import org.arcanaforge.app.domain.ai.AiChatRole
import org.arcanaforge.app.domain.astrology.ChartAspect
import org.arcanaforge.app.domain.astrology.ChartPlacement
import org.arcanaforge.app.ui.components.ConfirmDeleteDialog
import org.arcanaforge.app.ui.navigation.AppViewModelFactory

@Composable
fun NatalChartDetailScreen(
    viewModelFactory: AppViewModelFactory,
    onDeleted: () -> Unit,
) {
    val viewModel: NatalChartDetailViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearError()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState)
        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        val chart = uiState.chart
        if (chart != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    NatalChartHeader(
                        chart = chart,
                        onFavorite = viewModel::toggleFavorite,
                        onShare = { shareChart(context, chart) },
                        onDelete = { showDeleteDialog = true },
                    )
                }
                item {
                    NatalChartNotes(
                        notes = uiState.notesDraft,
                        onNotesChange = viewModel::updateNotes,
                        onSave = viewModel::saveNotes,
                    )
                }
                item {
                    NatalChartAiPanel(
                        title = chart.entity.label,
                        messages = uiState.aiMessages,
                        questionDraft = uiState.aiQuestionDraft,
                        isAsking = uiState.isAskingAi,
                        onQuestionChange = viewModel::updateAiQuestionDraft,
                        onAsk = viewModel::askAiAboutChart,
                    )
                }
                item {
                    Text(text = "Placements", style = MaterialTheme.typography.titleLarge)
                }
                items(chart.snapshot.placements, key = { it.body.name }) { placement ->
                    PlacementRow(placement)
                }
                if (chart.snapshot.aspects.isNotEmpty()) {
                    item {
                        Text(text = "Major Aspects", style = MaterialTheme.typography.titleLarge)
                    }
                    items(chart.snapshot.aspects) { aspect ->
                        AspectRow(aspect)
                    }
                }
            }
        }
    }

    if (showDeleteDialog && uiState.chart != null) {
        ConfirmDeleteDialog(
            title = "Delete natal chart?",
            body = "This removes the chart, notes, and chart AI conversation.",
            confirmLabel = "Delete chart",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteChart(onDeleted)
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun NatalChartHeader(
    chart: NatalChartRecord,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = chart.entity.label, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = "${chart.entity.subjectName} - ${chart.entity.birthDate}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = if (chart.entity.timeKnown) {
                            "${chart.entity.birthTime} ${chart.entity.zoneId}"
                        } else {
                            "Time unknown; noon used for approximate placements"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (chart.entity.locationName.isNotBlank()) {
                        Text(
                            text = chart.entity.locationName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Row {
                    IconButton(onClick = onFavorite) {
                        Icon(
                            imageVector = if (chart.entity.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite chart",
                        )
                    }
                    IconButton(onClick = onShare) {
                        Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share chart")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete chart")
                    }
                }
            }
        }
    }
}

@Composable
private fun NatalChartNotes(
    notes: String,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Notes", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                label = { Text("Chart notes") },
            )
            Button(
                onClick = onSave,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Save Notes")
            }
        }
    }
}

@Composable
private fun PlacementRow(placement: ChartPlacement) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = placement.body.displayName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = buildString {
                        append("${formatDegrees(placement.degreeInSign)} ${placement.sign.displayName}")
                        placement.house?.let { append(" - House $it") }
                        if (placement.retrograde) append(" - Retrograde")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = "${formatDegrees(placement.longitude)}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AspectRow(aspect: ChartAspect) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "${aspect.first.displayName} ${aspect.type.displayName} ${aspect.second.displayName}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Orb ${formatDegrees(aspect.orb)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NatalChartAiPanel(
    title: String,
    messages: List<AiChatMessage>,
    questionDraft: String,
    isAsking: Boolean,
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
                Text(text = "Ask About This Chart", style = MaterialTheme.typography.titleLarge)
            }
            if (messages.isEmpty()) {
                Text(
                    text = "Ask about placements, patterns, aspects, or symbolic connections with life events.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    messages.forEach { message -> AiChatBubble(message) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { copyChartChat(context, title, messages) }) {
                        Text("Copy Chat")
                    }
                    TextButton(onClick = { shareChartChat(context, title, messages) }) {
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
                    placeholder = { Text("What patterns stand out here?") },
                    enabled = !isAsking,
                )
                IconButton(
                    onClick = onAsk,
                    enabled = questionDraft.isNotBlank() && !isAsking,
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Outlined.Send, contentDescription = "Ask AI")
                }
            }
        }
    }
}

@Composable
private fun AiChatBubble(message: AiChatMessage) {
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
                Text(text = message.text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun shareChart(context: Context, chart: NatalChartRecord) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Moonlight Guidance - ${chart.entity.label}")
        putExtra(Intent.EXTRA_TEXT, formatChartSummary(chart))
    }
    context.startActivity(Intent.createChooser(intent, "Share natal chart"))
}

private fun copyChartChat(context: Context, title: String, messages: List<AiChatMessage>) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Moonlight Guidance chart chat", formatChatTranscript(title, messages)))
    Toast.makeText(context, "Chat copied.", Toast.LENGTH_SHORT).show()
}

private fun shareChartChat(context: Context, title: String, messages: List<AiChatMessage>) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Moonlight Guidance - $title")
        putExtra(Intent.EXTRA_TEXT, formatChatTranscript(title, messages))
    }
    context.startActivity(Intent.createChooser(intent, "Share chart chat"))
}

private fun formatChatTranscript(title: String, messages: List<AiChatMessage>): String = buildString {
    appendLine("Moonlight Guidance")
    appendLine(title)
    appendLine()
    messages.forEach { message ->
        appendLine(if (message.role == AiChatRole.User) "You:" else "AI:")
        appendLine(message.text)
        appendLine()
    }
}.trim()

private fun formatChartSummary(chart: NatalChartRecord): String = buildString {
    appendLine("Moonlight Guidance")
    appendLine(chart.entity.label)
    appendLine("${chart.entity.subjectName} - ${chart.entity.birthDate}")
    appendLine()
    appendLine("Placements")
    chart.snapshot.placements.forEach { placement ->
        append("- ${placement.body.displayName}: ${formatDegrees(placement.degreeInSign)} ${placement.sign.displayName}")
        placement.house?.let { append(", House $it") }
        if (placement.retrograde) append(", retrograde")
        appendLine()
    }
    if (chart.snapshot.aspects.isNotEmpty()) {
        appendLine()
        appendLine("Major Aspects")
        chart.snapshot.aspects.forEach { aspect ->
            appendLine("- ${aspect.first.displayName} ${aspect.type.displayName.lowercase()} ${aspect.second.displayName}, orb ${formatDegrees(aspect.orb)}")
        }
    }
}.trim()
