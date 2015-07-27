package com.jenwis.websocket.base;

import android.util.Log;

/**
 * Created by zhengyuji on 15/6/17.
 */
public class LogUtils {
    private static final boolean IS_DEBUG = true;
    private static final String TAG = "LogUtils";

    public static String makeLogTag(Class clazz) {
        return clazz.getSimpleName();
    }

    public static void LogD(String log) {
        if (IS_DEBUG) {
            Log.d(TAG, log);
        }
    }

    public static void LogD(String tag, String log) {
        if (IS_DEBUG) {
            Log.d(tag, log);
        }
    }

    public static void LogE(String log) {
        Log.e(TAG, log);
    }

    public static void LogE(String tag, String log) {
        Log.e(TAG, log);
    }
}
