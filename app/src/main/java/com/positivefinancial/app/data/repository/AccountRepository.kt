package com.positivefinancial.app.data.repository

import androidx.room.withTransaction
import com.positivefinancial.app.data.local.AppDatabase
import com.positivefinancial.app.data.local.dao.AccountDao
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.model.AccountType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val database: AppDatabase,
    private val accountDao: AccountDao
) {
    fun observeAccounts(): Flow<List<AccountEntity>> = accountDao.getAllActive()

    fun observeAccountsByType(type: AccountType): Flow<List<AccountEntity>> = accountDao.getByType(type)

    fun observeAccount(id: Long): Flow<AccountEntity?> = accountDao.getByIdFlow(id)

    fun observeTotalBalance(): Flow<Long> = accountDao.getTotalBalance()

    suspend fun getAccount(id: Long): AccountEntity? = accountDao.getById(id)

    suspend fun addAccount(
        name: String,
        type: AccountType,
        iconKey: String,
        colorHex: String,
        openingBalance: Long
    ): Long = accountDao.insert(
        AccountEntity(
            name = name,
            type = type,
            balance = openingBalance,
            iconKey = iconKey,
            colorHex = colorHex
        )
    )

    suspend fun updateAccount(account: AccountEntity) = accountDao.update(account)

    suspend fun deleteAccount(account: AccountEntity) = accountDao.delete(account)

    suspend fun seedDefaultAccounts(accounts: List<AccountEntity>) {
        database.withTransaction {
            accounts.forEach { accountDao.insert(it) }
        }
    }
}
