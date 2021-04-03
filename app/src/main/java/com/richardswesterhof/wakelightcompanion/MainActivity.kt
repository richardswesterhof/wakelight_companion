package com.richardswesterhof.wakelightcompanion

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.richardswesterhof.wakelightcompanion.settings_page.SettingsActivity
import java.util.*


class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setPreferencesToDefaultIfUndefined()

        replacePlaceholders()

        val enableNotifCat = getString(R.string.notif_cat_enable_name)
        val enableNotifDesc = getString(R.string.notif_cat_enable_desc)

        val stopNotifCat = getString(R.string.notif_cat_stop_name)
        val stopNotifDesc = getString(R.string.notif_cat_stop_desc)

        createNotificationChannel(
            enableNotifCat, enableNotifDesc, NotificationManager.IMPORTANCE_HIGH, getString(
                R.string.notif_cat_enable_id
            )
        )
        createNotificationChannel(
            stopNotifCat, stopNotifDesc, NotificationManager.IMPORTANCE_LOW, getString(
                R.string.notif_cat_stop_id
            )
        )
    }


    private fun setPreferencesToDefaultIfUndefined() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
    }


    private fun replacePlaceholders() {
        val textView = findViewById<View>(R.id.text_next_user_alarm) as TextView
        val sharedPrefs = this.getSharedPreferences(getString(R.string.preference_file_store_internal_vars), Context.MODE_PRIVATE)
        val nextAlarm = sharedPrefs.getLong("nextAlarmMillis", 0)
        val nextDate = Date(nextAlarm)
        textView.text = nextDate.toString()
    }


    private fun createNotificationChannel(
        name: String,
        descriptionText: String,
        importance: Int,
        id: String
    ) {
        val channel = NotificationChannel(id, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


    fun goToSetting(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}