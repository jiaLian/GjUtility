package com.goodjia.utility.sample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.TextView;

import com.goodjia.utility.HidKeyReader;
import com.goodjia.utility.Logger;
import com.goodjia.utility.Util;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private HidKeyReader hidKeyReader;
    private TextView tvHidReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnClearHid).setOnClickListener(v -> {
            tvHidReader.setText("");
        });
        tvHidReader = findViewById(R.id.tvHidReader);
        hidKeyReader = new HidKeyReader(keyCode -> {
            tvHidReader.append(keyCode + "\n");
        });
//        hidKeyReader.setDelayMillis(500);

        Logger.setIsDebug(BuildConfig.DEBUG);

        findViewById(R.id.btnLogD).setOnClickListener(v -> {
            Logger.d(TAG, "btnLogD");
            Util.toastLong(this, "Log d onclicked");
        });
        findViewById(R.id.btnLogV).setOnClickListener(v -> {
            Logger.v(TAG, "btnLogV");
            Util.toastShort(this, "Log v onclicked");
        });
        findViewById(R.id.btnLogW).setOnClickListener(v -> {
            Logger.w(TAG, "btnLogW");
            Util.toastShort(this, R.string.app_name);
        });
        findViewById(R.id.btnLogI).setOnClickListener(v -> {
            Logger.i(TAG, "btnLogI");
            boolean isOnline = Util.isOnline(5_000L, "yahoo.com");
            Logger.d(TAG, "isOnline " + isOnline);
        });
        findViewById(R.id.btnLogE).setOnClickListener(v -> {
            Logger.e(TAG, "btnLogE");
            boolean isOnline = Util.isOnline(5_000L);
            Logger.d(TAG, "isOnline " + isOnline);
        });

        SharedPreferences sharedPreferences = Util.getSharedPreferences(this);
        sharedPreferences.edit().putInt("1", 1).commit();
        Logger.d(TAG, "get key 1: " + sharedPreferences.getInt("1", 0));

        SharedPreferences sharedPreferences1 = Util.getSharedPreferences(this, "test");
        sharedPreferences.edit().putString("1", "1").commit();
        Logger.d(TAG, "get key 1: " + sharedPreferences.getString("1", "null"));

        Util.toastShort(this, "is network available: " + Util.isNetworkAvailable(this));

        Logger.d(TAG, "has nav bar: " + Util.hasNavBar(this));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        hidKeyReader.parseKeyEvent(keyCode, event);
        return super.onKeyDown(keyCode, event);
    }
}
