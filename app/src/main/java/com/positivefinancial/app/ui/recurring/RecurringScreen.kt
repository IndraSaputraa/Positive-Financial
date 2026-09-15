package com.positivefinancial.app.ui.recurring

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.positivefinancial.app.data.local.entity.RecurringItemEntity
import com.positivefinancial.app.data.model.RecurringFrequency
import com.positivefinancial.app.ui.components.ConfirmDialog
import com.positivefinancial.app.ui.components.EmptyState
import com.positivefinancial.app.ui.components.IconBadge
import com.positivefinancial.app.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    onAddItem: () -> Unit,
    onEditItem: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: RecurringViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var itemToDelete by remember { mutableStateOf<RecurringItemEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring & Bills") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddItem) {
                Icon(Icons.Filled.Add, contentDescription = "Add recurring item")
            }
        }
    ) { padding ->
        if (state.items.isEmpty() && !state.isLoading) {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                EmptyState(
                    "No recurring items yet",
                    "Add rent, salary, or subscriptions to auto-add them each month, or set a reminder for annual bills like vehicle tax or PBB."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.item.id }) { display ->
                    val item = display.item
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditItem(item.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconBadge(
                                    iconKey = if (item.autoCreateTransaction) "receipt_long" else "calendar_month",
                                    colorHex = if (item.isActive) "#0F766E" else "#94A3B8"
                                )
                                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                                    Text(item.name, fontWeight = FontWeight.Medium)
                                    Text(
                                        "${Formatters.currency(item.amount)} • ${scheduleLabel(item)} • ${display.accountName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = item.isActive,
                                    onCheckedChange = { viewModel.setActive(item, it) }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    (if (item.autoCreateTransaction) "Auto-adds on " else "Reminds on ") +
                                        Formatters.date(item.nextDueDate),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(onClick = { itemToDelete = item }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    itemToDelete?.let { item ->
        ConfirmDialog(
            title = "Delete \"${item.name}\"?",
            message = "This stops future reminders or auto-added transactions. Past transactions it already created are not affected.",
            onConfirm = {
                viewModel.deleteItem(item)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }
}

private fun scheduleLabel(item: RecurringItemEntity): String = when (item.frequency) {
    RecurringFrequency.MONTHLY -> "Monthly on day ${item.dayOfMonth}"
    RecurringFrequency.YEARLY -> "Yearly"
}
