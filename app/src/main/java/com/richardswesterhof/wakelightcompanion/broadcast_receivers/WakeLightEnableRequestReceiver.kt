package com.richardswesterhof.wakelightcompanion.broadcast_receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.richardswesterhof.wakelightcompanion.R
import java.util.Date

private val listeningFors: List<String> =
    listOf("com.richardswesterhof.wakelightcompanion.SET_WAKELIGHT_ALARM")

class WakeLightEnableRequestReceiver : ExtendedBroadcastReceiver(listeningFors) {

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
        if (intent.extras?.containsKey("id")!!) {
            with(NotificationManagerCompat.from(context)) {
                cancel(intent.extras!!.get("id") as Int)
            }
        }
    }


    fun scheduleAlarm(context: Context, date: Date) {
        val userAlarmMillis = date.time
        // this can happen if the device was turned off at the time the alarm should have been enabled
        // since we can also get here from BOOT_COMPLETED
        if (userAlarmMillis < System.currentTimeMillis()) {
            Log.w(this::class.simpleName, "Date $date is in the past, alarm will not be set")
            return
        }

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        val durationMinutes: Long =
            sharedPrefs.getString("pref_wakelight_duration1", "30")!!.toLong()
        val windowMinutes: Long = 0 // effectively disable windowing

        val windowSize: Long = windowMinutes * 60 * 1000
        val systemAlarmMillis: Long =
            userAlarmMillis - (durationMinutes * 60 * 1000 + windowSize / 2)

        // schedule the system alarm "durationMinutes" minutes before the user alarm
        val au = AlarmUtil(context)
        val am: AlarmManager = au.getAlarmManager()

        // the intent that we will broadcast when the wakelight alarm should start
        val startWakeLightIntent = Intent(context, WakeLightStarter::class.java).apply {
            action = "com.richardswesterhof.wakelightcompanion.START_WAKELIGHT_ALARM"
            putExtra("startTimeMillis", systemAlarmMillis)
            putExtra("userTimeMillis", userAlarmMillis)
        }
        val startPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            startWakeLightIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // allow a window size of 10 minutes (i.e. 5 minutes earlier or later)
        // note that android implements the window as a start time and a windows size however
//        am.setWindow(AlarmManager.RTC_WAKEUP, systemAlarmMillis, windowSize, startPendingIntent)

        // set an exact alarm, not sure what the difference between setExactAndAllowWhileIdle and setExact is though,
        // android docs suggest that setExact should already be exact without restrictions anyway:
        // "[setExact] does not permit the OS to adjust the delivery time. The alarm will be delivered as nearly as possible to the requested trigger time"
        // but I guess using setExactAndAllowWhileIdle should be the intended way?
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, systemAlarmMillis, startPendingIntent)
        // finally store this alarm in the shared preferences
        val sharedPref = context.getSharedPreferences(
            context.resources.getString(R.string.preference_file_store_internal_vars),
            Context.MODE_PRIVATE
        )
        with(sharedPref.edit()) {
            putLong("nextAlarmMillis", userAlarmMillis)
            apply()
        }
    }
}
