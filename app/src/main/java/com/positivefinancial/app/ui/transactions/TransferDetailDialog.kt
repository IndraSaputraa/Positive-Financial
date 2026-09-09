package com.positivefinancial.app.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.positivefinancial.app.data.local.dao.TransactionWithDetails
import com.positivefinancial.app.ui.components.ConfirmDialog
import com.positivefinancial.app.util.Formatters

@Composable
fun TransferDetailDialog(
    transaction: TransactionWithDetails,
    onDismiss: () -> Unit,
    viewModel: TransferActionViewModel = hiltViewModel()
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transfer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("From", transaction.fromAccountName ?: "-")
                DetailRow("To", transaction.toAccountName ?: "-")
                DetailRow("Amount", Formatters.currency(transaction.amount))
                DetailRow("Date", Formatters.date(transaction.date))
                if (transaction.note.isNotBlank()) DetailRow("Note", transaction.note)
                HorizontalDivider()
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        dismissButton = {
            TextButton(onClick = { showDeleteConfirm = true }) { Text("Delete") }
        }
    )

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete transfer?",
            message = "This will reverse the amount moved between the two accounts.",
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deleteTransfer(transaction.id, onDismiss)
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value)
    }
}
