package org.arcanaforge.app.ui.screens.readings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.arcanaforge.app.core.database.entity.CardEntity
import org.arcanaforge.app.core.database.entity.LayoutSlotEntity
import org.arcanaforge.app.domain.reading.ReadingOrientation

@Composable
fun ReadingCreateScreen(
    viewModelFactory: ViewModelProvider.Factory,
    onReadingCreated: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ReadingCreateViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ReadingCreateContent(
        uiState = uiState,
        onDeckChange = viewModel::updateDeck,
        onLayoutChange = viewModel::updateLayout,
        onQuestionChange = viewModel::updateQuestion,
        onModeChange = viewModel::updateMode,
        onReversalsChange = viewModel::updateReversalsEnabled,
        onManualCardChange = viewModel::updateManualCard,
        onManualOrientationChange = viewModel::updateManualOrientation,
        onDraw = { viewModel.drawAndSave(onReadingCreated) },
        modifier = modifier,
    )
}

@Composable
private fun ReadingCreateContent(
    uiState: ReadingCreateUiState,
    onDeckChange: (String) -> Unit,
    onLayoutChange: (String) -> Unit,
    onQuestionChange: (String) -> Unit,
    onModeChange: (ReadingCreateMode) -> Unit,
    onReversalsChange: (Boolean) -> Unit,
    onManualCardChange: (String, String) -> Unit,
    onManualOrientationChange: (String, ReadingOrientation) -> Unit,
    onDraw: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "New Reading",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Choose a local deck and layout, then draw without replacement.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (uiState.isSaving) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        uiState.errorMessage?.let { message ->
            item {
                MessageCard(message = message)
            }
        }

        item {
            DeckDropdown(
                uiState = uiState,
                onDeckChange = onDeckChange,
            )
        }
        item {
            LayoutDropdown(
                uiState = uiState,
                onLayoutChange = onLayoutChange,
            )
        }
        item {
            ModeSelector(
                selectedMode = uiState.mode,
                onModeChange = onModeChange,
            )
        }
        item {
            OutlinedTextField(
                value = uiState.question,
                onValueChange = onQuestionChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                label = { Text("Question or intention") },
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reversals enabled",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Applies only to slots that allow reversed cards.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = uiState.reversalsEnabled,
                    onCheckedChange = onReversalsChange,
                )
            }
        }
        if (uiState.mode == ReadingCreateMode.Manual) {
            item {
                ManualReadingCard(
                    uiState = uiState,
                    onManualCardChange = onManualCardChange,
                    onManualOrientationChange = onManualOrientationChange,
                )
            }
        }
        item {
            Button(
                onClick = onDraw,
                enabled = !uiState.isSaving && uiState.decks.isNotEmpty() && uiState.layouts.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (uiState.isSaving) {
                        "Saving..."
                    } else if (uiState.mode == ReadingCreateMode.Manual) {
                        "Save Physical Reading"
                    } else {
                        "Draw and Save Reading"
                    },
                )
            }
        }
    }
}

@Composable
private fun ModeSelector(
    selectedMode: ReadingCreateMode,
    onModeChange: (ReadingCreateMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Draw method",
            style = MaterialTheme.typography.titleMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedMode == ReadingCreateMode.Random,
                onClick = { onModeChange(ReadingCreateMode.Random) },
                label = { Text("In-app draw") },
            )
            FilterChip(
                selected = selectedMode == ReadingCreateMode.Manual,
                onClick = { onModeChange(ReadingCreateMode.Manual) },
                label = { Text("Physical cards") },
            )
        }
    }
}

@Composable
private fun ManualReadingCard(
    uiState: ReadingCreateUiState,
    onManualCardChange: (String, String) -> Unit,
    onManualOrientationChange: (String, ReadingOrientation) -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Physical Card Entry",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Record cards you drew outside the app, one card per slot.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (uiState.slots.isEmpty()) {
                Text(
                    text = "This layout has no slots yet.",
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                uiState.slots.sortedBy { it.drawOrder }.forEach { slot ->
                    ManualSlotPicker(
                        slot = slot,
                        cards = uiState.cards,
                        selectedCardId = uiState.manualCardIds[slot.id].orEmpty(),
                        selectedOrientation = uiState.manualOrientations[slot.id] ?: ReadingOrientation.Upright,
                        reversalsEnabled = uiState.reversalsEnabled,
                        onCardChange = { onManualCardChange(slot.id, it) },
                        onOrientationChange = { onManualOrientationChange(slot.id, it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ManualSlotPicker(
    slot: LayoutSlotEntity,
    cards: List<CardEntity>,
    selectedCardId: String,
    selectedOrientation: ReadingOrientation,
    reversalsEnabled: Boolean,
    onCardChange: (String) -> Unit,
    onOrientationChange: (ReadingOrientation) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = slot.title,
            style = MaterialTheme.typography.titleMedium,
        )
        if (slot.description.isNotBlank()) {
            Text(
                text = slot.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        CardChoiceDropdown(
            cards = cards,
            selectedCardId = selectedCardId,
            onCardChange = onCardChange,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedOrientation == ReadingOrientation.Upright,
                onClick = { onOrientationChange(ReadingOrientation.Upright) },
                label = { Text("Upright") },
            )
            FilterChip(
                selected = selectedOrientation == ReadingOrientation.Reversed,
                onClick = { onOrientationChange(ReadingOrientation.Reversed) },
                enabled = reversalsEnabled && slot.reversedAllowed,
                label = { Text("Reversed") },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardChoiceDropdown(
    cards: List<CardEntity>,
    selectedCardId: String,
    onCardChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = cards.firstOrNull { it.id == selectedCardId }?.title.orEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            label = { Text("Card") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            cards.forEach { card ->
                DropdownMenuItem(
                    text = { Text(card.title) },
                    onClick = {
                        onCardChange(card.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckDropdown(
    uiState: ReadingCreateUiState,
    onDeckChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = uiState.decks.firstOrNull { it.id == uiState.selectedDeckId }?.name.orEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            label = { Text("Deck") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            uiState.decks.forEach { deck ->
                DropdownMenuItem(
                    text = { Text(deck.name) },
                    onClick = {
                        onDeckChange(deck.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LayoutDropdown(
    uiState: ReadingCreateUiState,
    onLayoutChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = uiState.layouts.firstOrNull { it.id == uiState.selectedLayoutId }?.name.orEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            label = { Text("Layout") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            uiState.layouts.forEach { layout ->
                DropdownMenuItem(
                    text = { Text("${layout.name} (${layout.slotCount})") },
                    onClick = {
                        onLayoutChange(layout.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun MessageCard(message: String) {
    Card {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.error,
        )
    }
}
