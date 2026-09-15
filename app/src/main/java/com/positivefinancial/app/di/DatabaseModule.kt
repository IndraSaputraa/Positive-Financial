package com.positivefinancial.app.di

import android.content.Context
import androidx.room.Room
import com.positivefinancial.app.data.local.AppDatabase
import com.positivefinancial.app.data.local.MIGRATION_1_2
import com.positivefinancial.app.data.local.dao.AccountDao
import com.positivefinancial.app.data.local.dao.BudgetDao
import com.positivefinancial.app.data.local.dao.CategoryDao
import com.positivefinancial.app.data.local.dao.CreditCardDao
import com.positivefinancial.app.data.local.dao.GoalDao
import com.positivefinancial.app.data.local.dao.RecurringItemDao
import com.positivefinancial.app.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideAccountDao(database: AppDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideCreditCardDao(database: AppDatabase): CreditCardDao = database.creditCardDao()

    @Provides
    fun provideBudgetDao(database: AppDatabase): BudgetDao = database.budgetDao()

    @Provides
    fun provideGoalDao(database: AppDatabase): GoalDao = database.goalDao()

    @Provides
    fun provideRecurringItemDao(database: AppDatabase): RecurringItemDao = database.recurringItemDao()
}
