package com.positivefinancial.app.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.local.entity.BudgetEntity
import com.positivefinancial.app.data.local.entity.CategoryEntity
import com.positivefinancial.app.data.model.CategoryType
import com.positivefinancial.app.data.repository.BudgetRepository
import com.positivefinancial.app.data.repository.CategoryRepository
import com.positivefinancial.app.data.repository.TransactionRepository
import com.positivefinancial.app.util.DateRanges
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetItem(
    val category: CategoryEntity,
    val budget: BudgetEntity?,
    val spentThisMonth: Long
) {
    val hasBudget: Boolean get() = budget != null
    val progress: Float get() = budget?.let { (spentThisMonth.toFloat() / it.monthlyLimit.toFloat()).coerceIn(0f, 3f) } ?: 0f
    val isOverBudget: Boolean get() = budget != null && spentThisMonth > budget.monthlyLimit
}

data class BudgetsUiState(
    val items: List<BudgetItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<BudgetsUiState> = run {
        val (start, end) = DateRanges.currentMonthRange()
        combine(
            categoryRepository.observeByType(CategoryType.EXPENSE),
            budgetRepository.observeAll(),
            transactionRepository.observeExpenseBreakdown(start, end)
        ) { categories, budgets, breakdown ->
            val spentByCategory = breakdown.associate { it.categoryId to it.total }
            val budgetsByCategory = budgets.associateBy { it.categoryId }
            val items = categories.map { category ->
                BudgetItem(
                    category = category,
                    budget = budgetsByCategory[category.id],
                    spentThisMonth = spentByCategory[category.id] ?: 0L
                )
            }.sortedWith(compareByDescending<BudgetItem> { it.hasBudget }.thenByDescending { it.spentThisMonth })
            BudgetsUiState(items = items, isLoading = false)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetsUiState())

    fun setBudget(categoryId: Long, monthlyLimit: Long) {
        viewModelScope.launch { budgetRepository.setBudget(categoryId, monthlyLimit) }
    }

    fun removeBudget(budget: BudgetEntity) {
        viewModelScope.launch { budgetRepository.deleteBudget(budget) }
    }
}
