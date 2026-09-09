package com.positivefinancial.app.data.repository

import androidx.room.withTransaction
import com.positivefinancial.app.data.local.AppDatabase
import com.positivefinancial.app.data.local.dao.AccountDao
import com.positivefinancial.app.data.local.dao.CreditCardDao
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.local.entity.CreditCardDetailsEntity
import com.positivefinancial.app.data.model.AccountType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

data class CreditCardInput(
    val name: String,
    val colorHex: String,
    val creditLimit: Long,
    val outstandingBalance: Long,
    val cardNetwork: String,
    val lastFourDigits: String,
    val billingCycleDay: Int,
    val paymentDueDay: Int,
    val reminderEnabled: Boolean,
    val reminderDayOfMonth: Int,
    val reminderHour: Int,
    val reminderMinute: Int
)

@Singleton
class CreditCardRepository @Inject constructor(
    private val database: AppDatabase,
    private val accountDao: AccountDao,
    private val creditCardDao: CreditCardDao
) {
    fun observeCardAccounts(): Flow<List<AccountEntity>> = accountDao.getByType(AccountType.CREDIT_CARD)

    fun observeCardDetails(accountId: Long): Flow<CreditCardDetailsEntity?> =
        creditCardDao.getByAccountIdFlow(accountId)

    fun observeAllCardDetails(): Flow<List<CreditCardDetailsEntity>> = creditCardDao.getAll()

    suspend fun getCardDetails(accountId: Long): CreditCardDetailsEntity? = creditCardDao.getByAccountId(accountId)

    suspend fun addCreditCard(input: CreditCardInput): Long {
        var accountId = 0L
        database.withTransaction {
            accountId = accountDao.insert(
                AccountEntity(
                    name = input.name,
                    type = AccountType.CREDIT_CARD,
                    balance = -input.outstandingBalance,
                    iconKey = "credit_card",
                    colorHex = input.colorHex
                )
            )
            creditCardDao.insert(
                CreditCardDetailsEntity(
                    accountId = accountId,
                    creditLimit = input.creditLimit,
                    cardNetwork = input.cardNetwork,
                    lastFourDigits = input.lastFourDigits,
                    billingCycleDay = input.billingCycleDay,
                    paymentDueDay = input.paymentDueDay,
                    reminderEnabled = input.reminderEnabled,
                    reminderDayOfMonth = input.reminderDayOfMonth,
                    reminderHour = input.reminderHour,
                    reminderMinute = input.reminderMinute
                )
            )
        }
        return accountId
    }

    suspend fun updateCreditCard(accountId: Long, input: CreditCardInput) {
        database.withTransaction {
            val account = accountDao.getById(accountId) ?: return@withTransaction
            accountDao.update(
                account.copy(name = input.name, colorHex = input.colorHex)
            )
            creditCardDao.update(
                CreditCardDetailsEntity(
                    accountId = accountId,
                    creditLimit = input.creditLimit,
                    cardNetwork = input.cardNetwork,
                    lastFourDigits = input.lastFourDigits,
                    billingCycleDay = input.billingCycleDay,
                    paymentDueDay = input.paymentDueDay,
                    reminderEnabled = input.reminderEnabled,
                    reminderDayOfMonth = input.reminderDayOfMonth,
                    reminderHour = input.reminderHour,
                    reminderMinute = input.reminderMinute
                )
            )
        }
    }

    suspend fun deleteCreditCard(account: AccountEntity) = accountDao.delete(account)
}
