package com.richardswesterhof.wakelightcompanion.implementation_details

abstract class Config<DeviceType>(private val values: Map<String, Any>) {
    fun getValues(): Map<String, Any> {
        return values;
    }
}