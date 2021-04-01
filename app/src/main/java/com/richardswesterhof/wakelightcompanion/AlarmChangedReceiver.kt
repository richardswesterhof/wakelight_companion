package com.richardswesterhof.wakelightcompanion

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.*

// this class will get its onReceive method called when the next alarm changes on the device
class AlarmChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val receivedAction: String? = intent.action;
        if(receivedAction != "android.app.action.NEXT_ALARM_CLOCK_CHANGED") return

        val date: Date? = AlarmUtil(context).getNextAlarmDate()
        Log.i("my pp", "next alarm date is set to be $date")

        if(date == null) return


        val format = SimpleDateFormat("EEEE HH:mm")

        val formattedDate = format.format(date)

        val notificationChannel = "Wakelight Enable Option"

        // TODO: store the notification id somewhere so we can retrieve it later
        val nextNotificationId = 1

        Log.i("pp time", "Successfully set up the notification builder")

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
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("New alarm detected for $formattedDate")
                .setContentText("Would you like to enable your WakeLight for this alarm?")
                .setContentIntent(mainPendingIntent)
                .addAction(R.mipmap.ic_launcher, "Enable", enablePendingIntent)
                .setAutoCancel(true)

        // TODO: if wakelight currently active, stop it

        with(NotificationManagerCompat.from(context)) {
            // Google: notificationId is a unique int for each notification that you must define
            // Me: haha hardcoded 1 go brrrrrr
            Log.i("suck pp", "I don't even really know what this is but we are here")
            notify(nextNotificationId, builder.build())
            Log.i("asd","sent the notification")
        }



    }
}