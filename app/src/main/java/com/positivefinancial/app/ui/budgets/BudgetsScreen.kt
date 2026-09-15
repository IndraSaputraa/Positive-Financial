package com.positivefinancial.app.ui.budgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.positivefinancial.app.ui.components.IconBadge
import com.positivefinancial.app.ui.theme.ExpenseRed
import com.positivefinancial.app.ui.theme.IncomeGreen
import com.positivefinancial.app.ui.theme.WarningAmber
import com.positivefinancial.app.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    onBack: () -> Unit,
    viewModel: BudgetsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editingItem by remember { mutableStateOf<BudgetItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgets") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Set a monthly limit per category. You'll get a heads-up here and as a notification when you go over.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(state.items, key = { it.category.id }) { budgetItem ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editingItem = budgetItem }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            IconBadge(iconKey = budgetItem.category.iconKey, colorHex = budgetItem.category.colorHex, size = 36.dp)
                            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(budgetItem.category.name, fontWeight = FontWeight.Medium)
                                Text(
                                    if (budgetItem.hasBudget) {
                                        "${Formatters.currency(budgetItem.spentThisMonth)} of ${Formatters.currency(budgetItem.budget!!.monthlyLimit)}"
                                    } else {
                                        "No budget set"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (budgetItem.isOverBudget) {
                                Text("Over", color = ExpenseRed, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        if (budgetItem.hasBudget) {
                            LinearProgressIndicator(
                                progress = { budgetItem.progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = when {
                                    budgetItem.isOverBudget -> ExpenseRed
                                    budgetItem.progress > 0.8f -> WarningAmber
                                    else -> IncomeGreen
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    editingItem?.let { item ->
        SetBudgetDialog(
            item = item,
            onDismiss = { editingItem = null },
            onSave = { limit ->
                viewModel.setBudget(item.category.id, limit)
                editingItem = null
            },
            onRemove = item.budget?.let { budget ->
                {
                    viewModel.removeBudget(budget)
                    editingItem = null
                }
            }
        )
    }
}

@Composable
private fun SetBudgetDialog(
    item: BudgetItem,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit,
    onRemove: (() -> Unit)?
) {
    var limitText by remember { mutableStateOf(item.budget?.monthlyLimit?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${item.category.name} Budget") },
        text = {
            OutlinedTextField(
                value = limitText,
                onValueChange = { limitText = it.filter { c -> c.isDigit() }.take(15) },
                label = { Text("Monthly limit (IDR)") },
                prefix = { Text("Rp ") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { limitText.toLongOrNull()?.let { if (it > 0) onSave(it) } },
                enabled = (limitText.toLongOrNull() ?: 0) > 0
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (onRemove != null) {
                    TextButton(onClick = onRemove) { Text("Remove", color = ExpenseRed) }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
