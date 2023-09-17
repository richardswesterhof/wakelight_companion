package com.richardswesterhof.wakelightcompanion.broadcast_receivers

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.richardswesterhof.wakelightcompanion.R
import com.richardswesterhof.wakelightcompanion.devices.yeelight.YeelightImpl
import com.richardswesterhof.wakelightcompanion.utils.IdManager

private val starterListeningFors: List<String> =
    listOf("com.richardswesterhof.wakelightcompanion.START_WAKELIGHT_ALARM")
private val stopperListeningFors: List<String> =
    listOf("com.richardswesterhof.wakelightcompanion.STOP_WAKELIGHT_ALARM")

class WakeLightStarter : ExtendedBroadcastReceiver(starterListeningFors) {

    private lateinit var notificationChannel: String
    private lateinit var settings: SharedPreferences

    override fun trigger(context: Context, intent: Intent) {
        // init the notification channel id
        notificationChannel = context.resources.getString(R.string.notif_cat_stop_id)

        settings = PreferenceManager.getDefaultSharedPreferences(context)

        // check if alarm still exists
        val au = AlarmUtil(context)
        val am = au.am
        if (am.nextAlarmClock.triggerTime == (intent.extras?.get("userTimeMillis") as Long)) {
            // only if it does, start wakelight
            Log.d(this::class.simpleName, "Received request to start wakelight")
//            sendDisableNotif(context)
            startWakeLight(context)
        }
    }

    fun startWakeLight(context: Context) {
        val prefID = settings.getString("pref_wakelight_id", "")!!

        Log.d(this::class.simpleName, "Calling start wakelight on the implementation")
        // TODO: get proper implementation for the device and create its corresponding config
        val device = YeelightImpl()
        // TODO: commented for testing purposes


//        device.startWakeLight(context, prefID)
    }


//    fun sendDisableNotif(context: Context) {
//        // the intent that we will use for the click action on the notification itself
//        val stopWakeLightIntent = Intent(context, WakeLightStopper::class.java).apply {
//            action = "com.richardswesterhof.wakelightcompanion.STOP_WAKELIGHT_ALARM"
//        }
//        val stopPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 1, stopWakeLightIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//
//        val nextNotificationId = IdManager.getNextNotifId(context)
//
//        val builder = NotificationCompat.Builder(context, notificationChannel)
//            .setDefaults(Notification.DEFAULT_ALL)
//            .setSmallIcon(R.drawable.lightbulb)
//            .setContentTitle(context.getString(R.string.notif_ask_stop_wakelight_title))
//            .setContentText(context.getString(R.string.notif_ask_stop_wakelight_content))
//            .setColor(context.getColor(R.color.navy_blue_light))
//            .setContentIntent(stopPendingIntent)
//            .setAutoCancel(true)
//
//        with(NotificationManagerCompat.from(context)) {
//            notify(nextNotificationId, builder.build())
//            Log.d(this::class.simpleName,"Sent the notification")
//        }
//    }
}

class WakeLightStopper : ExtendedBroadcastReceiver(stopperListeningFors) {

    private lateinit var settings: SharedPreferences

    override fun trigger(context: Context, intent: Intent) {
        Log.d(this::class.simpleName, "Received request to stop wakelight")
        settings = PreferenceManager.getDefaultSharedPreferences(context)
        stopWakeLight(context)
    }

    fun stopWakeLight(context: Context) {
        val prefID = settings.getString("pref_wakelight_id", "")!!
        Log.d(this::class.simpleName, "Calling stop wakelight on the implementation")
        val yeelight = YeelightImpl()
        // TODO: commented for testing purposes
//        yeelight.stopWakeLight(context, prefID)
    }
}
