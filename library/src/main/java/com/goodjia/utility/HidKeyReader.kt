package com.goodjia.utility

import android.os.Handler
import android.os.HandlerThread
import android.view.KeyEvent
import com.goodjia.utility.HidKeyReader

class HidKeyReader @JvmOverloads constructor(hidKeyListener: HidKeyListener? = null) {
    private val code = StringBuilder()
    private val handlerThread by lazy {
        HandlerThread(TAG)
    }
    private val handler by lazy {
        Handler(handlerThread.looper) { msg ->
            if (msg.what == HANDLER_WHAT_PUBLISH) {
                publish()
            }
            true
        }
    }

    private val hidKeyListeners by lazy {
        mutableListOf<HidKeyListener>()
    }

    var delayMillis = DELAY_MILLIS

    init {
        handlerThread.start()
        hidKeyListener?.let {
            addOnHidKeyListener(it)
        }
    }

    @JvmOverloads
    fun parseKeyEvent(keyCode: Int, event: KeyEvent, action: Int = KeyEvent.ACTION_DOWN) {
        if (event.action != action) return

        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            publish()
        } else {
            code.append(event.unicodeChar.toChar())
            handler.sendEmptyMessageDelayed(HANDLER_WHAT_PUBLISH, delayMillis)
        }
    }

    fun destroy() {
        hidKeyListeners.clear()
        handlerThread.quit()
    }

    fun addOnHidKeyListener(hidKeyListener: HidKeyListener) {
        synchronized(hidKeyListeners) {
            hidKeyListeners.add(hidKeyListener)
        }
    }

    fun removeOnHidKeyListener(hidKeyListener: HidKeyListener) {
        synchronized(hidKeyListeners) {
            hidKeyListeners.remove(hidKeyListener)
        }
    }

    private fun publish() {
        synchronized(hidKeyListeners) {
            if (code.toString().isNotEmpty()) {
                hidKeyListeners.forEach {
                    it.onKeyEvent(code.toString())
                }
            }
            code.setLength(0)
            handler.removeCallbacksAndMessages(null)
        }
    }

    interface HidKeyListener {
        fun onKeyEvent(keyCode: String?)
    }

    companion object {
        private val TAG = HidKeyReader::class.simpleName
        private const val HANDLER_WHAT_PUBLISH = 12
        private const val DELAY_MILLIS = 300L
    }

}