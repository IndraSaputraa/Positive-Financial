package com.positivefinancial.app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.local.dao.TransactionWithDetails
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.local.entity.CategoryEntity
import com.positivefinancial.app.data.model.TransactionType
import com.positivefinancial.app.data.repository.AccountRepository
import com.positivefinancial.app.data.repository.CategoryRepository
import com.positivefinancial.app.data.repository.TransactionRepository
import com.positivefinancial.app.util.DateRanges
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import javax.inject.Inject

private data class Filters(
    val query: String = "",
    val type: TransactionType? = null,
    val month: YearMonth? = YearMonth.now(),
    val accountId: Long? = null,
    val categoryId: Long? = null
)

data class TransactionListUiState(
    val query: String = "",
    val typeFilter: TransactionType? = null,
    val selectedMonth: YearMonth? = YearMonth.now(),
    val accountFilterId: Long? = null,
    val categoryFilterId: Long? = null,
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val transactions: List<TransactionWithDetails> = emptyList(),
    val isLoading: Boolean = true
) {
    val activeFilterCount: Int get() = listOfNotNull(accountFilterId, categoryFilterId).size
    val accountFilterName: String? get() = accounts.firstOrNull { it.id == accountFilterId }?.name
    val categoryFilterName: String? get() = categories.firstOrNull { it.id == categoryFilterId }?.name
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    private val filters = MutableStateFlow(Filters())

    private val filteredTransactions = filters.flatMapLatest { f ->
        val (start, end) = f.month?.let { DateRanges.monthRange(it) } ?: (null to null)
        transactionRepository.observeFiltered(
            type = f.type,
            accountId = f.accountId,
            categoryId = f.categoryId,
            startDate = start,
            endDate = end,
            query = f.query
        )
    }

    val uiState: StateFlow<TransactionListUiState> = combine(
        filters,
        filteredTransactions,
        accountRepository.observeAccounts(),
        categoryRepository.observeAll()
    ) { f, transactions, accounts, categories ->
        TransactionListUiState(
            query = f.query,
            typeFilter = f.type,
            selectedMonth = f.month,
            accountFilterId = f.accountId,
            categoryFilterId = f.categoryId,
            accounts = accounts,
            categories = categories,
            transactions = transactions,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionListUiState())

    fun onQueryChange(query: String) {
        filters.value = filters.value.copy(query = query)
    }

    fun onTypeFilterChange(type: TransactionType?) {
        filters.value = filters.value.copy(type = type)
    }

    fun onAccountFilterChange(accountId: Long?) {
        filters.value = filters.value.copy(accountId = accountId)
    }

    fun onCategoryFilterChange(categoryId: Long?) {
        filters.value = filters.value.copy(categoryId = categoryId)
    }

    fun onClearAccountCategoryFilters() {
        filters.value = filters.value.copy(accountId = null, categoryId = null)
    }

    fun onPreviousMonth() {
        val current = filters.value.month ?: YearMonth.now()
        filters.value = filters.value.copy(month = current.minusMonths(1))
    }

    fun onNextMonth() {
        val current = filters.value.month ?: YearMonth.now()
        filters.value = filters.value.copy(month = current.plusMonths(1))
    }

    fun onToggleAllTime() {
        filters.value = filters.value.copy(month = if (filters.value.month == null) YearMonth.now() else null)
    }
}
