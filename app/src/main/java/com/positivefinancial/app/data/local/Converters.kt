package com.positivefinancial.app.data.local

import androidx.room.TypeConverter
import com.positivefinancial.app.data.model.AccountType
import com.positivefinancial.app.data.model.CategoryType
import com.positivefinancial.app.data.model.GoalType
import com.positivefinancial.app.data.model.RecurringFrequency
import com.positivefinancial.app.data.model.TransactionType

class Converters {

    @TypeConverter
    fun fromAccountType(value: AccountType?): String? = value?.name

    @TypeConverter
    fun toAccountType(value: String?): AccountType? = value?.let { AccountType.valueOf(it) }

    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? = value?.let { TransactionType.valueOf(it) }

    @TypeConverter
    fun fromCategoryType(value: CategoryType?): String? = value?.name

    @TypeConverter
    fun toCategoryType(value: String?): CategoryType? = value?.let { CategoryType.valueOf(it) }

    @TypeConverter
    fun fromGoalType(value: GoalType?): String? = value?.name

    @TypeConverter
    fun toGoalType(value: String?): GoalType? = value?.let { GoalType.valueOf(it) }

    @TypeConverter
    fun fromRecurringFrequency(value: RecurringFrequency?): String? = value?.name

    @TypeConverter
    fun toRecurringFrequency(value: String?): RecurringFrequency? = value?.let { RecurringFrequency.valueOf(it) }
}
