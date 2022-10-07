package com.goodjia.utility.sample

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.goodjia.utility.*
import com.goodjia.utility.device.*
import com.goodjia.utility.sample.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), HidKeyReader.HidKeyListener,
    DeviceInfoCollector.Listener {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val hidKeyReader by lazy {
        HidKeyReader(this).apply {
            delayMillis = 500L
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnClearHid.setOnClickListener { binding.tvHidReader.text = "" }
        Logger.setIsDebug(BuildConfig.DEBUG)

        binding.btnLogD.setOnClickListener {
            Logger.d(TAG, "btnLogD")
            Util.toastLong(this, "Log d on Clicked")
        }

        binding.btnLogV.setOnClickListener {
            Logger.v(TAG, "btnLogV")
            Util.toastShort(this, "Log v on Clicked")
        }
        binding.btnLogW.setOnClickListener {
            Logger.w(TAG, "btnLogW")
            Util.toastShort(this, R.string.app_name)
        }
        binding.btnLogI.setOnClickListener {
            Logger.i(TAG, "btnLogI")
            val isOnline = Util.isOnline(5_000L, "yahoo.com")
            Logger.d(TAG, "isOnline $isOnline")
        }
        binding.btnLogE.setOnClickListener {
            Logger.e(TAG, "btnLogE")
            val isOnline = Util.isOnline(5_000L)
            Logger.d(TAG, "isOnline $isOnline")
        }

        val sharedPreferences = Util.getSharedPreferences(this)
        sharedPreferences.edit().putInt("1", 1).apply()
        Logger.d(TAG, "get key 1: " + sharedPreferences.getInt("1", 0))

        val sharedPreferences1 = Util.getSharedPreferences(this, "test")
        sharedPreferences.edit().putString("1", "1").apply()
        Logger.d(TAG, "get key 1: " + sharedPreferences.getString("1", "null")!!)

        Util.toastShort(this, "is network available: " + Util.isNetworkAvailable(this))

        Logger.d(TAG, "has nav bar: " + Util.hasNavBar(this))


        //Testing Device Info Collector
        DeviceInfoCollector.run {
            initialize(this@MainActivity, /*lifecycleScope,*/ periodMillisecond = 10_000)
            registerListener(this@MainActivity)
            Logger.d(
                TAG,
                "deviceBuild ${deviceBuild.json}, display ${display.toDisplayMap().json}, apps ${
                    gson.toJson(appMap)
                }"
            )
        }
        Logger.d(TAG, "files: $storageFiles")
    }

    override fun onDestroy() {
        super.onDestroy()
        DeviceInfoCollector.unregisterListener(this)
        hidKeyReader.destroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        fullscreenOnWindowFocusChanged(hasFocus)
    }

    override fun onKeyEvent(keyCode: String?) {
        binding.tvHidReader?.append(keyCode + "\n")
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
//        hidKeyReader.parseKeyEvent(keyCode, event,KeyEvent.ACTION_UP)
        event?.let { hidKeyReader.parseKeyEvent(it.keyCode, it) }
        return super.dispatchKeyEvent(event)
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun updated(
        systemUptime: Long,
        storages: List<Storage>,
        ram: Ram,
        cpu: Cpu,
        network: Network?
    ) {
        storages.forEach {
            Logger.d(
                TAG,
                "path ${it.path}, free ${it.free.capacity}, total ${it.total.capacity}"
            )
        }
        Logger.d(
            TAG,
            "ram free ${ram.free.capacity}, total ${ram.total.capacity}"
        )
        Logger.d(
            TAG,
            "systemUptime $systemUptime, storages ${gson.toJson(storages)}, ram ${ram.json}, cpu ${cpu.json}, network ${network?.json}"
        )
    }

}
