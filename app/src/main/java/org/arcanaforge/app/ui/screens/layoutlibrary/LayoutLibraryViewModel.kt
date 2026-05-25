package org.arcanaforge.app.ui.screens.layoutlibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.arcanaforge.app.data.layout.LayoutRepository

data class LayoutLibraryItem(
    val id: String,
    val name: String,
    val description: String,
    val slotCount: Int,
    val isBuiltIn: Boolean,
)

data class LayoutLibraryUiState(
    val layouts: List<LayoutLibraryItem> = emptyList(),
    val isCreating: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
)

class LayoutLibraryViewModel(
    private val layoutRepository: LayoutRepository,
) : ViewModel() {
    private val transientState = MutableStateFlow(LayoutLibraryUiState())

    val uiState: StateFlow<LayoutLibraryUiState> = combine(
        layoutRepository.observeLayouts(),
        transientState,
    ) { layouts, transient ->
        transient.copy(
            layouts = layouts.map { layout ->
                LayoutLibraryItem(
                    id = layout.id,
                    name = layout.name,
                    description = layout.description,
                    slotCount = layout.slotCount,
                    isBuiltIn = layout.isBuiltIn,
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LayoutLibraryUiState(),
    )

    fun createLayout(onCreated: (String) -> Unit) {
        viewModelScope.launch {
            transientState.value = transientState.value.copy(
                isCreating = true,
                errorMessage = null,
                statusMessage = null,
            )
            runCatching {
                layoutRepository.createCustomLayout("Custom Layout")
            }.onSuccess { layout ->
                transientState.value = transientState.value.copy(isCreating = false)
                onCreated(layout.id)
            }.onFailure { throwable ->
                transientState.value = transientState.value.copy(
                    isCreating = false,
                    errorMessage = throwable.message ?: "Layout could not be created.",
                )
            }
        }
    }
}
