package com.richardswesterhof.wakelightcompanion.devices.tuya

import android.content.Context
import androidx.lifecycle.ViewModel
import com.richardswesterhof.wakelightcompanion.devices.IWakeLightImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private val deviceCache = "device_cache.csv"
private val HEADER_DEVICE_ID = "id"
private val HEADER_DEVICE_IP = "ip"
private val HEADER_DEVICE_PORT = "port"

/**
 * the methods in this file will actually send the request to the Tuya device
 * using the given config
 */
class TuyaImpl : ViewModel(), IWakeLightImpl<TuyaConfig> {

    fun sendStartCommand(context: Context, config: TuyaConfig) {

        // create a new coroutine to move the execution off the UI thread
//        viewModelScope.launch(Dispatchers.IO) {
//            val device =
//                config.getPort()?.let { YeelightDevice(config.getIp(), it) } ?: YeelightDevice(
//                    config.getIp()
//                )
//            initDevice(device, config)
//            device.startFlow(flow)
//            Log.d("startWakeLight", "done")
//        }
    }

    override fun startWakeLight(context: Context, config: TuyaConfig) {
        GlobalScope.launch(Dispatchers.IO) {
            val api = TuyaApi(context)
            val commands = """{
  "commands": [
    {
      "code": "switch_led",
      "value": true
    },
    {
      "code": "work_mode",
      "value": "scene"
    },
    {
      "code": "scene_data",
      "value": {
        "scene_num": 1,
        "scene_units": [
          {
            "bright": 0,
            "temperature": 0,
            "unit_change_mode": "gradient",
            "unit_gradient_duration": 0,
            "unit_switch_duration": 0,
            "h": 0,
            "s": 0,
            "v": 0
          },
          {
            "bright": 125,
            "temperature": 0,
            "unit_change_mode": "gradient",
            "unit_gradient_duration": 0,
            "unit_switch_duration": 0,
            "h": 0,
            "s": 0,
            "v": 0
          },
          {
            "bright": 250,
            "temperature": 0,
            "unit_change_mode": "gradient",
            "unit_gradient_duration": 0,
            "unit_switch_duration": 0,
            "h": 0,
            "s": 0,
            "v": 0
          },
          {
            "bright": 375,
            "temperature": 0,
            "unit_change_mode": "gradient",
            "unit_gradient_duration": 0,
            "unit_switch_duration": 0,
            "h": 0,
            "s": 0,
            "v": 0
          },
          {
            "bright": 500,
            "temperature": 0,
            "unit_change_mode": "gradient",
            "unit_gradient_duration": 0,
            "unit_switch_duration": 0,
            "h": 0,
            "s": 0,
            "v": 0
          },
          {
            "bright": 625,
            "temperature": 0,
            "unit_change_mode": "gradient",
            "unit_gradient_duration": 0,
            "unit_switch_duration": 0,
            "h": 0,
            "s": 0,
            "v": 0
          },
          {
            "bright": 750,
            "temperature": 0,
            "unit_change_mode": "gradient",
            "unit_gradient_duration": 0,
            "unit_switch_duration": 0,
            "h": 0,
            "s": 0,
            "v": 0
          },
          {
            "bright": 875,
            "temperature": 0,
            "unit_change_mode": "gradient",
            "unit_gradient_duration": 0,
            "unit_switch_duration": 0,
            "h": 0,
            "s": 0,
            "v": 0
          }
        ]
      }
    }
  ]
}"""
            api.commandDevice("bf299ec91da11b5b13i8gw", commands)
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
        }
    }

    override fun stopWakeLight(config: TuyaConfig) {
        // create a new coroutine to move the execution off the UI thread
//        viewModelScope.launch(Dispatchers.IO) {
//            val device =
//                config.getPort()?.let { YeelightDevice(config.getIp(), it) } ?: YeelightDevice(
//                    config.getIp()
//                )
//            device.stopFlow()
//            Log.d("stopWakeLight", "sent request to wakelight")
//        }
    }

    private fun getDeviceById(context: Context, id: String) {

    }
}
