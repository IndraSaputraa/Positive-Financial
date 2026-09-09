package com.positivefinancial.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.positivefinancial.app.data.settings.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class DailyReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = settingsRepository.settings.first()
                if (settings.dailyReminderEnabled) {
                    notificationHelper.showDailyReminder()
                    alarmScheduler.scheduleDaily(settings.dailyReminderHour, settings.dailyReminderMinute)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
