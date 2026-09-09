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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardUiState(
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

private data class DashboardGroup1(
    val totalBalance: Long,
    val accounts: List<AccountEntity>,
    val income: Long,
    val expense: Long,
    val breakdown: List<CategorySpendingRow>
)

private data class DashboardGroup2(
    val recent: List<TransactionWithDetails>,
    val monthly: List<MonthlySummaryRow>
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    accountRepository: AccountRepository,
    transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = run {
        val (start, end) = DateRanges.currentMonthRange()
        val since6Months = DateRanges.monthsAgoStart(6)

        val group1 = combine(
            accountRepository.observeTotalBalance(),
            accountRepository.observeAccounts(),
            transactionRepository.observeTotalIncome(start, end),
            transactionRepository.observeTotalExpense(start, end),
            transactionRepository.observeExpenseBreakdown(start, end)
        ) { totalBalance, accounts, income, expense, breakdown ->
            DashboardGroup1(totalBalance, accounts, income, expense, breakdown)
        }

        val group2 = combine(
            transactionRepository.observeRecent(6),
            transactionRepository.observeMonthlySummary(since6Months)
        ) { recent, monthly -> DashboardGroup2(recent, monthly) }

        combine(group1, group2) { g1, g2 ->
            val topCategory = g1.breakdown.firstOrNull { it.total > 0 }
            DashboardUiState(
                totalBalance = g1.totalBalance,
                accounts = g1.accounts,
                monthlyIncome = g1.income,
                monthlyExpense = g1.expense,
                expenseBreakdown = g1.breakdown,
                recentTransactions = g2.recent,
                monthlySummary = g2.monthly,
                insights = InsightEngine.generate(g1.income, g1.expense, topCategory),
                isLoading = false
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
}
