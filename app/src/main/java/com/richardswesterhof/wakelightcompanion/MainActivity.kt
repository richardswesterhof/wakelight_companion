package com.richardswesterhof.wakelightcompanion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.richardswesterhof.wakelightcompanion.settings_page.SettingsFragment


class MainActivity : AppCompatActivity() {

    private lateinit var sharedPrefs: SharedPreferences

    private var navListener = BottomNavigationView.OnNavigationItemSelectedListener {
        val nextAlarmMillis = sharedPrefs.getLong("nextAlarmMillis", 0)
        val selectedFragment: Fragment? = when (it.itemId) {
            R.id.nav_home -> MainFragment.newInstance(nextAlarmMillis)
            R.id.nav_settings -> SettingsFragment.newInstance()
            else -> null
        }

        if (selectedFragment == null) {
            Log.e(this::class.simpleName, "Cannot navigate to: \"${it.itemId}\": unknown id")
            false
        } else {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit()

            true
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPrefs = this.getSharedPreferences(
            getString(R.string.preference_file_store_internal_vars),
            Context.MODE_PRIVATE
        )
        setPreferencesToDefaultIfUndefined()

        createNotificationChannels()

        // add the bottom navigation
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener(navListener)

        val nextAlarmMillis = sharedPrefs.getLong("nextAlarmMillis", 0)

        // set main fragment active
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, MainFragment.newInstance(nextAlarmMillis))
            .commit()
    }


    private fun setPreferencesToDefaultIfUndefined() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
    }


    private fun createNotificationChannels() {
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
}
