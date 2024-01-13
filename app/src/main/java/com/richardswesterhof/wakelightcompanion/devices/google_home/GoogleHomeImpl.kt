package com.richardswesterhof.wakelightcompanion.devices.google_home

import android.content.Context
import androidx.lifecycle.ViewModel
import com.richardswesterhof.wakelightcompanion.devices.IWakeLightImpl

/**
 * A wrapper for any device added to the user's Google Home.
 * Will be controlled by programmatically sending commands to Google Assistant
 */
class GoogleHomeImpl : ViewModel(), IWakeLightImpl<GoogleHomeImpl> {
    override fun startWakeLight(context: Context, config: GoogleHomeImpl) {
        TODO("Not yet implemented")
    }

    override fun stopWakeLight(context: Context, config: GoogleHomeImpl) {
        TODO("Not yet implemented")
    }


}