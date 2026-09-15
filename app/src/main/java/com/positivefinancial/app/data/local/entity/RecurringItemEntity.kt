package com.positivefinancial.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.positivefinancial.app.data.model.RecurringFrequency
import com.positivefinancial.app.data.model.TransactionType

/**
 * Covers two related needs with one model: a recurring bill/income that should
 * post itself automatically (rent, salary, a subscription — [autoCreateTransaction]
 * true), and a pure reminder for something the user pays manually and logs
 * themselves (annual vehicle tax, PBB — [autoCreateTransaction] false). Either way
 * [nextDueDate] drives scheduling and [reminderDaysBefore] controls how early the
 * heads-up notification fires.
 */
@Entity(
    tableName = "recurring_items",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("accountId"), Index("categoryId")]
)
data class RecurringItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val amount: Long,
    val accountId: Long,
    val categoryId: Long,
    val frequency: RecurringFrequency,
    val dayOfMonth: Int,
    val monthOfYear: Int,
    val autoCreateTransaction: Boolean,
    val reminderDaysBefore: Int,
    val isActive: Boolean = true,
    val nextDueDate: Long,
    val lastProcessedDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
