package com.lucafilippi.rotaeuropa

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.time.LocalDate
import java.time.ZoneId

object LocalReminderNotifications {
    private const val CHANNEL_ID = "rota_europa_reminders"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Lembretes Rota Europa", NotificationManager.IMPORTANCE_DEFAULT)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun schedule(context: Context, title: String, dueDateEpochDay: Long) {
        val trigger = LocalDate.ofEpochDay(dueDateEpochDay).atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (trigger <= System.currentTimeMillis()) return
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction("com.lucafilippi.rotaeuropa.LOCAL_REMINDER.${title.hashCode()}")
            .putExtra("title", title)
        val pendingIntent = PendingIntent.getBroadcast(
            context, title.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pendingIntent)
    }

    fun cancel(context: Context, title: String) {
        val intent = Intent(context, ReminderReceiver::class.java).setAction("com.lucafilippi.rotaeuropa.LOCAL_REMINDER.${title.hashCode()}")
        val pendingIntent = PendingIntent.getBroadcast(
            context, title.hashCode(), intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent)
        pendingIntent.cancel()
    }

    internal fun show(context: Context, title: String) {
        createChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Lembrete Rota Europa")
            .setContentText(title)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(title.hashCode(), notification)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        LocalReminderNotifications.show(context, intent.getStringExtra("title").orEmpty().ifBlank { "Confira seu planejamento." })
    }
}
