package com.positivefinancial.app.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.model.GoalType
import com.positivefinancial.app.ui.accounts.AccountColorPalette
import com.positivefinancial.app.ui.components.AppDatePickerDialog
import com.positivefinancial.app.ui.components.ConfirmDialog
import com.positivefinancial.app.ui.components.EmptyState
import com.positivefinancial.app.ui.components.IconBadge
import com.positivefinancial.app.ui.components.parseHexColor
import com.positivefinancial.app.ui.theme.ExpenseRed
import com.positivefinancial.app.ui.theme.IncomeGreen
import com.positivefinancial.app.util.Formatters

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GoalsScreen(
    onBack: () -> Unit,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddGoal by remember { mutableStateOf(false) }
    var goalToManage by remember { mutableStateOf<GoalItem?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddGoal = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add goal")
            }
        }
    ) { padding ->
        if (state.goals.isEmpty() && !state.isLoading) {
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                EmptyState(
                    "No goals yet",
                    "Create a savings goal or a debt payoff goal linked to a real account, and track progress automatically."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.goals, key = { it.goal.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { goalToManage = item }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconBadge(iconKey = item.goal.iconKey, colorHex = item.goal.colorHex)
                                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                                    Text(item.goal.name, fontWeight = FontWeight.Medium)
                                    Text(
                                        if (item.goal.goalType == GoalType.SAVINGS) "Savings goal" else "Debt payoff",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (item.isCompleted) {
                                    Icon(Icons.Filled.Check, contentDescription = "Complete", tint = IncomeGreen)
                                }
                            }
                            LinearProgressIndicator(
                                progress = { item.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (item.isCompleted) IncomeGreen else parseHexColor(item.goal.colorHex)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${Formatters.currency(item.currentAmount)} of ${Formatters.currency(item.goal.targetAmount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${(item.progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddGoal) {
        AddGoalDialog(
            availableSavingsAccounts = state.availableAccounts,
            availableCreditCards = state.availableCreditCards,
            onDismiss = { showAddGoal = false },
            onSave = { name, type, targetAmount, accountId, targetDate, iconKey, colorHex ->
                viewModel.addGoal(name, type, targetAmount, accountId, targetDate, iconKey, colorHex)
                showAddGoal = false
            }
        )
    }

    goalToManage?.let { item ->
        AlertDialog(
            onDismissRequest = { goalToManage = null },
            title = { Text(item.goal.name) },
            text = {
                Text(
                    "This goal is linked to ${item.linkedAccount?.name ?: "an account"}. " +
                        "Archiving hides it here without deleting its account or transactions."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.archiveGoal(item.goal)
                    goalToManage = null
                }) { Text("Archive") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { showDeleteConfirm = true }) { Text("Delete", color = ExpenseRed) }
                    TextButton(onClick = { goalToManage = null }) { Text("Cancel") }
                }
            }
        )
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete this goal?",
            message = "This only removes the goal — its linked account and transactions are untouched.",
            onConfirm = {
                goalToManage?.let { viewModel.deleteGoal(it.goal) }
                showDeleteConfirm = false
                goalToManage = null
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddGoalDialog(
    availableSavingsAccounts: List<AccountEntity>,
    availableCreditCards: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSave: (name: String, type: GoalType, targetAmount: Long, accountId: Long, targetDate: Long?, iconKey: String, colorHex: String) -> Unit
) {
    var goalType by remember { mutableStateOf(GoalType.SAVINGS) }
    var name by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var targetAmountText by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var colorHex by remember { mutableStateOf(AccountColorPalette.first()) }

    val accountOptions = if (goalType == GoalType.SAVINGS) availableSavingsAccounts else availableCreditCards

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Goal") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = goalType == GoalType.SAVINGS,
                        onClick = { goalType = GoalType.SAVINGS; selectedAccountId = null },
                        shape = SegmentedButtonDefaults.itemShape(0, 2)
                    ) { Text("Savings") }
                    SegmentedButton(
                        selected = goalType == GoalType.DEBT_PAYOFF,
                        onClick = { goalType = GoalType.DEBT_PAYOFF; selectedAccountId = null },
                        shape = SegmentedButtonDefaults.itemShape(1, 2)
                    ) { Text("Debt Payoff") }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (goalType == GoalType.SAVINGS) "Linked account" else "Linked credit card",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (accountOptions.isEmpty()) {
                        Text(
                            if (goalType == GoalType.SAVINGS) "Add a cash/bank/e-wallet/investment account first."
                            else "Add a credit card first.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            accountOptions.forEach { account ->
                                FilterChip(
                                    selected = selectedAccountId == account.id,
                                    onClick = {
                                        selectedAccountId = account.id
                                        if (goalType == GoalType.DEBT_PAYOFF && targetAmountText.isBlank()) {
                                            val outstanding = if (account.balance < 0) -account.balance else 0
                                            targetAmountText = outstanding.toString()
                                        }
                                    },
                                    label = { Text(account.name) }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it.filter { c -> c.isDigit() }.take(15) },
                    label = { Text(if (goalType == GoalType.SAVINGS) "Target amount (IDR)" else "Starting debt (IDR)") },
                    prefix = { Text("Rp ") },
                    visualTransformation = com.positivefinancial.app.ui.components.ThousandsSeparatorVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Target date (optional)", style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(targetDate?.let { Formatters.date(it) } ?: "Set date")
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AccountColorPalette.take(6).forEach { hex ->
                            val color = parseHexColor(hex)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { colorHex = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (colorHex == hex) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val amount = targetAmountText.toLongOrNull() ?: 0
            TextButton(
                onClick = {
                    onSave(
                        name,
                        goalType,
                        amount,
                        selectedAccountId!!,
                        targetDate,
                        if (goalType == GoalType.SAVINGS) "savings" else "credit_card",
                        colorHex
                    )
                },
                enabled = name.isNotBlank() && amount > 0 && selectedAccountId != null
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDateMillis = targetDate ?: com.positivefinancial.app.util.DateRanges.now(),
            onDismiss = { showDatePicker = false },
            onConfirm = { millis ->
                targetDate = millis
                showDatePicker = false
            }
        )
    }
}
