package com.positivefinancial.app.util

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val idNumberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale("in", "ID")).apply {
    maximumFractionDigits = 0
}

private val dayMonthYearFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
private val dayMonthFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
private val monthYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)

object Formatters {

    fun currency(amountInRupiah: Long): String {
        val sign = if (amountInRupiah < 0) "-" else ""
        return "${sign}Rp ${idNumberFormat.format(abs(amountInRupiah))}"
    }

    fun currencySigned(amountInRupiah: Long): String {
        val sign = if (amountInRupiah > 0) "+" else if (amountInRupiah < 0) "-" else ""
        return "${sign}Rp ${idNumberFormat.format(abs(amountInRupiah))}"
    }

    fun date(millis: Long): String = zonedDate(millis).format(dayMonthYearFormatter)

    fun localDate(date: LocalDate): String = date.format(dayMonthYearFormatter)

    fun dateShort(millis: Long): String = zonedDate(millis).format(dayMonthFormatter)

    fun dayLabel(millis: Long): String {
        val date = zonedDate(millis)
        val today = LocalDate.now()
        return when (date) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> date.format(dayMonthYearFormatter)
        }
    }

    fun monthYearFromKey(yearMonth: String): String =
        runCatching { YearMonth.parse(yearMonth).atDay(1).format(monthYearFormatter) }.getOrDefault(yearMonth)

    fun monthYearShort(yearMonth: String): String =
        runCatching {
            YearMonth.parse(yearMonth).atDay(1).format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH))
        }.getOrDefault(yearMonth)

    private fun zonedDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
}
