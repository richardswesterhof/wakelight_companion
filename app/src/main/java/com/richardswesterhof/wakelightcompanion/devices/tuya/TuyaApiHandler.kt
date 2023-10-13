package com.richardswesterhof.wakelightcompanion.devices.tuya


import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Arrays
import java.util.Locale
import java.util.TreeMap
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

//import javax.xml.bind.annotation.adapters.HexBinaryAdapter

/**
 * based on original code by:
 * gongtai.yin
 * 2021/08/18
 */
class TuyaApiHandler {
    // Access ID
    private val accessId = "9muuqqs85c8y4aguj5jn"

    // Access Secret
    private val accessKey = "6b43db10cabe410081f79d66b7fdee4f"

    // Tuya central europe cloud endpoint
    private val endpoint = "https://openapi.tuyaeu.com"

    val getTokenPath = "/v1.0/token?grant_type=1"

    private val CONTENT_TYPE: MediaType? = "application/json".toMediaTypeOrNull()
    private val EMPTY_HASH =
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    private val SING_HEADER_NAME = "Signature-Headers"
    private val NONE_STRING = ""
//    private val gson: Gson = Gson().newBuilder().create()

    init {
        // Designated area domain name
        Constant.CONTAINER[Constant.ENDPOINT] = endpoint
        Constant.CONTAINER[Constant.ACCESS_ID] = accessId
        Constant.CONTAINER[Constant.ACCESS_KEY] = accessKey
    }

    /**
     * Used to obtain tokens, refresh tokens: no token request
     */
    fun execute(
        path: String,
        method: String,
        body: String,
        customHeaders: Map<String?, String?>
    ): Response {
        return execute("", path, method, body, customHeaders)
    }

    /**
     * For business interface: carry Token request
     */
    fun execute(
        accessToken: String?,
        path: String,
        method: String,
        body: String,
        customHeaders: Map<String?, String?>
    ): Response {
        return try {
            var requestHeaders = customHeaders
            // Verify developer information
            if (Constant.CONTAINER.isEmpty()) {
                throw TuyaCloudSDKException("Developer information is not initialized!")
            }
            val url = Constant.CONTAINER[Constant.ENDPOINT] + path
            val request: Request.Builder = when (method) {
                "GET" -> getRequest(url)
                "POST" -> postRequest(url, body)
                "PUT" -> putRequest(url, body)
                "DELETE" -> deleteRequest(url, body)
                else -> throw TuyaCloudSDKException("Method only support GET, POST, PUT, DELETE")
            }
            if (requestHeaders.isEmpty()) {
                requestHeaders = HashMap()
            }
            val headers: Headers = getHeader(accessToken, request.build(), body, requestHeaders)
            request.headers(headers)
            request.url(Constant.CONTAINER[Constant.ENDPOINT] + getPathAndSortParam(url.toHttpUrl()))
            return doRequest(request.build())
        } catch (e: Exception) {
            val exception = TuyaCloudSDKException(e.message)
            exception.stackTrace = e.stackTrace
            throw exception
        }
    }

    /**
     * Generate header
     *
     * @param accessToken Do  need to carry token
     * @param headerMap   Custom header
     */
    @Throws(Exception::class)
    fun getHeader(
        accessToken: String?,
        request: Request,
        body: String,
        headerMap: Map<String?, String?>
    ): Headers {
        val hb: Headers.Builder = Headers.Builder()
        val flattenHeaders = flattenHeaders(headerMap)
        var t = flattenHeaders["t"]
        if (t.isNullOrBlank()) {
            t = System.currentTimeMillis().toString()
        }
        hb.add(
            "client_id",
            Constant.CONTAINER[Constant.ACCESS_ID] ?: ""
        )
        hb.add("t", t)
        hb.add("sign_method", "HMAC-SHA256")
        hb.add("lang", "zh")
        hb.add(
            SING_HEADER_NAME,
            flattenHeaders[SING_HEADER_NAME] ?: ""
        )
        val nonceStr = flattenHeaders[Constant.NONCE_HEADER_NAME] ?: ""
        hb.add(
            Constant.NONCE_HEADER_NAME,
            flattenHeaders[Constant.NONCE_HEADER_NAME] ?: ""
        )
        val stringToSign = stringToSign(request, body, flattenHeaders)
        if (accessToken?.isNotBlank() == true) {
            hb.add("access_token", accessToken)
            hb.add(
                "sign", sign(
                    Constant.CONTAINER[Constant.ACCESS_ID],
                    Constant.CONTAINER[Constant.ACCESS_KEY], t, accessToken, nonceStr, stringToSign
                )
            )
        } else {
            hb.add(
                "sign", sign(
                    Constant.CONTAINER[Constant.ACCESS_ID],
                    Constant.CONTAINER[Constant.ACCESS_KEY], t, nonceStr, stringToSign
                )
            )
        }
        return hb.build()
    }

    fun getPathAndSortParam(url: HttpUrl): String {
        return try {
            // supported the query contains zh-Han char
            val query = URLDecoder.decode(url.query, "UTF-8")
            val path = URLDecoder.decode(url.encodedPath, "UTF-8")
            if (query.isBlank()) {
                return path
            }
            val kvMap: MutableMap<String, String> = TreeMap()
            val kvs = query.split("\\&".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            for (kv in kvs) {
                val kvArr = kv.split("=".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                if (kvArr.size > 1) {
                    kvMap[kvArr[0]] = kvArr[1]
                } else {
                    kvMap[kvArr[0]] = ""
                }
            }
            "$path?" + kvMap.entries.stream()
                .map { (key, value): Map.Entry<String, String> -> "$key=$value" }
                .collect(Collectors.joining("&"))
        } catch (e: Exception) {
            URLDecoder.decode(url.encodedPath, "UTF-8")
        }
    }

    @Throws(Exception::class)
    private fun stringToSign(
        request: Request,
        body: String,
        headers: Map<String?, String>
    ): String {
        val lines: MutableList<String> = ArrayList(16)
        lines.add(request.method.uppercase(Locale.ROOT))
        var bodyHash = EMPTY_HASH
        if ((request.body?.contentLength() ?: 0) > 0) {
            bodyHash = Sha256Util.encryption(body)
        }
        val signHeaders = headers[SING_HEADER_NAME]
        var headerLine = ""
        if (signHeaders != null) {
            val sighHeaderNames =
                signHeaders.split("\\s*:\\s*".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            headerLine =
                Arrays.stream(sighHeaderNames).map { obj: String -> obj.trim { it <= ' ' } }
                    .filter { it: String -> it.isNotEmpty() }
                    .map { it: String ->
                        "$it:" + headers[it]
                    }
                    .collect(Collectors.joining("\n"))
        }
        lines.add(bodyHash)
        lines.add(headerLine)
        val paramSortedPath = getPathAndSortParam(request.url)
        lines.add(paramSortedPath)
        return java.lang.String.join("\n", lines)
    }

    private fun flattenHeaders(headers: Map<String?, String?>): Map<String?, String> {
        val newHeaders: MutableMap<String?, String> = HashMap()
        headers.forEach { (name: String?, values: String?) ->
            if (values.isNullOrEmpty()) {
                newHeaders[name] = ""
            } else {
                newHeaders[name] = values
            }
        }
        return newHeaders
    }

    /**
     * Calculate sign
     */
    private fun sign(
        accessId: String?,
        secret: String?,
        t: String?,
        accessToken: String?,
        nonce: String,
        stringToSign: String
    ): String {
        val sb = StringBuilder()
        sb.append(accessId)
        if (accessToken?.isNotBlank() == true) {
            sb.append(accessToken)
        }
        sb.append(t)
        if (nonce.isNotBlank()) {
            sb.append(nonce)
        }
        sb.append(stringToSign)
        return Sha256Util.sha256HMAC(sb.toString(), secret)
    }

    private fun sign(
        accessId: String?,
        secret: String?,
        t: String?,
        nonce: String,
        stringToSign: String
    ): String {
        return sign(accessId, secret, t, NONE_STRING, nonce, stringToSign)
    }

    /**
     * Handle get request
     */
    fun getRequest(url: String): Request.Builder {
        val request: Request.Builder = try {
            Request.Builder()
                .url(url)
                .get()
        } catch (e: IllegalArgumentException) {
            val exception = TuyaCloudSDKException(e.message)
            exception.stackTrace = e.stackTrace
            throw exception
        }
        return request
    }

    /**
     * Handle post request
     */
    fun postRequest(url: String, body: String): Request.Builder {
        val request: Request.Builder = try {
            Request.Builder()
                .url(url)
                .post(body.toRequestBody(CONTENT_TYPE))
        } catch (e: IllegalArgumentException) {
            val exception = TuyaCloudSDKException(e.message)
            exception.stackTrace = e.stackTrace
            throw exception
        }
        return request
    }

    /**
     * Handle put request
     */
    fun putRequest(url: String, body: String): Request.Builder {
        val request: Request.Builder = try {
            Request.Builder()
                .url(url)
                .put(body.toRequestBody(CONTENT_TYPE))
        } catch (e: IllegalArgumentException) {
            val exception = TuyaCloudSDKException(e.message)
            exception.stackTrace = e.stackTrace
            throw exception
        }
        return request
    }

    /**
     * Handle delete request
     */
    fun deleteRequest(url: String, body: String): Request.Builder {
        val request: Request.Builder = try {
            Request.Builder()
                .url(url)
                .delete(body.toRequestBody(CONTENT_TYPE))
        } catch (e: IllegalArgumentException) {
            val exception = TuyaCloudSDKException(e.message)
            exception.stackTrace = e.stackTrace
            throw exception
        }
        return request
    }

    /**
     * Execute request
     */
    fun doRequest(request: Request): Response {
        val response: Response = try {
            httpClient.newCall(request).execute()
        } catch (e: IOException) {
            val exception = TuyaCloudSDKException(e.message)
            exception.stackTrace = e.stackTrace
            throw exception
        }
        return response
    }

    private val httpClient: OkHttpClient
        // Get http client
        get() {
            return OkHttpClient()
        }

    internal object Constant {
        /**
         * Store developer information container
         */
        val CONTAINER: MutableMap<String, String> = ConcurrentHashMap()

        /**
         * Developer account, used as a key in the container
         */
        const val ACCESS_ID = "accessId"

        /**
         * Developer key, used as a key in the container
         */
        const val ACCESS_KEY = "accessKey"
        const val ENDPOINT = "endpoint"
        const val NONCE_HEADER_NAME = "nonce"
    }

    internal object Sha256Util {
        @Throws(Exception::class)
        fun encryption(str: String): String {
            return encryption(str.toByteArray(StandardCharsets.UTF_8))
        }

        @Throws(Exception::class)
        fun encryption(buf: ByteArray?): String {
            val messageDigest: MessageDigest = MessageDigest.getInstance("SHA-256")
            messageDigest.update(buf)
            return byte2Hex(messageDigest.digest())
        }

        private fun byte2Hex(bytes: ByteArray): String {
            val stringBuffer = StringBuilder()
            var temp: String
            for (aByte in bytes) {
                temp = Integer.toHexString(aByte.toInt() and 0xFF)
                if (temp.length == 1) {
                    stringBuffer.append("0")
                }
                stringBuffer.append(temp)
            }
            return stringBuffer.toString()
        }

        fun sha256HMAC(content: String, secret: String?): String {
            var sha256HMAC: Mac? = null
            try {
                sha256HMAC = Mac.getInstance("HmacSHA256")
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            val secretKey: SecretKey =
                SecretKeySpec(secret!!.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
            try {
                sha256HMAC!!.init(secretKey)
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            }
            val digest = sha256HMAC!!.doFinal(content.toByteArray(StandardCharsets.UTF_8))
            val builder = StringBuilder()
            digest.forEach { builder.append(String.format("%02X", it)) }
            return builder.toString()
        }
    }

    internal class TuyaCloudSDKException : RuntimeException {
        var code: Int? = null

        constructor(message: String?) : super(message)
        constructor(code: Int?, message: String?) : super(message) {
            this.code = code
        }

        override fun toString(): String {
            return if (code != null) {
                "TuyaCloudSDKException: " +
                        "[" + code + "] " + message
            } else "TuyaCloudSDKException: $message"
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val handler = TuyaApiHandler()


            val result = handler.execute(handler.getTokenPath, "GET", "", HashMap())
//            println(gson.toJson(result))
            println(result)
        }
    }
}