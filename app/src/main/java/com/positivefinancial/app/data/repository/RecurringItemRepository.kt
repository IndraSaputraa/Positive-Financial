package com.positivefinancial.app.data.repository

import com.positivefinancial.app.data.local.dao.RecurringItemDao
import com.positivefinancial.app.data.local.entity.RecurringItemEntity
import com.positivefinancial.app.data.model.RecurringFrequency
import com.positivefinancial.app.data.model.TransactionType
import com.positivefinancial.app.util.DateRanges
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringItemRepository @Inject constructor(
    private val recurringItemDao: RecurringItemDao
) {
    fun observeAll(): Flow<List<RecurringItemEntity>> = recurringItemDao.getAll()

    suspend fun getDue(cutoff: Long): List<RecurringItemEntity> = recurringItemDao.getDue(cutoff)

    suspend fun getById(id: Long): RecurringItemEntity? = recurringItemDao.getById(id)

    suspend fun addItem(
        name: String,
        type: TransactionType,
        amount: Long,
        accountId: Long,
        categoryId: Long,
        frequency: RecurringFrequency,
        dayOfMonth: Int,
        monthOfYear: Int,
        autoCreateTransaction: Boolean,
        reminderDaysBefore: Int
    ): Long {
        val nextDue = nextDueDateFor(frequency, dayOfMonth, monthOfYear, LocalDate.now())
        return recurringItemDao.insert(
            RecurringItemEntity(
                name = name,
                type = type,
                amount = amount,
                accountId = accountId,
                categoryId = categoryId,
                frequency = frequency,
                dayOfMonth = dayOfMonth,
                monthOfYear = monthOfYear,
                autoCreateTransaction = autoCreateTransaction,
                reminderDaysBefore = reminderDaysBefore,
                nextDueDate = DateRanges.toEpochMillis(nextDue)
            )
        )
    }

    suspend fun updateItem(
        item: RecurringItemEntity,
        name: String,
        amount: Long,
        accountId: Long,
        categoryId: Long,
        frequency: RecurringFrequency,
        dayOfMonth: Int,
        monthOfYear: Int,
        autoCreateTransaction: Boolean,
        reminderDaysBefore: Int,
        isActive: Boolean
    ) {
        val nextDue = nextDueDateFor(frequency, dayOfMonth, monthOfYear, LocalDate.now())
        recurringItemDao.update(
            item.copy(
                name = name,
                amount = amount,
                accountId = accountId,
                categoryId = categoryId,
                frequency = frequency,
                dayOfMonth = dayOfMonth,
                monthOfYear = monthOfYear,
                autoCreateTransaction = autoCreateTransaction,
                reminderDaysBefore = reminderDaysBefore,
                isActive = isActive,
                nextDueDate = DateRanges.toEpochMillis(nextDue)
            )
        )
    }

    suspend fun deleteItem(item: RecurringItemEntity) = recurringItemDao.delete(item)

    /** Advances [item] to its next occurrence after today's processing. */
    suspend fun advancePastDueDate(item: RecurringItemEntity, processedDate: Long) {
        val reference = DateRanges.toLocalDate(item.nextDueDate)
        val next = when (item.frequency) {
            RecurringFrequency.MONTHLY -> DateRanges.nextMonthlyDateAfter(item.dayOfMonth, reference)
            RecurringFrequency.YEARLY -> DateRanges.nextYearlyDateAfter(item.monthOfYear, item.dayOfMonth, reference)
        }
        recurringItemDao.update(
            item.copy(nextDueDate = DateRanges.toEpochMillis(next), lastProcessedDate = processedDate)
        )
    }

    private fun nextDueDateFor(
        frequency: RecurringFrequency,
        dayOfMonth: Int,
        monthOfYear: Int,
        reference: LocalDate
    ): LocalDate = when (frequency) {
        RecurringFrequency.MONTHLY -> DateRanges.nextMonthlyDateOnOrAfter(dayOfMonth, reference)
        RecurringFrequency.YEARLY -> DateRanges.nextYearlyDateOnOrAfter(monthOfYear, dayOfMonth, reference)
    }
}
