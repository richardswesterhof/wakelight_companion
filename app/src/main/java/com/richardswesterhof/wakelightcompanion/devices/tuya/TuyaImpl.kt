package com.richardswesterhof.wakelightcompanion.devices.tuya

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.richardswesterhof.wakelightcompanion.devices.IWakeLightImpl
import com.richardswesterhof.wakelightcompanion.utils.UniformStepCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.ceil

/**
 * the methods in this file will actually send the request to the Tuya device
 * using the given config
 */
class TuyaImpl : ViewModel(), IWakeLightImpl<TuyaConfig> {

    val SECS_PER_STEP = 10.0
    val SECS_PER_MIN = 60.0
    val STEPS_PER_REQ = 8

    val MAX_GRADIENT_DURATION = 100;
    val MAX_SWITCH_DURATION = 100;

    fun sendStartCommand(context: Context, config: TuyaConfig, jsonRequest: JSONObject) {
        // create a new coroutine to move the execution off the UI thread
        viewModelScope.launch(Dispatchers.IO) {
            GlobalScope.launch(Dispatchers.IO) {
                val api = TuyaApi(context)
                api.commandDevice(config.id, jsonRequest.toString())
            }
            Log.d("startWakeLight", "done")
        }
    }

    override fun startWakeLight(context: Context, config: TuyaConfig) {
        val requests = ceil(config.durationInMinutes * SECS_PER_MIN / STEPS_PER_REQ / SECS_PER_STEP)
//        val stepDuration = config.durationInMinutes * SECS_PER_MIN / requests * STEPS_PER_REQ
        val stepCalculator = UniformStepCalculator(
            requests * STEPS_PER_REQ,
            config.startingBrightness,
            config.endingBrightness,
            config.startingColorTemp,
            config.endingColorTemp,
            0.0,
            config.durationInMinutes * SECS_PER_MIN
        )

        // Below code outlines the basic principle of constructing each individual request
        val baseCommands = JSONArray()
        baseCommands.put(JSONObject(mapOf("code" to "switch_led", "value" to true)))
        baseCommands.put(JSONObject(mapOf("code" to "work_mode", "value" to "scene")))
        for (request in 0 until requests.toInt()) {
            val sceneUnits = JSONArray()
            for (step in 0 until STEPS_PER_REQ) {
                val absoluteStep = request * STEPS_PER_REQ + step
                val sceneUnit = createSceneUnit(absoluteStep, stepCalculator)
                sceneUnits.put(sceneUnit)
            }
            val sceneData = JSONObject()
            sceneData.put("scene_num", 1)
            sceneData.put("scene_units", sceneUnits)

            val jsonCommands = JSONArray(baseCommands)
            jsonCommands.put(JSONObject(mapOf("code" to "scene_data", "value" to sceneData)))

            val jsonRequest = JSONObject()
            jsonRequest.put("commands", jsonCommands)

            sendStartCommand(context, config, jsonRequest)
            // For now the we just sleep, but eventually we could use scheduled intents
            if (request < requests - 1) SystemClock.sleep((stepCalculator.calcDuration(STEPS_PER_REQ) * 1000).toLong())
        }

//        GlobalScope.launch(Dispatchers.IO) {
//            val api = TuyaApi(context)
//            val commands = """{
//  "commands": [
//    {
//      "code": "switch_led",
//      "value": true
//    },
//    {
//      "code": "work_mode",
//      "value": "scene"
//    },
//    {
//      "code": "scene_data",
//      "value": {
//        "scene_num": 1,
//        "scene_units": [
//          {
//            "bright": 0,
//            "temperature": 0,
//            "unit_change_mode": "gradient",
//            "unit_gradient_duration": 0,
//            "unit_switch_duration": 0,
//            "h": 0,
//            "s": 0,
//            "v": 0
//          },
//          {
//            "bright": 125,
//            "temperature": 0,
//            "unit_change_mode": "gradient",
//            "unit_gradient_duration": 0,
//            "unit_switch_duration": 0,
//            "h": 0,
//            "s": 0,
//            "v": 0
//          },
//          {
//            "bright": 250,
//            "temperature": 0,
//            "unit_change_mode": "gradient",
//            "unit_gradient_duration": 0,
//            "unit_switch_duration": 0,
//            "h": 0,
//            "s": 0,
//            "v": 0
//          },
//          {
//            "bright": 375,
//            "temperature": 0,
//            "unit_change_mode": "gradient",
//            "unit_gradient_duration": 0,
//            "unit_switch_duration": 0,
//            "h": 0,
//            "s": 0,
//            "v": 0
//          },
//          {
//            "bright": 500,
//            "temperature": 0,
//            "unit_change_mode": "gradient",
//            "unit_gradient_duration": 0,
//            "unit_switch_duration": 0,
//            "h": 0,
//            "s": 0,
//            "v": 0
//          },
//          {
//            "bright": 625,
//            "temperature": 0,
//            "unit_change_mode": "gradient",
//            "unit_gradient_duration": 0,
//            "unit_switch_duration": 0,
//            "h": 0,
//            "s": 0,
//            "v": 0
//          },
//          {
//            "bright": 750,
//            "temperature": 0,
//            "unit_change_mode": "gradient",
//            "unit_gradient_duration": 0,
//            "unit_switch_duration": 0,
//            "h": 0,
//            "s": 0,
//            "v": 0
//          },
//          {
//            "bright": 875,
//            "temperature": 0,
//            "unit_change_mode": "gradient",
//            "unit_gradient_duration": 0,
//            "unit_switch_duration": 0,
//            "h": 0,
//            "s": 0,
//            "v": 0
//          }
//        ]
//      }
//    }
//  ]
//}"""
//            api.commandDevice("bf299ec91da11b5b13i8gw", commands)
//            api.commandDevice(config.id, commands)
//            val id = config.getYeelightId() ?: ""
//            // first try to find out if the location of the light is stored in cache
//            val device: YeelightDeviceMeta? = getDeviceById(context, id)
//            device?.let {
//                Log.d(
//                    "device found",
//                    "Device with id $id has been found on the network on ip ${device.ip}"
//                )
//                sendStartCommand(context, config)
//                sendDisableNotif(context, id)
//            } ?: run {
//                Log.w("device not found", "Could not find device with id '$id'")
//                sendNotifIdNotActive(context, id)
//            }
//        }
    }

    override fun stopWakeLight(context: Context, config: TuyaConfig) {
        val commands = JSONArray()
        commands.put(JSONObject(mapOf("code" to "switch_led", "value" to true)))
        commands.put(JSONObject(mapOf("code" to "work_mode", "value" to "scene")))
        val sceneData = JSONObject(mapOf("scene_num" to 1, "scene_units" to JSONArray()))
        commands.put(JSONObject(mapOf("code" to "scene_data", "value" to sceneData)))

        val jsonRequest = JSONObject()
        jsonRequest.put("commands", commands)

        viewModelScope.launch(Dispatchers.IO) {
            val api = TuyaApi(context)
            api.commandDevice(config.id, jsonRequest.toString())
            Log.d("stopWakeLight", "sent request to wakelight")
        }
    }

    private fun createSceneUnit(step: Int, stepCalculator: UniformStepCalculator): JSONObject {
        val retVal = JSONObject()
        retVal.put("bright", stepCalculator.calcBrightness(step))
        retVal.put("temperature", stepCalculator.calcTemperature(step))
        retVal.put("unit_change_mode", "gradient")
        // durations, not sure if both are used in gradient mode or only the gradient duration
//        retVal.put("unit_gradient_duration", stepCalculator.calcDuration(step))
        retVal.put("unit_gradient_duration", 1) // always evaluate at step 1 for relative duration
//        retVal.put("unit_switch_duration", stepCalculator.calcDuration(step))
        retVal.put("unit_switch_duration", 1) // always evaluate at step 1 for relative duration
        // We don't use HSV, but they are still mandatory in the request structure
        retVal.put("h", 0)
        retVal.put("s", 0)
        retVal.put("v", 0)

        return retVal
    }
}
