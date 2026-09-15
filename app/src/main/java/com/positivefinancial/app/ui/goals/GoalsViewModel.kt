package com.positivefinancial.app.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.local.entity.GoalEntity
import com.positivefinancial.app.data.model.AccountType
import com.positivefinancial.app.data.model.GoalType
import com.positivefinancial.app.data.repository.AccountRepository
import com.positivefinancial.app.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalItem(
    val goal: GoalEntity,
    val linkedAccount: AccountEntity?
) {
    private val outstanding: Long get() = linkedAccount?.balance?.let { if (it < 0) -it else 0 } ?: 0

    val currentAmount: Long
        get() = when (goal.goalType) {
            GoalType.SAVINGS -> (linkedAccount?.balance ?: 0).coerceAtLeast(0)
            GoalType.DEBT_PAYOFF -> (goal.targetAmount - outstanding).coerceIn(0, goal.targetAmount)
        }

    val progress: Float
        get() = if (goal.targetAmount > 0) (currentAmount.toFloat() / goal.targetAmount.toFloat()).coerceIn(0f, 1f) else 0f

    val isCompleted: Boolean get() = progress >= 1f
}

data class GoalsUiState(
    val goals: List<GoalItem> = emptyList(),
    val availableAccounts: List<AccountEntity> = emptyList(),
    val availableCreditCards: List<AccountEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    accountRepository: AccountRepository
) : ViewModel() {

    val uiState: StateFlow<GoalsUiState> = combine(
        goalRepository.observeActive(),
        accountRepository.observeAccounts()
    ) { goals, accounts ->
        val accountsById = accounts.associateBy { it.id }
        GoalsUiState(
            goals = goals.map { GoalItem(it, accountsById[it.linkedAccountId]) },
            availableAccounts = accounts.filter { it.type != AccountType.CREDIT_CARD },
            availableCreditCards = accounts.filter { it.type == AccountType.CREDIT_CARD },
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalsUiState())

    fun addGoal(
        name: String,
        goalType: GoalType,
        targetAmount: Long,
        linkedAccountId: Long,
        targetDate: Long?,
        iconKey: String,
        colorHex: String
    ) {
        viewModelScope.launch {
            goalRepository.addGoal(name, goalType, targetAmount, linkedAccountId, targetDate, iconKey, colorHex)
        }
    }

    fun archiveGoal(goal: GoalEntity) {
        viewModelScope.launch { goalRepository.updateGoal(goal.copy(isArchived = true)) }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch { goalRepository.deleteGoal(goal) }
    }
}
