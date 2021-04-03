package com.richardswesterhof.wakelightcompanion.settings_page

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.richardswesterhof.wakelightcompanion.R


class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // we have to do this bs because google can't implement using the "inputType" property in the xml properly :)
        val editTextPreferenceDuration = preferenceManager.findPreference<EditTextPreference>("pref_wakelight_duration")
        editTextPreferenceDuration!!.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }

        val editTextPreferencePort = preferenceManager.findPreference<EditTextPreference>("pref_wakelight_port")
        editTextPreferencePort!!.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }
    }
}