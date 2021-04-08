package com.richardswesterhof.wakelightcompanion.implementation_details

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.mollin.yapi.YeelightDevice
import com.mollin.yapi.enumeration.YeelightEffect
import com.mollin.yapi.enumeration.YeelightFlowAction
import com.mollin.yapi.flow.YeelightFlow
import com.mollin.yapi.flow.transition.YeelightColorTemperatureTransition
import com.mollin.yapi.utils.YeelightUtils
import kotlinx.coroutines.*


private val maxColorTemp = 6500
private val minColorTemp = 1700


/**
 * the methods in this file will actually send the request to the Yeelight bulb
 * at the given IP, with the option to give a port as well
 */
class YeelightWrapper: ViewModel() {

    private lateinit var sharedPrefs: SharedPreferences

    fun startWakeLight(context: Context, ip: String, port: Int?) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        //get all variables from shared preferences (aka settings)
        val duration1Minutes = clampStringPref("pref_wakelight_duration1", 15, 0, Integer.MAX_VALUE)
        val duration2Minutes = clampStringPref("pref_wakelight_duration2", 15, 0, Integer.MAX_VALUE)
        val duration1: Int = duration1Minutes * 60 * 1000
        val duration2: Int = duration2Minutes * 60 * 1000

        val startingColorTemp = clampStringPref("pref_wakelight_start_color_temp", 1700, minColorTemp, maxColorTemp)
        val midColorTemp = clampStringPref("pref_wakelight_mid_color_temp", 2000, minColorTemp, maxColorTemp)
        val endingColorTemp = clampStringPref("pref_wakelight_end_color_temp", 5000, minColorTemp, maxColorTemp)
        val startingBrightness = clampIntPref("pref_wakelight_start_brightness", 1, 0, 100)
        val midBrightness = clampIntPref("pref_wakelight_mid_brightness", 50, 0, 100)
        val endingBrightness = clampIntPref("pref_wakelight_end_brightness", 100, 0, 100)

        // create yeelight flow
        val trans1 = YeelightColorTemperatureTransition(midColorTemp, duration1, midBrightness)
        val trans2 = YeelightColorTemperatureTransition(endingColorTemp, duration2, endingBrightness)
        val flow = YeelightFlow(1, YeelightFlowAction.STAY)
        flow.transitions.addAll(listOf(trans1, trans2))

        // create a new coroutine to move the execution off the UI thread
        viewModelScope.launch(Dispatchers.IO) {
            val device = port?.let { YeelightDevice(ip, port) } ?: YeelightDevice(ip)
//            Log.i("startWakeLight", device.getProperties().toString())
            device.setPower(true)
            device.setColorTemperature(startingColorTemp)
            device.setBrightness(startingBrightness)
            device.setEffect(YeelightEffect.SMOOTH)
            device.startFlow(flow)
        }
    }

    fun clampStringPref(pref: String, default: Int, min: Int, max: Int): Int {
        return YeelightUtils.clamp(sharedPrefs.getString(pref, default.toString())!!.toInt(), min, max)
    }

    fun clampIntPref(pref: String, default: Int, min: Int, max: Int): Int {
        return YeelightUtils.clamp(sharedPrefs.getInt(pref, default), min, max)
    }

    fun stopWakeLight(ip: String, port: Int?) {
        // create a new coroutine to move the execution off the UI thread
        viewModelScope.launch(Dispatchers.IO) {
            val device = port?.let { YeelightDevice(ip, port) } ?: YeelightDevice(ip)
//            Log.i("startWakeLight", device.getProperties().toString())
            device.stopFlow()
        }
    }
}
