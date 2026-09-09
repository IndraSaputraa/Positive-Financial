package com.positivefinancial.app.data.local.dao

import com.positivefinancial.app.data.model.TransactionType

data class TransactionWithDetails(
    val id: Long,
    val type: TransactionType,
    val amount: Long,
    val note: String,
    val date: Long,
    val accountId: Long?,
    val accountName: String?,
    val accountIconKey: String?,
    val accountColorHex: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryIconKey: String?,
    val categoryColorHex: String?,
    val fromAccountId: Long?,
    val fromAccountName: String?,
    val toAccountId: Long?,
    val toAccountName: String?
)

data class CategorySpendingRow(
    val categoryId: Long?,
    val categoryName: String?,
    val iconKey: String?,
    val colorHex: String?,
    val total: Long
)

data class MonthlySummaryRow(
    val yearMonth: String,
    val income: Long,
    val expense: Long
)
