package com.richardswesterhof.wakelightcompanion.settings_page

import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mollin.yapi.YeelightDiscoveryManager
import com.richardswesterhof.wakelightcompanion.R
import kotlinx.coroutines.*


class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        Log.d("yuppp", "settings menu created")
        val lp: ListPreference? = findPreference("pref_wakelight_id")

//        lp?.entries = arrayOf("my", "pp")
//        lp?.entryValues = arrayOf("0", "1")
//        val job = Job()
        GlobalScope.launch(Dispatchers.IO) {
            val devices = YeelightDiscoveryManager.search()
            Log.d("yeelightinfo", "found ${devices.size} devices: " + devices.map {it.model}.toString())
            lp?.entries = devices.map {it.model}.toTypedArray()
            lp?.entryValues = devices.map {it.id}.toTypedArray()
        }


        // access arguments fom newInstance like:
        // arguments.get[TYPE]("name", default)

        // we have to do this bs because google can't implement using the "inputType" property in the xml properly :)
        val editTextPreferenceDuration1 = preferenceManager.findPreference<EditTextPreference>("pref_wakelight_duration1")
        editTextPreferenceDuration1?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }

        val editTextPreferenceDuration2 = preferenceManager.findPreference<EditTextPreference>("pref_wakelight_duration2")
        editTextPreferenceDuration2?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }

        val editTextPreferenceStartColorTemp = preferenceManager.findPreference<EditTextPreference>("pref_wakelight_start_color_temp")
        editTextPreferenceStartColorTemp?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }

        val editTextPreferenceMidColorTemp = preferenceManager.findPreference<EditTextPreference>("pref_wakelight_mid_color_temp")
        editTextPreferenceMidColorTemp?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }

        val editTextPreferenceEndColorTemp = preferenceManager.findPreference<EditTextPreference>("pref_wakelight_end_color_temp")
        editTextPreferenceEndColorTemp?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }

        val editTextPreferencePort = preferenceManager.findPreference<EditTextPreference>("pref_wakelight_port")
        editTextPreferencePort?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(): SettingsFragment =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    // do put[TYPE]("name", value) here to add arguments
                }
            }
    }
}