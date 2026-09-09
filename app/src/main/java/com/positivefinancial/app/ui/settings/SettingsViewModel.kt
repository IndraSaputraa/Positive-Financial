package com.positivefinancial.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.settings.AppSettings
import com.positivefinancial.app.data.settings.SettingsRepository
import com.positivefinancial.app.notification.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setBiometricLockEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setBiometricLockEnabled(enabled) }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = settingsRepository.settings.first()
            settingsRepository.setDailyReminder(enabled, current.dailyReminderHour, current.dailyReminderMinute)
            if (enabled) {
                alarmScheduler.scheduleDaily(current.dailyReminderHour, current.dailyReminderMinute)
            } else {
                alarmScheduler.cancelDaily()
            }
        }
    }

    fun setDailyReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyReminder(enabled = true, hour = hour, minute = minute)
            alarmScheduler.scheduleDaily(hour, minute)
        }
    }
}
