package org.arcanaforge.app.ui.screens.schedule

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.arcanaforge.app.core.database.entity.DeckEntity
import org.arcanaforge.app.core.database.entity.LayoutEntity
import org.arcanaforge.app.core.notifications.ScheduleNotificationHelper
import org.arcanaforge.app.domain.schedule.ScheduleRuleCodec
import org.arcanaforge.app.ui.components.ConfirmDeleteDialog
import org.arcanaforge.app.ui.components.EmptyState

@Composable
fun ScheduleScreen(
    viewModelFactory: ViewModelProvider.Factory,
    modifier: Modifier = Modifier,
) {
    val viewModel: ScheduleViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {},
    )

    ScheduleContent(
        uiState = uiState,
        onTitleChange = viewModel::updateTitle,
        onDeckChange = viewModel::updateDeck,
        onLayoutChange = viewModel::updateLayout,
        onQuestionChange = viewModel::updateQuestion,
        onRuleChange = viewModel::updateRule,
        onReminderTimeChange = viewModel::updateReminderTime,
        onCreate = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !ScheduleNotificationHelper.canPostNotifications(context)
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            viewModel.createSchedule()
        },
        onEnabledChange = viewModel::setEnabled,
        onDelete = viewModel::deleteSchedule,
        modifier = modifier,
    )
}

@Composable
private fun ScheduleContent(
    uiState: ScheduleUiState,
    onTitleChange: (String) -> Unit,
    onDeckChange: (String) -> Unit,
    onLayoutChange: (String) -> Unit,
    onQuestionChange: (String) -> Unit,
    onRuleChange: (String) -> Unit,
    onReminderTimeChange: (String) -> Unit,
    onCreate: () -> Unit,
    onEnabledChange: (ScheduleListItem, Boolean) -> Unit,
    onDelete: (ScheduleListItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<ScheduleListItem?>(null) }

    pendingDelete?.let { item ->
        ConfirmDeleteDialog(
            title = "Delete reminder",
            body = "This deletes the scheduled reminder from this device.",
            confirmLabel = "Delete Reminder",
            onConfirm = {
                onDelete(item)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Scheduled Readings",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Reminders open a prepared reading flow. They do not auto-draw cards.",
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
            item { MessageCard(message = message, isError = true) }
        }

        uiState.statusMessage?.let { message ->
            item { MessageCard(message = message, isError = false) }
        }

        item {
            ScheduleCreateCard(
                uiState = uiState,
                onTitleChange = onTitleChange,
                onDeckChange = onDeckChange,
                onLayoutChange = onLayoutChange,
                onQuestionChange = onQuestionChange,
                onRuleChange = onRuleChange,
                onReminderTimeChange = onReminderTimeChange,
                onCreate = onCreate,
            )
        }

        item {
            Text(
                text = "Reminders",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        if (uiState.schedules.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.CalendarMonth,
                    title = "No reminders scheduled",
                    body = "Create a daily, weekly, or monthly reading reminder.",
                )
            }
        } else {
            items(uiState.schedules, key = { it.schedule.id }) { item ->
                ScheduleItemCard(
                    item = item,
                    onEnabledChange = { enabled -> onEnabledChange(item, enabled) },
                    onDelete = { pendingDelete = item },
                )
            }
        }
    }
}

@Composable
private fun ScheduleCreateCard(
    uiState: ScheduleUiState,
    onTitleChange: (String) -> Unit,
    onDeckChange: (String) -> Unit,
    onLayoutChange: (String) -> Unit,
    onQuestionChange: (String) -> Unit,
    onRuleChange: (String) -> Unit,
    onReminderTimeChange: (String) -> Unit,
    onCreate: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Create Reminder",
                style = MaterialTheme.typography.titleLarge,
            )
            OutlinedTextField(
                value = uiState.form.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                placeholder = { Text("Morning check-in") },
            )
            DeckDropdown(
                decks = uiState.decks,
                selectedDeckId = uiState.form.selectedDeckId,
                onDeckChange = onDeckChange,
            )
            LayoutDropdown(
                layouts = uiState.layouts,
                selectedLayoutId = uiState.form.selectedLayoutId,
                onLayoutChange = onLayoutChange,
            )
            OutlinedTextField(
                value = uiState.form.questionTemplate,
                onValueChange = onQuestionChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                label = { Text("Question template") },
                placeholder = { Text("What should I reflect on today?") },
            )
            ScheduleRuleSelector(
                selectedRule = uiState.form.scheduleRule,
                onRuleChange = onRuleChange,
            )
            OutlinedTextField(
                value = uiState.form.reminderTime,
                onValueChange = onReminderTimeChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Reminder time") },
                supportingText = { Text("24-hour time, for example 09:30 or 18:45") },
                singleLine = true,
            )
            Button(
                onClick = onCreate,
                enabled = !uiState.isSaving && uiState.decks.isNotEmpty() && uiState.layouts.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.isSaving) "Scheduling..." else "Create Reminder")
            }
        }
    }
}

@Composable
private fun ScheduleRuleSelector(
    selectedRule: String,
    onRuleChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Frequency",
            style = MaterialTheme.typography.titleMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                ScheduleRuleCodec.Daily,
                ScheduleRuleCodec.Weekly,
                ScheduleRuleCodec.Monthly,
            ).forEach { rule ->
                FilterChip(
                    selected = selectedRule == rule,
                    onClick = { onRuleChange(rule) },
                    label = { Text(ScheduleRuleCodec.label(rule)) },
                )
            }
        }
    }
}

@Composable
private fun ScheduleItemCard(
    item: ScheduleListItem,
    onEnabledChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.schedule.title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${item.deckName} - ${item.layoutName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (item.hasMissingDependency) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Text(
                    text = "${ScheduleRuleCodec.label(item.schedule.scheduleRule)} at ${item.schedule.reminderTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.schedule.questionTemplate.isNotBlank()) {
                    Text(
                        text = item.schedule.questionTemplate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (item.hasMissingDependency) {
                    Text(
                        text = "Selected deck or layout was deleted.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Switch(
                checked = item.schedule.enabled,
                onCheckedChange = onEnabledChange,
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete reminder",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckDropdown(
    decks: List<DeckEntity>,
    selectedDeckId: String,
    onDeckChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = decks.firstOrNull { it.id == selectedDeckId }?.name.orEmpty()

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
            decks.forEach { deck ->
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
    layouts: List<LayoutEntity>,
    selectedLayoutId: String,
    onLayoutChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = layouts.firstOrNull { it.id == selectedLayoutId }?.name.orEmpty()

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
            layouts.forEach { layout ->
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
