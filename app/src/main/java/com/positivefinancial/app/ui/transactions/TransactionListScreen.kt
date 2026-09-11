package com.positivefinancial.app.ui.transactions

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.positivefinancial.app.data.local.dao.TransactionWithDetails
import com.positivefinancial.app.data.model.TransactionType
import com.positivefinancial.app.ui.components.EmptyState
import com.positivefinancial.app.ui.components.MonthSelector
import com.positivefinancial.app.ui.components.TransactionRow
import com.positivefinancial.app.util.Formatters
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onTransactionClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    viewModel: TransactionListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val grouped = state.transactions.groupBy { Formatters.dayLabel(it.date) }
    var transferToView by remember { mutableStateOf<TransactionWithDetails?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Activity") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add transaction")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val month = state.selectedMonth
                    if (month != null) {
                        MonthSelector(
                            month = month,
                            onPrevious = viewModel::onPreviousMonth,
                            onNext = viewModel::onNextMonth,
                            nextEnabled = month != YearMonth.now(),
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            text = "All time",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    TextButton(onClick = viewModel::onToggleAllTime) {
                        Text(if (state.selectedMonth == null) "By month" else "All time")
                    }
                }

                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = { Text("Search by note, account, or category") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    FilterChip(
                        selected = state.typeFilter == null,
                        onClick = { viewModel.onTypeFilterChange(null) },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = state.typeFilter == TransactionType.INCOME,
                        onClick = { viewModel.onTypeFilterChange(TransactionType.INCOME) },
                        label = { Text("Income") }
                    )
                    FilterChip(
                        selected = state.typeFilter == TransactionType.EXPENSE,
                        onClick = { viewModel.onTypeFilterChange(TransactionType.EXPENSE) },
                        label = { Text("Expense") }
                    )
                    FilterChip(
                        selected = state.typeFilter == TransactionType.TRANSFER,
                        onClick = { viewModel.onTypeFilterChange(TransactionType.TRANSFER) },
                        label = { Text("Transfer") }
                    )
                }
            }

            if (state.transactions.isEmpty() && !state.isLoading) {
                EmptyState("No transactions found", "Try a different search or filter, or add a new transaction.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    grouped.forEach { (day, transactions) ->
                        item(key = "header_$day") {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(transactions, key = { it.id }) { tx ->
                            TransactionRow(
                                transaction = tx,
                                onClick = {
                                    if (tx.type == TransactionType.TRANSFER) transferToView = tx
                                    else onTransactionClick(tx.id)
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    transferToView?.let { tx ->
        TransferDetailDialog(transaction = tx, onDismiss = { transferToView = null })
    }
}
