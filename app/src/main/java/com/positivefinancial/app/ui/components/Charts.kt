package com.positivefinancial.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DonutSlice(val label: String, val value: Float, val color: Color)

/**
 * Simple ring chart drawn with Canvas so the app has zero charting dependency.
 * Slices smaller than a hair-width are still given a visible minimum sweep so
 * long-tail categories don't disappear entirely.
 */
@Composable
fun DonutChart(
    data: List<DonutSlice>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 26.dp,
    centerContent: @Composable BoxScope.() -> Unit = {}
) {
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val topLeft = Offset(strokePx / 2, strokePx / 2)

            if (total <= 0f || data.isEmpty()) {
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                    size = arcSize,
                    topLeft = topLeft
                )
                return@Canvas
            }
            var startAngle = -90f
            val gapDegrees = if (data.size > 1) 3f else 0f
            data.forEach { slice ->
                val sweep = (slice.value / total) * (360f - gapDegrees * data.size)
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep.coerceAtLeast(if (slice.value > 0) 2f else 0f),
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    size = arcSize,
                    topLeft = topLeft
                )
                startAngle += sweep + gapDegrees
            }
        }
        centerContent()
    }
}

data class MonthlyBarData(val label: String, val income: Float, val expense: Float)

@Composable
fun MonthlyBarChart(
    data: List<MonthlyBarData>,
    modifier: Modifier = Modifier,
    incomeColor: Color = MaterialTheme.colorScheme.primary,
    expenseColor: Color = MaterialTheme.colorScheme.error
) {
    val maxValue = (data.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0f).coerceAtLeast(1f)
    Row(
        modifier = modifier.fillMaxWidth().height(160.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        data.forEach { month ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxHeight().padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Bar(heightFraction = month.income / maxValue, color = incomeColor)
                    Bar(heightFraction = month.expense / maxValue, color = expenseColor)
                }
                Text(
                    text = month.label,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun Bar(heightFraction: Float, color: Color) {
    Box(modifier = Modifier.width(10.dp).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
        Box(
            modifier = Modifier
                .width(10.dp)
                .fillMaxHeight(fraction = heightFraction.coerceIn(0.02f, 1f))
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(color)
        )
    }
}
