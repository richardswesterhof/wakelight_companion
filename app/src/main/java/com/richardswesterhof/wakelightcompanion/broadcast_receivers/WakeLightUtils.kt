package com.richardswesterhof.wakelightcompanion.broadcast_receivers

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.richardswesterhof.wakelightcompanion.R
import com.richardswesterhof.wakelightcompanion.implementation_details.*

private val starterListeningFors: List<String> = listOf("com.richardswesterhof.wakelightcompanion.START_WAKELIGHT_ALARM")
private val stopperListeningFors: List<String> = listOf("com.richardswesterhof.wakelightcompanion.STOP_WAKELIGHT_ALARM")

class WakeLightStarter: ExtendedBroadcastReceiver(starterListeningFors) {

    private val notificationChannel: String = "Wakelight Stop Button"

    override fun trigger(context: Context, intent: Intent) {
        // check if alarm still exists
        val au = AlarmUtil(context)
        val am = au.am
        if(am.nextAlarmClock.triggerTime == (intent.extras?.get("userTimeMillis") as Long)) {
            // only if it does start wakelight
            Log.d("WakeLightStarter", "Received request to start wakelight")
            sendDisableNotif(context)
            startWakelight()
        }
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
                .setSmallIcon(R.drawable.lightbulb)
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

class WakeLightStopper: ExtendedBroadcastReceiver(stopperListeningFors) {

    override fun trigger(context: Context, intent: Intent) {
        Log.d("WakeLightStopper", "Received request to stop wakelight")
        stopWakelight()
    }

    fun stopWakelight() {
        // TODO: get stored ip
        val ip = "NONE"
        Log.d("WakeLightStarter", "calling stop wakelight on the implementation")
        stopWakeLight(ip)
    }
}