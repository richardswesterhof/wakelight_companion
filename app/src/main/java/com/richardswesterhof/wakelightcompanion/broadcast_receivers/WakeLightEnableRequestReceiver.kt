package com.richardswesterhof.wakelightcompanion.broadcast_receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.richardswesterhof.wakelightcompanion.*
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
        // only if this request came from a notification
        // (it could also come from the AlarmReSetter, for example)
        if(intent.extras?.containsKey("id")!!) {
            with(NotificationManagerCompat.from(context)) {
                cancel(intent.extras!!.get("id") as Int)
            }
        }
    }


    fun scheduleAlarm(context: Context, date: Date) {
        val userAlarmMillis = date.time
        if(userAlarmMillis < System.currentTimeMillis()) {
            Log.w(this::class.simpleName, "Date $date is in the past, alarm will not be set")
            return
        }

        val windowSize: Long = 10*60*1000
        val systemAlarmMillis: Long = userAlarmMillis - (30*60*1000 + windowSize/2)


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
        // note that android implements the window as a start time and a windows size however
        am.setWindow(AlarmManager.RTC_WAKEUP, systemAlarmMillis, windowSize, startPendingIntent)

        // finally store this alarm in the shared preferences
        val sharedPref = context.getSharedPreferences(context.resources.getString(R.string.preference_file_store_alarms), Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putLong("nextAlarmMillis", userAlarmMillis)
            apply()
        }
    }
}