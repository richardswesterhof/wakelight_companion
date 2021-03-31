package com.example.wakelightcompanion

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
//        StringBuilder().apply {
//            append("Action: ${intent.action}\n")
//            append("Extras: ${intent.extras}\n")
//            append("Data: ${intent.data}\n")
//            append("Categories: ${intent.categories}\n")
//            append("Flags: ${intent.flags}\n")
//            append("Identifier: ${intent.identifier}\n")
//            append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
//            toString().also { log ->
//                Log.d("MyBroadcastReceiver", log)
//                Toast.makeText(context, log, Toast.LENGTH_LONG).show()
//            }
//        }

        val date: Date? = AlarmUtil(context).getNextAlarmDate()
        Log.i("my pp", "next alarm date is set to be $date")

        if(date == null) return


        val format = SimpleDateFormat("EEEE HH:mm")

        val formattedDate = format.format(date)

        val notificationChannel = "Wakelight Enable Option"

        Log.i("pp time", "Successfully set up the notification builder")

        val newIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, newIntent, 0)

        val builder = NotificationCompat.Builder(context, context.resources.getString(R.string.app_name) + notificationChannel)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("New alarm detected for $formattedDate")
                .setContentText("Would you like to enable your wakelight for this alarm?")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)


        with(NotificationManagerCompat.from(context)) {
            // Google: notificationId is a unique int for each notification that you must define
            // Me: haha hardcoded 1 go brrrrrr
            Log.i("suck pp", "I don't even really know what this is but we are here")
            notify(1, builder.build())
            Log.i("asd","sent the notification")
        }



    }
}