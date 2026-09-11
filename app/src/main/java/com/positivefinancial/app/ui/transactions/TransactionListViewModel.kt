package com.positivefinancial.app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.local.dao.TransactionWithDetails
import com.positivefinancial.app.data.model.TransactionType
import com.positivefinancial.app.data.repository.TransactionRepository
import com.positivefinancial.app.util.DateRanges
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import javax.inject.Inject

private data class Filters(
    val query: String = "",
    val type: TransactionType? = null,
    val month: YearMonth? = YearMonth.now()
)

data class TransactionListUiState(
    val query: String = "",
    val typeFilter: TransactionType? = null,
    val selectedMonth: YearMonth? = YearMonth.now(),
    val transactions: List<TransactionWithDetails> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val filters = MutableStateFlow(Filters())

    val uiState = filters
        .flatMapLatest { f ->
            val (start, end) = f.month?.let { DateRanges.monthRange(it) } ?: (null to null)
            transactionRepository.observeFiltered(
                type = f.type,
                accountId = null,
                categoryId = null,
                startDate = start,
                endDate = end,
                query = f.query
            ).map { list ->
                TransactionListUiState(
                    query = f.query,
                    typeFilter = f.type,
                    selectedMonth = f.month,
                    transactions = list,
                    isLoading = false
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionListUiState())

    fun onQueryChange(query: String) {
        filters.value = filters.value.copy(query = query)
    }

    fun onTypeFilterChange(type: TransactionType?) {
        filters.value = filters.value.copy(type = type)
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
