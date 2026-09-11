package com.positivefinancial.app.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.min

object DateRanges {

    fun currentMonthRange(): Pair<Long, Long> = monthRange(YearMonth.now())

    fun monthRange(month: YearMonth): Pair<Long, Long> {
        val start = month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = LocalDateTime.of(month.atEndOfMonth(), LocalTime.of(23, 59, 59))
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return start to end
    }

    fun monthsAgoStart(months: Int): Long {
        val start = YearMonth.now().minusMonths((months - 1).toLong()).atDay(1)
        return start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun endOfToday(): Long =
        LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59))
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun startOfToday(): Long =
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun now(): Long = System.currentTimeMillis()

    /** Next upcoming calendar date (today counts) that falls on [dayOfMonth], clamped per-month length. */
    fun nextMonthlyDate(dayOfMonth: Int): LocalDate {
        val today = LocalDate.now()
        var month = YearMonth.from(today)
        var day = min(dayOfMonth, month.lengthOfMonth())
        var candidate = month.atDay(day)
        if (candidate.isBefore(today)) {
            month = month.plusMonths(1)
            day = min(dayOfMonth, month.lengthOfMonth())
            candidate = month.atDay(day)
        }
        return candidate
    }
}
