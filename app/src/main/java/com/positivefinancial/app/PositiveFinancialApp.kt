package com.positivefinancial.app

import android.app.Application
import com.positivefinancial.app.data.backup.BackupManager
import com.positivefinancial.app.data.local.DefaultData
import com.positivefinancial.app.data.repository.AccountRepository
import com.positivefinancial.app.data.repository.CategoryRepository
import com.positivefinancial.app.data.settings.SettingsRepository
import com.positivefinancial.app.notification.AlarmScheduler
import com.positivefinancial.app.notification.NotificationHelper
import com.positivefinancial.app.notification.RecurringItemProcessor
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PositiveFinancialApp : Application() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var accountRepository: AccountRepository
    @Inject lateinit var categoryRepository: CategoryRepository
    @Inject lateinit var recurringItemProcessor: RecurringItemProcessor
    @Inject lateinit var backupManager: BackupManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Must happen before anything below touches the database: this swaps the
        // raw .db file on disk in place, before Room's first query opens it.
        backupManager.applyPendingRestoreIfNeeded()

        notificationHelper.createChannels()

        applicationScope.launch {
            seedDefaultDataIfNeeded()

            val settings = settingsRepository.settings.first()
            if (settings.dailyReminderEnabled) {
                alarmScheduler.scheduleDaily(settings.dailyReminderHour, settings.dailyReminderMinute)
            }

            alarmScheduler.scheduleRecurringProcessor()
            recurringItemProcessor.processDueItems()
        }
    }

    private suspend fun seedDefaultDataIfNeeded() {
        val settings = settingsRepository.settings.first()
        if (settings.defaultDataSeeded) return

        if (categoryRepository.isEmpty()) {
            categoryRepository.seedDefaults(DefaultData.defaultExpenseCategories() + DefaultData.defaultIncomeCategories())
        }
        accountRepository.seedDefaultAccounts(DefaultData.defaultAccounts())
        settingsRepository.markDefaultDataSeeded()
    }
}
