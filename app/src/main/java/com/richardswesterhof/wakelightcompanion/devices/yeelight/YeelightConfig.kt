package com.richardswesterhof.wakelightcompanion.devices.yeelight

import android.content.SharedPreferences
import com.richardswesterhof.wakelightcompanion.devices.Config

private const val COLOR_TEMP_MIN = 1700
private const val COLOR_TEMP_MAX = 6500

class YeelightConfig(sharedPrefs: SharedPreferences) : Config(sharedPrefs) {


    fun getIp(): String {
        return getString("pref_wakelight_ip", "")
    }

    fun getPort(): Int? {
        return getInt("pref_wakelight_port", null)
    }

    fun getDurationMinutes(): Int {
        return clampStringPref("pref_wakelight_duration1", 30, 0, Int.MAX_VALUE)
    }

    fun getDuration(): Int {
        return getDurationMinutes() * 60 * 1000
    }

    fun getStartingColorTemp(): Int {
        return clampIntPref("pref_wakelight_start_color_temp", 1700, COLOR_TEMP_MIN, COLOR_TEMP_MAX)
    }

    fun getStartingBrightness(): Int {
        return clampIntPref("pref_wakelight_start_brightness", 1, 0, 100)
    }

    fun getEndingColorTemp(): Int {
        return clampIntPref("pref_wakelight_end_color_temp", 5000, COLOR_TEMP_MIN, COLOR_TEMP_MAX)
    }

    fun getEndingBrightness(): Int {
        return clampIntPref("pref_wakelight_end_brightness", 100, 0, 100)
    }
}