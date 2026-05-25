package org.arcanaforge.app.ui.screens.layouteditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.arcanaforge.app.core.database.entity.LayoutEntity
import org.arcanaforge.app.core.database.entity.LayoutSlotEntity
import org.arcanaforge.app.data.layout.LayoutRepository

data class LayoutSlotDraft(
    val id: String,
    val title: String,
    val description: String,
    val x: String,
    val y: String,
    val width: String,
    val height: String,
    val rotation: String,
    val drawOrder: String,
    val reversedAllowed: Boolean,
)

data class LayoutEditorUiState(
    val isLoading: Boolean = true,
    val layout: LayoutEntity? = null,
    val name: String = "",
    val description: String = "",
    val tagsText: String = "",
    val canvasWidth: String = "720",
    val canvasHeight: String = "520",
    val slots: List<LayoutSlotDraft> = emptyList(),
    val statusMessage: String? = null,
    val errorMessage: String? = null,
) {
    val canEdit: Boolean
        get() = layout?.isBuiltIn == false
}

class LayoutEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val layoutRepository: LayoutRepository,
) : ViewModel() {
    private val layoutId: String = checkNotNull(savedStateHandle["layoutId"])
    private val _uiState = MutableStateFlow(LayoutEditorUiState())
    val uiState: StateFlow<LayoutEditorUiState> = _uiState

    init {
        loadLayout()
    }

    fun updateName(value: String) {
        _uiState.update { it.copy(name = value, statusMessage = null) }
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value, statusMessage = null) }
    }

    fun updateTags(value: String) {
        _uiState.update { it.copy(tagsText = value, statusMessage = null) }
    }

    fun updateCanvasWidth(value: String) {
        _uiState.update { it.copy(canvasWidth = value.filterNumeric(), statusMessage = null) }
    }

    fun updateCanvasHeight(value: String) {
        _uiState.update { it.copy(canvasHeight = value.filterNumeric(), statusMessage = null) }
    }

    fun updateSlotTitle(slotId: String, value: String) = updateSlotDraft(slotId) { it.copy(title = value) }
    fun updateSlotDescription(slotId: String, value: String) = updateSlotDraft(slotId) { it.copy(description = value) }
    fun updateSlotX(slotId: String, value: String) = updateSlotDraft(slotId) { it.copy(x = value.filterNumeric()) }
    fun updateSlotY(slotId: String, value: String) = updateSlotDraft(slotId) { it.copy(y = value.filterNumeric()) }
    fun updateSlotWidth(slotId: String, value: String) = updateSlotDraft(slotId) { it.copy(width = value.filterNumeric()) }
    fun updateSlotHeight(slotId: String, value: String) = updateSlotDraft(slotId) { it.copy(height = value.filterNumeric()) }
    fun updateSlotRotation(slotId: String, value: String) = updateSlotDraft(slotId) { it.copy(rotation = value.filterSignedNumeric()) }
    fun updateSlotDrawOrder(slotId: String, value: String) = updateSlotDraft(slotId) { it.copy(drawOrder = value.filterDigits()) }
    fun updateSlotReversedAllowed(slotId: String, value: Boolean) =
        updateSlotDraft(slotId) { it.copy(reversedAllowed = value) }

    fun moveSlot(
        slotId: String,
        x: Float,
        y: Float,
    ) {
        updateSlotDraft(slotId) {
            it.copy(
                x = x.coerceAtLeast(0f).toInt().toString(),
                y = y.coerceAtLeast(0f).toInt().toString(),
            )
        }
    }

    fun saveLayout() {
        val state = _uiState.value
        val layout = state.layout ?: return
        if (!state.canEdit) return

        viewModelScope.launch {
            runCatching {
                val updated = layout.copy(
                    name = state.name.trim().ifBlank { "Custom Layout" },
                    description = state.description.trim(),
                    canvasWidth = state.canvasWidth.toFloatOrNull()?.coerceAtLeast(1f) ?: layout.canvasWidth,
                    canvasHeight = state.canvasHeight.toFloatOrNull()?.coerceAtLeast(1f) ?: layout.canvasHeight,
                    tags = state.tagsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                )
                layoutRepository.updateLayout(updated)
                updated
            }.onSuccess { updated ->
                _uiState.update {
                    it.copy(
                        layout = updated,
                        statusMessage = "Layout saved.",
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "Layout could not be saved.")
                }
            }
        }
    }

    fun addSlot() {
        if (!_uiState.value.canEdit) return
        viewModelScope.launch {
            runCatching {
                layoutRepository.addSlot(layoutId)
            }.onSuccess {
                loadLayout(statusMessage = "Slot added.")
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "Slot could not be added.")
                }
            }
        }
    }

    fun duplicateSlot(slotId: String) {
        if (!_uiState.value.canEdit) return
        viewModelScope.launch {
            runCatching {
                layoutRepository.duplicateSlot(slotId)
            }.onSuccess {
                loadLayout(statusMessage = "Slot duplicated.")
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "Slot could not be duplicated.")
                }
            }
        }
    }

    fun saveSlot(slotId: String) {
        val state = _uiState.value
        if (!state.canEdit) return
        val draft = state.slots.firstOrNull { it.id == slotId } ?: return

        viewModelScope.launch {
            runCatching {
                layoutRepository.updateSlot(draft.toEntity(layoutId))
            }.onSuccess {
                loadLayout(statusMessage = "Slot saved.")
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "Slot could not be saved.")
                }
            }
        }
    }

    fun deleteSlot(slotId: String) {
        if (!_uiState.value.canEdit) return
        viewModelScope.launch {
            runCatching {
                layoutRepository.deleteSlot(slotId)
            }.onSuccess {
                loadLayout(statusMessage = "Slot deleted.")
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "Slot could not be deleted.")
                }
            }
        }
    }

    fun deleteLayout(onDeleted: () -> Unit) {
        if (!_uiState.value.canEdit) return
        viewModelScope.launch {
            runCatching {
                layoutRepository.deleteLayout(layoutId)
            }.onSuccess {
                onDeleted()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "Layout could not be deleted.")
                }
            }
        }
    }

    private fun loadLayout(statusMessage: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val layout = layoutRepository.getLayout(layoutId) ?: error("Layout not found.")
                val slots = layoutRepository.getSlots(layoutId)
                layout to slots
            }.onSuccess { (layout, slots) ->
                _uiState.value = LayoutEditorUiState(
                    isLoading = false,
                    layout = layout,
                    name = layout.name,
                    description = layout.description,
                    tagsText = layout.tags.joinToString(", "),
                    canvasWidth = layout.canvasWidth.toInt().toString(),
                    canvasHeight = layout.canvasHeight.toInt().toString(),
                    slots = slots.map { it.toDraft() },
                    statusMessage = statusMessage,
                )
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Layout could not be loaded.",
                    )
                }
            }
        }
    }

    private fun updateSlotDraft(
        slotId: String,
        update: (LayoutSlotDraft) -> LayoutSlotDraft,
    ) {
        _uiState.update { state ->
            state.copy(
                slots = state.slots.map { draft ->
                    if (draft.id == slotId) update(draft) else draft
                },
                statusMessage = null,
            )
        }
    }
}

private fun LayoutSlotEntity.toDraft(): LayoutSlotDraft =
    LayoutSlotDraft(
        id = id,
        title = title,
        description = description,
        x = x.toInt().toString(),
        y = y.toInt().toString(),
        width = width.toInt().toString(),
        height = height.toInt().toString(),
        rotation = rotation.toInt().toString(),
        drawOrder = drawOrder.toString(),
        reversedAllowed = reversedAllowed,
    )

private fun LayoutSlotDraft.toEntity(layoutId: String): LayoutSlotEntity =
    LayoutSlotEntity(
        id = id,
        layoutId = layoutId,
        title = title.trim().ifBlank { "Slot" },
        description = description.trim(),
        x = x.toFloatOrNull() ?: 0f,
        y = y.toFloatOrNull() ?: 0f,
        width = width.toFloatOrNull()?.coerceAtLeast(1f) ?: 120f,
        height = height.toFloatOrNull()?.coerceAtLeast(1f) ?: 190f,
        rotation = rotation.toFloatOrNull() ?: 0f,
        drawOrder = drawOrder.toIntOrNull()?.coerceAtLeast(0) ?: 0,
        reversedAllowed = reversedAllowed,
    )

private fun String.filterNumeric(): String =
    filter { it.isDigit() || it == '.' }

private fun String.filterSignedNumeric(): String =
    filterIndexed { index, char -> char.isDigit() || char == '.' || (char == '-' && index == 0) }

private fun String.filterDigits(): String =
    filter { it.isDigit() }
