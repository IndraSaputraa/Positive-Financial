package com.positivefinancial.app.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.model.AccountType
import com.positivefinancial.app.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AccountsUiState(
    val accounts: List<AccountEntity> = emptyList(),
    val totalBalance: Long = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    accountRepository: AccountRepository
) : ViewModel() {

    val uiState: StateFlow<AccountsUiState> = accountRepository.observeAccounts()
        .map { accounts ->
            val nonCardAccounts = accounts.filter { it.type != AccountType.CREDIT_CARD }
            AccountsUiState(
                accounts = nonCardAccounts,
                totalBalance = nonCardAccounts.sumOf { it.balance },
                isLoading = false
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AccountsUiState())
}
