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
import com.richardswesterhof.wakelightcompanion.implementation_details.yeelight.YeelightDevice
import com.richardswesterhof.wakelightcompanion.utils.IdManager

private val starterListeningFors: List<String> = listOf("com.richardswesterhof.wakelightcompanion.START_WAKELIGHT_ALARM")
private val stopperListeningFors: List<String> = listOf("com.richardswesterhof.wakelightcompanion.STOP_WAKELIGHT_ALARM")

class WakeLightStarter: ExtendedBroadcastReceiver(starterListeningFors) {

    private lateinit var notificationChannel: String
    private lateinit var settings: SharedPreferences

    override fun trigger(context: Context, intent: Intent) {
        // init the notification channel id
        notificationChannel = context.resources.getString(R.string.notif_cat_stop_id)

        settings = PreferenceManager.getDefaultSharedPreferences(context)

        // check if alarm still exists
        val au = AlarmUtil(context)
        val am = au.am
        if(am.nextAlarmClock.triggerTime == (intent.extras?.get("userTimeMillis") as Long)) {
            // only if it does, start wakelight
            Log.d(this::class.simpleName, "Received request to start wakelight")
            sendDisableNotif(context)
            startWakeLight(context)
        }
    }

    fun startWakeLight(context: Context) {
        val ip = settings.getString("pref_wakelight_ip", "")!!
        val port = settings.getString("pref_wakelight_port", "")!!
        Log.d(this::class.simpleName, "Calling start wakelight on the implementation")
        val portInt = port.toIntOrNull()
        val yeelight = YeelightDevice()
        if(port.isNotBlank() && portInt != null) yeelight.startWakeLight(context, ip, portInt)
        else yeelight.startWakeLight(context, ip, null)

    }

    fun sendDisableNotif(context: Context) {
        // the intent that we will use for the click action on the notification itself
        val stopWakeLightIntent = Intent(context, WakeLightStopper::class.java).apply {
            action = "com.richardswesterhof.wakelightcompanion.STOP_WAKELIGHT_ALARM"
        }
        val stopPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 1, stopWakeLightIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val nextNotificationId = IdManager.getNextNotifId(context)

        val builder = NotificationCompat.Builder(context, notificationChannel)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.lightbulb)
            .setContentTitle(context.getString(R.string.notif_ask_stop_wakelight_title))
            .setContentText(context.getString(R.string.notif_ask_stop_wakelight_content))
            .setColor(context.getColor(R.color.navy_blue_light))
            .setContentIntent(stopPendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // Google: notificationId is a unique int for each notification that you must define
            // Me: haha hardcoded 2 go brrrrrr
            notify(nextNotificationId, builder.build())
            Log.d(this::class.simpleName,"Sent the notification")
        }
    }
}

class WakeLightStopper: ExtendedBroadcastReceiver(stopperListeningFors) {

    private lateinit var settings: SharedPreferences

    override fun trigger(context: Context, intent: Intent) {
        Log.d(this::class.simpleName, "Received request to stop wakelight")

        settings = PreferenceManager.getDefaultSharedPreferences(context)

        stopWakeLight()
    }

    fun stopWakeLight() {
        val ip = settings.getString("pref_wakelight_ip", "")!!
        val port = settings.getString("pref_wakelight_port", "")!!
        Log.d(this::class.simpleName, "Calling stop wakelight on the implementation")
        val portInt = port.toIntOrNull()
        val yeelight = YeelightDevice()
        if(port.isNotBlank() && portInt != null) yeelight.stopWakeLight(ip, portInt)
        else yeelight.stopWakeLight(ip, null)
    }
}