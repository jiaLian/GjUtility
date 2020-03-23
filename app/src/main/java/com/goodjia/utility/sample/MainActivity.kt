package com.goodjia.utility.sample

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.goodjia.utility.HidKeyReader
import com.goodjia.utility.Logger
import com.goodjia.utility.Util
import com.goodjia.utility.fullscreenOnWindowFocusChanged
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), HidKeyReader.HidKeyListener {

    private val hidKeyReader by lazy {
        HidKeyReader(this).apply {
            delayMillis = 500L
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnClearHid.setOnClickListener { tvHidReader.text = "" }
        Logger.setIsDebug(BuildConfig.DEBUG)

        btnLogD.setOnClickListener {
            Logger.d(TAG, "btnLogD")
            Util.toastLong(this, "Log d onclicked")
        }

        btnLogV.setOnClickListener {
            Logger.v(TAG, "btnLogV")
            Util.toastShort(this, "Log v onclicked")
        }
        btnLogW.setOnClickListener {
            Logger.w(TAG, "btnLogW")
            Util.toastShort(this, R.string.app_name)
        }
        btnLogI.setOnClickListener {
            Logger.i(TAG, "btnLogI")
            val isOnline = Util.isOnline(5_000L, "yahoo.com")
            Logger.d(TAG, "isOnline $isOnline")
        }
        btnLogE.setOnClickListener {
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
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        fullscreenOnWindowFocusChanged(hasFocus)
    }

    override fun onKeyEvent(keyCode: String?) {
        tvHidReader?.append(keyCode + "\n")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
//        hidKeyReader.parseKeyEvent(keyCode, event,KeyEvent.ACTION_UP)
        hidKeyReader.parseKeyEvent(keyCode, event)
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

}
