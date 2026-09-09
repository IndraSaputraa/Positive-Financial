package com.positivefinancial.app.ui.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.local.dao.TransactionWithDetails
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.repository.AccountRepository
import com.positivefinancial.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountDetailUiState(
    val account: AccountEntity? = null,
    val transactions: List<TransactionWithDetails> = emptyList(),
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false
)

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    transactionRepository: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val accountId: Long = checkNotNull(savedStateHandle.get<Long>("accountId"))

    private val _isDeleted = MutableStateFlow(false)

    val uiState: StateFlow<AccountDetailUiState> = combine(
        accountRepository.observeAccount(accountId),
        transactionRepository.observeFiltered(accountId = accountId),
        _isDeleted
    ) { account, transactions, deleted ->
        AccountDetailUiState(account = account, transactions = transactions, isLoading = false, isDeleted = deleted)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AccountDetailUiState())

    fun deleteAccount() {
        viewModelScope.launch {
            uiState.value.account?.let { accountRepository.deleteAccount(it) }
            _isDeleted.value = true
        }
    }
}
