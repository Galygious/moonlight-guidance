package org.arcanaforge.app.ui.screens.layouteditor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.arcanaforge.app.ui.components.ConfirmDeleteDialog

@Composable
fun LayoutEditorScreen(
    viewModelFactory: ViewModelProvider.Factory,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: LayoutEditorViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Delete layout",
            body = "This deletes the custom layout and its slots from this device. Readings that used it may lose layout details.",
            confirmLabel = "Delete Layout",
            onConfirm = { viewModel.deleteLayout(onDeleted) },
            onDismiss = { showDeleteDialog = false },
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
            item { MessageCard(message = message, isError = true) }
        }

        uiState.statusMessage?.let { message ->
            item { MessageCard(message = message, isError = false) }
        }

        item {
            LayoutHeader(
                uiState = uiState,
                onNameChange = viewModel::updateName,
                onDescriptionChange = viewModel::updateDescription,
                onTagsChange = viewModel::updateTags,
                onCanvasWidthChange = viewModel::updateCanvasWidth,
                onCanvasHeightChange = viewModel::updateCanvasHeight,
                onSave = viewModel::saveLayout,
                onRequestDeleteLayout = { showDeleteDialog = true },
            )
        }

        item {
            LayoutPreview(
                uiState = uiState,
                onMoveSlot = viewModel::moveSlot,
                onSaveSlot = viewModel::saveSlot,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Slots",
                    style = MaterialTheme.typography.titleLarge,
                )
                Button(
                    onClick = viewModel::addSlot,
                    enabled = uiState.canEdit,
                ) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                    Text("Add Slot", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        items(uiState.slots, key = { it.id }) { slot ->
            SlotEditorCard(
                slot = slot,
                canEdit = uiState.canEdit,
                onTitleChange = { viewModel.updateSlotTitle(slot.id, it) },
                onDescriptionChange = { viewModel.updateSlotDescription(slot.id, it) },
                onXChange = { viewModel.updateSlotX(slot.id, it) },
                onYChange = { viewModel.updateSlotY(slot.id, it) },
                onWidthChange = { viewModel.updateSlotWidth(slot.id, it) },
                onHeightChange = { viewModel.updateSlotHeight(slot.id, it) },
                onRotationChange = { viewModel.updateSlotRotation(slot.id, it) },
                onDrawOrderChange = { viewModel.updateSlotDrawOrder(slot.id, it) },
                onReversedAllowedChange = { viewModel.updateSlotReversedAllowed(slot.id, it) },
                onSave = { viewModel.saveSlot(slot.id) },
                onDuplicate = { viewModel.duplicateSlot(slot.id) },
                onDelete = { viewModel.deleteSlot(slot.id) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LayoutHeader(
    uiState: LayoutEditorUiState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onCanvasWidthChange: (String) -> Unit,
    onCanvasHeightChange: (String) -> Unit,
    onSave: () -> Unit,
    onRequestDeleteLayout: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = uiState.name.ifBlank { "Layout" },
                style = MaterialTheme.typography.headlineMedium,
            )
            AssistChip(
                onClick = {},
                label = { Text(if (uiState.canEdit) "Custom" else "Built-in") },
            )
        }
        if (!uiState.canEdit) {
            Text(
                text = "Built-in layouts are read-only. Create a custom layout to edit slots.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            enabled = uiState.canEdit,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Name") },
        )
        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            enabled = uiState.canEdit,
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            label = { Text("Description") },
        )
        OutlinedTextField(
            value = uiState.tagsText,
            onValueChange = onTagsChange,
            enabled = uiState.canEdit,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Tags") },
            supportingText = { Text("Comma-separated") },
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = uiState.canvasWidth,
                onValueChange = onCanvasWidthChange,
                enabled = uiState.canEdit,
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Canvas width") },
            )
            OutlinedTextField(
                value = uiState.canvasHeight,
                onValueChange = onCanvasHeightChange,
                enabled = uiState.canEdit,
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Canvas height") },
            )
        }
        Button(
            onClick = onSave,
            enabled = uiState.canEdit,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save Layout")
        }
        Button(
            onClick = onRequestDeleteLayout,
            enabled = uiState.canEdit,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
            Text("Delete Layout", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun LayoutPreview(
    uiState: LayoutEditorUiState,
    onMoveSlot: (String, Float, Float) -> Unit,
    onSaveSlot: (String) -> Unit,
) {
    val canvasWidth = uiState.canvasWidth.toFloatOrNull()?.coerceAtLeast(1f) ?: 720f
    val canvasHeight = uiState.canvasHeight.toFloatOrNull()?.coerceAtLeast(1f) ?: 520f
    val aspectRatio = (canvasWidth / canvasHeight).coerceIn(0.45f, 2.4f)
    val density = LocalDensity.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Preview",
            style = MaterialTheme.typography.titleLarge,
        )
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
        ) {
            val previewWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
            val previewHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)
            uiState.slots.forEach { slot ->
                val x = ((slot.x.toFloatOrNull() ?: 0f) / canvasWidth).coerceIn(0f, 1f)
                val y = ((slot.y.toFloatOrNull() ?: 0f) / canvasHeight).coerceIn(0f, 1f)
                val width = ((slot.width.toFloatOrNull() ?: 120f) / canvasWidth).coerceIn(0.05f, 1f)
                val height = ((slot.height.toFloatOrNull() ?: 190f) / canvasHeight).coerceIn(0.05f, 1f)
                val rotation = slot.rotation.toFloatOrNull() ?: 0f
                Box(
                    modifier = Modifier
                        .offset(x = maxWidth * x, y = maxHeight * y)
                        .size(width = maxWidth * width, height = maxHeight * height)
                        .graphicsLayer { rotationZ = rotation }
                        .slotDragHandle(
                            enabled = uiState.canEdit,
                            slot = slot,
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight,
                            previewWidthPx = previewWidthPx,
                            previewHeightPx = previewHeightPx,
                            onMoveSlot = onMoveSlot,
                            onSaveSlot = onSaveSlot,
                        )
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = slot.drawOrder.ifBlank { "0" },
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SlotEditorCard(
    slot: LayoutSlotDraft,
    canEdit: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onXChange: (String) -> Unit,
    onYChange: (String) -> Unit,
    onWidthChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onRotationChange: (String) -> Unit,
    onDrawOrderChange: (String) -> Unit,
    onReversedAllowedChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = slot.title.ifBlank { "Slot" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Row {
                    IconButton(
                        onClick = onDuplicate,
                        enabled = canEdit,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Duplicate slot",
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        enabled = canEdit,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete slot",
                            tint = if (canEdit) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
            OutlinedTextField(
                value = slot.title,
                onValueChange = onTitleChange,
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Slot title") },
            )
            OutlinedTextField(
                value = slot.description,
                onValueChange = onDescriptionChange,
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                label = { Text("Slot description") },
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CompactField("X", slot.x, canEdit, onXChange)
                CompactField("Y", slot.y, canEdit, onYChange)
                CompactField("Width", slot.width, canEdit, onWidthChange)
                CompactField("Height", slot.height, canEdit, onHeightChange)
                CompactField("Rotation", slot.rotation, canEdit, onRotationChange)
                CompactField("Draw order", slot.drawOrder, canEdit, onDrawOrderChange)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Reversed allowed")
                Switch(
                    checked = slot.reversedAllowed,
                    onCheckedChange = onReversedAllowedChange,
                    enabled = canEdit,
                )
            }
            Button(
                onClick = onSave,
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save Slot")
            }
        }
    }
}

@Composable
private fun CompactField(
    label: String,
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = Modifier
            .widthIn(min = 120.dp)
            .height(72.dp),
        singleLine = true,
        label = { Text(label) },
    )
}

private fun Modifier.slotDragHandle(
    enabled: Boolean,
    slot: LayoutSlotDraft,
    canvasWidth: Float,
    canvasHeight: Float,
    previewWidthPx: Float,
    previewHeightPx: Float,
    onMoveSlot: (String, Float, Float) -> Unit,
    onSaveSlot: (String) -> Unit,
): Modifier {
    if (!enabled) return this
    return pointerInput(slot.id, canvasWidth, canvasHeight, previewWidthPx, previewHeightPx) {
        var currentX = slot.x.toFloatOrNull() ?: 0f
        var currentY = slot.y.toFloatOrNull() ?: 0f
        val slotWidth = slot.width.toFloatOrNull() ?: 120f
        val slotHeight = slot.height.toFloatOrNull() ?: 190f
        detectDragGestures(
            onDragEnd = { onSaveSlot(slot.id) },
            onDragCancel = { onSaveSlot(slot.id) },
        ) { change, dragAmount ->
            change.consume()
            val nextX = currentX + (dragAmount.x / previewWidthPx) * canvasWidth
            val nextY = currentY + (dragAmount.y / previewHeightPx) * canvasHeight
            currentX = nextX.coerceIn(0f, (canvasWidth - slotWidth).coerceAtLeast(0f))
            currentY = nextY.coerceIn(0f, (canvasHeight - slotHeight).coerceAtLeast(0f))
            onMoveSlot(
                slot.id,
                currentX,
                currentY,
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
