package com.richardswesterhof.wakelightcompanion.devices.tuya

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.richardswesterhof.wakelightcompanion.R
import org.json.JSONObject

class TuyaApi(context: Context) {
    private val internalPrefs: SharedPreferences
    private val httpHandler: TuyaHttpHandler

    init {
        internalPrefs = context.getSharedPreferences(
            context.getString(R.string.preference_file_store_internal_vars),
            Context.MODE_PRIVATE
        )
        httpHandler = TuyaHttpHandler(context)
    }

    /**
     * Retrieve a valid access token. This is done by either getting one from the
     * internal preferences, if present and not expired, or refreshing it if it
     * is expired, or getting a completely new one if nothing is present
     */
    fun getAccessToken(): String {
        val accessToken = internalPrefs.getString("tuyaApiToken", null)
        val expireTime = internalPrefs.getLong("tuyaApiTokenExpires", 0)
        val refreshToken = internalPrefs.getString("tuyaApiRefreshToken", null)
        return if (System.currentTimeMillis() > expireTime) { // current token is expired
            // if we have a refresh token, use it to refresh access  token, else get new access token
            refreshToken?.let { refreshAccessToken() } ?: getNewAccessToken()
        } else accessToken?.let { accessToken } ?: run {
            Log.wtf(
                this::class.simpleName,
                "tuyaApiTokenExpires is not yet expired but no token is present in internalPrefs"
            )
            ""
        }

    }

    private fun getNewAccessToken(): String {
        val path = Uri.Builder()
            .scheme(Constants.ENDPOINT_SCHEME)
            .authority(Constants.ENDPOINT_HOST)
            .appendPath(Constants.V1_0_PATH)
            .appendPath(Constants.GET_TOKEN_PATH)
            .appendQueryParameter("grant_type", "1")
            .build()
        val response = httpHandler.execute("", path.toString(), "GET")
        val json = JSONObject(response.body?.string() ?: "{}")
        val (token, tokenExpires, refreshToken) = parseAccessTokens(json)
        storeAccessTokens(token, tokenExpires, refreshToken)
        return token
    }

    private fun parseAccessTokens(json: JSONObject): Triple<String, Long, String> {
        Log.d(this::class.simpleName, "parsing $json")
        val token = json.getJSONObject("result").getString("access_token")
        // timestamp (t) is in milliseconds, expire time is in seconds
        val tokenExpires =
            json.getLong("t") + json.getJSONObject("result").getLong("expire_time") * 1000
        val refreshToken = json.getJSONObject("result").getString("refresh_token")
        Log.d(
            this::class.simpleName,
            "parsed json $json into token: $token, expires: $tokenExpires, refreshToken: $refreshToken"
        )
        return Triple(token, tokenExpires, refreshToken)
    }

    private fun storeAccessTokens(token: String, tokenExpires: Long, refreshToken: String) {
        with(internalPrefs.edit()) {
            putString("tuyaApiToken", token)
            putLong("tuyaApiTokenExpires", tokenExpires)
            putString("tuyaApiRefreshToken", refreshToken)
            apply()
        }
    }

    /**
     * Method to refresh access tokens. This method is private, so in order to force
     * the application to refresh its tokens, set "tuyaApiTokenExpires" to 0 in internalPrefs
     * (or any timestamp in the past)
     */
    private fun refreshAccessToken(): String? {
        val refreshToken = internalPrefs.getString("tuyaApiRefreshToken", null)
        return refreshToken?.let {
            val path = Uri.Builder()
                .scheme(Constants.ENDPOINT_SCHEME)
                .authority(Constants.ENDPOINT_HOST)
                .appendPath(Constants.V1_0_PATH)
                .appendPath(Constants.GET_TOKEN_PATH)
                .appendPath(refreshToken)
                .build()
            val response =
                httpHandler.execute("", path.toString(), "GET")
            val json = JSONObject(response.body?.string() ?: "{}")
            val (token, tokenExpires, newRefreshToken) = parseAccessTokens(json)
            storeAccessTokens(token, tokenExpires, newRefreshToken)
            token
        } ?: run {
            Log.e(this::class.simpleName, "No refresh token present in internal prefs :(")
            null
        }
    }

    fun getDeviceList(): Collection<JSONObject> {
        val devices: MutableList<JSONObject> = mutableListOf()
        val deviceId = "TODO" // TODO: retrieve from *somewhere*
        try {
//        do {
//        var lastRowKey: String? = null
            val accessToken = getAccessToken()
            val pathBuilder = Uri.Builder()
            pathBuilder
                .scheme(Constants.ENDPOINT_SCHEME)
                .authority(Constants.ENDPOINT_HOST)
                .appendPath(Constants.V1_0_PATH)
                .appendPath(Constants.USER_PATH)
                .appendPath(deviceId)
                .appendPath(Constants.DEVICES_PATH)
                .appendQueryParameter("page_size", "1000")
                .appendQueryParameter("page_no", "1")
//                .appendQueryParameter("schema", "true")
//            lastRowKey?.let { pathBuilder.appendQueryParameter("last_row_key", lastRowKey) }
            val path = pathBuilder.build().toString()
            val response = httpHandler.execute(accessToken, path, "GET")
            val json = JSONObject(response.body?.string() ?: "{}")
            Log.d(this::class.simpleName, "received json $json")
//            lastRowKey = json.getJSONObject("result").getString("last_row_key")
//        val deviceArr = json.getJSONObject("result").getJSONArray("devices")
            val deviceArr = json.getJSONArray("result")
            for (i in 0 until deviceArr.length()) {
                devices.add(deviceArr.getJSONObject(i))
            }
//        } while (json.getJSONObject("result").getBoolean("has_more"))
        } catch (e: Exception) {
            Log.e(
                this::class.simpleName,
                "Unexpected error while parsing result from list devices",
                e
            )
        }
        return devices
    }

    fun commandDevice(deviceId: String, commandBody: String): Boolean {
        var success = false
        try {
            val accessToken = getAccessToken()
            val pathBuilder = Uri.Builder()
            pathBuilder
                .scheme(Constants.ENDPOINT_SCHEME)
                .authority(Constants.ENDPOINT_HOST)
                .appendPath(Constants.V1_0_PATH)
                .appendPath(Constants.DEVICES_PATH)
                .appendPath(deviceId)
                .appendPath(Constants.COMMANDS_PATH)
            val path = pathBuilder.build().toString()
            val response = httpHandler.execute(accessToken, path, "POST", commandBody, emptyMap())
            val json = JSONObject(response.body?.string() ?: "{}")
            Log.d(this::class.simpleName, "received json $json")
            success = json.getBoolean("success") && json.getBoolean("result");
        } catch (e: Exception) {
            Log.e(
                this::class.simpleName,
                "Unexpected exception occurred while commanding device",
                e
            )
            return false
        }
        return success
    }

    internal object Constants {
        // Tuya central europe cloud endpoint
        const val ENDPOINT_SCHEME = "https"
        const val ENDPOINT_HOST = "openapi.tuyaeu.com"

        const val V1_0_PATH = "v1.0"
        const val GET_TOKEN_PATH = "token"
        const val USER_PATH = "users"
        const val DEVICES_PATH = "devices"
        const val COMMANDS_PATH = "commands"
    }
}