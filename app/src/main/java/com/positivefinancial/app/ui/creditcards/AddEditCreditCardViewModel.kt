package com.positivefinancial.app.ui.creditcards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.repository.AccountRepository
import com.positivefinancial.app.data.repository.CreditCardInput
import com.positivefinancial.app.data.repository.CreditCardRepository
import com.positivefinancial.app.notification.AlarmScheduler
import com.positivefinancial.app.ui.accounts.AccountColorPalette
import com.positivefinancial.app.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

val CardNetworks = listOf("Visa", "Mastercard", "JCB", "Amex", "Other")

data class AddEditCreditCardUiState(
    val isEditing: Boolean = false,
    val name: String = "",
    val colorHex: String = AccountColorPalette[1],
    val creditLimitText: String = "",
    val outstandingBalanceText: String = "0",
    val cardNetwork: String = "Visa",
    val lastFourDigits: String = "",
    val billingCycleDay: Int = 1,
    val paymentDueDay: Int = 20,
    val reminderEnabled: Boolean = true,
    val reminderDayOfMonth: Int = 15,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false
) {
    val isSaveEnabled: Boolean get() = name.isNotBlank() && (creditLimitText.toLongOrNull() ?: 0) > 0
}

@HiltViewModel
class AddEditCreditCardViewModel @Inject constructor(
    private val creditCardRepository: CreditCardRepository,
    private val accountRepository: AccountRepository,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val accountId: Long = savedStateHandle.get<Long>("accountId") ?: Screen.NEW_ID

    private val _uiState = MutableStateFlow(AddEditCreditCardUiState(isEditing = accountId != Screen.NEW_ID))
    val uiState = _uiState.asStateFlow()

    init {
        if (accountId != Screen.NEW_ID) {
            viewModelScope.launch {
                val account = accountRepository.getAccount(accountId)
                val details = creditCardRepository.getCardDetails(accountId)
                if (account != null && details != null) {
                    _uiState.value = _uiState.value.copy(
                        name = account.name,
                        colorHex = account.colorHex,
                        creditLimitText = details.creditLimit.toString(),
                        outstandingBalanceText = (if (account.balance < 0) -account.balance else 0).toString(),
                        cardNetwork = details.cardNetwork,
                        lastFourDigits = details.lastFourDigits,
                        billingCycleDay = details.billingCycleDay,
                        paymentDueDay = details.paymentDueDay,
                        reminderEnabled = details.reminderEnabled,
                        reminderDayOfMonth = details.reminderDayOfMonth,
                        reminderHour = details.reminderHour,
                        reminderMinute = details.reminderMinute,
                        isLoading = false
                    )
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(name = v) }
    fun onColorSelect(v: String) { _uiState.value = _uiState.value.copy(colorHex = v) }
    fun onCreditLimitChange(v: String) { _uiState.value = _uiState.value.copy(creditLimitText = v.filter { it.isDigit() }.take(15)) }
    fun onOutstandingChange(v: String) { _uiState.value = _uiState.value.copy(outstandingBalanceText = v.filter { it.isDigit() }.take(15)) }
    fun onCardNetworkChange(v: String) { _uiState.value = _uiState.value.copy(cardNetwork = v) }
    fun onLastFourChange(v: String) { _uiState.value = _uiState.value.copy(lastFourDigits = v.filter { it.isDigit() }.take(4)) }
    fun onBillingCycleDayChange(v: Int) { _uiState.value = _uiState.value.copy(billingCycleDay = v.coerceIn(1, 28)) }
    fun onPaymentDueDayChange(v: Int) { _uiState.value = _uiState.value.copy(paymentDueDay = v.coerceIn(1, 28)) }
    fun onReminderEnabledChange(v: Boolean) { _uiState.value = _uiState.value.copy(reminderEnabled = v) }
    fun onReminderDayChange(v: Int) { _uiState.value = _uiState.value.copy(reminderDayOfMonth = v.coerceIn(1, 28)) }
    fun onReminderTimeChange(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(reminderHour = hour, reminderMinute = minute)
    }

    fun save() {
        val state = _uiState.value
        if (!state.isSaveEnabled) return
        viewModelScope.launch {
            val input = CreditCardInput(
                name = state.name,
                colorHex = state.colorHex,
                creditLimit = state.creditLimitText.toLongOrNull() ?: 0,
                outstandingBalance = state.outstandingBalanceText.toLongOrNull() ?: 0,
                cardNetwork = state.cardNetwork,
                lastFourDigits = state.lastFourDigits,
                billingCycleDay = state.billingCycleDay,
                paymentDueDay = state.paymentDueDay,
                reminderEnabled = state.reminderEnabled,
                reminderDayOfMonth = state.reminderDayOfMonth,
                reminderHour = state.reminderHour,
                reminderMinute = state.reminderMinute
            )

            val resolvedId = if (state.isEditing) {
                creditCardRepository.updateCreditCard(accountId, input)
                accountId
            } else {
                creditCardRepository.addCreditCard(input)
            }

            if (state.reminderEnabled) {
                alarmScheduler.scheduleCardReminder(
                    resolvedId, state.name, state.reminderDayOfMonth, state.reminderHour, state.reminderMinute
                )
            } else {
                alarmScheduler.cancelCardReminder(resolvedId)
            }

            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
