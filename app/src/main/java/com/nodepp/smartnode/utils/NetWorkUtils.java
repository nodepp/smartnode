package com.nodepp.smartnode.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 判断网络状态的相关工具类
 * Created by yuyue on 2016/9/9.
 */
public class NetWorkUtils {
    //     判断是否有网络连接
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    //   判断WIFI网络是否可用
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    //    判断MOBILE网络是否可用
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    //     获取当前网络连接的类型信息
    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                return mNetworkInfo.getType();
            }
        }
        return -1;
    }
    // 获得所连路由器的MAC地址
    public static String getRouterMac(Context context) {//be:9f:ef:cd:ba:3f
        String mac = "";
        WifiManager wifiMan = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiInfo info = wifiMan.getConnectionInfo();
        if (info != null){
            mac = info.getBSSID();
        }
        return mac;
    }
    public static ArrayList<String> getAllIp(Context context) {
        ArrayList<String> IPs = new ArrayList<String>();
        WifiManager wm = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        DhcpInfo di = wm.getDhcpInfo();
        long getewayIpL = di.gateway;
        long netmaskIpL = di.netmask;
        Log.i("ip", "currentGateway=" + longToip(getewayIpL));
        long getewayIp = reverseLong(getewayIpL);//网关
        long netmaskIp = reverseLong(netmaskIpL);//子网掩码
        long ipSum = (long) Math.pow(2, 32) - 1 - netmaskIp;//总共有的ip数
        for (int i = 0; i < ipSum; i++) {
            long ipL = getewayIp + i;
            String ipS = long2ip(ipL);
            IPs.add(ipS);
        }
        Log.i("ff", "a=" + getewayIp);
        Log.i("ff", "netmaskIp=" + netmaskIp);
        Log.i("ff", "getewayIp&netmaskIp=" + (getewayIp & netmaskIp));
        Log.i("ff", "ipSum=" + ipSum);
        return IPs;
    }
    public static long getCurrentIp(Context context){
        context = context.getApplicationContext();
        WifiManager wm = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiInfo wifiInfo = wm.getConnectionInfo();
        if (wifiInfo != null){
            int ipAddress = wifiInfo.getIpAddress();
            return reverseInt(ipAddress);
        }else {
            return 0;//空的情况直接返回0
        }

    }
    public static String long2ip(long ip) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf((int) ((ip >> 24) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 16) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 8) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) (ip & 0xff)));
        return sb.toString();
    }

    public static String longToip(long ip) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf((int) (ip & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 8) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 16) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 24) & 0xff)));
        return sb.toString();
    }
    public static long reverseInt(int value) {
        long temp = value & 0xff;
        for (int i = 0; i < 3; i++) {
            value = (value >> 8);
            int n = value & 0xff;
            temp = (temp << 8) | n;
            Log.i("www","temp-"+temp);
        }
        return temp;
    }
    public static long reverseLong(long value) {
        long temp = value & 0xff;
        for (int i = 0; i < 3; i++) {
            value = (value >> 8);
            long n = value & 0xff;
            temp = (temp << 8) | n;
        }
        return temp;
    }
    public static boolean isNetworkOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("ping -c 2 -w 2 114.114.114.114");
            int status = ipProcess.waitFor();
            return (status == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
