package com.richardswesterhof.wakelightcompanion.implementation_details.yeelight

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.mollin.yapi.YeelightDevice
import com.mollin.yapi.command.YeelightCommand
import com.mollin.yapi.enumeration.YeelightFlowAction
import com.mollin.yapi.flow.YeelightFlow
import com.mollin.yapi.flow.transition.YeelightColorTemperatureTransition
import com.mollin.yapi.utils.YeelightUtils
import com.richardswesterhof.wakelightcompanion.implementation_details.Config
import com.richardswesterhof.wakelightcompanion.implementation_details.IWakeLightDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Arrays

private val maxColorTemp = 6500
private val minColorTemp = 1700

/**
 * the methods in this file will actually send the request to the Yeelight bulb
 * at the given IP, with the option to give a port as well
 */
class YeelightDevice: ViewModel(), IWakeLightDevice<YeelightDevice> {

    private lateinit var sharedPrefs: SharedPreferences

    private var varsInited: Boolean = false

    private var duration1Minutes: Int = 0
    private var duration1: Int = 0
    private var startingColorTemp: Int = 0
    private var endingColorTemp: Int = 0
    private var endingBrightness: Int = 0
    private var startingBrightness: Int = 1


    fun startWakeLight(context: Context, ip: String, port: Int?) {
        // TODO: deprecate this method in favor of the one from IWakeLightDevice
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        initVars()

        val flow = createFlow(duration1, /* midColorTemp */ endingColorTemp, /* midBrightness */ endingBrightness)

        // create a new coroutine to move the execution off the UI thread
        viewModelScope.launch(Dispatchers.IO) {
            val device = port?.let { YeelightDevice(ip, port) } ?: YeelightDevice(ip)
//            Log.i("startWakeLight", device.getProperties().toString())
            initDevice(device)
            device.startFlow(flow)
            Log.d("startWakeLight", "done")
        }
    }

    private fun initVars() {
        //get all variables from shared preferences (aka settings)
        duration1Minutes = clampStringPref("pref_wakelight_duration1", /* 15 */30, 0, Integer.MAX_VALUE)
//        val duration2Minutes = clampStringPref("pref_wakelight_duration2", 15, 0, Integer.MAX_VALUE)
        duration1 = duration1Minutes * 60 * 1000
//        val duration2: Int = duration2Minutes * 60 * 1000

        startingColorTemp = clampStringPref("pref_wakelight_start_color_temp", 1700,
            minColorTemp,
            maxColorTemp
        )
//        val midColorTemp = clampStringPref("pref_wakelight_mid_color_temp", 2000, minColorTemp, maxColorTemp)
        endingColorTemp = clampStringPref("pref_wakelight_end_color_temp", 5000,
            minColorTemp,
            maxColorTemp
        )
        val startingBrightness = clampIntPref("pref_wakelight_start_brightness", 1, 0, 100)
//        val midBrightness = clampIntPref("pref_wakelight_mid_brightness", 50, 0, 100)
        endingBrightness = clampIntPref("pref_wakelight_end_brightness", 100, 0, 100)

        varsInited = true
    }

    private fun initDevice(device: YeelightDevice) {
        if(!varsInited) initVars()
//        device.setPower(true)
//        device.setBrightness(startingBrightness)
//        device.setColorTemperature(startingColorTemp)
//        device.setEffect(YeelightEffect.SMOOTH)
        // send a custom command to power on the wakelight with custom color temp and brightness settings
        // right from the start, so no flashing the previous settings
        val cmd = YeelightCommand("set_scene", "ct", startingColorTemp, startingBrightness)
        val response = device.sendCommand(cmd)
        Log.d("initdevice", Arrays.toString(response))
    }

    fun createFlow(duration: Int, colorTemp: Int, brightness: Int): YeelightFlow {
        val trans1 = YeelightColorTemperatureTransition(colorTemp, duration, brightness)
//        val trans2 = YeelightColorTemperatureTransition(endingColorTemp, duration2, endingBrightness)
        val flow = YeelightFlow(1, YeelightFlowAction.STAY)
        flow.transitions.add(trans1)
//        flow.transitions.addAll(listOf(trans1, trans2))
        return flow
    }

    fun clampStringPref(pref: String, default: Int, min: Int, max: Int): Int {
        return YeelightUtils.clamp(
            sharedPrefs.getString(pref, default.toString())!!.toInt(),
            min,
            max
        )
    }

    fun clampIntPref(pref: String, default: Int, min: Int, max: Int): Int {
        return YeelightUtils.clamp(sharedPrefs.getInt(pref, default), min, max)
    }

    fun stopWakeLight(ip: String, port: Int?) {
        // TODO: deprecate this method in favor of the one from IWakeLightDevice
        // create a new coroutine to move the execution off the UI thread
        viewModelScope.launch(Dispatchers.IO) {
            val device = port?.let { YeelightDevice(ip, port) } ?: YeelightDevice(ip)
//            Log.i("startWakeLight", device.getProperties().toString())
            device.stopFlow()
            Log.d("stopWakeLight", "sent request to wakelight")
        }
    }

    override fun startWakeLight(context: Context, config: Config<YeelightDevice>) {
        TODO("Not yet implemented")
    }

    override fun stopWakeLight(context: Context, config: Config<YeelightDevice>) {
        TODO("Not yet implemented")
    }
}