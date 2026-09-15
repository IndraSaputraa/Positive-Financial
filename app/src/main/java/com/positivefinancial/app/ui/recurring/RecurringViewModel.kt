package com.positivefinancial.app.ui.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.local.entity.RecurringItemEntity
import com.positivefinancial.app.data.repository.AccountRepository
import com.positivefinancial.app.data.repository.CategoryRepository
import com.positivefinancial.app.data.repository.RecurringItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringItemDisplay(
    val item: RecurringItemEntity,
    val accountName: String,
    val categoryName: String
)

data class RecurringUiState(
    val items: List<RecurringItemDisplay> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val recurringItemRepository: RecurringItemRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<RecurringUiState> = combine(
        recurringItemRepository.observeAll(),
        accountRepository.observeAccounts(),
        categoryRepository.observeAll()
    ) { items, accounts, categories ->
        val accountsById = accounts.associateBy { it.id }
        val categoriesById = categories.associateBy { it.id }
        RecurringUiState(
            items = items.map { item ->
                RecurringItemDisplay(
                    item = item,
                    accountName = accountsById[item.accountId]?.name ?: "Unknown account",
                    categoryName = categoriesById[item.categoryId]?.name ?: "Uncategorized"
                )
            },
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecurringUiState())

    fun setActive(item: RecurringItemEntity, isActive: Boolean) {
        viewModelScope.launch {
            recurringItemRepository.updateItem(
                item = item,
                name = item.name,
                amount = item.amount,
                accountId = item.accountId,
                categoryId = item.categoryId,
                frequency = item.frequency,
                dayOfMonth = item.dayOfMonth,
                monthOfYear = item.monthOfYear,
                autoCreateTransaction = item.autoCreateTransaction,
                reminderDaysBefore = item.reminderDaysBefore,
                isActive = isActive
            )
        }
    }

    fun deleteItem(item: RecurringItemEntity) {
        viewModelScope.launch { recurringItemRepository.deleteItem(item) }
    }
}
