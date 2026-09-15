package com.positivefinancial.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.positivefinancial.app.MainActivity
import com.positivefinancial.app.R
import com.positivefinancial.app.util.Formatters
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val DAILY_CHANNEL_ID = "daily_reminder_channel"
        const val CARD_CHANNEL_ID = "credit_card_reminder_channel"
        const val BUDGET_CHANNEL_ID = "budget_alert_channel"
        const val RECURRING_CHANNEL_ID = "recurring_bills_channel"
        const val DAILY_NOTIFICATION_ID = 1001
        const val CARD_NOTIFICATION_ID_BASE = 2000
        const val BUDGET_NOTIFICATION_ID_BASE = 3000
        const val RECURRING_NOTIFICATION_ID_BASE = 4000
        const val BILL_REMINDER_NOTIFICATION_ID_BASE = 5000
    }

    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)

        val dailyChannel = NotificationChannel(
            DAILY_CHANNEL_ID,
            context.getString(R.string.notification_channel_daily_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_daily_desc)
        }

        val cardChannel = NotificationChannel(
            CARD_CHANNEL_ID,
            context.getString(R.string.notification_channel_card_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_card_desc)
        }

        val budgetChannel = NotificationChannel(
            BUDGET_CHANNEL_ID,
            context.getString(R.string.notification_channel_budget_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_budget_desc)
        }

        val recurringChannel = NotificationChannel(
            RECURRING_CHANNEL_ID,
            context.getString(R.string.notification_channel_recurring_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_recurring_desc)
        }

        manager.createNotificationChannels(listOf(dailyChannel, cardChannel, budgetChannel, recurringChannel))
    }

    fun showDailyReminder() {
        if (!hasNotificationPermission()) return
        val notification = NotificationCompat.Builder(context, DAILY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.daily_reminder_title))
            .setContentText(context.getString(R.string.daily_reminder_body))
            .setContentIntent(contentIntent())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(DAILY_NOTIFICATION_ID, notification)
    }

    fun showCardReminder(accountId: Long, cardName: String) {
        if (!hasNotificationPermission()) return
        val notification = NotificationCompat.Builder(context, CARD_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.card_reminder_title))
            .setContentText(context.getString(R.string.card_reminder_body, cardName))
            .setContentIntent(contentIntent())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(context)
            .notify(CARD_NOTIFICATION_ID_BASE + accountId.toInt(), notification)
    }

    fun showBudgetExceeded(categoryId: Long, categoryName: String, spent: Long, limit: Long) {
        if (!hasNotificationPermission()) return
        val notification = NotificationCompat.Builder(context, BUDGET_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.budget_exceeded_title, categoryName))
            .setContentText(
                context.getString(
                    R.string.budget_exceeded_body,
                    categoryName,
                    Formatters.currency(spent),
                    Formatters.currency(limit)
                )
            )
            .setContentIntent(contentIntent())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context)
            .notify(BUDGET_NOTIFICATION_ID_BASE + categoryId.toInt(), notification)
    }

    fun showRecurringCreated(itemId: Long, itemName: String, amountText: String) {
        if (!hasNotificationPermission()) return
        val notification = NotificationCompat.Builder(context, RECURRING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.recurring_created_title))
            .setContentText(context.getString(R.string.recurring_created_body, itemName, amountText))
            .setContentIntent(contentIntent())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context)
            .notify(RECURRING_NOTIFICATION_ID_BASE + itemId.toInt(), notification)
    }

    fun showBillReminder(itemId: Long, itemName: String, amountText: String) {
        if (!hasNotificationPermission()) return
        val notification = NotificationCompat.Builder(context, RECURRING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.bill_reminder_title, itemName))
            .setContentText(context.getString(R.string.bill_reminder_body, itemName, amountText))
            .setContentIntent(contentIntent())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(context)
            .notify(BILL_REMINDER_NOTIFICATION_ID_BASE + itemId.toInt(), notification)
    }

    private fun contentIntent() =
        android.app.PendingIntent.getActivity(
            context,
            0,
            android.content.Intent(context, MainActivity::class.java),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
