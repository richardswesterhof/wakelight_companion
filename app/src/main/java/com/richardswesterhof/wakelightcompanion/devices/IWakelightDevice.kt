package com.richardswesterhof.wakelightcompanion.devices

import android.content.Context

interface IWakeLightImpl<DeviceConfig> {

    fun startWakeLight(context: Context, config: DeviceConfig)

    fun stopWakeLight(config: DeviceConfig)
}