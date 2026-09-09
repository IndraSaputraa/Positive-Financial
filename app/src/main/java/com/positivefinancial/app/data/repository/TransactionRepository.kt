package com.positivefinancial.app.data.repository

import androidx.room.withTransaction
import com.positivefinancial.app.data.local.AppDatabase
import com.positivefinancial.app.data.local.dao.AccountDao
import com.positivefinancial.app.data.local.dao.CategorySpendingRow
import com.positivefinancial.app.data.local.dao.MonthlySummaryRow
import com.positivefinancial.app.data.local.dao.TransactionDao
import com.positivefinancial.app.data.local.dao.TransactionWithDetails
import com.positivefinancial.app.data.local.entity.TransactionEntity
import com.positivefinancial.app.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val database: AppDatabase,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao
) {
    fun observeRecent(limit: Int): Flow<List<TransactionWithDetails>> = transactionDao.getRecent(limit)

    suspend fun getById(id: Long): TransactionEntity? = transactionDao.getById(id)

    fun observeFiltered(
        type: TransactionType? = null,
        accountId: Long? = null,
        categoryId: Long? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        query: String? = null
    ): Flow<List<TransactionWithDetails>> =
        transactionDao.getFiltered(type, accountId, categoryId, startDate, endDate, query?.trim())

    fun observeCardPaymentHistory(cardAccountId: Long): Flow<List<TransactionWithDetails>> =
        transactionDao.getCardPaymentHistory(cardAccountId)

    fun observeTotalIncome(start: Long, end: Long): Flow<Long> = transactionDao.getTotalIncome(start, end)

    fun observeTotalExpense(start: Long, end: Long): Flow<Long> = transactionDao.getTotalExpense(start, end)

    fun observeExpenseBreakdown(start: Long, end: Long): Flow<List<CategorySpendingRow>> =
        transactionDao.getExpenseBreakdown(start, end)

    fun observeMonthlySummary(since: Long): Flow<List<MonthlySummaryRow>> =
        transactionDao.getMonthlySummary(since)

    suspend fun addIncomeOrExpense(
        type: TransactionType,
        amount: Long,
        accountId: Long,
        categoryId: Long,
        note: String,
        date: Long
    ) {
        require(type == TransactionType.INCOME || type == TransactionType.EXPENSE)
        database.withTransaction {
            val entity = TransactionEntity(
                type = type,
                amount = amount,
                accountId = accountId,
                categoryId = categoryId,
                note = note,
                date = date
            )
            transactionDao.insert(entity)
            applyEffect(entity, sign = 1)
        }
    }

    suspend fun addTransfer(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Long,
        note: String,
        date: Long
    ) {
        require(fromAccountId != toAccountId) { "Cannot transfer to the same account" }
        database.withTransaction {
            val entity = TransactionEntity(
                type = TransactionType.TRANSFER,
                amount = amount,
                fromAccountId = fromAccountId,
                toAccountId = toAccountId,
                note = note,
                date = date
            )
            transactionDao.insert(entity)
            applyEffect(entity, sign = 1)
        }
    }

    suspend fun updateTransaction(
        id: Long,
        amount: Long,
        accountId: Long?,
        categoryId: Long?,
        fromAccountId: Long?,
        toAccountId: Long?,
        note: String,
        date: Long
    ) {
        database.withTransaction {
            val existing = transactionDao.getById(id) ?: return@withTransaction
            applyEffect(existing, sign = -1)
            val updated = existing.copy(
                amount = amount,
                accountId = accountId,
                categoryId = categoryId,
                fromAccountId = fromAccountId,
                toAccountId = toAccountId,
                note = note,
                date = date
            )
            transactionDao.update(updated)
            applyEffect(updated, sign = 1)
        }
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        database.withTransaction {
            applyEffect(transaction, sign = -1)
            transactionDao.delete(transaction)
        }
    }

    private suspend fun applyEffect(transaction: TransactionEntity, sign: Int) {
        when (transaction.type) {
            TransactionType.INCOME -> accountDao.adjustBalance(transaction.accountId!!, sign * transaction.amount)
            TransactionType.EXPENSE -> accountDao.adjustBalance(transaction.accountId!!, -sign * transaction.amount)
            TransactionType.TRANSFER -> {
                accountDao.adjustBalance(transaction.fromAccountId!!, -sign * transaction.amount)
                accountDao.adjustBalance(transaction.toAccountId!!, sign * transaction.amount)
            }
        }
    }
}
