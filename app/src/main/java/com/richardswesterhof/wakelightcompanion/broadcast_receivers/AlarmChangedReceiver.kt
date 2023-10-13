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
import com.richardswesterhof.wakelightcompanion.MainActivity
import com.richardswesterhof.wakelightcompanion.R
import com.richardswesterhof.wakelightcompanion.utils.IdManager
import com.richardswesterhof.wakelightcompanion.utils.schedule_interval_regex
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

private val listeningFors: List<String> = listOf("android.app.action.NEXT_ALARM_CLOCK_CHANGED")

class AlarmChangedReceiver : ExtendedBroadcastReceiver(listeningFors) {

    private lateinit var internalPref: SharedPreferences // used to persistently store data internal to the functioning of the app
    private lateinit var settings: SharedPreferences // used to store user's settings

    override fun trigger(context: Context, intent: Intent) {
        val date: Date? = AlarmUtil(context).getNextAlarmDate()
        Log.d(this::class.simpleName, "Next alarm date is set to be $date")

        internalPref = context.getSharedPreferences(
            context.resources.getString(R.string.preference_file_store_internal_vars),
            Context.MODE_PRIVATE
        )
        settings = PreferenceManager.getDefaultSharedPreferences(context)

        if (date == Date(internalPref.getLong("lastReceivedAlarmMillis", 0))) {
            Log.d(
                this::class.simpleName,
                "Skipping alarm at $date because it has already been received before"
            )
            return
        }

        if (date == null) removeFromStorage()
        else {
            askEnable(context, date)
            storeLastReceivedAlarm(date)
        }
    }


    private fun askEnable(context: Context, date: Date) {
        // the intent that we will broadcast when the "enable" button is clicked
        val enableWakeLightIntent =
            Intent(context, WakeLightEnableRequestReceiver::class.java).apply {
                action = "com.richardswesterhof.wakelightcompanion.SET_WAKELIGHT_ALARM"
                putExtra("date", date)
            }

        if (!autoEnabled(context, date, enableWakeLightIntent)) sendAskNotification(
            context,
            date,
            enableWakeLightIntent
        )
    }


    private fun sendAskNotification(context: Context, date: Date, enableIntent: Intent) {
        val format = SimpleDateFormat("EEEE HH:mm")
        val formattedDate = format.format(date)

        val nextNotificationId = IdManager.getNextNotifId(context)

        // the intent that we will use for the click action on the notification itself
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, mainIntent, 0)

        // if we send the intent through a notification, we want to know the notification id
        enableIntent.putExtra("id", nextNotificationId)
        val enablePendingIntent: PendingIntent =
            PendingIntent.getBroadcast(context, 1, enableIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder =
            NotificationCompat.Builder(context, context.getString(R.string.notif_cat_enable_id))
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.lightbulb)
                .setContentTitle(
                    context.getString(
                        R.string.notif_ask_enable_wakelight_title,
                        formattedDate
                    )
                )
                .setContentText(context.getString(R.string.notif_ask_enable_wakelight_content))
                .setColor(context.getColor(R.color.navy_blue_light))
                .setContentIntent(mainPendingIntent)
                .addAction(
                    0,
                    context.getString(R.string.notif_ask_enable_wakelight_confirm_button),
                    enablePendingIntent
                )
                .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(nextNotificationId, builder.build())
            Log.d(this::class.simpleName, "Sent the notification")
        }
    }


    private fun autoEnabled(context: Context, date: Date, enableIntent: Intent): Boolean {
        // check if the given date falls within an auto enabled interval
        val schedule = settings.getString("pref_wakelight_schedule", "")!!
        val intervals = schedule.split(",")

        val alwaysEnable = settings.getBoolean("pref_always_auto_enable", false)

        if (alwaysEnable) {
            Log.d(
                this::class.simpleName,
                "WakeLight was enabled since settings alwaysEnable was true"
            )
            enableNow(context, enableIntent)
            return true
        }

        for (interval in intervals) {
            val trimmedInterval = interval.trim()
            val match = schedule_interval_regex.matchEntire(trimmedInterval)
            val localDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
            if (match != null) {
                Log.d(
                    this::class.simpleName,
                    "$date matches interval \"$trimmedInterval\" (based on regex)"
                )
                val (intervalDay, hourLow, minuteLow, hourHigh, minuteHigh, wholeDay) = match.destructured
                val alarmDay = localDate.dayOfWeek.toString().lowercase(Locale.ROOT)
                if (intervalDay.equals(alarmDay, ignoreCase = true) || intervalDay.lowercase(
                        Locale.ROOT
                    ) == "everyday"
                ) {
                    val hour = localDate.hour
                    val minute = localDate.minute
                    // if the whole day is specified
                    if (wholeDay.lowercase(Locale.ROOT) == "wholeday" ||
                        // if the hour is truly in between the low and high hours of the interval
                        (hourLow.toInt() < hour && hourHigh.toInt() > hour) ||
                        // if the hour is equal to the low we need to compare the minutes
                        (hourLow.toInt() == hour && minuteLow.toInt() <= minute) ||
                        // if the hour is equal to the high we need to compare the minute as well
                        (hourHigh.toInt() == hour && minuteHigh.toInt() >= minute)
                    ) {
                        Log.d(
                            this::class.simpleName,
                            "$date truly matches interval \"$trimmedInterval\""
                        )
                        enableNow(context, enableIntent)
                        return true
                    }
                }
            } else if (trimmedInterval.isNotEmpty()) {
                Log.w(this::class.simpleName, "\"$trimmedInterval\" does not match the regex")
                // TODO: send warning to user that his interval doesn't match the pattern
            }
        }

        return false
    }


    private fun enableNow(context: Context, enableIntent: Intent) {
        context.sendBroadcast(enableIntent)
        // TODO: send info notif that the wakelight was enabled?
    }


    private fun stopWakeLight(context: Context) {
        // create an intent to stop the wakelight and broadcast it
        // so it can be received by tbe WakeLightStopper
        val stopWakeLightIntent = Intent(context, WakeLightStopper::class.java).apply {
            action = "com.richardswesterhof.wakelightcompanion.STOP_WAKELIGHT_ALARM"
        }
        context.sendBroadcast(stopWakeLightIntent)
    }


    private fun removeFromStorage() {
        with(internalPref.edit()) {
            putLong("nextAlarmMillis", 0)
            apply()
        }
    }

    private fun storeLastReceivedAlarm(date: Date) {
        with(internalPref.edit()) {
            putLong("lastReceivedAlarmMillis", date.time)
            apply()
        }
    }
}
