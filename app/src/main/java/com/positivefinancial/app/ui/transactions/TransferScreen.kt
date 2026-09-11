package com.positivefinancial.app.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.positivefinancial.app.ui.components.EmptyState
import com.positivefinancial.app.ui.components.IconBadge
import com.positivefinancial.app.util.Formatters

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransferScreen(
    onDone: () -> Unit,
    viewModel: TransferViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer Between Accounts") },
                navigationIcon = {
                    IconButton(onClick = onDone) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        if (state.accounts.size < 2) {
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                EmptyState("Need at least two accounts", "Add another account before making a transfer.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("From", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.accounts.forEach { account ->
                        FilterChip(
                            selected = state.fromAccountId == account.id,
                            onClick = { viewModel.onFromSelect(account.id) },
                            label = { Text("${account.name} (${Formatters.currency(account.balance)})") },
                            leadingIcon = { IconBadge(iconKey = account.iconKey, colorHex = account.colorHex, size = 20.dp) }
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.SwapVert, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("To", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.accounts.filter { it.id != state.fromAccountId }.forEach { account ->
                        FilterChip(
                            selected = state.toAccountId == account.id,
                            onClick = { viewModel.onToSelect(account.id) },
                            label = { Text("${account.name} (${Formatters.currency(account.balance)})") },
                            leadingIcon = { IconBadge(iconKey = account.iconKey, colorHex = account.colorHex, size = 20.dp) }
                        )
                    }
                }
            }

            Column {
                OutlinedTextField(
                    value = state.amountText,
                    onValueChange = viewModel::onAmountChange,
                    label = { Text("Amount (IDR)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (state.amount > 0) {
                    Text(
                        text = Formatters.currency(state.amount),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }

            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::onNoteChange,
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = viewModel::save,
                enabled = state.isSaveEnabled,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Transfer")
            }
        }
    }
}
