package com.goodjia.utility

import android.os.Handler
import android.view.KeyEvent

class HidKeyReader @JvmOverloads constructor(private val hidKeyListener: HidKeyListener?) {
    private val code = StringBuilder()
    private val handler = Handler(Handler.Callback { msg ->
        if (msg.what == HANDLER_WHAT_PUBLISH) {
            publish()
        }
        true
    })
    var delayMillis = DELAY_MILLIS
    @JvmOverloads
    fun parseKeyEvent(keyCode: Int?, event: KeyEvent, action: Int = KeyEvent.ACTION_DOWN) {
        keyCode ?: return
        if (event.action != action) return

        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            publish()
        } else {
            code.append(event.unicodeChar.toChar())
            handler.sendEmptyMessageDelayed(HANDLER_WHAT_PUBLISH, delayMillis)
        }
    }

    private fun publish() {
        if (code.toString().isNotEmpty() && hidKeyListener != null) {
            hidKeyListener.onKeyEvent(code.toString())
        }
        code.setLength(0)
        handler.removeCallbacksAndMessages(null)
    }

    interface HidKeyListener {
        fun onKeyEvent(keyCode: String?)
    }

    companion object {
        private val TAG = HidKeyReader::class.java.simpleName
        private const val HANDLER_WHAT_PUBLISH = 12
        private const val DELAY_MILLIS = 300L
    }

}