package com.positivefinancial.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.positivefinancial.app.data.local.dao.CategorySpendingRow
import com.positivefinancial.app.ui.components.DonutChart
import com.positivefinancial.app.ui.components.DonutSlice
import com.positivefinancial.app.ui.components.EmptyState
import com.positivefinancial.app.ui.components.IconBadge
import com.positivefinancial.app.ui.components.MonthlyBarChart
import com.positivefinancial.app.ui.components.MonthlyBarData
import com.positivefinancial.app.ui.components.SectionHeader
import com.positivefinancial.app.ui.components.TransactionRow
import com.positivefinancial.app.ui.components.parseHexColor
import com.positivefinancial.app.ui.theme.ExpenseRed
import com.positivefinancial.app.ui.theme.IncomeGreen
import com.positivefinancial.app.util.Formatters

@Composable
fun DashboardScreen(
    onAddTransaction: (String) -> Unit,
    onSeeAllTransactions: () -> Unit,
    onTransfer: () -> Unit,
    onOpenAccount: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text(text = "Positive Financial", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = "Here's how your money is doing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { BalanceHeroCard(state.totalBalance, state.monthlyIncome, state.monthlyExpense) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(onClick = { onAddTransaction("INCOME") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.SouthWest, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Income")
                }
                Button(onClick = { onAddTransaction("EXPENSE") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.NorthEast, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Expense")
                }
                OutlinedButton(onClick = onTransfer, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Transfer")
                }
            }
        }

        item { SavingsProgressCard(state.monthlyIncome, state.monthlyExpense) }

        if (state.insights.isNotEmpty()) {
            item { InsightsCard(state.insights) }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(title = "Spending Breakdown")
                if (state.expenseBreakdown.isEmpty() || state.monthlyExpense == 0L) {
                    EmptyState("No expenses yet", "Record an expense to see your breakdown by category.")
                } else {
                    SpendingBreakdownCard(state.expenseBreakdown, state.monthlyExpense)
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(title = "Your Accounts")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.accounts.forEach { account ->
                        AccountChip(
                            name = account.name,
                            balance = account.balance,
                            iconKey = account.iconKey,
                            colorHex = account.colorHex,
                            onClick = { onOpenAccount(account.id) }
                        )
                    }
                }
            }
        }

        if (state.monthlySummary.size > 1) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(title = "Monthly Summary")
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                        MonthlyBarChart(
                            data = state.monthlySummary.map {
                                MonthlyBarData(
                                    label = Formatters.monthYearShort(it.yearMonth),
                                    income = it.income.toFloat(),
                                    expense = it.expense.toFloat()
                                )
                            },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Recent Activity",
                actionLabel = "See all",
                onActionClick = onSeeAllTransactions
            )
        }

        if (state.recentTransactions.isEmpty()) {
            item { EmptyState("No activity yet", "Your recorded income, expenses, and transfers will show up here.") }
        } else {
            items(state.recentTransactions, key = { it.id }) { tx ->
                TransactionRow(transaction = tx)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun BalanceHeroCard(totalBalance: Long, income: Long, expense: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clipToRoundedCard()
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text("Total Balance", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = Formatters.currency(totalBalance),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                MiniStat(label = "Income (this month)", value = income, color = Color.White)
                MiniStat(label = "Expenses (this month)", value = expense, color = Color.White)
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: Long, color: Color) {
    Column {
        Text(label, color = color.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
        Text(Formatters.currency(value), color = color, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun SavingsProgressCard(income: Long, expense: Long) {
    val net = income - expense
    val rate = if (income > 0) (net.toFloat() / income.toFloat()).coerceIn(-1f, 1f) else 0f
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Savings this month", style = MaterialTheme.typography.titleMedium)
                Text(
                    Formatters.currencySigned(net),
                    color = if (net >= 0) IncomeGreen else ExpenseRed,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { rate.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clipToRoundedCard(),
                color = if (net >= 0) IncomeGreen else ExpenseRed
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (income > 0) "${(rate * 100).toInt().coerceAtLeast(0)}% of income saved" else "Add income to track your savings rate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InsightsCard(insights: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.width(8.dp))
                Text(
                    "AI Insights",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.height(10.dp))
            insights.forEach {
                Text(
                    "•  $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SpendingBreakdownCard(breakdown: List<CategorySpendingRow>, totalExpense: Long) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            DonutChart(
                data = breakdown.map {
                    DonutSlice(it.categoryName ?: "Other", it.total.toFloat(), parseHexColor(it.colorHex ?: "#94A3B8"))
                },
                modifier = Modifier.size(120.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total", style = MaterialTheme.typography.labelSmall)
                    Text(Formatters.currency(totalExpense), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                breakdown.take(5).forEach { row ->
                    val percent = if (totalExpense > 0) (row.total * 100 / totalExpense) else 0
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clipToCircle()
                                .background(parseHexColor(row.colorHex ?: "#94A3B8"))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            row.categoryName ?: "Other",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text("$percent%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountChip(name: String, balance: Long, iconKey: String, colorHex: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            IconBadge(iconKey = iconKey, colorHex = colorHex, size = 36.dp)
            Spacer(Modifier.height(8.dp))
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(Formatters.currency(balance), style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun Modifier.clipToRoundedCard() = clip(RoundedCornerShape(20.dp))
private fun Modifier.clipToCircle() = clip(CircleShape)
