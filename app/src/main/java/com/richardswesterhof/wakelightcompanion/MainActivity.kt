package com.richardswesterhof.wakelightcompanion

import android.app.*
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// yes this is hella ugly but idgaf
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val enableNotifCat = "Wakelight Enable Option"
        val enableNotifDesc = "Notifications that ask you if want to enable your WakeLight for your next upcoming alarm."

        val stopNotifCat = "Wakelight Stop Button"
        val stopNotifDesc = "Notifications that let you stop the WakeLight when it is going off."

        createNotificationChannel(enableNotifCat, enableNotifDesc, NotificationManager.IMPORTANCE_HIGH)
        createNotificationChannel(stopNotifCat, stopNotifDesc, NotificationManager.IMPORTANCE_LOW)
    }


    private fun createNotificationChannel(name: String, descriptionText: String, importance: Int) {
        val channel = NotificationChannel(this.resources.getString(R.string.app_name) + name, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}