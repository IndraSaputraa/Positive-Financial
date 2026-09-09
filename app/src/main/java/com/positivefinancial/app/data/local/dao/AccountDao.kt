package com.positivefinancial.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.model.AccountType
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE isArchived = 0 ORDER BY sortOrder ASC, createdAt ASC")
    fun getAllActive(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE type = :type AND isArchived = 0 ORDER BY sortOrder ASC, createdAt ASC")
    fun getByType(type: AccountType): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY sortOrder ASC, createdAt ASC")
    fun getAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("UPDATE accounts SET balance = balance + :delta WHERE id = :id")
    suspend fun adjustBalance(id: Long, delta: Long)

    @Query("SELECT COALESCE(SUM(balance), 0) FROM accounts WHERE isArchived = 0")
    fun getTotalBalance(): Flow<Long>
}
