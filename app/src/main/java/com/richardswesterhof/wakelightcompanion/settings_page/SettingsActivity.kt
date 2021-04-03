package com.richardswesterhof.wakelightcompanion.settings_page

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.richardswesterhof.wakelightcompanion.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }
}