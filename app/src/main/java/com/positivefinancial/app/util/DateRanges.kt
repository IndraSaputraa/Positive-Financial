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
    fun nextMonthlyDate(dayOfMonth: Int): LocalDate = nextMonthlyDateOnOrAfter(dayOfMonth, LocalDate.now())

    /** Smallest date >= [reference] that falls on [dayOfMonth] of some month, clamped per-month length. */
    fun nextMonthlyDateOnOrAfter(dayOfMonth: Int, reference: LocalDate): LocalDate {
        var month = YearMonth.from(reference)
        var day = min(dayOfMonth, month.lengthOfMonth())
        var candidate = month.atDay(day)
        if (candidate.isBefore(reference)) {
            month = month.plusMonths(1)
            day = min(dayOfMonth, month.lengthOfMonth())
            candidate = month.atDay(day)
        }
        return candidate
    }

    /** Smallest date strictly after [reference] that falls on [dayOfMonth] of some month. */
    fun nextMonthlyDateAfter(dayOfMonth: Int, reference: LocalDate): LocalDate =
        nextMonthlyDateOnOrAfter(dayOfMonth, reference.plusDays(1))

    /** Smallest date >= [reference] that falls on [monthOfYear]/[dayOfMonth], clamped per-month length. */
    fun nextYearlyDateOnOrAfter(monthOfYear: Int, dayOfMonth: Int, reference: LocalDate): LocalDate {
        var year = reference.year
        fun candidateFor(y: Int): LocalDate {
            val month = YearMonth.of(y, monthOfYear)
            return month.atDay(min(dayOfMonth, month.lengthOfMonth()))
        }
        var candidate = candidateFor(year)
        if (candidate.isBefore(reference)) {
            year += 1
            candidate = candidateFor(year)
        }
        return candidate
    }

    fun nextYearlyDate(monthOfYear: Int, dayOfMonth: Int): LocalDate =
        nextYearlyDateOnOrAfter(monthOfYear, dayOfMonth, LocalDate.now())

    /** Smallest date strictly after [reference] that falls on [monthOfYear]/[dayOfMonth]. */
    fun nextYearlyDateAfter(monthOfYear: Int, dayOfMonth: Int, reference: LocalDate): LocalDate =
        nextYearlyDateOnOrAfter(monthOfYear, dayOfMonth, reference.plusDays(1))

    fun toEpochMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun toLocalDate(millis: Long): LocalDate =
        java.time.Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
}
