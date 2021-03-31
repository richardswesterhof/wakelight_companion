package com.example.wakelightcompanion

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.lang.IllegalStateException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*

// yes this is hella ugly but idgaf
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val notificationChannel = "Wakelight Enable Option"
        val channelDescription = "sends funny notifications sometimes xd"

        createNotificationChannel(notificationChannel, channelDescription, NotificationManager.IMPORTANCE_DEFAULT)

        val builder = NotificationCompat.Builder(this, this.resources.getString(R.string.app_name) + notificationChannel)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Test notif")
            .setContentText("suck pp google")
            .setAutoCancel(true)


        with(NotificationManagerCompat.from(this)) {
            // Google: notificationId is a unique int for each notification that you must define
            // Me: haha hardcoded 1 go brrrrrr
            Log.i("suck pp", "I don't even really know what this is but we are here")
            notify(1, builder.build())
            Log.i("asd","sent the notification")
        }


        val am: AlarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextAlarm: AlarmManager.AlarmClockInfo? = am.nextAlarmClock

        nextAlarm?.triggerTime?.let { Date(it).toString() }?.let { Log.i("next_alarm_info", it) }

    }


    private fun createNotificationChannel(name: String, descriptionText: String, importance: Int) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val channel = NotificationChannel(this.resources.getString(R.string.app_name) + name, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}