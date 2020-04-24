package com.goodjia.utility.device

import android.os.Build
import com.goodjia.utility.isRooted
import com.google.gson.Gson
import java.text.DecimalFormat

const val NETWORK_TYPE_WIFI = "wifi"
const val NETWORK_TYPE_MOBILE = "mobile"
const val NETWORK_TYPE_ETHERNET = "ethernet"

const val MOBILE_TYPE_LTE = "lte"
const val MOBILE_TYPE_NR = "nr"

val gson = Gson()

data class DeviceBuild @JvmOverloads constructor(
    val os: String = "Android ${Build.VERSION.RELEASE}(${Build.VERSION.SDK_INT})",
    val codeName: String = Build.VERSION.CODENAME,
    val board: String = Build.BOARD,
    val bootloader: String = Build.BOOTLOADER,
    val brand: String = Build.BRAND,
    val device: String = Build.DEVICE,
    val display: String = Build.DISPLAY,
    val fingerprint: String = Build.FINGERPRINT,
    val hardware: String = Build.HARDWARE,
    val host: String = Build.HOST,
    val id: String = Build.ID,
    val manufacturer: String = Build.MANUFACTURER,
    val model: String = Build.MODEL,
    val product: String = Build.PRODUCT,
    val serial: String = Build.SERIAL,
    val abi: String = Build.CPU_ABI,
    val tags: String = Build.TAGS,
    val type: String = Build.TYPE,
    val user: String = Build.USER,
    val kernel: String? = System.getProperty("os.version"),
    val rooted: Boolean = isRooted()
) {
    val json
        get() = gson.toJson(this)
}

data class Display @JvmOverloads constructor(
    val primacy: Screen?,
    var presentationList: List<Screen>? = null,
    var presentation: Map<Int, Screen>? = null
) {
    fun toDisplayMap(): Display {
        presentation = presentationList?.map { it.id to it }?.toMap()
        presentationList = null
        return this
    }

    val json
        get() = gson.toJson(this)
}

data class Screen @JvmOverloads constructor(
    val id: Int,
    val name: String,
    val width: Int,
    val height: Int,
    val density: Float,
    val rotation: Int,
    val refreshRate: Float
) {
    val json
        get() = gson.toJson(this)
}

data class Storage @JvmOverloads constructor(val path: String, val free: Long, val total: Long) {
    val json
        get() = gson.toJson(this)
}

data class Ram @JvmOverloads constructor(val free: Long, val total: Long) {
    val json
        get() = gson.toJson(this)
}

data class Cpu @JvmOverloads constructor(
    val cores: List<Frequency>,
    val usagePercentage: Int,
    val temperature: Float
) {
    val json
        get() = gson.toJson(this)
}

data class Frequency @JvmOverloads constructor(
    val currentFreq: Long,
    val minFreq: Long,
    val maxFreq: Long
) {
    val json
        get() = gson.toJson(this)
}

data class Network @JvmOverloads constructor(
    val networkType: String,
    val mobileType: String? = null,
    val mobileName: String? = null,
    val ssid: String? = null,
    val ip: String? = null,
    val signalLevel: Int? = null
) {
    val json
        get() = gson.toJson(this)
}

data class Package @JvmOverloads constructor(val packageName: String, val version: String) {
    val json
        get() = gson.toJson(this)
}

val Long.megaBytes
    get() = this / (1024 * 1024f)

val Long.gigaBytes
    get() = this / (1024 * 1024 * 1024f)

internal val decimalFormat = DecimalFormat("#.##")
val Long.capacity: String
    get() {
        gigaBytes.let {
            return if (it < 1) "${decimalFormat.format(megaBytes)}MB"
            else "${decimalFormat.format(it)}GB"
        }
    }

