package com.richardswesterhof.wakelightcompanion

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.richardswesterhof.wakelightcompanion.implementation_details.*

class WakeLightStarter: BroadcastReceiver() {

    private val notificationChannel: String = "Wakelight Stop Button"

    override fun onReceive(context: Context, intent: Intent) {
        val receivedAction: String? = intent.action
        if(receivedAction != "com.richardswesterhof.wakelightcompanion.START_WAKELIGHT_ALARM") {
            Log.d("WakeLightReceiver", "Bad action: $receivedAction")
            return
        }

        Log.i("start_wakelight", "Received request to start wakelight")
        sendDisableNotif(context)
        startWakelight()
    }

    fun startWakelight() {
        // TODO: get stored ip
        val ip = "NONE"
        Log.d("WakeLightStarter", "calling start wakelight on the implementation")
        startWakeLight(ip)
    }

    fun sendDisableNotif(context: Context) {
        // the intent that we will use for the click action on the notification itself
        val stopWakeLightIntent = Intent(context, WakeLightStopper::class.java).apply {
            action = "com.richardswesterhof.wakelightcompanion.STOP_WAKELIGHT_ALARM"
        }
        val stopPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 1, stopWakeLightIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val nextNotificationId = 2

        val builder = NotificationCompat.Builder(context, context.resources.getString(R.string.app_name) + notificationChannel)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("WakeLight active")
                .setContentText("Tap to dismiss WakeLight alarm")
                .setContentIntent(stopPendingIntent)
                .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // Google: notificationId is a unique int for each notification that you must define
            // Me: haha hardcoded 2 go brrrrrr
            notify(nextNotificationId, builder.build())
            Log.d("WakeLightStarter","sent the notification")
        }
    }
}

class WakeLightStopper: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val receivedAction: String? = intent.action
        if(receivedAction != "com.richardswesterhof.wakelightcompanion.STOP_WAKELIGHT_ALARM") {
            Log.d("WakeLightStopper", "Bad action: $receivedAction")
            return
        }

        Log.i("WakeLightStopper", "Received request to stop wakelight")
        stopWakelight()
    }

    fun stopWakelight() {
        // TODO: get stored ip
        val ip = "NONE"
        Log.d("WakeLightStarter", "calling stop wakelight on the implementation")
        stopWakeLight(ip)
    }
}