package org.arcanaforge.app.ui.screens.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.arcanaforge.app.data.astrology.NatalChartRecord
import org.arcanaforge.app.data.astrology.NatalChartRepository
import org.arcanaforge.app.domain.astrology.NatalChartInput

data class NatalChartLibraryUiState(
    val charts: List<NatalChartRecord> = emptyList(),
    val label: String = "",
    val subjectName: String = "",
    val birthDate: String = LocalDate.now().toString(),
    val birthTime: String = LocalTime.now().withSecond(0).withNano(0).toString(),
    val timeKnown: Boolean = true,
    val zoneId: String = ZoneId.systemDefault().id,
    val locationName: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val isCreating: Boolean = false,
    val errorMessage: String? = null,
    val createdChartId: String? = null,
)

class NatalChartLibraryViewModel(
    private val natalChartRepository: NatalChartRepository,
) : ViewModel() {
    private val formState = MutableStateFlow(NatalChartLibraryUiState())

    val uiState: StateFlow<NatalChartLibraryUiState> = combine(
        natalChartRepository.observeCharts(),
        formState,
    ) { charts, form ->
        form.copy(charts = charts)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NatalChartLibraryUiState(),
    )

    fun updateLabel(value: String) = formState.update { it.copy(label = value) }
    fun updateSubjectName(value: String) = formState.update { it.copy(subjectName = value) }
    fun updateBirthDate(value: String) = formState.update { it.copy(birthDate = value) }
    fun updateBirthTime(value: String) = formState.update { it.copy(birthTime = value) }
    fun updateTimeKnown(value: Boolean) = formState.update { it.copy(timeKnown = value) }
    fun updateZoneId(value: String) = formState.update { it.copy(zoneId = value) }
    fun updateLocationName(value: String) = formState.update { it.copy(locationName = value) }
    fun updateLatitude(value: String) = formState.update { it.copy(latitude = value) }
    fun updateLongitude(value: String) = formState.update { it.copy(longitude = value) }

    fun createChart() {
        val current = formState.value
        viewModelScope.launch {
            formState.update { it.copy(isCreating = true, errorMessage = null, createdChartId = null) }
            runCatching {
                validateDateTime(current)
                natalChartRepository.createChart(
                    NatalChartInput(
                        label = current.label,
                        subjectName = current.subjectName,
                        birthDate = current.birthDate.trim(),
                        birthTime = current.birthTime.trim().ifBlank { "12:00" },
                        timeKnown = current.timeKnown,
                        zoneId = current.zoneId.trim().ifBlank { ZoneId.systemDefault().id },
                        locationName = current.locationName,
                        latitude = current.latitude.toNullableDouble("Latitude", -90.0, 90.0),
                        longitude = current.longitude.toNullableDouble("Longitude", -180.0, 180.0),
                    ),
                )
            }.onSuccess { chartId ->
                formState.update {
                    it.copy(
                        label = "",
                        subjectName = "",
                        locationName = "",
                        isCreating = false,
                        createdChartId = chartId,
                    )
                }
            }.onFailure { throwable ->
                formState.update {
                    it.copy(
                        isCreating = false,
                        errorMessage = throwable.message ?: "Could not create natal chart.",
                    )
                }
            }
        }
    }

    fun createdChartOpened() {
        formState.update { it.copy(createdChartId = null) }
    }

    fun clearError() {
        formState.update { it.copy(errorMessage = null) }
    }

    private fun validateDateTime(state: NatalChartLibraryUiState) {
        LocalDate.parse(state.birthDate.trim())
        if (state.timeKnown) {
            LocalTime.parse(state.birthTime.trim())
        }
        ZoneId.of(state.zoneId.trim().ifBlank { ZoneId.systemDefault().id })
        if (state.subjectName.isBlank()) {
            error("Enter a name or label for this chart.")
        }
    }

    private fun String.toNullableDouble(label: String, min: Double, max: Double): Double? {
        val trimmed = trim()
        if (trimmed.isBlank()) return null
        val value = trimmed.toDoubleOrNull() ?: error("$label must be a number.")
        if (value < min || value > max) {
            error("$label must be between $min and $max.")
        }
        return value
    }
}

