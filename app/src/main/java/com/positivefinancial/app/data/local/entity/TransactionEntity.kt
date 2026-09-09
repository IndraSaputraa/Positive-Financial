package com.positivefinancial.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.positivefinancial.app.data.model.TransactionType

/**
 * For INCOME/EXPENSE, [accountId] and [categoryId] are set. For TRANSFER,
 * [fromAccountId] and [toAccountId] are set instead (moving money between
 * two accounts, including paying down a credit card).
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["fromAccountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["toAccountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("accountId"),
        Index("fromAccountId"),
        Index("toAccountId"),
        Index("categoryId"),
        Index("date")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amount: Long,
    val accountId: Long? = null,
    val categoryId: Long? = null,
    val fromAccountId: Long? = null,
    val toAccountId: Long? = null,
    val note: String = "",
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)
