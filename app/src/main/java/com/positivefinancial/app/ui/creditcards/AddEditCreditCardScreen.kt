package com.positivefinancial.app.ui.creditcards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.positivefinancial.app.ui.accounts.AccountColorPalette
import com.positivefinancial.app.ui.components.AppTimePickerDialog
import com.positivefinancial.app.ui.components.parseHexColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditCreditCardScreen(
    onDone: () -> Unit,
    viewModel: AddEditCreditCardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Credit Card" else "Add Credit Card") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Card name (e.g. BCA Everyday Card)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Network", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CardNetworks.forEach { network ->
                        FilterChip(
                            selected = state.cardNetwork == network,
                            onClick = { viewModel.onCardNetworkChange(network) },
                            label = { Text(network) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.lastFourDigits,
                onValueChange = viewModel::onLastFourChange,
                label = { Text("Last 4 digits (optional)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.creditLimitText,
                onValueChange = viewModel::onCreditLimitChange,
                label = { Text("Credit limit (IDR)") },
                prefix = { Text("Rp ") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (!state.isEditing) {
                OutlinedTextField(
                    value = state.outstandingBalanceText,
                    onValueChange = viewModel::onOutstandingChange,
                    label = { Text("Current outstanding balance (if any)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            DayStepper(
                label = "Billing cycle day (statement date)",
                value = state.billingCycleDay,
                onChange = viewModel::onBillingCycleDayChange
            )

            DayStepper(
                label = "Payment due day",
                value = state.paymentDueDay,
                onChange = viewModel::onPaymentDueDayChange
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccountColorPalette.forEach { hex ->
                        val color = parseHexColor(hex)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { viewModel.onColorSelect(hex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.colorHex == hex) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Payment reminder", fontWeight = FontWeight.SemiBold)
                        Switch(checked = state.reminderEnabled, onCheckedChange = viewModel::onReminderEnabledChange)
                    }
                    if (state.reminderEnabled) {
                        DayStepper(
                            label = "Reminder day of month",
                            value = state.reminderDayOfMonth,
                            onChange = viewModel::onReminderDayChange
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Reminder time")
                            TextButton(onClick = { showTimePicker = true }) {
                                Text("%02d:%02d".format(state.reminderHour, state.reminderMinute))
                            }
                        }
                    }
                }
            }

            Button(
                onClick = viewModel::save,
                enabled = state.isSaveEnabled,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (state.isEditing) "Save Changes" else "Add Card")
            }
        }
    }

    if (showTimePicker) {
        AppTimePickerDialog(
            initialHour = state.reminderHour,
            initialMinute = state.reminderMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                viewModel.onReminderTimeChange(hour, minute)
                showTimePicker = false
            }
        )
    }
}

@Composable
private fun DayStepper(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onChange(value - 1) }) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Decrease")
            }
            Text("$value", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = { onChange(value + 1) }) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Increase")
            }
        }
    }
}
