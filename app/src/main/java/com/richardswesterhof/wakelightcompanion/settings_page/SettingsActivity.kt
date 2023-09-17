package com.richardswesterhof.wakelightcompanion.settings_page

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
