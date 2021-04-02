package com.richardswesterhof.wakelightcompanion.broadcast_receivers

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.richardswesterhof.wakelightcompanion.*
import java.text.SimpleDateFormat
import java.util.*

private val listeningFors: List<String> = listOf("android.app.action.NEXT_ALARM_CLOCK_CHANGED")

class AlarmChangedReceiver : ExtendedBroadcastReceiver(listeningFors) {

    override fun trigger(context: Context, intent: Intent) {
        val date: Date? = AlarmUtil(context).getNextAlarmDate()
        Log.d(this::class.simpleName, "next alarm date is set to be $date")

        if(date == null) return

        // TODO: if wakelight currently active, stop it

        sendNotification(context, date)
    }


    fun sendNotification(context: Context, date: Date) {
        val format = SimpleDateFormat("EEEE HH:mm")
        val formattedDate = format.format(date)
        val notificationChannel = "Wakelight Enable Option"

        // TODO: store the notification id somewhere so we can retrieve it later
        val nextNotificationId = 1

        // the intent that we will use for the click action on the notification itself
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, mainIntent, 0)

        // the intent that we will broadcast when the "enable" button is clicked
        val enableWakeLightIntent = Intent(context, WakeLightEnableRequestReceiver::class.java).apply {
            action = "com.richardswesterhof.wakelightcompanion.SET_WAKELIGHT_ALARM"
            putExtra("date", date)
            putExtra("id", nextNotificationId)
        }
        val enablePendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 1, enableWakeLightIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, context.resources.getString(R.string.app_name) + notificationChannel)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.lightbulb)
                .setContentTitle("New alarm detected for $formattedDate")
                .setContentText("Would you like to enable your WakeLight for this alarm?")
                .setContentIntent(mainPendingIntent)
                .addAction(R.mipmap.ic_launcher, "Enable", enablePendingIntent)
                .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // Google: notificationId is a unique int for each notification that you must define
            // Me: haha hardcoded 1 go brrrrrr
            notify(nextNotificationId, builder.build())
            Log.d(this::class.simpleName,"sent the notification")
        }
    }
}