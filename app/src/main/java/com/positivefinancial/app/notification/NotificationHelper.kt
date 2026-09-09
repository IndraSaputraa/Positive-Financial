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
        const val DAILY_NOTIFICATION_ID = 1001
        const val CARD_NOTIFICATION_ID_BASE = 2000
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

        manager.createNotificationChannels(listOf(dailyChannel, cardChannel))
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
