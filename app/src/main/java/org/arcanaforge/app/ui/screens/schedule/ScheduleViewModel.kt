package org.arcanaforge.app.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.arcanaforge.app.core.database.entity.DeckEntity
import org.arcanaforge.app.core.database.entity.LayoutEntity
import org.arcanaforge.app.core.database.entity.ScheduledReadingEntity
import org.arcanaforge.app.data.deck.DeckRepository
import org.arcanaforge.app.data.layout.LayoutRepository
import org.arcanaforge.app.data.schedule.ScheduleRepository
import org.arcanaforge.app.domain.schedule.ScheduleRuleCodec

data class ScheduleListItem(
    val schedule: ScheduledReadingEntity,
    val deckName: String,
    val layoutName: String,
    val hasMissingDependency: Boolean,
)

data class ScheduleFormState(
    val title: String = "",
    val selectedDeckId: String = "",
    val selectedLayoutId: String = "",
    val questionTemplate: String = "",
    val scheduleRule: String = ScheduleRuleCodec.Daily,
    val reminderTime: String = defaultReminderTime(),
)

data class ScheduleUiState(
    val schedules: List<ScheduleListItem> = emptyList(),
    val decks: List<DeckEntity> = emptyList(),
    val layouts: List<LayoutEntity> = emptyList(),
    val form: ScheduleFormState = ScheduleFormState(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
)

class ScheduleViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val deckRepository: DeckRepository,
    private val layoutRepository: LayoutRepository,
) : ViewModel() {
    private val formState = MutableStateFlow(ScheduleFormState())
    private val transientState = MutableStateFlow(ScheduleUiState())

    val uiState: StateFlow<ScheduleUiState> = combine(
        scheduleRepository.observeSchedules(),
        deckRepository.observeDecks(),
        layoutRepository.observeLayouts(),
        formState,
        transientState,
    ) { schedules, decks, layouts, form, transient ->
        val deckNames = decks.associate { it.id to it.name }
        val layoutNames = layouts.associate { it.id to it.name }
        transient.copy(
            schedules = schedules.map { schedule ->
                ScheduleListItem(
                    schedule = schedule,
                    deckName = deckNames[schedule.deckId] ?: "Missing deck",
                    layoutName = layoutNames[schedule.layoutId] ?: "Missing layout",
                    hasMissingDependency = deckNames[schedule.deckId] == null || layoutNames[schedule.layoutId] == null,
                )
            },
            decks = decks,
            layouts = layouts,
            form = form.copy(
                selectedDeckId = form.selectedDeckId.ifBlank { decks.firstOrNull()?.id.orEmpty() },
                selectedLayoutId = form.selectedLayoutId.ifBlank { layouts.firstOrNull()?.id.orEmpty() },
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScheduleUiState(),
    )

    fun updateTitle(value: String) {
        formState.update { it.copy(title = value) }
        clearMessages()
    }

    fun updateDeck(deckId: String) {
        formState.update { it.copy(selectedDeckId = deckId) }
        clearMessages()
    }

    fun updateLayout(layoutId: String) {
        formState.update { it.copy(selectedLayoutId = layoutId) }
        clearMessages()
    }

    fun updateQuestion(value: String) {
        formState.update { it.copy(questionTemplate = value) }
        clearMessages()
    }

    fun updateRule(rule: String) {
        formState.update { it.copy(scheduleRule = rule) }
        clearMessages()
    }

    fun updateReminderTime(value: String) {
        formState.update { it.copy(reminderTime = value) }
        clearMessages()
    }

    fun createSchedule() {
        val current = uiState.value
        val form = current.form
        if (form.selectedDeckId.isBlank() || form.selectedLayoutId.isBlank()) {
            transientState.update { it.copy(errorMessage = "Choose a deck and layout first.") }
            return
        }
        val reminderTime = runCatching { LocalTime.parse(form.reminderTime) }.getOrElse {
            transientState.update { it.copy(errorMessage = "Use 24-hour time, for example 09:30 or 18:45.") }
            return
        }

        viewModelScope.launch {
            transientState.update { it.copy(isSaving = true, errorMessage = null, statusMessage = null) }
            runCatching {
                val deck = deckRepository.getDeck(form.selectedDeckId)
                    ?: error("The selected deck no longer exists.")
                val layout = layoutRepository.getLayout(form.selectedLayoutId)
                    ?: error("The selected layout no longer exists.")
                scheduleRepository.createSchedule(
                    title = form.title.ifBlank { "${layout.name} - ${deck.name}" },
                    deckId = deck.id,
                    layoutId = layout.id,
                    questionTemplate = form.questionTemplate,
                    scheduleRule = form.scheduleRule,
                    reminderTime = reminderTime,
                )
            }.onSuccess {
                formState.update { it.copy(title = "", questionTemplate = "", reminderTime = defaultReminderTime()) }
                transientState.update {
                    it.copy(
                        isSaving = false,
                        statusMessage = "Reminder scheduled.",
                    )
                }
            }.onFailure { throwable ->
                transientState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "Could not schedule reminder.",
                    )
                }
            }
        }
    }

    fun setEnabled(item: ScheduleListItem, enabled: Boolean) {
        viewModelScope.launch {
            scheduleRepository.setEnabled(item.schedule, enabled)
        }
    }

    fun deleteSchedule(item: ScheduleListItem) {
        viewModelScope.launch {
            scheduleRepository.deleteSchedule(item.schedule)
            transientState.update { it.copy(statusMessage = "Reminder deleted.") }
        }
    }

    private fun clearMessages() {
        transientState.update { it.copy(errorMessage = null, statusMessage = null) }
    }
}

private fun defaultReminderTime(): String =
    LocalTime.now()
        .plusMinutes(5)
        .format(DateTimeFormatter.ofPattern("HH:mm"))
