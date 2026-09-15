package com.positivefinancial.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.positivefinancial.app.data.local.dao.AccountDao
import com.positivefinancial.app.data.local.dao.BudgetDao
import com.positivefinancial.app.data.local.dao.CategoryDao
import com.positivefinancial.app.data.local.dao.CreditCardDao
import com.positivefinancial.app.data.local.dao.GoalDao
import com.positivefinancial.app.data.local.dao.RecurringItemDao
import com.positivefinancial.app.data.local.dao.TransactionDao
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.local.entity.BudgetEntity
import com.positivefinancial.app.data.local.entity.CategoryEntity
import com.positivefinancial.app.data.local.entity.CreditCardDetailsEntity
import com.positivefinancial.app.data.local.entity.GoalEntity
import com.positivefinancial.app.data.local.entity.RecurringItemEntity
import com.positivefinancial.app.data.local.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        CreditCardDetailsEntity::class,
        BudgetEntity::class,
        GoalEntity::class,
        RecurringItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun recurringItemDao(): RecurringItemDao

    companion object {
        const val DATABASE_NAME = "positive_financial.db"
    }
}
