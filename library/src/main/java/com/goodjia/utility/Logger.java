package com.goodjia.utility;


import android.util.Log;

public final class Logger {
    private static int level = Log.DEBUG;
    private static boolean isDebug = false;

    public static void init(int level, boolean isDebug) {
        setLevel(level);
        setIsDebug(isDebug);
    }

    public static void v(String tag, String message) {
        if (isDebug && Log.VERBOSE >= level) {
            Log.v(tag, message);
        }
    }

    public static void d(String tag, String message) {
        if (isDebug && Log.DEBUG >= level) {
            Log.d(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (Log.INFO >= level) {
            Log.i(tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (Log.WARN >= level) {
            Log.w(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (Log.ERROR >= level) {
            Log.e(tag, message);
        }
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static void setIsDebug(boolean isDebug) {
        Logger.isDebug = isDebug;
    }

    public static int getLevel() {
        return level;
    }

    public static void setLevel(int level) {
        Logger.level = level;
    }
}
