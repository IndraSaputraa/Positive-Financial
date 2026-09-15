package com.positivefinancial.app.notification

import com.positivefinancial.app.data.repository.RecurringItemRepository
import com.positivefinancial.app.data.repository.TransactionRepository
import com.positivefinancial.app.util.DateRanges
import com.positivefinancial.app.util.Formatters
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs once a day (see [AlarmScheduler] + RecurringProcessorReceiver, plus once
 * at app launch) to post due auto-create transactions and fire bill reminders.
 * Kept independent of Android's alarm/receiver plumbing so it's easy to trigger
 * from multiple places without duplicating the actual logic.
 */
@Singleton
class RecurringItemProcessor @Inject constructor(
    private val recurringItemRepository: RecurringItemRepository,
    private val transactionRepository: TransactionRepository,
    private val notificationHelper: NotificationHelper
) {
    suspend fun processDueItems() {
        val today = DateRanges.now()
        val items = recurringItemRepository.observeAll().first().filter { it.isActive }

        items.forEach { item ->
            if (item.reminderSentAt == null) {
                val reminderTriggerMillis = DateRanges.toEpochMillis(
                    DateRanges.toLocalDate(item.nextDueDate).minusDays(item.reminderDaysBefore.toLong())
                )
                if (today in reminderTriggerMillis until item.nextDueDate) {
                    notificationHelper.showBillReminder(item.id, item.name, Formatters.currency(item.amount))
                    recurringItemRepository.markReminderSent(item, today)
                }
            }

            if (today >= item.nextDueDate) {
                if (item.autoCreateTransaction) {
                    transactionRepository.addIncomeOrExpense(
                        type = item.type,
                        amount = item.amount,
                        accountId = item.accountId,
                        categoryId = item.categoryId,
                        note = "Recurring: ${item.name}",
                        date = item.nextDueDate
                    )
                    notificationHelper.showRecurringCreated(item.id, item.name, Formatters.currency(item.amount))
                } else {
                    notificationHelper.showBillReminder(item.id, item.name, Formatters.currency(item.amount))
                }
                recurringItemRepository.advancePastDueDate(item, today)
            }
        }
    }
}
