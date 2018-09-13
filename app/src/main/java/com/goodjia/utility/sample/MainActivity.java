package com.goodjia.utility.sample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.TextView;

import com.goodjia.utility.GjLogger;
import com.goodjia.utility.GjUtil;
import com.goodjia.utility.HidKeyReader;

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

        GjLogger.setIsDebug(BuildConfig.DEBUG);

        findViewById(R.id.btnLogD).setOnClickListener(v -> {
            GjLogger.d(TAG, "btnLogD");
            GjUtil.toastLong(this, "Log d onclicked");
        });
        findViewById(R.id.btnLogV).setOnClickListener(v -> {
            GjLogger.v(TAG, "btnLogV");
            GjUtil.toastShort(this, "Log v onclicked");
        });
        findViewById(R.id.btnLogW).setOnClickListener(v -> {
            GjLogger.w(TAG, "btnLogW");
            GjUtil.toastShort(this, R.string.app_name);
        });
        findViewById(R.id.btnLogI).setOnClickListener(v -> {
            GjLogger.i(TAG, "btnLogI");
        });
        findViewById(R.id.btnLogE).setOnClickListener(v -> {
            GjLogger.e(TAG, "btnLogE");
        });

        SharedPreferences sharedPreferences = GjUtil.getSharedPreferences(this);
        sharedPreferences.edit().putInt("1", 1).commit();
        GjLogger.d(TAG, "get key 1: " + sharedPreferences.getInt("1", 0));

        SharedPreferences sharedPreferences1 = GjUtil.getSharedPreferences(this, "test");
        sharedPreferences.edit().putString("1", "1").commit();
        GjLogger.d(TAG, "get key 1: " + sharedPreferences.getString("1", "null"));

        GjUtil.toastShort(this, "is network available: " + GjUtil.isNetworkAvailable(this));

        GjLogger.d(TAG, "has nav bar: " + GjUtil.hasNavBar(this));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        hidKeyReader.parseKeyEvent(keyCode, event);
        return super.onKeyDown(keyCode, event);
    }
}
