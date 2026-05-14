package org.arcanaforge.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Interests
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.arcanaforge.app.core.database.entity.DeckEntity
import org.arcanaforge.app.core.database.entity.ReadingEntity
import org.arcanaforge.app.core.database.entity.ScheduledReadingEntity
import org.arcanaforge.app.domain.deck.displayName
import org.arcanaforge.app.ui.components.EmptyState
import org.arcanaforge.app.ui.navigation.Screen

@Composable
fun HomeScreen(
    viewModelFactory: ViewModelProvider.Factory,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: HomeViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onNavigate = onNavigate,
        modifier = modifier,
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Moonlight Guidance",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Local decks, layouts, readings, and notes.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (uiState.isLoading) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        item {
            QuickActions(onNavigate = onNavigate)
        }

        item {
            NextScheduleCard(schedule = uiState.nextSchedule)
        }

        item {
            SectionTitle(title = "Favorite Decks")
        }

        if (uiState.decks.isEmpty() && !uiState.isLoading) {
            item {
                EmptyState(
                    icon = Icons.AutoMirrored.Outlined.LibraryBooks,
                    title = "No decks yet",
                    body = "Create or import a deck to begin.",
                )
            }
        } else if (uiState.favoriteDecks.isEmpty()) {
            item {
                Text(
                    text = "Favorite decks will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(uiState.favoriteDecks, key = { it.id }) { deck ->
                DeckSummaryCard(deck = deck)
            }
        }

        item {
            SectionTitle(title = "Recent Readings")
        }

        if (uiState.recentReadings.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.AutoStories,
                    title = "No readings saved",
                    body = "Saved readings and notes will appear here.",
                )
            }
        } else {
            items(uiState.recentReadings, key = { it.id }) { reading ->
                ReadingSummaryCard(reading = reading)
            }
        }
    }
}

@Composable
private fun QuickActions(onNavigate: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionButton(
                label = "Quick Draw",
                icon = Icons.Outlined.PlayArrow,
                onClick = { onNavigate(Screen.Readings.route) },
                modifier = Modifier.weight(1f),
            )
            QuickActionButton(
                label = "New Reading",
                icon = Icons.Outlined.AutoStories,
                onClick = { onNavigate(Screen.Readings.route) },
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionButton(
                label = "Deck Library",
                icon = Icons.AutoMirrored.Outlined.LibraryBooks,
                onClick = { onNavigate(Screen.Decks.route) },
                modifier = Modifier.weight(1f),
            )
            QuickActionButton(
                label = "Layout Library",
                icon = Icons.Outlined.Interests,
                onClick = { onNavigate(Screen.Layouts.route) },
                modifier = Modifier.weight(1f),
            )
        }
        QuickActionButton(
            label = "Scheduled Readings",
            icon = Icons.Outlined.CalendarMonth,
            onClick = { onNavigate(Screen.Schedule.route) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp),
            maxLines = 2,
        )
    }
}

@Composable
private fun NextScheduleCard(schedule: ScheduledReadingEntity?) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Next Scheduled Reading",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = schedule?.let { "${it.title} at ${it.reminderTime}" }
                    ?: "No reminders scheduled.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun DeckSummaryCard(deck: DeckEntity) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = deck.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Icon(
                    imageVector = Icons.Outlined.SelfImprovement,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
            Text(
                text = deck.deckType.displayName(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
            if (deck.description.isNotBlank()) {
                Text(
                    text = deck.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ReadingSummaryCard(reading: ReadingEntity) {
    val date = rememberDateFormatter().format(reading.createdAt.atZone(ZoneId.systemDefault()))
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = reading.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun rememberDateFormatter(): DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d, yyyy")
