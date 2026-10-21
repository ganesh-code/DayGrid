package com.daygrid.app.ui.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daygrid.app.domain.model.DayRecord
import com.daygrid.app.domain.model.DayStatus
import com.daygrid.app.domain.repository.DayRecordRepository
import com.daygrid.app.domain.usecase.RecordDayUseCase
import com.daygrid.app.widget.WidgetRefreshManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class CheckInUiEvent {
    object SaveSuccess : CheckInUiEvent()
    data class Error(val message: String) : CheckInUiEvent()
}

data class CheckInUiState(
    val date: LocalDate = LocalDate.now(),
    val selectedStatus: DayStatus? = null,
    val note: String = "",
    val showReflection: Boolean = false,
    val isSaving: Boolean = false,
    val existingRecord: DayRecord? = null,
    val confirmationMessage: String? = null
)

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val recordDayUseCase: RecordDayUseCase,
    private val repository: DayRecordRepository,
    private val widgetRefreshManager: WidgetRefreshManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CheckInUiEvent>()
    val events: SharedFlow<CheckInUiEvent> = _events.asSharedFlow()

    fun loadDate(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
        viewModelScope.launch {
            val existing = repository.getRecordForDate(date)
            _uiState.update {
                it.copy(
                    existingRecord = existing,
                    selectedStatus = existing?.status,
                    note = existing?.note ?: ""
                )
            }
        }
    }

    fun selectStatus(status: DayStatus) {
        _uiState.update { it.copy(selectedStatus = status) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun showReflectionStep() {
        _uiState.update { it.copy(showReflection = true) }
    }

    fun save(skipReflection: Boolean = false) {
        val state = _uiState.value
        val status = state.selectedStatus ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val note = if (skipReflection) null else state.note.trim().takeIf { it.isNotEmpty() }

            recordDayUseCase(
                date = state.date,
                status = status,
                note = note,
                allowFuture = false
            ).fold(
                onSuccess = {
                    val message = confirmationMessage(status)
                    _uiState.update { it.copy(isSaving = false, confirmationMessage = message) }
                    widgetRefreshManager.refreshAll()
                    _events.emit(CheckInUiEvent.SaveSuccess)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(CheckInUiEvent.Error(e.message ?: "Failed to save"))
                }
            )
        }
    }

    private fun confirmationMessage(status: DayStatus): String = when (status) {
        DayStatus.PRODUCTIVE -> "Progress recorded. Keep going at your pace."
        DayStatus.NEUTRAL -> "Rest is part of a sustainable life."
        DayStatus.UNPRODUCTIVE -> "One day doesn't define your year."
    }
}
