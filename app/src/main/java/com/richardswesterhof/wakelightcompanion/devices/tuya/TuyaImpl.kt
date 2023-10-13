package com.richardswesterhof.wakelightcompanion.devices.tuya

import android.content.Context
import androidx.lifecycle.ViewModel
import com.richardswesterhof.wakelightcompanion.devices.IWakeLightImpl

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
//        GlobalScope.launch(Dispatchers.IO) {
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
