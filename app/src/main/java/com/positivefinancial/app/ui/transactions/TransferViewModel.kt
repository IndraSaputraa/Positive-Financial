package com.positivefinancial.app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.repository.AccountRepository
import com.positivefinancial.app.data.repository.TransactionRepository
import com.positivefinancial.app.util.DateRanges
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransferUiState(
    val accounts: List<AccountEntity> = emptyList(),
    val fromAccountId: Long? = null,
    val toAccountId: Long? = null,
    val amountText: String = "",
    val note: String = "",
    val date: Long = DateRanges.now(),
    val isSaved: Boolean = false
) {
    val amount: Long get() = amountText.toLongOrNull() ?: 0L
    val isSaveEnabled: Boolean get() =
        amount > 0 && fromAccountId != null && toAccountId != null && fromAccountId != toAccountId
}

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val accounts = accountRepository.observeAccounts().first()
            _uiState.value = _uiState.value.copy(
                accounts = accounts,
                fromAccountId = accounts.getOrNull(0)?.id,
                toAccountId = accounts.getOrNull(1)?.id
            )
        }
    }

    fun onFromSelect(id: Long) {
        _uiState.value = _uiState.value.copy(fromAccountId = id)
    }

    fun onToSelect(id: Long) {
        _uiState.value = _uiState.value.copy(toAccountId = id)
    }

    fun onAmountChange(raw: String) {
        _uiState.value = _uiState.value.copy(amountText = raw.filter { it.isDigit() }.take(15))
    }

    fun onNoteChange(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun save() {
        val state = _uiState.value
        if (!state.isSaveEnabled) return
        viewModelScope.launch {
            transactionRepository.addTransfer(
                fromAccountId = state.fromAccountId!!,
                toAccountId = state.toAccountId!!,
                amount = state.amount,
                note = state.note.ifBlank { "Transfer" },
                date = state.date
            )
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
