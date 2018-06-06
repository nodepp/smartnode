package com.nodepp.smartnode.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.nodepp.smartnode.task.UpdateTask;

/**
 * Created by yuyue on 2016/9/21.
 * 版本更新检测类
 */
public class CheckUpdate {

    /**
     * 获取本地app版本
     */
    public static int getLocalVersionCode(Context context) {
        int version = 0;
        try {
            PackageInfo packageInfo =context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }
    /**
     * 获取本地app版本名
     */
    public static String getLocalVersionName(Context context) {
        String version = "";
        try {
            PackageInfo packageInfo =context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 版本更新
     * @param context
     * @param path
     * @param versionNew
     */
    public static void update(Context context,String path,int versionNew){
        UpdateTask updateTask = new UpdateTask(context);
        updateTask.execute(path,String.valueOf(versionNew));
    }
}
