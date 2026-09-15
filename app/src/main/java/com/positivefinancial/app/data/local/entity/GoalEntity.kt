package com.positivefinancial.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.positivefinancial.app.data.model.GoalType

/**
 * A goal always tracks a real account rather than a separate manual tally:
 * SAVINGS reads the linked account's balance against [targetAmount]; DEBT_PAYOFF
 * reads a linked credit card's outstanding balance shrinking from [targetAmount]
 * (the debt at the time the goal was created) toward zero. Progress is derived,
 * not stored, so it always matches the real transactions/transfers on that account.
 */
@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["linkedAccountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("linkedAccountId")]
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val goalType: GoalType,
    val targetAmount: Long,
    val linkedAccountId: Long,
    val targetDate: Long? = null,
    val iconKey: String,
    val colorHex: String,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
