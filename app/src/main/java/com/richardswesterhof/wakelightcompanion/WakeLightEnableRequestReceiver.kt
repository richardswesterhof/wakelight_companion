package com.richardswesterhof.wakelightcompanion

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import java.util.*

class WakeLightEnableRequestReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val receivedAction: String? = intent.action;
        if(receivedAction != "com.richardswesterhof.wakelightcompanion.SET_WAKELIGHT_ALARM") {
            Log.d("stuff", "Bad action: $receivedAction")
            return
        }

        Log.i("pp juice yosh", "Received request to enable wakelight for ${intent.extras?.get("date")}")

        // delete the notification this request came from
        if(intent.extras?.containsKey("id")!!) {
            with(NotificationManagerCompat.from(context)) {
                cancel(intent.extras!!.get("id") as Int)
            }
        }

        val userAlarmMillis = (intent.extras?.get("date") as Date).time
        val windowSize: Long = 10*60*1000
        val systemAlarmMillis = userAlarmMillis - (30*60*1000 + windowSize/2)


        // schedule the system alarm 30 minutes before the user alarm
        // TODO: make these 30 minutes configurable
        val au = AlarmUtil(context)
        val am: AlarmManager = au.getAlarmManager()

        // the intent that we will broadcast when the wakelight alarm should start
        val startWakeLightIntent = Intent(context, WakeLightStarter::class.java).apply {
            action = "com.richardswesterhof.wakelightcompanion.START_WAKELIGHT_ALARM"
            putExtra("startTimeMillis", systemAlarmMillis)
        }
        val startPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 1, startWakeLightIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        // allow a window size of 10 minutes (i.e. 5 minutes earlier or later)
        am.setWindow(AlarmManager.RTC_WAKEUP, systemAlarmMillis, windowSize, startPendingIntent)
    }
}