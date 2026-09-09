package com.positivefinancial.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "positive_financial_settings")

data class AppSettings(
    val biometricLockEnabled: Boolean = false,
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderHour: Int = 21,
    val dailyReminderMinute: Int = 0,
    val defaultDataSeeded: Boolean = false
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_lock_enabled")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val DAILY_REMINDER_HOUR = intPreferencesKey("daily_reminder_hour")
        val DAILY_REMINDER_MINUTE = intPreferencesKey("daily_reminder_minute")
        val DEFAULT_DATA_SEEDED = booleanPreferencesKey("default_data_seeded")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            biometricLockEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false,
            dailyReminderEnabled = prefs[Keys.DAILY_REMINDER_ENABLED] ?: true,
            dailyReminderHour = prefs[Keys.DAILY_REMINDER_HOUR] ?: 21,
            dailyReminderMinute = prefs[Keys.DAILY_REMINDER_MINUTE] ?: 0,
            defaultDataSeeded = prefs[Keys.DEFAULT_DATA_SEEDED] ?: false
        )
    }

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setDailyReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.DAILY_REMINDER_ENABLED] = enabled
            it[Keys.DAILY_REMINDER_HOUR] = hour
            it[Keys.DAILY_REMINDER_MINUTE] = minute
        }
    }

    suspend fun markDefaultDataSeeded() {
        context.dataStore.edit { it[Keys.DEFAULT_DATA_SEEDED] = true }
    }
}
