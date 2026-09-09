package com.positivefinancial.app.data.local

import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.local.entity.CategoryEntity
import com.positivefinancial.app.data.model.AccountType
import com.positivefinancial.app.data.model.CategoryType

/**
 * Starter data seeded once on first launch so the app is immediately useful.
 * Categories reflect common Indonesian personal-finance line items; account
 * presets cover the wallets most people in Indonesia actually use day to day.
 */
object DefaultData {

    fun defaultExpenseCategories(): List<CategoryEntity> = listOf(
        CategoryEntity(name = "Food & Beverages", type = CategoryType.EXPENSE, iconKey = "restaurant", colorHex = "#F97316", isDefault = true, sortOrder = 0),
        CategoryEntity(name = "Groceries", type = CategoryType.EXPENSE, iconKey = "local_grocery_store", colorHex = "#84CC16", isDefault = true, sortOrder = 1),
        CategoryEntity(name = "Transportation", type = CategoryType.EXPENSE, iconKey = "directions_car", colorHex = "#3B82F6", isDefault = true, sortOrder = 2),
        CategoryEntity(name = "Fuel & Parking", type = CategoryType.EXPENSE, iconKey = "local_gas_station", colorHex = "#A855F7", isDefault = true, sortOrder = 3),
        CategoryEntity(name = "Shopping", type = CategoryType.EXPENSE, iconKey = "shopping_bag", colorHex = "#EC4899", isDefault = true, sortOrder = 4),
        CategoryEntity(name = "Bills & Utilities", type = CategoryType.EXPENSE, iconKey = "receipt_long", colorHex = "#EF4444", isDefault = true, sortOrder = 5),
        CategoryEntity(name = "Internet & Phone Credit", type = CategoryType.EXPENSE, iconKey = "phone_android", colorHex = "#14B8A6", isDefault = true, sortOrder = 6),
        CategoryEntity(name = "Housing & Rent", type = CategoryType.EXPENSE, iconKey = "home", colorHex = "#6366F1", isDefault = true, sortOrder = 7),
        CategoryEntity(name = "Health", type = CategoryType.EXPENSE, iconKey = "favorite", colorHex = "#06B6D4", isDefault = true, sortOrder = 8),
        CategoryEntity(name = "Education", type = CategoryType.EXPENSE, iconKey = "school", colorHex = "#8B5CF6", isDefault = true, sortOrder = 9),
        CategoryEntity(name = "Entertainment", type = CategoryType.EXPENSE, iconKey = "movie", colorHex = "#F59E0B", isDefault = true, sortOrder = 10),
        CategoryEntity(name = "Family & Social", type = CategoryType.EXPENSE, iconKey = "groups", colorHex = "#F472B6", isDefault = true, sortOrder = 11),
        CategoryEntity(name = "Personal Care", type = CategoryType.EXPENSE, iconKey = "spa", colorHex = "#FB7185", isDefault = true, sortOrder = 12),
        CategoryEntity(name = "Donation & Zakat", type = CategoryType.EXPENSE, iconKey = "volunteer_activism", colorHex = "#22C55E", isDefault = true, sortOrder = 13),
        CategoryEntity(name = "Investment", type = CategoryType.EXPENSE, iconKey = "trending_up", colorHex = "#0EA5E9", isDefault = true, sortOrder = 14),
        CategoryEntity(name = "Debt & Card Payment", type = CategoryType.EXPENSE, iconKey = "credit_card", colorHex = "#64748B", isDefault = true, sortOrder = 15),
        CategoryEntity(name = "Other Expense", type = CategoryType.EXPENSE, iconKey = "category", colorHex = "#94A3B8", isDefault = true, sortOrder = 16)
    )

    fun defaultIncomeCategories(): List<CategoryEntity> = listOf(
        CategoryEntity(name = "Salary", type = CategoryType.INCOME, iconKey = "work", colorHex = "#10B981", isDefault = true, sortOrder = 0),
        CategoryEntity(name = "Business", type = CategoryType.INCOME, iconKey = "storefront", colorHex = "#059669", isDefault = true, sortOrder = 1),
        CategoryEntity(name = "Freelance & Side Job", type = CategoryType.INCOME, iconKey = "laptop_mac", colorHex = "#0D9488", isDefault = true, sortOrder = 2),
        CategoryEntity(name = "Bonus & THR", type = CategoryType.INCOME, iconKey = "card_giftcard", colorHex = "#F59E0B", isDefault = true, sortOrder = 3),
        CategoryEntity(name = "Gift", type = CategoryType.INCOME, iconKey = "redeem", colorHex = "#EC4899", isDefault = true, sortOrder = 4),
        CategoryEntity(name = "Investment Returns", type = CategoryType.INCOME, iconKey = "savings", colorHex = "#0EA5E9", isDefault = true, sortOrder = 5),
        CategoryEntity(name = "Other Income", type = CategoryType.INCOME, iconKey = "attach_money", colorHex = "#6B7280", isDefault = true, sortOrder = 6)
    )

    fun defaultAccounts(): List<AccountEntity> = listOf(
        AccountEntity(name = "Cash", type = AccountType.CASH, balance = 0, iconKey = "payments", colorHex = "#16A34A", sortOrder = 0),
        AccountEntity(name = "BCA", type = AccountType.BANK, balance = 0, iconKey = "account_balance", colorHex = "#2563EB", sortOrder = 1),
        AccountEntity(name = "Mandiri", type = AccountType.BANK, balance = 0, iconKey = "account_balance", colorHex = "#EAB308", sortOrder = 2),
        AccountEntity(name = "GoPay", type = AccountType.E_WALLET, balance = 0, iconKey = "account_balance_wallet", colorHex = "#22C55E", sortOrder = 3),
        AccountEntity(name = "OVO", type = AccountType.E_WALLET, balance = 0, iconKey = "account_balance_wallet", colorHex = "#7C3AED", sortOrder = 4),
        AccountEntity(name = "DANA", type = AccountType.E_WALLET, balance = 0, iconKey = "account_balance_wallet", colorHex = "#2563EB", sortOrder = 5),
        AccountEntity(name = "ShopeePay", type = AccountType.E_WALLET, balance = 0, iconKey = "account_balance_wallet", colorHex = "#F97316", sortOrder = 6)
    )
}
