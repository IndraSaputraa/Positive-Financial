package com.positivefinancial.app.util

import com.positivefinancial.app.data.local.dao.CategorySpendingRow

/**
 * Fully on-device "AI Insights" for the dashboard: a small rule-based engine
 * that turns this month's numbers into plain-English observations and tips.
 * No network calls, no data leaves the device.
 */
object InsightEngine {

    private val categoryTips: Map<String, String> = mapOf(
        "Food & Beverages" to "Cooking at home a few more nights a week can meaningfully cut food costs.",
        "Groceries" to "Planning meals ahead helps avoid impulse buys at the store.",
        "Transportation" to "Carpooling or public transit a couple of days a week can add up to real savings.",
        "Fuel & Parking" to "Combining errands into one trip saves both fuel and time.",
        "Shopping" to "Try a 24-hour rule before non-essential purchases to curb impulse spending.",
        "Bills & Utilities" to "Review recurring subscriptions — cancel the ones you rarely use.",
        "Internet & Phone Credit" to "Compare data plans occasionally; providers often have better bundles.",
        "Entertainment" to "Setting a monthly entertainment budget keeps fun spending in check.",
        "Housing & Rent" to "If rent feels tight, renegotiating or revisiting your space needs once a year can help.",
        "Health" to "Preventive care now often costs less than treatment later — keep it up.",
        "Education" to "Investing in skills pays off long-term — nice work prioritizing this.",
        "Family & Social" to "Setting a friendly budget for gatherings keeps social life sustainable.",
        "Personal Care" to "Small recurring personal-care costs are easy to trim if needed.",
        "Donation & Zakat" to "Giving regularly is a great habit — keep it consistent with your income.",
        "Investment" to "Consistent investing, even in small amounts, compounds well over time.",
        "Debt & Card Payment" to "Paying more than the minimum on debt saves you interest over time."
    )

    fun generate(
        totalIncome: Long,
        totalExpense: Long,
        topExpenseCategory: CategorySpendingRow?
    ): List<String> {
        val insights = mutableListOf<String>()
        val net = totalIncome - totalExpense

        insights += when {
            totalIncome == 0L && totalExpense == 0L ->
                "No transactions recorded yet this month. Add your first one to see insights here."
            else -> {
                val incomeText = Formatters.currency(totalIncome)
                val expenseText = Formatters.currency(totalExpense)
                "This month you earned $incomeText and spent $expenseText."
            }
        }

        if (topExpenseCategory != null && topExpenseCategory.total > 0 && totalExpense > 0) {
            val share = (topExpenseCategory.total * 100.0 / totalExpense).let { "%.0f".format(it) }
            val name = topExpenseCategory.categoryName ?: "Uncategorized"
            insights += "Your biggest expense was $name at ${Formatters.currency(topExpenseCategory.total)} ($share% of spending)."
            categoryTips[name]?.let { insights += it }
        }

        if (totalIncome > 0) {
            val savingsRate = (net * 100.0 / totalIncome)
            insights += when {
                net < 0 -> "You're spending more than you earn this month — worth a closer look at your biggest categories."
                savingsRate < 10 -> "You're saving about ${"%.0f".format(savingsRate)}% of your income. Aiming for 20% builds a stronger safety net."
                savingsRate < 20 -> "You're saving about ${"%.0f".format(savingsRate)}% of your income — solid progress toward the 20% mark."
                else -> "Great job! You saved about ${"%.0f".format(savingsRate)}% of your income this month."
            }
        }

        return insights
    }
}
