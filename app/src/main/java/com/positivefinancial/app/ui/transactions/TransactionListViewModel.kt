package com.positivefinancial.app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.local.dao.TransactionWithDetails
import com.positivefinancial.app.data.model.TransactionType
import com.positivefinancial.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private data class Filters(
    val query: String = "",
    val type: TransactionType? = null
)

data class TransactionListUiState(
    val query: String = "",
    val typeFilter: TransactionType? = null,
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
            transactionRepository.observeFiltered(
                type = f.type,
                accountId = null,
                categoryId = null,
                startDate = null,
                endDate = null,
                query = f.query
            ).map { list ->
                TransactionListUiState(
                    query = f.query,
                    typeFilter = f.type,
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
}
