package com.positivefinancial.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.positivefinancial.app.data.repository.CreditCardRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CreditCardReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var creditCardRepository: CreditCardRepository

    override fun onReceive(context: Context, intent: Intent) {
        val accountId = intent.getLongExtra(AlarmScheduler.EXTRA_ACCOUNT_ID, -1L)
        val cardName = intent.getStringExtra(AlarmScheduler.EXTRA_CARD_NAME) ?: return
        if (accountId < 0) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val details = creditCardRepository.getCardDetails(accountId)
                if (details != null && details.reminderEnabled) {
                    notificationHelper.showCardReminder(accountId, cardName)
                    alarmScheduler.scheduleCardReminder(
                        accountId,
                        cardName,
                        details.reminderDayOfMonth,
                        details.reminderHour,
                        details.reminderMinute
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
