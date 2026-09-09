package com.positivefinancial.app.ui.creditcards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.local.dao.TransactionWithDetails
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.local.entity.CreditCardDetailsEntity
import com.positivefinancial.app.data.model.AccountType
import com.positivefinancial.app.data.repository.AccountRepository
import com.positivefinancial.app.data.repository.CreditCardRepository
import com.positivefinancial.app.data.repository.TransactionRepository
import com.positivefinancial.app.notification.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class CardDetailCore(
    val account: AccountEntity?,
    val details: CreditCardDetailsEntity?,
    val paymentHistory: List<TransactionWithDetails>
)

private data class CardDetailExtras(
    val recentActivity: List<TransactionWithDetails>,
    val sourceAccounts: List<AccountEntity>
)

data class CreditCardDetailUiState(
    val account: AccountEntity? = null,
    val details: CreditCardDetailsEntity? = null,
    val paymentHistory: List<TransactionWithDetails> = emptyList(),
    val recentActivity: List<TransactionWithDetails> = emptyList(),
    val availableSourceAccounts: List<AccountEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false
) {
    val outstanding: Long get() = account?.balance?.let { if (it < 0) -it else 0 } ?: 0
    val availableLimit: Long get() = ((details?.creditLimit ?: 0) - outstanding).coerceAtLeast(0)
    val utilization: Float get() {
        val limit = details?.creditLimit ?: 0
        return if (limit > 0) (outstanding.toFloat() / limit.toFloat()).coerceIn(0f, 1f) else 0f
    }
}

@HiltViewModel
class CreditCardDetailViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val creditCardRepository: CreditCardRepository,
    private val transactionRepository: TransactionRepository,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val accountId: Long = checkNotNull(savedStateHandle.get<Long>("accountId"))
    private val _isDeleted = MutableStateFlow(false)

    val uiState: StateFlow<CreditCardDetailUiState> = run {
        val core = combine(
            accountRepository.observeAccount(accountId),
            creditCardRepository.observeCardDetails(accountId),
            transactionRepository.observeCardPaymentHistory(accountId)
        ) { account, details, history -> CardDetailCore(account, details, history) }

        val extras = combine(
            transactionRepository.observeFiltered(accountId = accountId),
            accountRepository.observeAccounts()
        ) { activity, accounts -> CardDetailExtras(activity, accounts) }

        combine(core, extras, _isDeleted) { c, e, deleted ->
            CreditCardDetailUiState(
                account = c.account,
                details = c.details,
                paymentHistory = c.paymentHistory,
                recentActivity = e.recentActivity,
                availableSourceAccounts = e.sourceAccounts.filter {
                    it.id != accountId && it.type != AccountType.CREDIT_CARD
                },
                isLoading = false,
                isDeleted = deleted
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CreditCardDetailUiState())

    fun deleteCard() {
        viewModelScope.launch {
            uiState.value.account?.let { creditCardRepository.deleteCreditCard(it) }
            alarmScheduler.cancelCardReminder(accountId)
            _isDeleted.value = true
        }
    }

    fun recordPayment(fromAccountId: Long, amount: Long, date: Long) {
        viewModelScope.launch {
            transactionRepository.addTransfer(
                fromAccountId = fromAccountId,
                toAccountId = accountId,
                amount = amount,
                note = "Card payment",
                date = date
            )
        }
    }
}
