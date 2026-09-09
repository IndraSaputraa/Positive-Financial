package com.positivefinancial.app.ui.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.local.entity.CategoryEntity
import com.positivefinancial.app.data.model.CategoryType
import com.positivefinancial.app.data.model.TransactionType
import com.positivefinancial.app.data.repository.AccountRepository
import com.positivefinancial.app.data.repository.CategoryRepository
import com.positivefinancial.app.data.repository.TransactionRepository
import com.positivefinancial.app.ui.navigation.Screen
import com.positivefinancial.app.util.DateRanges
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditTransactionUiState(
    val isEditing: Boolean = false,
    val type: TransactionType = TransactionType.EXPENSE,
    val amountText: String = "",
    val note: String = "",
    val date: Long = DateRanges.now(),
    val selectedAccountId: Long? = null,
    val selectedCategoryId: Long? = null,
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isSaved: Boolean = false
) {
    val amount: Long get() = amountText.toLongOrNull() ?: 0L
    val isSaveEnabled: Boolean get() = amount > 0 && selectedAccountId != null && selectedCategoryId != null
}

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: Long = savedStateHandle.get<Long>("transactionId") ?: Screen.NEW_ID
    private val initialType: TransactionType =
        runCatching { TransactionType.valueOf(savedStateHandle.get<String>("type") ?: "EXPENSE") }
            .getOrDefault(TransactionType.EXPENSE)

    private val _uiState = MutableStateFlow(
        AddEditTransactionUiState(isEditing = transactionId != Screen.NEW_ID, type = initialType)
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val accounts = accountRepository.observeAccounts().first()
            _uiState.value = _uiState.value.copy(accounts = accounts)

            if (transactionId != Screen.NEW_ID) {
                val existing = transactionRepository.getById(transactionId)
                if (existing != null) {
                    _uiState.value = _uiState.value.copy(
                        type = existing.type,
                        amountText = existing.amount.toString(),
                        note = existing.note,
                        date = existing.date,
                        selectedAccountId = existing.accountId,
                        selectedCategoryId = existing.categoryId
                    )
                }
            } else if (accounts.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(selectedAccountId = accounts.first().id)
            }

            loadCategories(_uiState.value.type)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.value = _uiState.value.copy(type = type, selectedCategoryId = null)
        viewModelScope.launch { loadCategories(type) }
    }

    fun onAmountChange(raw: String) {
        _uiState.value = _uiState.value.copy(amountText = raw.filter { it.isDigit() }.take(15))
    }

    fun onNoteChange(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun onDateChange(millis: Long) {
        _uiState.value = _uiState.value.copy(date = millis)
    }

    fun onAccountSelect(id: Long) {
        _uiState.value = _uiState.value.copy(selectedAccountId = id)
    }

    fun onCategorySelect(id: Long) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = id)
    }

    fun save() {
        val state = _uiState.value
        if (!state.isSaveEnabled) return
        viewModelScope.launch {
            if (state.isEditing) {
                transactionRepository.updateTransaction(
                    id = transactionId,
                    amount = state.amount,
                    accountId = state.selectedAccountId,
                    categoryId = state.selectedCategoryId,
                    fromAccountId = null,
                    toAccountId = null,
                    note = state.note,
                    date = state.date
                )
            } else {
                transactionRepository.addIncomeOrExpense(
                    type = state.type,
                    amount = state.amount,
                    accountId = state.selectedAccountId!!,
                    categoryId = state.selectedCategoryId!!,
                    note = state.note,
                    date = state.date
                )
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    fun delete() {
        if (transactionId == Screen.NEW_ID) return
        viewModelScope.launch {
            transactionRepository.getById(transactionId)?.let { transactionRepository.deleteTransaction(it) }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    private suspend fun loadCategories(type: TransactionType) {
        val categoryType = if (type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
        val categories = categoryRepository.observeByType(categoryType).first()
        val stillValid = categories.any { it.id == _uiState.value.selectedCategoryId }
        _uiState.value = _uiState.value.copy(
            categories = categories,
            selectedCategoryId = _uiState.value.selectedCategoryId?.takeIf { stillValid }
        )
    }
}
