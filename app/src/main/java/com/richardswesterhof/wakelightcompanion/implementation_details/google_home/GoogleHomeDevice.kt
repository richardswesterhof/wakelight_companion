package com.richardswesterhof.wakelightcompanion.implementation_details.google_home

import android.content.Context
import androidx.lifecycle.ViewModel
import com.richardswesterhof.wakelightcompanion.implementation_details.Config
import com.richardswesterhof.wakelightcompanion.implementation_details.IWakeLightDevice

/**
 * A wrapper for any device added to the user's Google Home.
 * Will be controlled by programmatically sending commands to Google Assistant
 */
class GoogleHomeDevice: ViewModel(), IWakeLightDevice<GoogleHomeDevice> {
    override fun startWakeLight(context: Context, config: Config<GoogleHomeDevice>) {
        TODO("Not yet implemented")
    }

    override fun stopWakeLight(context: Context, config: Config<GoogleHomeDevice>) {
        TODO("Not yet implemented")
    }


}