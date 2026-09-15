package com.positivefinancial.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RecurringProcessorReceiver : BroadcastReceiver() {

    @Inject lateinit var recurringItemProcessor: RecurringItemProcessor
    @Inject lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                recurringItemProcessor.processDueItems()
            } finally {
                alarmScheduler.scheduleRecurringProcessor()
                pendingResult.finish()
            }
        }
    }
}
