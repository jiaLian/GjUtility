package com.goodjia.utility;

import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

public class HidKeyReader {
    private static final String TAG = HidKeyReader.class.getSimpleName();
    private static final int HANDLER_WHAT_PUBLISH = 12;
    private static final int DELAY_MILLIS = 300;

    private StringBuilder code = new StringBuilder();
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == HANDLER_WHAT_PUBLISH) {
                publish();
            }
            return true;
        }
    });

    private HidKeyListener hidKeyListener;
    private long delayMillis = DELAY_MILLIS;

    public HidKeyReader(HidKeyListener hidKeyListener) {
        this.hidKeyListener = hidKeyListener;
    }

    public void setDelayMillis(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public void parseKeyEvent(int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) {
            return;
        }

        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            publish();
        } else {
            code.append((char) event.getUnicodeChar());
            handler.sendEmptyMessageDelayed(HANDLER_WHAT_PUBLISH, delayMillis);
        }
    }

    private void publish() {
        if (!code.toString().isEmpty() && hidKeyListener != null) {
            hidKeyListener.onKeyEvent(code.toString());
        }
        this.code.setLength(0);
        handler.removeCallbacksAndMessages(null);
    }

    public interface HidKeyListener {
        void onKeyEvent(String keyCode);
    }
}
