package org.arcanaforge.app.ui.screens.charts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.arcanaforge.app.data.ai.ReadingAiService
import org.arcanaforge.app.data.astrology.NatalChartAiChatRepository
import org.arcanaforge.app.data.astrology.NatalChartRecord
import org.arcanaforge.app.data.astrology.NatalChartRepository
import org.arcanaforge.app.domain.ai.AiChatMessage
import org.arcanaforge.app.domain.ai.AiChatRole

data class NatalChartDetailUiState(
    val isLoading: Boolean = true,
    val chart: NatalChartRecord? = null,
    val notesDraft: String = "",
    val aiMessages: List<AiChatMessage> = emptyList(),
    val aiQuestionDraft: String = "",
    val isAskingAi: Boolean = false,
    val errorMessage: String? = null,
)

class NatalChartDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val natalChartRepository: NatalChartRepository,
    private val natalChartAiChatRepository: NatalChartAiChatRepository,
    private val readingAiService: ReadingAiService,
) : ViewModel() {
    private val chartId: String = checkNotNull(savedStateHandle["chartId"])
    private val _uiState = MutableStateFlow(NatalChartDetailUiState())
    val uiState: StateFlow<NatalChartDetailUiState> = _uiState.asStateFlow()

    init {
        loadChart()
    }

    fun updateNotes(value: String) {
        _uiState.update { it.copy(notesDraft = value) }
    }

    fun saveNotes() {
        viewModelScope.launch {
            natalChartRepository.updateNotes(chartId, _uiState.value.notesDraft)
            loadChart()
        }
    }

    fun toggleFavorite() {
        val chart = _uiState.value.chart?.entity ?: return
        viewModelScope.launch {
            natalChartRepository.updateFavorite(chart.id, !chart.isFavorite)
            loadChart()
        }
    }

    fun deleteChart(onDeleted: () -> Unit) {
        viewModelScope.launch {
            natalChartRepository.delete(chartId)
            onDeleted()
        }
    }

    fun updateAiQuestionDraft(value: String) {
        _uiState.update { it.copy(aiQuestionDraft = value) }
    }

    fun askAiAboutChart() {
        val state = _uiState.value
        val chart = state.chart ?: return
        val question = state.aiQuestionDraft.trim()
        if (question.isBlank() || state.isAskingAi) {
            return
        }
        viewModelScope.launch {
            val userMessage = natalChartAiChatRepository.addMessage(
                chartId = chart.entity.id,
                role = AiChatRole.User,
                text = question,
            )
            _uiState.update {
                it.copy(
                    aiMessages = it.aiMessages + userMessage,
                    aiQuestionDraft = "",
                    isAskingAi = true,
                    errorMessage = null,
                )
            }
            runCatching {
                readingAiService.askAboutReading(
                    readingContext = buildChartAiContext(chart),
                    history = state.aiMessages,
                    question = question,
                )
            }.onSuccess { answer ->
                val assistantMessage = natalChartAiChatRepository.addMessage(
                    chartId = chart.entity.id,
                    role = AiChatRole.Assistant,
                    text = answer,
                )
                _uiState.update {
                    it.copy(
                        aiMessages = it.aiMessages + assistantMessage,
                        isAskingAi = false,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isAskingAi = false,
                        errorMessage = throwable.message ?: "AI could not answer this chart question.",
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun loadChart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val chart = natalChartRepository.getChart(chartId) ?: error("Natal chart not found.")
                val messages = natalChartAiChatRepository.getMessages(chartId)
                _uiState.value = NatalChartDetailUiState(
                    isLoading = false,
                    chart = chart,
                    notesDraft = chart.entity.notes,
                    aiMessages = messages,
                )
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Could not load natal chart.",
                    )
                }
            }
        }
    }
}

fun buildChartAiContext(chart: NatalChartRecord): String = buildString {
    appendLine("Natal chart: ${chart.entity.label}")
    appendLine("Subject: ${chart.entity.subjectName}")
    appendLine("Birth date: ${chart.entity.birthDate}")
    appendLine("Birth time: ${if (chart.entity.timeKnown) chart.entity.birthTime else "Unknown; noon was used for approximate placements"}")
    appendLine("Timezone: ${chart.entity.zoneId}")
    if (chart.entity.locationName.isNotBlank()) appendLine("Birth location: ${chart.entity.locationName}")
    if (chart.entity.latitude != null && chart.entity.longitude != null) {
        appendLine("Coordinates: ${chart.entity.latitude}, ${chart.entity.longitude}")
    }
    if (chart.entity.notes.isNotBlank()) {
        appendLine("User notes: ${chart.entity.notes}")
    }
    appendLine()
    appendLine("Placements:")
    chart.snapshot.placements.forEach { placement ->
        append("- ${placement.body.displayName}: ")
        append("${formatDegrees(placement.degreeInSign)} ${placement.sign.displayName}")
        placement.house?.let { append(", House $it") }
        if (placement.retrograde) append(", retrograde")
        appendLine()
    }
    appendLine()
    appendLine("Major aspects:")
    chart.snapshot.aspects.forEach { aspect ->
        appendLine("- ${aspect.first.displayName} ${aspect.type.displayName.lowercase()} ${aspect.second.displayName}, orb ${formatDegrees(aspect.orb)}")
    }
    appendLine()
    appendLine("Use this as symbolic reflection only. Do not make deterministic, medical, legal, or financial claims.")
}

fun formatDegrees(value: Double): String {
    val degrees = value.toInt()
    val minutes = ((value - degrees) * 60.0).toInt()
    return "%d°%02d'".format(degrees, minutes)
}
