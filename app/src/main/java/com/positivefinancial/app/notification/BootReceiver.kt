package com.positivefinancial.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.positivefinancial.app.data.local.dao.AccountDao
import com.positivefinancial.app.data.repository.CreditCardRepository
import com.positivefinancial.app.data.settings.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var creditCardRepository: CreditCardRepository
    @Inject lateinit var accountDao: AccountDao

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = settingsRepository.settings.first()
                if (settings.dailyReminderEnabled) {
                    alarmScheduler.scheduleDaily(settings.dailyReminderHour, settings.dailyReminderMinute)
                }

                val cards = creditCardRepository.observeAllCardDetails().first()
                cards.filter { it.reminderEnabled }.forEach { details ->
                    val account = accountDao.getById(details.accountId)
                    if (account != null) {
                        alarmScheduler.scheduleCardReminder(
                            details.accountId,
                            account.name,
                            details.reminderDayOfMonth,
                            details.reminderHour,
                            details.reminderMinute
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
