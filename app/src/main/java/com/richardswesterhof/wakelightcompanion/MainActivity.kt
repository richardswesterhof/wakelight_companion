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

        val enableNotifCat = getString(R.string.notif_cat_enable_name)
        val enableNotifDesc = getString(R.string.notif_cat_enable_desc)

        val stopNotifCat = getString(R.string.notif_cat_stop_name)
        val stopNotifDesc = getString(R.string.notif_cat_stop_desc)

        createNotificationChannel(enableNotifCat, enableNotifDesc, NotificationManager.IMPORTANCE_HIGH, getString(R.string.notif_cat_enable_id))
        createNotificationChannel(stopNotifCat, stopNotifDesc, NotificationManager.IMPORTANCE_LOW, getString(R.string.notif_cat_stop_id))
    }


    private fun createNotificationChannel(name: String, descriptionText: String, importance: Int, id: String) {
        val channel = NotificationChannel(id, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}