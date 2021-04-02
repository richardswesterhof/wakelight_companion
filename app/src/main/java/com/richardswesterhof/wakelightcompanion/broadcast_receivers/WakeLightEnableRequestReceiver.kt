package com.richardswesterhof.wakelightcompanion.broadcast_receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import java.util.*

private val listeningFors: List<String> = listOf("com.richardswesterhof.wakelightcompanion.SET_WAKELIGHT_ALARM")

class WakeLightEnableRequestReceiver: ExtendedBroadcastReceiver(listeningFors) {

    override fun trigger(context: Context, intent: Intent) {
        val date: Date = intent.extras?.get("date") as Date

        Log.d(this::class.simpleName, "Received request to enable wakelight for $date")

        // delete the notification this request came from
        deleteNotification(context, intent)
        // schedule the alarm 30 minutes before the user's original alarm
        scheduleAlarm(context, date)
    }


    fun deleteNotification(context: Context, intent: Intent) {
        if(intent.extras?.containsKey("id")!!) {
            with(NotificationManagerCompat.from(context)) {
                cancel(intent.extras!!.get("id") as Int)
            }
        }
    }


    fun scheduleAlarm(context: Context, date: Date) {
        val userAlarmMillis = date.time
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
            putExtra("userTimeMillis", userAlarmMillis)
        }
        val startPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 1, startWakeLightIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        // allow a window size of 10 minutes (i.e. 5 minutes earlier or later)
        am.setWindow(AlarmManager.RTC_WAKEUP, systemAlarmMillis, windowSize, startPendingIntent)
    }
}