package com.positivefinancial.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.positivefinancial.app.data.local.dao.TransactionWithDetails
import com.positivefinancial.app.data.model.TransactionType
import com.positivefinancial.app.ui.theme.ExpenseRed
import com.positivefinancial.app.ui.theme.IncomeGreen
import com.positivefinancial.app.ui.theme.TransferBlue
import com.positivefinancial.app.util.Formatters
import com.positivefinancial.app.util.IconMapper

fun parseHexColor(hex: String): Color = runCatching { Color(android.graphics.Color.parseColor(hex)) }
    .getOrDefault(Color.Gray)

@Composable
fun IconBadge(
    iconKey: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 44.dp
) {
    val color = parseHexColor(colorHex)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = IconMapper.resolve(iconKey), contentDescription = null, tint = color)
    }
}

private fun Modifier.clip(shape: androidx.compose.ui.graphics.Shape) = this.then(androidx.compose.ui.draw.clip(shape))

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(actionLabel)
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }
        }
    }
}

@Composable
fun TransactionRow(
    transaction: TransactionWithDetails,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (transaction.type) {
            TransactionType.TRANSFER -> IconBadge(iconKey = "swap_horiz", colorHex = "#3B82F6")
            else -> IconBadge(
                iconKey = transaction.categoryIconKey ?: "category",
                colorHex = transaction.categoryColorHex ?: "#94A3B8"
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (transaction.type) {
                    TransactionType.TRANSFER -> "${transaction.fromAccountName ?: "?"} → ${transaction.toAccountName ?: "?"}"
                    else -> transaction.categoryName ?: "Uncategorized"
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            val subtitle = when (transaction.type) {
                TransactionType.TRANSFER -> Formatters.dayLabel(transaction.date)
                else -> "${transaction.accountName ?: ""} • ${Formatters.dayLabel(transaction.date)}"
            }
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        val (amountText, amountColor, icon) = when (transaction.type) {
            TransactionType.INCOME -> Triple(Formatters.currencySigned(transaction.amount), IncomeGreen, Icons.Filled.SouthWest)
            TransactionType.EXPENSE -> Triple(Formatters.currencySigned(-transaction.amount), ExpenseRed, Icons.Filled.NorthEast)
            TransactionType.TRANSFER -> Triple(Formatters.currency(transaction.amount), TransferBlue, Icons.Filled.SwapHoriz)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = amountText, color = amountColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Delete",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = ExpenseRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        content = content
    )
}
