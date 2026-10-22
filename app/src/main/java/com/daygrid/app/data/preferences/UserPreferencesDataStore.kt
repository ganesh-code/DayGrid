package com.daygrid.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "daygrid_prefs")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fastPrefs = context.getSharedPreferences("daygrid_fast_prefs", Context.MODE_PRIVATE)

    companion object {
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val KEY_REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val KEY_REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val KEY_ACCENT_COLOR = stringPreferencesKey("accent_color")
        val KEY_SELECTED_YEAR = intPreferencesKey("selected_year")
        val KEY_NOTIFICATION_PERMISSION_ASKED = booleanPreferencesKey("notification_permission_asked")
    }

    fun isOnboardingDoneSync(): Boolean = fastPrefs.getBoolean("onboarding_done", false)

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val done = prefs[KEY_ONBOARDING_DONE] ?: fastPrefs.getBoolean("onboarding_done", false)
        UserPreferences(
            onboardingDone = done,
            reminderEnabled = prefs[KEY_REMINDER_ENABLED] ?: true,
            reminderHour = prefs[KEY_REMINDER_HOUR] ?: 20, // 8 PM default
            reminderMinute = prefs[KEY_REMINDER_MINUTE] ?: 30,
            accentColor = prefs[KEY_ACCENT_COLOR] ?: "#FF8A00",
            selectedYear = prefs[KEY_SELECTED_YEAR] ?: java.time.LocalDate.now().year,
            notificationPermissionAsked = prefs[KEY_NOTIFICATION_PERMISSION_ASKED] ?: false
        )
    }

    suspend fun setOnboardingDone(done: Boolean) {
        fastPrefs.edit().putBoolean("onboarding_done", done).commit()
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = done }
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_REMINDER_ENABLED] = enabled }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[KEY_REMINDER_HOUR] = hour
            it[KEY_REMINDER_MINUTE] = minute
        }
    }

    suspend fun setAccentColor(color: String) {
        context.dataStore.edit { it[KEY_ACCENT_COLOR] = color }
    }

    suspend fun setSelectedYear(year: Int) {
        context.dataStore.edit { it[KEY_SELECTED_YEAR] = year }
    }

    suspend fun setNotificationPermissionAsked(asked: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATION_PERMISSION_ASKED] = asked }
    }
}

data class UserPreferences(
    val onboardingDone: Boolean,
    val reminderEnabled: Boolean,
    val reminderHour: Int,
    val reminderMinute: Int,
    val accentColor: String,
    val selectedYear: Int,
    val notificationPermissionAsked: Boolean
)
