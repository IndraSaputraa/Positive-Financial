package com.positivefinancial.app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferActionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    fun deleteTransfer(id: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            transactionRepository.getById(id)?.let { transactionRepository.deleteTransaction(it) }
            onDone()
        }
    }
}
