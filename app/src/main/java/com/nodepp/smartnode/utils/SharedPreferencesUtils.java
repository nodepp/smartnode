package com.nodepp.smartnode.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yuyue on 2016/9/12.
 */
public class SharedPreferencesUtils {
    public static final String SP_NAME = "info";
    public static void saveString(Context context, String key, String value) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(SP_NAME, 0);
        sp.edit().putString(key, value).commit();
    }
    public static String getString(Context context, String key, String defValue) {
        SharedPreferences sp =  context.getApplicationContext().getSharedPreferences(SP_NAME, 0);
        return sp.getString(key, defValue);
    }
    public static void saveLong(Context context, String key, long value) {
        SharedPreferences sp =  context.getApplicationContext().getSharedPreferences(SP_NAME, 0);
        sp.edit().putLong(key, value).commit();
    }
    public static long getLong(Context context, String key, long defValue) {
        SharedPreferences sp =  context.getApplicationContext().getSharedPreferences(SP_NAME, 0);
        return sp.getLong(key, defValue);
    }
    public static void saveInt(Context context, String key, int value) {
        SharedPreferences sp =   context.getApplicationContext().getApplicationContext().getSharedPreferences(SP_NAME, 0);
        sp.edit().putInt(key, value).commit();
    }
    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences sp =  context.getApplicationContext().getSharedPreferences(SP_NAME, 0);
        return sp.getInt(key, defValue);
    }
    public static void saveBoolean(Context context, String key, boolean value) {
        SharedPreferences sp =  context.getApplicationContext().getSharedPreferences(SP_NAME, 0);
        sp.edit().putBoolean(key, value).commit();
    }
    public static boolean getBoolean(Context context, String key, boolean defValue) {
        SharedPreferences sp =  context.getApplicationContext().getSharedPreferences(SP_NAME, 0);
        return sp.getBoolean(key, defValue);
    }
    public static void remove(Context context,String key) {
        SharedPreferences sp =  context.getApplicationContext().getSharedPreferences(SP_NAME, 0);
        sp.edit().remove(key).commit();
    }
}
