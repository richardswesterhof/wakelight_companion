package com.richardswesterhof.wakelightcompanion.devices.yeelight

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mollin.yapi.YeelightDevice
import com.mollin.yapi.YeelightDeviceMeta
import com.mollin.yapi.YeelightDiscoveryManager
import com.mollin.yapi.command.YeelightCommand
import com.mollin.yapi.enumeration.YeelightFlowAction
import com.mollin.yapi.exception.YeelightSocketException
import com.mollin.yapi.flow.YeelightFlow
import com.mollin.yapi.flow.transition.YeelightColorTemperatureTransition
import com.richardswesterhof.wakelightcompanion.R
import com.richardswesterhof.wakelightcompanion.broadcast_receivers.WakeLightStopper
import com.richardswesterhof.wakelightcompanion.devices.IWakeLightImpl
import com.richardswesterhof.wakelightcompanion.utils.IdManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.Reader
import java.util.Arrays

private val deviceCache = "yeelight_device_cache.csv"
private val HEADER_DEVICE_ID = "id"
private val HEADER_DEVICE_IP = "ip"
private val HEADER_DEVICE_PORT = "port"

/**
 * the methods in this file will actually send the request to the Yeelight bulb
 * at the given IP, with the option to give a port as well
 */
class YeelightImpl : ViewModel(), IWakeLightImpl<YeelightConfig> {

    fun sendStartCommand(context: Context, config: YeelightConfig) {
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

    override fun startWakeLight(context: Context, config: YeelightConfig) {
        GlobalScope.launch(Dispatchers.IO) {
            val id = config.getYeelightId() ?: ""
            // first try to find out if the location of the light is stored in cache
            val device: YeelightDeviceMeta? = getDeviceById(context, id)
            device?.let {
                Log.d(
                    "device found",
                    "Device with id $id has been found on the network on ip ${device.ip}"
                )
                sendStartCommand(context, config)
                sendDisableNotif(context, id)
            } ?: run {
                Log.w("device not found", "Could not find device with id '$id'")
                sendNotifIdNotActive(context, id)
            }
        }
    }

    override fun stopWakeLight(context: Context, config: YeelightConfig) {
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

    private fun getDeviceById(context: Context, id: String): YeelightDeviceMeta? {
        return getDeviceFromCache(context, id)?.meta ?: run {
            Log.d(
                "device_location",
                "Device location is not cached, asking active bulbs on the network to identify themselves"
            )
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

    fun getDeviceFromCache(context: Context, deviceId: String): YeelightDevice? {
        val cacheDir = context.cacheDir
        return if (cacheDir.exists()) {
            val deviceFile = File(cacheDir, deviceCache)
            if (deviceFile.exists()) {
                // Read the deviceFile contents to find the last known IP of the provided ID
                val input: Reader = FileReader(deviceFile)
                val records: Iterable<CSVRecord> =
                    CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(input)
                with(records.filter { it[HEADER_DEVICE_ID] == deviceId }) {
                    when {
                        isNotEmpty() -> {
                            val device = this[0]
                            val metaInfo = YeelightDeviceMeta().apply {
                                this.id = deviceId
                                this.ip = device[HEADER_DEVICE_IP]
                                this.port = device[HEADER_DEVICE_PORT].toInt()
                            }.also {
                                Log.d(
                                    "cache_info",
                                    "Device '${it.id}' found in cache: ${it.ip}:${it.port}"
                                )
                            }
                            try {
                                val dev = YeelightDevice(metaInfo)
                                // verify that this device is actually the device we are looking for
                                if (dev.refreshMetaInfo().id == deviceId) {
                                    Log.i(
                                        "cache_info",
                                        "Device '${deviceId}' has been confirmed to still reside at ${dev.meta.ip}:${dev.meta.port}"
                                    )
                                    dev
                                } else null
                            } catch (e: YeelightSocketException) {
                                Log.d(
                                    "cache_info",
                                    "Socket exception occurred while connecting to '${metaInfo.id}' at ${metaInfo.ip}:${metaInfo.port}"
                                )
                                invalidateCache(context, metaInfo.id)
                                null
                            }
                        }

                        else -> null
                    }
                }
            } else null
        } else null
    }


    fun invalidateCache(context: Context, deviceId: String) {
        val f = File(context.cacheDir, deviceCache)
        val parser = CSVParser(FileReader(f), CSVFormat.DEFAULT)
        val list = parser.records

        f.delete()
        val printer = CSVPrinter(
            FileWriter(f),
            CSVFormat.DEFAULT.withHeader(HEADER_DEVICE_ID, HEADER_DEVICE_IP, HEADER_DEVICE_PORT)
        )
        for (record in list) {
            if (record[HEADER_DEVICE_ID] != deviceId) printer.print(record)
        }
        printer.flush()
        parser.close()
        printer.close()
        Log.d("cache_info", "Device '$deviceId' removed from cache")
    }

    fun batchUpsertCache(context: Context, devices: Iterable<YeelightDeviceMeta>) {
        val f = File(context.cacheDir, deviceCache)
        val test = File(context.cacheDir, "tesfile")
        test.createNewFile()
        if (!f.exists()) f.createNewFile()
        val parser = CSVParser(FileReader(f), CSVFormat.DEFAULT.withFirstRecordAsHeader())
        val list = parser.records

        f.delete()
        f.createNewFile()
        val printer = CSVPrinter(
            FileWriter(f),
            CSVFormat.DEFAULT.withHeader(HEADER_DEVICE_ID, HEADER_DEVICE_IP, HEADER_DEVICE_PORT)
        )
        printer.flush()
        val remainingDevices = HashSet<YeelightDeviceMeta>().apply { addAll(devices) }
        for (record in list) {
            val newRecord = HashMap<String, Any>().apply { putAll(record.toMap()) }
            with(remainingDevices.filter { it.id == record[HEADER_DEVICE_ID] }) {
                val dev = this[0]
                when {
                    isNotEmpty() -> {
                        remainingDevices.remove(dev)
                        dev.ip?.run { newRecord[HEADER_DEVICE_IP] = this }
                        dev.port?.run { newRecord[HEADER_DEVICE_PORT] = this }
                        Log.d(
                            "cache_info",
                            "Device '${dev.id}' has been updated in cache to ${dev.ip}:${dev.port}"
                        )
                    }

                    else -> Log.w(
                        "cache_info",
                        "Device '${dev.id}' was in the cache but not in the found devices, will be deleted to keep cache clean"
                    )
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

    fun createFlow(duration: Int, colorTemp: Int, brightness: Int): YeelightFlow {
        val trans1 = YeelightColorTemperatureTransition(colorTemp, duration, brightness)
        val flow = YeelightFlow(1, YeelightFlowAction.STAY)
        flow.transitions.add(trans1)
        return flow
    }

    fun sendNotifIdNotActive(context: Context, id: String) {
        val nextNotificationId = IdManager.getNextNotifId(context)

        val builder =
            NotificationCompat.Builder(context, context.getString(R.string.notif_cat_warning_id))
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.warning)
                .setContentTitle(context.getString(R.string.notif_device_not_found_title))
                .setContentText(context.getString(R.string.notif_device_not_found_content, id))
                .setColor(context.getColor(R.color.navy_blue_light))
                .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    "android.permission.POST_NOTIFICATIONS"
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(nextNotificationId, builder.build())
            Log.d(this::class.simpleName, "Sent the notification")
        }
    }

    fun sendDisableNotif(context: Context, id: String) {
        // the intent that we will use for the click action on the notification itself
        val stopWakeLightIntent = Intent(context, WakeLightStopper::class.java).apply {
            action = "com.richardswesterhof.wakelightcompanion.STOP_WAKELIGHT_ALARM"
        }
        val stopPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            stopWakeLightIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val nextNotificationId = IdManager.getNextNotifId(context)

        val builder =
            NotificationCompat.Builder(context, context.getString(R.string.notif_cat_stop_id))
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.lightbulb)
                .setContentTitle(context.getString(R.string.notif_ask_stop_wakelight_title))
                .setContentText(context.getString(R.string.notif_ask_stop_wakelight_content, id))
                .setColor(context.getColor(R.color.navy_blue_light))
                .setContentIntent(stopPendingIntent)
                .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    "android.permission.POST_NOTIFICATIONS"
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(nextNotificationId, builder.build())
            Log.d(this::class.simpleName, "Sent the notification")
        }
    }
}