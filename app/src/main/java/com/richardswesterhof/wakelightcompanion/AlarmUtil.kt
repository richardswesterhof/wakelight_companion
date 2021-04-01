package com.richardswesterhof.wakelightcompanion

import android.app.AlarmManager
import android.content.Context
import java.util.*

class AlarmUtil(context: Context) {

    val am: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun getNextAlarm(): AlarmManager.AlarmClockInfo? {
        return am.nextAlarmClock
    }

    fun getNextAlarmDate(): Date? {
        return getNextAlarm()?.triggerTime?.let { Date(it) }
    }

    fun getAlarmManager(): AlarmManager {
        return am
    }

}