package com.positivefinancial.app.ui.creditcards

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.positivefinancial.app.ui.components.EmptyState
import com.positivefinancial.app.ui.components.IconBadge
import com.positivefinancial.app.ui.theme.ExpenseRed
import com.positivefinancial.app.ui.theme.IncomeGreen
import com.positivefinancial.app.util.DateRanges
import com.positivefinancial.app.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardsScreen(
    onCardClick: (Long) -> Unit,
    onAddCard: () -> Unit,
    viewModel: CreditCardsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Credit Cards") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCard) {
                Icon(Icons.Filled.Add, contentDescription = "Add credit card")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total outstanding", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            Formatters.currency(state.totalOutstanding),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (state.totalOutstanding > 0) ExpenseRed else IncomeGreen
                        )
                    }
                }
            }

            if (state.cards.isEmpty() && !state.isLoading) {
                item { EmptyState("No credit cards yet", "Add a card to track its limit, balance, and payment due date.") }
            } else {
                items(state.cards, key = { it.account.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCardClick(item.account.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row {
                                    IconBadge(iconKey = item.account.iconKey, colorHex = item.account.colorHex)
                                    Column(modifier = Modifier.padding(start = 12.dp)) {
                                        Text(item.account.name, fontWeight = FontWeight.Medium)
                                        Text(
                                            "${item.details.cardNetwork}${if (item.details.lastFourDigits.isNotBlank()) " •••• ${item.details.lastFourDigits}" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Outstanding", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(Formatters.currency(item.outstanding), fontWeight = FontWeight.SemiBold, color = ExpenseRed)
                                }
                                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                    Text("Available", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(Formatters.currency(item.availableLimit), fontWeight = FontWeight.SemiBold, color = IncomeGreen)
                                }
                            }
                            LinearProgressIndicator(
                                progress = { item.utilization },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (item.utilization > 0.8f) ExpenseRed else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Next payment due: ${Formatters.localDate(DateRanges.nextMonthlyDate(item.details.paymentDueDay))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
