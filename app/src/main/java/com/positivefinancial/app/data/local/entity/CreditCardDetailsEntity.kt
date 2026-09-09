package com.positivefinancial.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * One-to-one extension of an [AccountEntity] of type CREDIT_CARD, holding
 * limit, billing cycle, and per-card reminder configuration. The outstanding
 * balance itself lives on the account row (see [AccountEntity] balance docs).
 */
@Entity(
    tableName = "credit_card_details",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CreditCardDetailsEntity(
    @PrimaryKey val accountId: Long,
    val creditLimit: Long,
    val cardNetwork: String = "Other",
    val lastFourDigits: String = "",
    val billingCycleDay: Int = 1,
    val paymentDueDay: Int = 20,
    val reminderEnabled: Boolean = true,
    val reminderDayOfMonth: Int = 15,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0
)
