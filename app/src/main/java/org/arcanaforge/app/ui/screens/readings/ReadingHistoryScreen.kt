package org.arcanaforge.app.ui.screens.readings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.arcanaforge.app.ui.components.EmptyState

@Composable
fun ReadingHistoryScreen(
    viewModelFactory: ViewModelProvider.Factory,
    onCreateReading: () -> Unit,
    onOpenReading: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ReadingHistoryViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Reading History",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "Saved local readings and notes.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Button(onClick = onCreateReading) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                    Text(text = "New", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        if (uiState.readings.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.AutoStories,
                    title = "No readings yet",
                    body = "Create a reading to save drawn cards and reflections.",
                )
            }
        } else {
            items(uiState.readings, key = { it.id }) { reading ->
                ReadingHistoryRow(
                    reading = reading,
                    onClick = { onOpenReading(reading.id) },
                )
            }
        }
    }
}

@Composable
private fun ReadingHistoryRow(
    reading: ReadingHistoryItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = reading.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "${reading.deckName} • ${reading.layoutName} • ${reading.createdAt}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (reading.question.isNotBlank()) {
                Text(
                    text = reading.question,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
