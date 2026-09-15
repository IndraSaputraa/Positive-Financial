package com.positivefinancial.app.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.positivefinancial.app.data.local.entity.AccountEntity
import com.positivefinancial.app.data.local.entity.CategoryEntity
import com.positivefinancial.app.ui.components.IconBadge

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionFilterDialog(
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    selectedAccountId: Long?,
    selectedCategoryId: Long?,
    onAccountSelect: (Long?) -> Unit,
    onCategorySelect: (Long?) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Activity") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Account", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedAccountId == null,
                            onClick = { onAccountSelect(null) },
                            label = { Text("All") }
                        )
                        accounts.forEach { account ->
                            FilterChip(
                                selected = selectedAccountId == account.id,
                                onClick = { onAccountSelect(account.id) },
                                label = { Text(account.name) },
                                leadingIcon = { IconBadge(iconKey = account.iconKey, colorHex = account.colorHex, size = 18.dp) }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedCategoryId == null,
                            onClick = { onCategorySelect(null) },
                            label = { Text("All") }
                        )
                        categories.forEach { category ->
                            FilterChip(
                                selected = selectedCategoryId == category.id,
                                onClick = { onCategorySelect(category.id) },
                                label = { Text(category.name) },
                                leadingIcon = { IconBadge(iconKey = category.iconKey, colorHex = category.colorHex, size = 18.dp) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = onClear) { Text("Clear") }
        }
    )
}
