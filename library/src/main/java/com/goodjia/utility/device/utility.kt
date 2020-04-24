package com.goodjia.utility.device

import android.view.Surface
import java.io.*

private const val CPU_FREQUENCY_PATH_PREFIX = "/sys/devices/system/cpu/cpu"
private const val CPU_MIN_FREQUENCY_PATH_SUFFIX = "/cpufreq/cpuinfo_min_freq"
private const val CPU_MAX_FREQUENCY_PATH_SUFFIX = "/cpufreq/cpuinfo_max_freq"
private const val CPU_FREQUENCY_PATH_SUFFIX = "/cpufreq/scaling_cur_freq"
private val CPU_TEMP_FILE_PATHS = listOf(
    "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
    "/sys/devices/system/cpu/cpu0/cpufreq/FakeShmoo_cpu_temp",
    "/sys/class/thermal/thermal_zone0/temp",
    "/sys/class/i2c-adapter/i2c-4/4-004c/temperature",
    "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/temperature",
    "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
    "/sys/devices/platform/tegra_tmon/temp1_input",
    "/sys/kernel/debug/tegra_thermal/temp_tj",
    "/sys/devices/platform/s5p-tmu/temperature",
    "/sys/class/thermal/thermal_zone1/temp",
    "/sys/class/hwmon/hwmon0/device/temp1_input",
    "/sys/devices/virtual/thermal/thermal_zone1/temp",
    "/sys/devices/virtual/thermal/thermal_zone0/temp",
    "/sys/class/thermal/thermal_zone3/temp",
    "/sys/class/thermal/thermal_zone4/temp",
    "/sys/class/hwmon/hwmonX/temp1_input",
    "/sys/devices/platform/s5p-tmu/curr_temp"
)


fun getRotation(rotation: Int) = when (rotation) {
    Surface.ROTATION_0 -> 0
    Surface.ROTATION_90 -> 90
    Surface.ROTATION_180 -> 180
    Surface.ROTATION_270 -> 270
    else -> Int.MIN_VALUE
}

internal fun getCpuTemperature(): Float {
    CPU_TEMP_FILE_PATHS.forEach {
        val temp = getTemp(it)
        if (temp != null) {
            return temp
        }
    }
    return 0f
}

internal val cpuFrequency: List<Frequency>
    get() {
        val list = mutableListOf<Frequency>()
        for (i in 0 until cores) {
            list.add(getFrequency(i))
        }
        return list
    }
internal val cores
    get() = Runtime.getRuntime().availableProcessors()

private fun getFrequency(coreIndex: Int) = Frequency(
    getFrequency(coreIndex, CPU_FREQUENCY_PATH_SUFFIX),
    getFrequency(coreIndex, CPU_MIN_FREQUENCY_PATH_SUFFIX),
    getFrequency(coreIndex, CPU_MAX_FREQUENCY_PATH_SUFFIX)
)

private fun getFrequency(coreIndex: Int, suffixPath: String): Long {
    return try {
        val reader = RandomAccessFile(
            CPU_FREQUENCY_PATH_PREFIX + coreIndex + suffixPath,
            "r"
        )
        val freq = reader.readLine().toLong() / 1000
        reader.close()
        freq
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}

private fun getTemp(path: String): Float? {
    val temp = readOneLine(File(path)) ?: return null

    return if (isTemperatureValid(temp)) {
        temp.toFloat()
    } else {
        (temp / 1000).toFloat()
    }
}

private fun readOneLine(file: File): Double? {
    val text: String?
    try {
        val fs = FileInputStream(file)
        val sr = InputStreamReader(fs)
        val br = BufferedReader(sr)
        text = br.readLine()
        br.close()
        sr.close()
        fs.close()
    } catch (ex: Exception) {
        return null
    }

    val value: Double?
    try {
        value = text.toDouble()
    } catch (nfe: NumberFormatException) {
        return null
    }

    return value
}

private fun isTemperatureValid(temp: Double): Boolean = temp in -30.0..250.0

