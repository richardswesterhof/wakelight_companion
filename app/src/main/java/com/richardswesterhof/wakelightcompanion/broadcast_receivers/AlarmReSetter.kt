package com.richardswesterhof.wakelightcompanion.broadcast_receivers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.richardswesterhof.wakelightcompanion.R
import java.util.*

private val listeningFors = listOf("android.intent.action.BOOT_COMPLETED")

class AlarmReSetter: ExtendedBroadcastReceiver(listeningFors) {

    override fun trigger(context: Context, intent: Intent) {
        val sharedPrefs = context.getSharedPreferences(context.getString(R.string.preference_file_store_alarms), Context.MODE_PRIVATE)
        val date = Date(sharedPrefs.getLong("nextAlarmMillis", 0))

        val enableWakeLightIntent = Intent(context, WakeLightEnableRequestReceiver::class.java).apply {
            action = "com.richardswesterhof.wakelightcompanion.SET_WAKELIGHT_ALARM"
            putExtra("date", date)
            // intentionally leave out the notification id, since this request did not come from a notification
            // and the WakeLightEnableRequestReceiver will simply skip cancelling the notification if we leave it out
        }

        // broadcast the action that asks the program to enable the wakelight
        // for the alarm at the specified date (in the "date" extra in the intent)
        context.sendBroadcast(enableWakeLightIntent)
    }

}