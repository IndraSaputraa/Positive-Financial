package com.positivefinancial.app.ui.creditcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.local.entity.CreditCardDetailsEntity
import com.positivefinancial.app.data.repository.CreditCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CardListItem(
    val account: AccountEntity,
    val details: CreditCardDetailsEntity
) {
    val outstanding: Long get() = if (account.balance < 0) -account.balance else 0
    val availableLimit: Long get() = (details.creditLimit - outstanding).coerceAtLeast(0)
    val utilization: Float get() =
        if (details.creditLimit > 0) (outstanding.toFloat() / details.creditLimit.toFloat()).coerceIn(0f, 1f) else 0f
}

data class CreditCardsUiState(
    val cards: List<CardListItem> = emptyList(),
    val totalOutstanding: Long = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class CreditCardsViewModel @Inject constructor(
    creditCardRepository: CreditCardRepository
) : ViewModel() {

    val uiState: StateFlow<CreditCardsUiState> = combine(
        creditCardRepository.observeCardAccounts(),
        creditCardRepository.observeAllCardDetails()
    ) { accounts, details ->
        val detailsByAccountId = details.associateBy { it.accountId }
        val items = accounts.mapNotNull { account ->
            detailsByAccountId[account.id]?.let { CardListItem(account, it) }
        }
        CreditCardsUiState(
            cards = items,
            totalOutstanding = items.sumOf { it.outstanding },
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CreditCardsUiState())
}
