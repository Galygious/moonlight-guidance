package org.arcanaforge.app.ui.screens.charts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.arcanaforge.app.data.astrology.NatalChartRecord
import org.arcanaforge.app.ui.navigation.AppViewModelFactory

@Composable
fun NatalChartLibraryScreen(
    viewModelFactory: AppViewModelFactory,
    onOpenChart: (String) -> Unit,
) {
    val viewModel: NatalChartLibraryViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearError()
    }

    LaunchedEffect(uiState.createdChartId) {
        val chartId = uiState.createdChartId ?: return@LaunchedEffect
        viewModel.createdChartOpened()
        onOpenChart(chartId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                CreateNatalChartCard(
                    uiState = uiState,
                    viewModel = viewModel,
                )
            }
            if (uiState.charts.isEmpty()) {
                item {
                    Text(
                        text = "No natal charts yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(uiState.charts, key = { it.entity.id }) { chart ->
                    NatalChartRow(
                        chart = chart,
                        onClick = { onOpenChart(chart.entity.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateNatalChartCard(
    uiState: NatalChartLibraryUiState,
    viewModel: NatalChartLibraryViewModel,
) {
    Card(shape = MaterialTheme.shapes.medium) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = Icons.Outlined.AddCircle, contentDescription = null)
                Text(text = "Create Natal Chart", style = MaterialTheme.typography.titleLarge)
            }
            OutlinedTextField(
                value = uiState.subjectName,
                onValueChange = viewModel::updateSubjectName,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Name") },
            )
            OutlinedTextField(
                value = uiState.label,
                onValueChange = viewModel::updateLabel,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Chart title") },
                placeholder = { Text("Optional") },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.birthDate,
                    onValueChange = viewModel::updateBirthDate,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                )
                OutlinedTextField(
                    value = uiState.birthTime,
                    onValueChange = viewModel::updateBirthTime,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = uiState.timeKnown,
                    label = { Text("Time") },
                    placeholder = { Text("HH:MM") },
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = uiState.timeKnown,
                    onCheckedChange = viewModel::updateTimeKnown,
                )
                Text("Birth time known")
            }
            OutlinedTextField(
                value = uiState.zoneId,
                onValueChange = viewModel::updateZoneId,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Timezone") },
                placeholder = { Text("America/Chicago") },
            )
            OutlinedTextField(
                value = uiState.locationName,
                onValueChange = viewModel::updateLocationName,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Birth location") },
                placeholder = { Text("Optional label") },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.latitude,
                    onValueChange = viewModel::updateLatitude,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Latitude") },
                )
                OutlinedTextField(
                    value = uiState.longitude,
                    onValueChange = viewModel::updateLongitude,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Longitude") },
                )
            }
            if (uiState.isCreating) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Button(
                onClick = viewModel::createChart,
                enabled = !uiState.isCreating,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(if (uiState.isCreating) "Creating..." else "Create Chart")
            }
        }
    }
}

@Composable
private fun NatalChartRow(
    chart: NatalChartRecord,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (chart.entity.isFavorite) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = chart.entity.label, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${chart.entity.subjectName} - ${chart.entity.birthDate}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = chart.snapshot.placements.take(3).joinToString("  ") {
                        "${it.body.displayName}: ${it.sign.displayName}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

