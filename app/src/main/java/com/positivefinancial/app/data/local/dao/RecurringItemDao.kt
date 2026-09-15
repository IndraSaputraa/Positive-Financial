package com.positivefinancial.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.positivefinancial.app.data.local.entity.RecurringItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringItemDao {

    @Query("SELECT * FROM recurring_items ORDER BY nextDueDate ASC")
    fun getAll(): Flow<List<RecurringItemEntity>>

    @Query("SELECT * FROM recurring_items WHERE isActive = 1 AND nextDueDate <= :cutoff")
    suspend fun getDue(cutoff: Long): List<RecurringItemEntity>

    @Query("SELECT * FROM recurring_items WHERE id = :id")
    suspend fun getById(id: Long): RecurringItemEntity?

    @Insert
    suspend fun insert(item: RecurringItemEntity): Long

    @Update
    suspend fun update(item: RecurringItemEntity)

    @Delete
    suspend fun delete(item: RecurringItemEntity)
}
