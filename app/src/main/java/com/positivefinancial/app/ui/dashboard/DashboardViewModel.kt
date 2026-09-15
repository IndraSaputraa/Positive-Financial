package com.positivefinancial.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.local.dao.CategorySpendingRow
import com.positivefinancial.app.data.local.dao.MonthlySummaryRow
import com.positivefinancial.app.data.local.dao.TransactionWithDetails
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.local.entity.BudgetEntity
import com.positivefinancial.app.data.local.entity.CategoryEntity
import com.positivefinancial.app.data.model.CategoryType
import com.positivefinancial.app.data.model.GoalType
import com.positivefinancial.app.data.repository.AccountRepository
import com.positivefinancial.app.data.repository.BudgetRepository
import com.positivefinancial.app.data.repository.CategoryRepository
import com.positivefinancial.app.data.repository.GoalRepository
import com.positivefinancial.app.data.repository.TransactionRepository
import com.positivefinancial.app.util.DateRanges
import com.positivefinancial.app.util.Formatters
import com.positivefinancial.app.util.InsightEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import javax.inject.Inject

data class BudgetAlert(
    val categoryName: String,
    val iconKey: String,
    val colorHex: String,
    val spent: Long,
    val limit: Long
) {
    val progress: Float get() = (spent.toFloat() / limit.toFloat()).coerceIn(0f, 3f)
    val isOverBudget: Boolean get() = spent > limit
}

data class NetWorthPoint(
    val label: String,
    val netWorth: Long
)

data class GoalSummary(
    val name: String,
    val iconKey: String,
    val colorHex: String,
    val currentAmount: Long,
    val targetAmount: Long
) {
    val progress: Float get() = if (targetAmount > 0) (currentAmount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f) else 0f
    val isCompleted: Boolean get() = progress >= 1f
}

data class DashboardUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val isCurrentMonth: Boolean = true,
    val totalBalance: Long = 0,
    val accounts: List<AccountEntity> = emptyList(),
    val monthlyIncome: Long = 0,
    val monthlyExpense: Long = 0,
    val expenseBreakdown: List<CategorySpendingRow> = emptyList(),
    val budgetAlerts: List<BudgetAlert> = emptyList(),
    val goals: List<GoalSummary> = emptyList(),
    val recentTransactions: List<TransactionWithDetails> = emptyList(),
    val monthlySummary: List<MonthlySummaryRow> = emptyList(),
    val netWorthTrend: List<NetWorthPoint> = emptyList(),
    val insights: List<String> = emptyList(),
    val isLoading: Boolean = true
)

private data class MonthTotals(
    val totalBalance: Long,
    val accounts: List<AccountEntity>,
    val income: Long,
    val expense: Long,
    val breakdown: List<CategorySpendingRow>
)

private data class MonthData(
    val month: YearMonth,
    val totals: MonthTotals,
    val recent: List<TransactionWithDetails>,
    val budgets: List<BudgetEntity>
)

private const val RECENT_ACTIVITY_LIMIT = 8

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val goalRepository: GoalRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    private val monthlySummary = transactionRepository.observeMonthlySummary(DateRanges.monthsAgoStart(6))
    private val expenseCategories = categoryRepository.observeByType(CategoryType.EXPENSE)

    private val goalsFlow = combine(
        goalRepository.observeActive(),
        accountRepository.observeAccounts()
    ) { goals, accounts ->
        val accountsById = accounts.associateBy { it.id }
        goals.mapNotNull { goal ->
            val account = accountsById[goal.linkedAccountId] ?: return@mapNotNull null
            val outstanding = if (account.balance < 0) -account.balance else 0
            val current = when (goal.goalType) {
                GoalType.SAVINGS -> account.balance.coerceAtLeast(0)
                GoalType.DEBT_PAYOFF -> (goal.targetAmount - outstanding).coerceIn(0, goal.targetAmount)
            }
            GoalSummary(goal.name, goal.iconKey, goal.colorHex, current, goal.targetAmount)
        }
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        selectedMonth.flatMapLatest { month -> monthScopedData(month) },
        monthlySummary,
        goalsFlow,
        expenseCategories
    ) { monthData, monthly, goals, categories ->
        val topCategory = monthData.totals.breakdown.firstOrNull { it.total > 0 }
        DashboardUiState(
            selectedMonth = monthData.month,
            isCurrentMonth = monthData.month == YearMonth.now(),
            totalBalance = monthData.totals.totalBalance,
            accounts = monthData.totals.accounts,
            monthlyIncome = monthData.totals.income,
            monthlyExpense = monthData.totals.expense,
            expenseBreakdown = monthData.totals.breakdown,
            budgetAlerts = budgetAlertsFrom(monthData.totals.breakdown, monthData.budgets, categories),
            goals = goals,
            recentTransactions = monthData.recent,
            monthlySummary = monthly,
            netWorthTrend = netWorthTrendFrom(monthly, monthData.totals.totalBalance),
            insights = InsightEngine.generate(
                monthData.totals.income,
                monthData.totals.expense,
                topCategory,
                isCurrentPeriod = monthData.month == YearMonth.now()
            ),
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    private fun monthScopedData(month: YearMonth): Flow<MonthData> {
        val (start, end) = DateRanges.monthRange(month)

        val totals = combine(
            accountRepository.observeTotalBalance(),
            accountRepository.observeAccounts(),
            transactionRepository.observeTotalIncome(start, end),
            transactionRepository.observeTotalExpense(start, end),
            transactionRepository.observeExpenseBreakdown(start, end)
        ) { totalBalance, accounts, income, expense, breakdown ->
            MonthTotals(totalBalance, accounts, income, expense, breakdown)
        }

        val recent = transactionRepository.observeFiltered(startDate = start, endDate = end)
            .map { it.take(RECENT_ACTIVITY_LIMIT) }

        return combine(totals, recent, budgetRepository.observeAll()) { t, r, budgets ->
            MonthData(month, t, r, budgets)
        }
    }

    /**
     * Reconstructs net worth at the end of each recent month by walking backward
     * from the current total balance: transfers net to zero across all accounts,
     * so undoing each month's recorded income (subtract) and expense (add back)
     * recovers the total balance as it stood before that month's activity.
     */
    private fun netWorthTrendFrom(monthly: List<MonthlySummaryRow>, currentTotal: Long): List<NetWorthPoint> {
        var runningNetWorth = currentTotal
        val points = ArrayDeque<NetWorthPoint>()
        for (row in monthly.asReversed()) {
            points.addFirst(NetWorthPoint(Formatters.monthYearShort(row.yearMonth), runningNetWorth))
            runningNetWorth = runningNetWorth - row.income + row.expense
        }
        return points.toList()
    }

    private fun budgetAlertsFrom(
        breakdown: List<CategorySpendingRow>,
        budgets: List<BudgetEntity>,
        categories: List<CategoryEntity>
    ): List<BudgetAlert> {
        val spentByCategory = breakdown.mapNotNull { row -> row.categoryId?.let { it to row.total } }.toMap()
        val categoriesById = categories.associateBy { it.id }
        return budgets.mapNotNull { budget ->
            val category = categoriesById[budget.categoryId] ?: return@mapNotNull null
            BudgetAlert(
                categoryName = category.name,
                iconKey = category.iconKey,
                colorHex = category.colorHex,
                spent = spentByCategory[budget.categoryId] ?: 0L,
                limit = budget.monthlyLimit
            )
        }.sortedByDescending { it.progress }
    }

    fun onPreviousMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun onNextMonth() {
        selectedMonth.value = selectedMonth.value.plusMonths(1)
    }
}
