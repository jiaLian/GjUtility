package com.goodjia.utility.device

import android.app.ActivityManager
import android.content.Context
import android.hardware.display.DisplayManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.SystemClock
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.Display
import com.goodjia.utility.Logger
import com.goodjia.utility.storageFiles
import kotlinx.coroutines.*
import org.jetbrains.anko.*
import java.math.BigInteger
import java.net.Inet4Address
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder

object DeviceInfoCollector {
    private val TAG = DeviceInfoCollector::class.java.simpleName
    private const val DEFAULT_PERIOD = 10 * 60_000L

    private var context: Context? = null

    private var coroutineScope: CoroutineScope? = null
    private var updatedJob: Job? = null

    private val listeners = mutableListOf<Listener>()

    var periodMillisecond: Long = DEFAULT_PERIOD

    val deviceBuild
        get() = DeviceBuild()

    val systemUptime
        get() = SystemClock.elapsedRealtime()

    val display: com.goodjia.utility.device.Display
        get() {
            var primacy: Screen? = null
            val presentation = mutableListOf<Screen>()
            context?.windowManager?.defaultDisplay?.let {
                primacy = getScreen(it)
            }
            context?.displayManager?.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
                ?.forEach {
                    presentation.add(getScreen(it))
                }
            return Display(primacy, presentation)
        }

    val storages: List<Storage>
        get() {
            val list = mutableListOf<Storage>()
            context?.storageFiles?.forEach {
                list.add(
                    Storage(
                        it.absolutePath.substringBefore("Android/data/"),
                        it.freeSpace,
                        it.totalSpace
                    )
                )
            }
            return list
        }

    val ram: Ram
        get() {
            ActivityManager.MemoryInfo().apply {
                context?.activityManager?.getMemoryInfo(this)
                return Ram(availMem, totalMem)
            }
        }

    val cpu: Cpu
        get() {
            val cores = cpuFrequency
            var percent = 0L
            try {
                cores.forEach {
                    percent += it.currentFreq * 100 / it.maxFreq
                }
                percent /= cores.size
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return Cpu(cores, percent.toInt(), getCpuTemperature())
        }

    private var signalStrength: SignalStrength? = null
    private val phoneStateListener = object : PhoneStateListener() {
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            this@DeviceInfoCollector.signalStrength = signalStrength
        }
    }
    val network: Network?
        get() {
            context?.connectivityManager?.let { connectivityManager ->
                val networkInfo = connectivityManager.activeNetworkInfo ?: return@let
                return when (networkInfo.type) {
                    ConnectivityManager.TYPE_WIFI -> getWifi()

                    ConnectivityManager.TYPE_MOBILE -> getMobile()

                    ConnectivityManager.TYPE_ETHERNET -> getEthernet()
                    else -> null
                }
            }
            return null
        }

    val apps
        get() =
            mutableListOf<Package>().apply {
                context?.packageManager?.getInstalledPackages(0)?.forEach {
                    add(
                        Package(
                            it.packageName,
                            "${it.versionName}(${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) it.longVersionCode else it.versionCode})"
                        )
                    )
                }
            }.toList()
    val appMap
        get() = apps.map { it.packageName.replace('.', '_') to it.version }.toMap()

    @JvmOverloads
    fun initialize(
        context: Context,
        coroutineScope: CoroutineScope? = GlobalScope,
        periodMillisecond: Long = DEFAULT_PERIOD
    ) {
        this.context = context
        this.coroutineScope = coroutineScope
        this.periodMillisecond = periodMillisecond
    }

    fun registerListener(listener: Listener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
        updated()
    }

    fun unregisterListener(listener: Listener) {
        synchronized(listeners) {
            listeners.remove(listener)
            if (listeners.isEmpty())
                updatedJob?.cancel()
        }
    }

    private fun updated() {
        if (updatedJob?.isActive != true) {
            updatedJob = coroutineScope?.launch(Dispatchers.IO) {
                while (true) {
                    synchronized(listeners) {
                        listeners.forEach {
                            it.updated(systemUptime, storages, ram, cpu, network)
                        }
                    }
                    delay(periodMillisecond)
                }
            }
        }
    }

    private fun getScreen(display: Display): Screen {
        val metrics = DisplayMetrics()
        display.name
        display.getRealMetrics(metrics)
        return Screen(
            display.displayId,
            display.name,
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.density,
            getRotation(display.rotation),
            display.refreshRate
        )
    }

    private fun getEthernet(): Network? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context?.connectivityManager?.getLinkProperties(context?.connectivityManager?.activeNetwork)?.linkAddresses?.forEach {
                if (it.address is Inet4Address) {
                    return Network(NETWORK_TYPE_ETHERNET, ip = it.address.hostAddress)
                }
            }
        }
        return null
    }

    private fun getMobile(): Network? {
        context?.telephonyManager?.let { telephonyManager ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                signalStrength = telephonyManager.signalStrength
            } else {
                telephonyManager.listen(
                    phoneStateListener,
                    PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                )
            }
            val mobileType = when (telephonyManager.networkType) {
                TelephonyManager.NETWORK_TYPE_LTE -> MOBILE_TYPE_LTE
                TelephonyManager.NETWORK_TYPE_NR -> MOBILE_TYPE_NR
                else -> telephonyManager.networkType.toString()
            }
            return Network(
                NETWORK_TYPE_MOBILE,
                mobileType,
                mobileName = telephonyManager.networkOperatorName,
                signalLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) signalStrength?.level else null
            )
        }
        return null
    }

    private fun getWifi(): Network? {
        val wifiInfo = context?.wifiManager?.connectionInfo
        wifiInfo?.let {
            var ipAddress = it.ipAddress
            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                ipAddress = Integer.reverseBytes(ipAddress)
            }

            val ipByteArray: ByteArray =
                BigInteger.valueOf(ipAddress.toLong()).toByteArray()

            val ipAddressString: String?
            ipAddressString = try {
                InetAddress.getByAddress(ipByteArray).hostAddress
            } catch (ex: UnknownHostException) {
                Logger.e("WIFIIP", "Unable to get host address.")
                null
            }
            return Network(
                NETWORK_TYPE_WIFI,
                ssid = it.ssid,
                ip = ipAddressString,
                signalLevel = WifiManager.calculateSignalLevel(
                    it.rssi, 5
                )
            )
        }
        return null
    }

    interface Listener {
        fun updated(
            systemUptime: Long,
            storages: List<Storage>,
            ram: Ram,
            cpu: Cpu,
            network: Network?
        )
    }
}