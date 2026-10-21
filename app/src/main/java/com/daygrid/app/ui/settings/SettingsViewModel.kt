package com.daygrid.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daygrid.app.data.preferences.UserPreferences
import com.daygrid.app.data.preferences.UserPreferencesDataStore
import com.daygrid.app.domain.repository.DayRecordRepository
import com.daygrid.app.domain.usecase.ExportDataUseCase
import com.daygrid.app.domain.usecase.ImportDataUseCase
import com.daygrid.app.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingsEvent {
    data class ExportReady(val json: String) : SettingsEvent()
    data class ImportSuccess(val count: Int) : SettingsEvent()
    data class Error(val message: String) : SettingsEvent()
    object DataDeleted : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsDataStore: UserPreferencesDataStore,
    private val notificationScheduler: NotificationScheduler,
    private val repository: DayRecordRepository,
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = prefsDataStore.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences(
            onboardingDone = false,
            reminderEnabled = true,
            reminderHour = 20,
            reminderMinute = 30,
            accentColor = "#FF8A00",
            selectedYear = java.time.LocalDate.now().year,
            notificationPermissionAsked = false
        ))

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsDataStore.setReminderEnabled(enabled)
            if (enabled) {
                val prefs = preferences.value
                notificationScheduler.scheduleReminder(prefs.reminderHour, prefs.reminderMinute)
            } else {
                notificationScheduler.cancelReminder()
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            prefsDataStore.setReminderTime(hour, minute)
            // Sync to SharedPreferences for BroadcastReceiver use
            // (BroadcastReceiver can't use DataStore's coroutine API)
            if (preferences.value.reminderEnabled) {
                notificationScheduler.scheduleReminder(hour, minute)
            }
        }
    }

    fun setAccentColor(color: String) {
        viewModelScope.launch {
            prefsDataStore.setAccentColor(color)
        }
    }

    fun exportData() {
        viewModelScope.launch {
            exportDataUseCase().fold(
                onSuccess = { json -> _events.emit(SettingsEvent.ExportReady(json)) },
                onFailure = { _events.emit(SettingsEvent.Error(it.message ?: "Export failed")) }
            )
        }
    }

    fun importData(json: String, replaceAll: Boolean) {
        viewModelScope.launch {
            importDataUseCase(json, replaceAll).fold(
                onSuccess = { count -> _events.emit(SettingsEvent.ImportSuccess(count)) },
                onFailure = { _events.emit(SettingsEvent.Error(it.message ?: "Import failed")) }
            )
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            runCatching { repository.deleteAllRecords() }.fold(
                onSuccess = { _events.emit(SettingsEvent.DataDeleted) },
                onFailure = { _events.emit(SettingsEvent.Error(it.message ?: "Delete failed")) }
            )
        }
    }
}
