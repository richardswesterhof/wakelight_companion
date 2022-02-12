package com.richardswesterhof.wakelightcompanion.implementation_details

import android.app.Notification
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.mollin.yapi.YeelightDevice
import com.mollin.yapi.YeelightDeviceMeta
import com.mollin.yapi.YeelightDiscoveryManager
import com.mollin.yapi.command.YeelightCommand
import com.mollin.yapi.enumeration.YeelightFlowAction
import com.mollin.yapi.exception.YeelightSocketException
import com.mollin.yapi.flow.YeelightFlow
import com.mollin.yapi.flow.transition.YeelightColorTemperatureTransition
import com.mollin.yapi.utils.YeelightUtils
import com.richardswesterhof.wakelightcompanion.R
import com.richardswesterhof.wakelightcompanion.utils.IdManager
import kotlinx.coroutines.*
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.Reader
import java.util.*
import kotlin.concurrent.schedule


private val maxColorTemp = 6500
private val minColorTemp = 1700

private val deviceCache =  "device_cache.csv"
private val HEADER_DEVICE_ID = "id"
private val HEADER_DEVICE_IP = "ip"
private val HEADER_DEVICE_PORT = "port"


/**
 * the methods in this file will actually send the request to the Yeelight bulb
 * at the given IP, with the option to give a port as well
 */
class YeelightWrapper: ViewModel() {

    private lateinit var sharedPrefs: SharedPreferences

    private var varsInited: Boolean = false

    private var duration1Minutes: Int = 0
    private var duration1: Int = 0
    private var startingColorTemp: Int = 0
    private var endingColorTemp: Int = 0
    private var endingBrightness: Int = 0
    private var startingBrightness: Int = 1


    fun startWakeLight(context: Context, id: String) {
        GlobalScope.launch(Dispatchers.IO) {
            // first try to find out if the location of the light is stored in cache
            val device: YeelightDeviceMeta? = getDeviceById(context, id)
            device?.let{
                Log.d("device found", "Device with id $id has been found on the network on ip ${device.ip}")
                startWakeLight(context, device.ip, device.port)
            } ?: run {
                Log.w("device not found", "Could not find device with id '$id'")
                sendNotifIdNotActive(context, id)
            }
        }
    }


    private fun getDeviceById(context: Context, id: String): YeelightDeviceMeta? {
        return getDeviceFromCache(context, id)?.meta ?: run {
            Log.d("device_location", "Device location is not cached, asking active bulbs on the network to identify themselves")
            val devices: Set<YeelightDeviceMeta> = YeelightDiscoveryManager.search()
            batchUpsertCache(context, devices)
            // there should only be one device that matches the id
            with(devices.filter { it.id == id }) {
                when {
                    size > 0 -> this[0]
                    else -> null
                }
            }
        }
    }


    fun batchUpsertCache(context: Context, devices: Iterable<YeelightDeviceMeta>) {
        val f = File(context.cacheDir, deviceCache)
        val test = File(context.cacheDir, "tesfile")
        test.createNewFile()
        if(!f.exists()) f.createNewFile()
        val parser = CSVParser(FileReader(f), CSVFormat.DEFAULT.withFirstRecordAsHeader())
        val list = parser.records

        f.delete()
        f.createNewFile()
        val printer = CSVPrinter(FileWriter(f), CSVFormat.DEFAULT.withHeader(HEADER_DEVICE_ID, HEADER_DEVICE_IP, HEADER_DEVICE_PORT))
        printer.flush()
        val remainingDevices = HashSet<YeelightDeviceMeta>().apply { addAll(devices) }
        for(record in list) {
            val newRecord = HashMap<String, Any>().apply { putAll(record.toMap()) }
            with(remainingDevices.filter {it.id == record[HEADER_DEVICE_ID] }) {
                val dev = this[0]
                when {
                    size > 0 -> {
                        remainingDevices.remove(dev)
                        dev.ip?.run { newRecord[HEADER_DEVICE_IP] = this }
                        dev.port?. run { newRecord[HEADER_DEVICE_PORT] = this }
                        Log.d("cache_info", "Device '${dev.id}' has been updated in cache to ${dev.ip}:${dev.port}")
                    }
                    else -> Log.w("cache_info", "Device '${dev.id}' was in the cache but not in the found devices, will be deleted to keep cache clean")
                }
            }
            printer.printRecord(record)
        }
        // the remaining devices at this point are new devices to the cache
        remainingDevices.forEach {
            printer.printRecord(listOf(it.id, it.ip, it.port))
            Log.d("cache_info", "Device '${it.id}' has been added to cache as ${it.ip}:${it.port}")
        }
        printer.flush()
        parser.close()
        printer.close()
    }


    fun getDeviceFromCache(context: Context, deviceId: String): YeelightDevice? {
        val cacheDir = context.cacheDir
        return if(cacheDir.exists()) {
            val deviceFile = File(cacheDir, deviceCache)
            if(deviceFile.exists()) {
                // Read the deviceFile contents to find the last known IP of the provided ID
                val input: Reader = FileReader(deviceFile)
                val records: Iterable<CSVRecord> = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(input)
                with(records.filter { it[HEADER_DEVICE_ID] == deviceId }) {
                    val device = this[0]
                    when {
                        size > 0 -> {
                            val metaInfo = YeelightDeviceMeta().apply {
                                this.id = deviceId
                                this.ip = device[HEADER_DEVICE_IP]
                                this.port = device[HEADER_DEVICE_PORT].toInt()
                            }.also {
                                Log.d("cache_info", "Device '${it.id}' found in cache: ${it.ip}:${it.port}")
                            }
                            try {
                                val dev = YeelightDevice(metaInfo)
                                // verify that this device is actually the device we are looking for
                                if(dev.refreshMetaInfo().id == deviceId) {
                                    Log.i("cache_info", "Device '${deviceId}' has been confirmed to still reside at ${dev.meta.ip}:${dev.meta.port}")
                                    dev
                                }
                                else null
                            }
                            catch(e: YeelightSocketException) {
                                Log.d("cache_info", "Socket exception occurred while connecting to '${metaInfo.id}' at ${metaInfo.ip}:${metaInfo.port}")
                                invalidateCache(context, metaInfo.id)
                                null
                            }
                        }
                        else -> null
                    }
                }
            }
            else null
        }
        else null
    }


    fun invalidateCache(context: Context, deviceId: String) {
        val f = File(context.cacheDir, deviceCache)
        val parser = CSVParser(FileReader(f), CSVFormat.DEFAULT)
        val list = parser.records

        f.delete()
        val printer = CSVPrinter(FileWriter(f), CSVFormat.DEFAULT.withHeader(HEADER_DEVICE_ID, HEADER_DEVICE_IP, HEADER_DEVICE_PORT))
        for(record in list) {
            if(record[HEADER_DEVICE_ID] != deviceId) printer.print(record)
        }
        printer.flush()
        parser.close()
        printer.close()
        Log.d("cache_info", "Device '$deviceId' removed from cache")
    }


    fun sendNotifIdNotActive(context: Context, id: String) {
        val nextNotificationId = IdManager.getNextNotifId(context)

        val builder = NotificationCompat.Builder(context, context.getString(R.string.notif_cat_warning_id))
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.warning)
            .setContentTitle(context.getString(R.string.notif_device_not_found_title))
            .setContentText(context.getString(R.string.notif_device_not_found_content, id))
            .setColor(context.getColor(R.color.navy_blue_light))
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(nextNotificationId, builder.build())
            Log.d(this::class.simpleName,"Sent the notification")
        }
    }


    fun startWakeLight(context: Context, ip: String, port: Int?) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        initVars()
        val flow = createFlow(duration1, endingColorTemp, endingBrightness)

        // create a new coroutine to move the execution off the UI thread
        viewModelScope.launch(Dispatchers.IO) {
            val device = port?.let { YeelightDevice(ip, port) } ?: YeelightDevice(ip)
            initDevice(device)
            Timer().schedule(1000){
                // wait 1000ms because the device doesn't like so many requests in a short time
                device.startFlow(flow)
            }
            Log.d("startWakeLight", "successfully started wakelight")
        }
    }


    private fun initVars() {
        //get all variables from shared preferences (aka settings)
        duration1Minutes = clampStringPref("pref_wakelight_duration1", /* 15 */30, 0, Integer.MAX_VALUE)
        duration1 = duration1Minutes * 60 * 1000
        startingColorTemp = clampStringPref("pref_wakelight_start_color_temp", 1700, minColorTemp, maxColorTemp)
        endingColorTemp = clampStringPref("pref_wakelight_end_color_temp", 5000, minColorTemp, maxColorTemp)
        startingBrightness = clampIntPref("pref_wakelight_start_brightness", 1, 0, 100)
        endingBrightness = clampIntPref("pref_wakelight_end_brightness", 100, 0, 100)

        varsInited = true
    }


    private fun initDevice(device: YeelightDevice) {
        if(!varsInited) initVars()
        // send a custom command to power on the wakelight with custom color temp and brightness settings
        // right from the start, so no flashing the previous settings
        val cmd = YeelightCommand("set_scene", "ct", startingColorTemp, startingBrightness)
        val response = device.sendCommand(cmd)
        Log.d("initdevice", Arrays.toString(response))
    }


    fun createFlow(duration: Int, colorTemp: Int, brightness: Int): YeelightFlow {
        val trans1 = YeelightColorTemperatureTransition(colorTemp, duration, brightness)
        val flow = YeelightFlow(1, YeelightFlowAction.STAY)
        flow.transitions.add(trans1)
        return flow
    }


    fun clampStringPref(pref: String, default: Int, min: Int, max: Int): Int {
        return YeelightUtils.clamp(sharedPrefs.getString(pref, default.toString())!!.toInt(), min, max)
    }


    fun clampIntPref(pref: String, default: Int, min: Int, max: Int): Int {
        return YeelightUtils.clamp(sharedPrefs.getInt(pref, default), min, max)
    }


    fun stopWakeLight(context: Context, deviceId: String) {
        // create a new coroutine to move the execution off the UI thread
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val device = YeelightDevice(getDeviceById(context, deviceId))
                device.setPower(false)
                Log.d("stopWakeLight", "sent request to wakelight")
            }
            catch(e: YeelightSocketException) {
                Log.e("stop_wakelight", "Could not connect to device to stop it (stacktrace below)")
                Log.e("stop_wakelight", e.toString())
            }
        }
    }
}
