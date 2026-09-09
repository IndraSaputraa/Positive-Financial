package com.positivefinancial.app.ui.creditcards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.ui.components.EmptyState
import com.positivefinancial.app.ui.components.IconBadge
import com.positivefinancial.app.ui.components.SectionHeader
import com.positivefinancial.app.ui.components.TransactionRow
import com.positivefinancial.app.ui.theme.ExpenseRed
import com.positivefinancial.app.ui.theme.IncomeGreen
import com.positivefinancial.app.util.DateRanges
import com.positivefinancial.app.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardDetailScreen(
    onEdit: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: CreditCardDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRecordPayment by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onBack()
    }

    val account = state.account
    val details = state.details

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(account?.name ?: "Credit Card") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (account != null) {
                        IconButton(onClick = { onEdit(account.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (account == null || details == null) {
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    IconBadge(iconKey = account.iconKey, colorHex = account.colorHex, size = 56.dp)
                    Text(
                        "${details.cardNetwork}${if (details.lastFourDigits.isNotBlank()) " •••• ${details.lastFourDigits}" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Outstanding", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(Formatters.currency(state.outstanding), fontWeight = FontWeight.Bold, color = ExpenseRed, style = MaterialTheme.typography.titleLarge)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Available", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(Formatters.currency(state.availableLimit), fontWeight = FontWeight.Bold, color = IncomeGreen, style = MaterialTheme.typography.titleLarge)
                            }
                        }
                        LinearProgressIndicator(
                            progress = { state.utilization },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = if (state.utilization > 0.8f) ExpenseRed else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Credit limit: ${Formatters.currency(details.creditLimit)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        InfoRow("Billing cycle day", "Day ${details.billingCycleDay} of each month")
                        InfoRow("Payment due", "Day ${details.paymentDueDay} • next ${Formatters.localDate(DateRanges.nextMonthlyDate(details.paymentDueDay))}")
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                if (details.reminderEnabled) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsOff,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (details.reminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                if (details.reminderEnabled)
                                    "Reminder on day ${details.reminderDayOfMonth} at %02d:%02d".format(details.reminderHour, details.reminderMinute)
                                else "Payment reminder is off",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Button(onClick = { showRecordPayment = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Record a Payment")
                }
            }

            item { SectionHeader(title = "Payment History") }
            if (state.paymentHistory.isEmpty()) {
                item { EmptyState("No payments yet", "Payments you record toward this card will appear here.") }
            } else {
                items(state.paymentHistory, key = { "pay_${it.id}" }) { tx ->
                    TransactionRow(transaction = tx)
                    HorizontalDivider()
                }
            }

            item { SectionHeader(title = "Recent Card Activity") }
            if (state.recentActivity.isEmpty()) {
                item { EmptyState("No activity yet", "Purchases and payments on this card will show up here.") }
            } else {
                items(state.recentActivity, key = { "activity_${it.id}" }) { tx ->
                    TransactionRow(transaction = tx)
                    HorizontalDivider()
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this card?") },
            text = { Text("This will permanently delete the card and all its transactions.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteCard()
                }) { Text("Delete", color = ExpenseRed) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }

    if (showRecordPayment) {
        RecordPaymentDialog(
            sourceAccounts = state.availableSourceAccounts,
            onDismiss = { showRecordPayment = false },
            onConfirm = { fromId, amount ->
                viewModel.recordPayment(fromId, amount, DateRanges.now())
                showRecordPayment = false
            }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RecordPaymentDialog(
    sourceAccounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    var selectedAccountId by remember { mutableStateOf(sourceAccounts.firstOrNull()?.id) }
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (sourceAccounts.isEmpty()) {
                    Text("Add another account first to pay from.")
                } else {
                    Text("Pay from", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        sourceAccounts.forEach { acc ->
                            FilterChip(
                                selected = selectedAccountId == acc.id,
                                onClick = { selectedAccountId = acc.id },
                                label = { Text(acc.name) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { c -> c.isDigit() }.take(15) },
                        label = { Text("Amount (IDR)") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    val fromId = selectedAccountId
                    if (amount > 0 && fromId != null) onConfirm(fromId, amount)
                },
                enabled = sourceAccounts.isNotEmpty() && (amountText.toLongOrNull() ?: 0) > 0 && selectedAccountId != null
            ) { Text("Pay") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
