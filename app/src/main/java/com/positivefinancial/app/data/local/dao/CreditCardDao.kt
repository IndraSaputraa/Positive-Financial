package com.positivefinancial.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.positivefinancial.app.data.local.entity.CreditCardDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {

    @Query("SELECT * FROM credit_card_details")
    fun getAll(): Flow<List<CreditCardDetailsEntity>>

    @Query("SELECT * FROM credit_card_details WHERE accountId = :accountId")
    fun getByAccountIdFlow(accountId: Long): Flow<CreditCardDetailsEntity?>

    @Query("SELECT * FROM credit_card_details WHERE accountId = :accountId")
    suspend fun getByAccountId(accountId: Long): CreditCardDetailsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(details: CreditCardDetailsEntity)

    @Update
    suspend fun update(details: CreditCardDetailsEntity)

    @Delete
    suspend fun delete(details: CreditCardDetailsEntity)
}
