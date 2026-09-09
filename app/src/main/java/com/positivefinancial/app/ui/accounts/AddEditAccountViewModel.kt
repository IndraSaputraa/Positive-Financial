package com.positivefinancial.app.ui.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.model.AccountType
import com.positivefinancial.app.data.repository.AccountRepository
import com.positivefinancial.app.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

val AccountColorPalette = listOf(
    "#16A34A", "#2563EB", "#EAB308", "#22C55E", "#7C3AED",
    "#F97316", "#EC4899", "#64748B", "#0EA5E9", "#DC2626"
)

fun iconKeyForAccountType(type: AccountType): String = when (type) {
    AccountType.CASH -> "payments"
    AccountType.BANK -> "account_balance"
    AccountType.E_WALLET -> "account_balance_wallet"
    AccountType.CREDIT_CARD -> "credit_card"
}

data class AddEditAccountUiState(
    val isEditing: Boolean = false,
    val name: String = "",
    val type: AccountType = AccountType.CASH,
    val openingBalanceText: String = "0",
    val currentBalance: Long = 0,
    val colorHex: String = AccountColorPalette.first(),
    val isLoading: Boolean = true,
    val isSaved: Boolean = false
) {
    val isSaveEnabled: Boolean get() = name.isNotBlank()
}

@HiltViewModel
class AddEditAccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val accountId: Long = savedStateHandle.get<Long>("accountId") ?: Screen.NEW_ID

    private val _uiState = MutableStateFlow(AddEditAccountUiState(isEditing = accountId != Screen.NEW_ID))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (accountId != Screen.NEW_ID) {
                accountRepository.getAccount(accountId)?.let { account ->
                    _uiState.value = _uiState.value.copy(
                        name = account.name,
                        type = account.type,
                        currentBalance = account.balance,
                        colorHex = account.colorHex,
                        isLoading = false
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onTypeChange(type: AccountType) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    fun onOpeningBalanceChange(raw: String) {
        _uiState.value = _uiState.value.copy(openingBalanceText = raw.filter { it.isDigit() }.take(15))
    }

    fun onColorSelect(colorHex: String) {
        _uiState.value = _uiState.value.copy(colorHex = colorHex)
    }

    fun save() {
        val state = _uiState.value
        if (!state.isSaveEnabled) return
        viewModelScope.launch {
            if (state.isEditing) {
                accountRepository.getAccount(accountId)?.let { existing ->
                    accountRepository.updateAccount(existing.copy(name = state.name, colorHex = state.colorHex))
                }
            } else {
                accountRepository.addAccount(
                    name = state.name,
                    type = state.type,
                    iconKey = iconKeyForAccountType(state.type),
                    colorHex = state.colorHex,
                    openingBalance = state.openingBalanceText.toLongOrNull() ?: 0
                )
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
