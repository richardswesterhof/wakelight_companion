package com.richardswesterhof.wakelightcompanion.devices.yeelight

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mollin.yapi.YeelightDevice
import com.mollin.yapi.command.YeelightCommand
import com.mollin.yapi.enumeration.YeelightFlowAction
import com.mollin.yapi.flow.YeelightFlow
import com.mollin.yapi.flow.transition.YeelightColorTemperatureTransition
import com.richardswesterhof.wakelightcompanion.devices.IWakeLightImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Arrays

/**
 * the methods in this file will actually send the request to the Yeelight bulb
 * at the given IP, with the option to give a port as well
 */
class YeelightImpl : ViewModel(), IWakeLightImpl<YeelightConfig> {

    override fun startWakeLight(context: Context, config: YeelightConfig) {
        val flow = createFlow(
            config.getDuration(),
            config.getEndingColorTemp(),
            config.getEndingBrightness()
        )

        // create a new coroutine to move the execution off the UI thread
        viewModelScope.launch(Dispatchers.IO) {
            val device =
                config.getPort()?.let { YeelightDevice(config.getIp(), it) } ?: YeelightDevice(
                    config.getIp()
                )
            initDevice(device, config)
            device.startFlow(flow)
            Log.d("startWakeLight", "done")
        }
    }

    override fun stopWakeLight(config: YeelightConfig) {
        // create a new coroutine to move the execution off the UI thread
        viewModelScope.launch(Dispatchers.IO) {
            val device =
                config.getPort()?.let { YeelightDevice(config.getIp(), it) } ?: YeelightDevice(
                    config.getIp()
                )
            device.stopFlow()
            Log.d("stopWakeLight", "sent request to wakelight")
        }
    }

    private fun initDevice(device: YeelightDevice, config: YeelightConfig) {
        // send a custom command to power on the wakelight with custom color temp and brightness settings
        // right from the start, so no flashing the previous settings
        val cmd =
            YeelightCommand(
                "set_scene",
                "ct",
                config.getStartingColorTemp(),
                config.getStartingBrightness()
            )
        val response = device.sendCommand(cmd)
        Log.d("initdevice", Arrays.toString(response))
    }

    fun createFlow(duration: Int, colorTemp: Int, brightness: Int): YeelightFlow {
        val trans1 = YeelightColorTemperatureTransition(colorTemp, duration, brightness)
        val flow = YeelightFlow(1, YeelightFlowAction.STAY)
        flow.transitions.add(trans1)
        return flow
    }
}