package com.positivefinancial.app.ui.recurring

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.local.entity.CategoryEntity
import com.positivefinancial.app.data.local.entity.RecurringItemEntity
import com.positivefinancial.app.data.model.CategoryType
import com.positivefinancial.app.data.model.RecurringFrequency
import com.positivefinancial.app.data.model.TransactionType
import com.positivefinancial.app.data.repository.AccountRepository
import com.positivefinancial.app.data.repository.CategoryRepository
import com.positivefinancial.app.data.repository.RecurringItemRepository
import com.positivefinancial.app.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditRecurringUiState(
    val isEditing: Boolean = false,
    val name: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val amountText: String = "",
    val selectedAccountId: Long? = null,
    val selectedCategoryId: Long? = null,
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val dayOfMonth: Int = 1,
    val monthOfYear: Int = 1,
    val autoCreateTransaction: Boolean = true,
    val reminderDaysBefore: Int = 3,
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isSaved: Boolean = false
) {
    val amount: Long get() = amountText.toLongOrNull() ?: 0L
    val isSaveEnabled: Boolean get() =
        name.isNotBlank() && amount > 0 && selectedAccountId != null && selectedCategoryId != null
}

@HiltViewModel
class AddEditRecurringViewModel @Inject constructor(
    private val recurringItemRepository: RecurringItemRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recurringId: Long = savedStateHandle.get<Long>("recurringId") ?: Screen.NEW_ID
    private var existing: RecurringItemEntity? = null

    private val _uiState = MutableStateFlow(AddEditRecurringUiState(isEditing = recurringId != Screen.NEW_ID))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val accounts = accountRepository.observeAccounts().first()
            _uiState.value = _uiState.value.copy(accounts = accounts)

            if (recurringId != Screen.NEW_ID) {
                val item = recurringItemRepository.getById(recurringId)
                existing = item
                if (item != null) {
                    _uiState.value = _uiState.value.copy(
                        name = item.name,
                        type = item.type,
                        amountText = item.amount.toString(),
                        selectedAccountId = item.accountId,
                        selectedCategoryId = item.categoryId,
                        frequency = item.frequency,
                        dayOfMonth = item.dayOfMonth,
                        monthOfYear = item.monthOfYear,
                        autoCreateTransaction = item.autoCreateTransaction,
                        reminderDaysBefore = item.reminderDaysBefore
                    )
                }
            } else if (accounts.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(selectedAccountId = accounts.first().id)
            }

            loadCategories(_uiState.value.type)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.value = _uiState.value.copy(type = type, selectedCategoryId = null)
        viewModelScope.launch { loadCategories(type) }
    }

    fun onAmountChange(raw: String) {
        _uiState.value = _uiState.value.copy(amountText = raw.filter { it.isDigit() }.take(15))
    }

    fun onAccountSelect(id: Long) {
        _uiState.value = _uiState.value.copy(selectedAccountId = id)
    }

    fun onCategorySelect(id: Long) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = id)
    }

    fun onFrequencyChange(frequency: RecurringFrequency) {
        _uiState.value = _uiState.value.copy(frequency = frequency)
    }

    fun onDayOfMonthChange(day: Int) {
        _uiState.value = _uiState.value.copy(dayOfMonth = day.coerceIn(1, 28))
    }

    fun onMonthOfYearChange(month: Int) {
        _uiState.value = _uiState.value.copy(monthOfYear = month.coerceIn(1, 12))
    }

    fun onAutoCreateChange(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoCreateTransaction = enabled)
    }

    fun onReminderDaysChange(days: Int) {
        _uiState.value = _uiState.value.copy(reminderDaysBefore = days.coerceIn(0, 30))
    }

    fun save() {
        val state = _uiState.value
        if (!state.isSaveEnabled) return
        viewModelScope.launch {
            if (state.isEditing) {
                existing?.let { item ->
                    recurringItemRepository.updateItem(
                        item = item,
                        name = state.name,
                        amount = state.amount,
                        accountId = state.selectedAccountId!!,
                        categoryId = state.selectedCategoryId!!,
                        frequency = state.frequency,
                        dayOfMonth = state.dayOfMonth,
                        monthOfYear = state.monthOfYear,
                        autoCreateTransaction = state.autoCreateTransaction,
                        reminderDaysBefore = state.reminderDaysBefore,
                        isActive = item.isActive
                    )
                }
            } else {
                recurringItemRepository.addItem(
                    name = state.name,
                    type = state.type,
                    amount = state.amount,
                    accountId = state.selectedAccountId!!,
                    categoryId = state.selectedCategoryId!!,
                    frequency = state.frequency,
                    dayOfMonth = state.dayOfMonth,
                    monthOfYear = state.monthOfYear,
                    autoCreateTransaction = state.autoCreateTransaction,
                    reminderDaysBefore = state.reminderDaysBefore
                )
            }
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
