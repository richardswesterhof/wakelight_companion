package com.richardswesterhof.wakelightcompanion.implementation_details

import android.content.Context

interface IWakeLightDevice<DeviceType> {

    fun startWakeLight(context: Context, config: Config<DeviceType>)

    fun stopWakeLight(context: Context, config: Config<DeviceType>)
}