package com.richardswesterhof.wakelightcompanion.settings_page.custom_prefences

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceDialogFragmentCompat

class DevicePreferenceDialog(private val pref: DevicePreference) :
    PreferenceDialogFragmentCompat() {

    init {
        arguments = Bundle().apply { putString(ARG_KEY, pref.key) }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onCreateDialogView(context: Context): View {
        // TODO implement custom view here
        return super.onCreateDialogView(context)
    }


}