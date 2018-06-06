package com.nodepp.smartnode.utils;

import com.nodepp.smartnode.Constant;

/**
 * Created by yuyue on 2017/3/24.
 */
public class Log {
    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static int logLevel = Log.VERBOSE;

    public static void i(String tag, String msg) {
        if (msg == null){
            return;
        }
        if ((logLevel <= Log.INFO) && Constant.isDebug)
            android.util.Log.i(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (msg == null){
            return;
        }
        if ((logLevel <= Log.ERROR) && Constant.isDebug)
            android.util.Log.e(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (msg == null){
            return;
        }
        if ((logLevel <= Log.DEBUG) && Constant.isDebug)
            android.util.Log.d(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (msg == null){
            return;
        }
        if ((logLevel <= Log.VERBOSE) && Constant.isDebug)
            android.util.Log.v(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (msg == null){
            return;
        }
        if ((logLevel <= Log.WARN) && Constant.isDebug)
            android.util.Log.w(tag, msg);
    }
}
