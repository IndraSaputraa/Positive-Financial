package com.positivefinancial.app.ui.recurring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.positivefinancial.app.data.model.RecurringFrequency
import com.positivefinancial.app.data.model.TransactionType
import com.positivefinancial.app.ui.components.IconBadge
import com.positivefinancial.app.ui.components.NumberStepper
import com.positivefinancial.app.ui.components.ThousandsSeparatorVisualTransformation
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditRecurringScreen(
    onDone: () -> Unit,
    viewModel: AddEditRecurringViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Recurring Item" else "Add Recurring Item") },
                navigationIcon = {
                    IconButton(onClick = onDone) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Name (e.g. Rent, Netflix, Vehicle Tax)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = state.type == TransactionType.EXPENSE,
                    onClick = { viewModel.onTypeChange(TransactionType.EXPENSE) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) { Text("Expense") }
                SegmentedButton(
                    selected = state.type == TransactionType.INCOME,
                    onClick = { viewModel.onTypeChange(TransactionType.INCOME) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) { Text("Income") }
            }

            OutlinedTextField(
                value = state.amountText,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Amount (IDR)") },
                prefix = { Text("Rp ") },
                visualTransformation = ThousandsSeparatorVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Account", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.accounts.forEach { account ->
                        FilterChip(
                            selected = state.selectedAccountId == account.id,
                            onClick = { viewModel.onAccountSelect(account.id) },
                            label = { Text(account.name) },
                            leadingIcon = { IconBadge(iconKey = account.iconKey, colorHex = account.colorHex, size = 20.dp) }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.categories.forEach { category ->
                        FilterChip(
                            selected = state.selectedCategoryId == category.id,
                            onClick = { viewModel.onCategorySelect(category.id) },
                            label = { Text(category.name) },
                            leadingIcon = { IconBadge(iconKey = category.iconKey, colorHex = category.colorHex, size = 20.dp) }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Repeats", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = state.frequency == RecurringFrequency.MONTHLY,
                        onClick = { viewModel.onFrequencyChange(RecurringFrequency.MONTHLY) },
                        shape = SegmentedButtonDefaults.itemShape(0, 2)
                    ) { Text("Monthly") }
                    SegmentedButton(
                        selected = state.frequency == RecurringFrequency.YEARLY,
                        onClick = { viewModel.onFrequencyChange(RecurringFrequency.YEARLY) },
                        shape = SegmentedButtonDefaults.itemShape(1, 2)
                    ) { Text("Yearly") }
                }
            }

            if (state.frequency == RecurringFrequency.YEARLY) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        Month.of(state.monthOfYear).getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    NumberStepper(
                        label = "Month",
                        value = state.monthOfYear,
                        range = 1..12,
                        onChange = viewModel::onMonthOfYearChange
                    )
                }
            }

            NumberStepper(
                label = "Day of month",
                value = state.dayOfMonth,
                range = 1..28,
                onChange = viewModel::onDayOfMonthChange
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Add automatically", fontWeight = FontWeight.Medium)
                            Text(
                                if (state.autoCreateTransaction) "The transaction is created for you on the due date."
                                else "You'll just get a reminder — log the transaction yourself when you pay.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = state.autoCreateTransaction, onCheckedChange = viewModel::onAutoCreateChange)
                    }
                    NumberStepper(
                        label = "Remind me this many days before",
                        value = state.reminderDaysBefore,
                        range = 0..30,
                        onChange = viewModel::onReminderDaysChange
                    )
                }
            }

            Button(
                onClick = viewModel::save,
                enabled = state.isSaveEnabled,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (state.isEditing) "Save Changes" else "Add")
            }
        }
    }
}
