package com.positivefinancial.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val EXTRA_ACCOUNT_ID = "extra_account_id"
        const val EXTRA_CARD_NAME = "extra_card_name"
        private const val DAILY_REQUEST_CODE = 9000
        private const val CARD_REQUEST_CODE_BASE = 9100
    }

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleDaily(hour: Int, minute: Int) {
        val triggerAt = nextDailyTrigger(hour, minute)
        val intent = Intent(context, DailyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, DAILY_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setAlarm(triggerAt, pendingIntent)
    }

    fun cancelDaily() {
        val intent = Intent(context, DailyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, DAILY_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleCardReminder(accountId: Long, cardName: String, dayOfMonth: Int, hour: Int, minute: Int) {
        val triggerAt = nextMonthlyTrigger(dayOfMonth, hour, minute)
        val intent = Intent(context, CreditCardReminderReceiver::class.java).apply {
            putExtra(EXTRA_ACCOUNT_ID, accountId)
            putExtra(EXTRA_CARD_NAME, cardName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, cardRequestCode(accountId), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setAlarm(triggerAt, pendingIntent)
    }

    fun cancelCardReminder(accountId: Long) {
        val intent = Intent(context, CreditCardReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, cardRequestCode(accountId), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun cardRequestCode(accountId: Long): Int = CARD_REQUEST_CODE_BASE + accountId.toInt()

    private fun setAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        val canUseExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canUseExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun nextDailyTrigger(hour: Int, minute: Int): Long {
        val now = ZonedDateTime.now()
        var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) {
            next = next.plusDays(1)
        }
        return next.toInstant().toEpochMilli()
    }

    private fun nextMonthlyTrigger(dayOfMonth: Int, hour: Int, minute: Int): Long {
        val now = ZonedDateTime.now()
        var month = YearMonth.from(now)
        var day = min(dayOfMonth, month.lengthOfMonth())
        var next = ZonedDateTime.of(month.atDay(day), LocalTime.of(hour, minute), now.zone)
        if (!next.isAfter(now)) {
            month = month.plusMonths(1)
            day = min(dayOfMonth, month.lengthOfMonth())
            next = ZonedDateTime.of(month.atDay(day), LocalTime.of(hour, minute), now.zone)
        }
        return next.toInstant().toEpochMilli()
    }
}
