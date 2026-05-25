package org.arcanaforge.app.ui.screens.layoutlibrary

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
import androidx.compose.material.icons.outlined.Interests
import androidx.compose.material3.AssistChip
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
fun LayoutLibraryScreen(
    viewModelFactory: ViewModelProvider.Factory,
    onOpenLayout: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: LayoutLibraryViewModel = viewModel(factory = viewModelFactory)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Layout Library",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "Built-in spreads and custom reading layouts.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Button(
                    onClick = { viewModel.createLayout(onOpenLayout) },
                    enabled = !uiState.isCreating,
                ) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                    Text(
                        text = if (uiState.isCreating) "Creating" else "New",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }

        uiState.errorMessage?.let { message ->
            item { MessageCard(message = message, isError = true) }
        }

        uiState.statusMessage?.let { message ->
            item { MessageCard(message = message, isError = false) }
        }

        if (uiState.layouts.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.Interests,
                    title = "No layouts yet",
                    body = "Create a custom spread or wait for seed data to finish loading.",
                )
            }
        } else {
            items(uiState.layouts, key = { it.id }) { layout ->
                LayoutRow(
                    layout = layout,
                    onClick = { onOpenLayout(layout.id) },
                )
            }
        }
    }
}

@Composable
private fun LayoutRow(
    layout: LayoutLibraryItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = layout.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                AssistChip(
                    onClick = onClick,
                    label = { Text("${layout.slotCount} slots") },
                )
            }
            if (layout.description.isNotBlank()) {
                Text(
                    text = layout.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AssistChip(
                onClick = onClick,
                label = { Text(if (layout.isBuiltIn) "Built-in" else "Custom") },
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
