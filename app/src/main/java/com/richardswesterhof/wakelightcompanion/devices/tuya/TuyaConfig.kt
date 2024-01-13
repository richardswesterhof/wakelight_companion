package com.richardswesterhof.wakelightcompanion.devices.tuya

private const val COLOR_TEMP_MIN = 0
private const val COLOR_TEMP_MAX = 1000

//class TuyaConfig(sharedPrefs: SharedPreferences) : Config(sharedPrefs) {
class TuyaConfig(
    val id: String = "__DOES_NOT_EXIST__",
    val durationInMinutes: Double = 30.0,
    val startingBrightness: Double = 0.0,
    val startingColorTemp: Double = 0.0,
    val endingBrightness: Double = 100.0,
    val endingColorTemp: Double = 1000.0
) {
//    fun getTuyaId(): String? {
//        return getString("pref_wakelight_id", null)
//    }
//
//    fun getIp(): String {
//        return getString("pref_wakelight_ip", "")
//    }
//
//    fun getPort(): Int? {
//        return getInt("pref_wakelight_port", null)
//    }
//
//    fun getDurationMinutes(): Int {
//        return clampStringPref("pref_wakelight_duration1", 30, 0, Int.MAX_VALUE)
//    }
//
//    fun getDuration(): Int {
//        return getDurationMinutes() * 60 * 1000
//    }
//
//    fun getStartingColorTemp(): Int {
//        return clampIntPref(
//            "pref_wakelight_start_color_temp",
//            1700,
//            COLOR_TEMP_MIN,
//            COLOR_TEMP_MAX
//        )
//    }
//
//    fun getStartingBrightness(): Int {
//        return clampIntPref("pref_wakelight_start_brightness", 1, 0, 100)
//    }
//
//    fun getEndingColorTemp(): Int {
//        return clampIntPref(
//            "pref_wakelight_end_color_temp",
//            5000,
//            COLOR_TEMP_MIN,
//            COLOR_TEMP_MAX
//        )
//    }
//
//    fun getEndingBrightness(): Int {
//        return clampIntPref("pref_wakelight_end_brightness", 100, 0, 100)
//    }
}