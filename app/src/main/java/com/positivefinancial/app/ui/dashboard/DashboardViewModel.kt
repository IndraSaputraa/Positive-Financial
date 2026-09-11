package com.positivefinancial.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.local.dao.CategorySpendingRow
import com.positivefinancial.app.data.local.dao.MonthlySummaryRow
import com.positivefinancial.app.data.local.dao.TransactionWithDetails
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.repository.AccountRepository
import com.positivefinancial.app.data.repository.TransactionRepository
import com.positivefinancial.app.util.DateRanges
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

data class DashboardUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val isCurrentMonth: Boolean = true,
    val totalBalance: Long = 0,
    val accounts: List<AccountEntity> = emptyList(),
    val monthlyIncome: Long = 0,
    val monthlyExpense: Long = 0,
    val expenseBreakdown: List<CategorySpendingRow> = emptyList(),
    val recentTransactions: List<TransactionWithDetails> = emptyList(),
    val monthlySummary: List<MonthlySummaryRow> = emptyList(),
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
    val recent: List<TransactionWithDetails>
)

private const val RECENT_ACTIVITY_LIMIT = 8

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    private val monthlySummary = transactionRepository.observeMonthlySummary(DateRanges.monthsAgoStart(6))

    val uiState: StateFlow<DashboardUiState> = combine(
        selectedMonth.flatMapLatest { month -> monthScopedData(month) },
        monthlySummary
    ) { monthData, monthly ->
        val topCategory = monthData.totals.breakdown.firstOrNull { it.total > 0 }
        DashboardUiState(
            selectedMonth = monthData.month,
            isCurrentMonth = monthData.month == YearMonth.now(),
            totalBalance = monthData.totals.totalBalance,
            accounts = monthData.totals.accounts,
            monthlyIncome = monthData.totals.income,
            monthlyExpense = monthData.totals.expense,
            expenseBreakdown = monthData.totals.breakdown,
            recentTransactions = monthData.recent,
            monthlySummary = monthly,
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

        return combine(totals, recent) { t, r -> MonthData(month, t, r) }
    }

    fun onPreviousMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun onNextMonth() {
        selectedMonth.value = selectedMonth.value.plusMonths(1)
    }
}
