package com.richardswesterhof.wakelightcompanion.devices

import android.content.SharedPreferences
import com.mollin.yapi.utils.YeelightUtils

abstract class Config(private val sharedPrefs: SharedPreferences) {

    private val cachedValues: MutableMap<String, Any?>

    init {
        this.cachedValues = HashMap()
    }

    fun getId(): String? {
        return getString("id", null)
    }

    fun <T : Int?> getInt(key: String, default: T): T {
        return cachedValues.getOrPut(key) {
            if (sharedPrefs.contains(key))
            // sharedPrefs' default will never be used but is required by the method's signature
                sharedPrefs.getInt(key, -1)
            else default
        } as T
    }

    fun <T : Long?> getLong(key: String, default: T): T {
        return cachedValues.getOrPut(key) {
            if (sharedPrefs.contains(key))
            // sharedPrefs' default will never be used but is required by the method's signature
                sharedPrefs.getLong(key, -1L)
            else default
        } as T
    }

    fun <T : Float?> getFloat(key: String, default: T): T {
        return cachedValues.getOrPut(key) {
            if (sharedPrefs.contains(key))
            // sharedPrefs' default will never be used but is required by the method's signature
                sharedPrefs.getFloat(key, -1F)
            else default
        } as T
    }

    fun <T : Boolean?> getBoolean(key: String, default: T): T {
        return cachedValues.getOrPut(key) {
            if (sharedPrefs.contains(key))
            // sharedPrefs' default will never be used but is required by the method's signature
                sharedPrefs.getBoolean(key, false)
            else default
        } as T
    }

    fun <T : String?> getString(key: String, default: T): T {
        return cachedValues.getOrPut(key) {
            if (sharedPrefs.contains(key))
            // sharedPrefs' default will never be used but is required by the method's signature
                sharedPrefs.getString(key, "")
            else default
        } as T
    }

    fun clampStringPref(pref: String, default: Int, min: Int, max: Int): Int {
        return YeelightUtils.clamp(
            getString(pref, default.toString()).toInt(),
            min,
            max
        )
    }

    fun clampIntPref(pref: String, default: Int, min: Int, max: Int): Int {
        return YeelightUtils.clamp(getInt(pref, default), min, max)
    }
}