package com.positivefinancial.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.positivefinancial.app.data.model.AccountType

/**
 * Balance semantics are uniform across all account types: expenses subtract,
 * income and transfer-in add. For a CREDIT_CARD account, balance is stored as
 * the negative of the outstanding balance, so spending on the card naturally
 * drives it further negative and paying it off (a transfer in) brings it back
 * toward zero. This lets a simple sum of all account balances equal the
 * user's true net worth without special-casing credit cards.
 */
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Long,
    val iconKey: String,
    val colorHex: String,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
