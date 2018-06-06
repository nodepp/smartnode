package com.nodepp.smartnode.model;

import com.google.protobuf.ByteString;

/**
 * Created by yuyue on 2016/8/17.
 * 用于临时记录信息
 */
public class UserInfo {
    public static String userName;
    public static String ip;
    public static long did = 0;
    public static long tid = 0;
    public static int deviceType = 0;
    public static int firmwareLevel = 0;
    public static int firmwareVersion = 0;
    public static int deviceMode = 0;
    public static ByteString dsig;//临时记录dsig
    public static ByteString keyClient;//秘钥
    public static ByteString keyClientWAN;//clietn-server秘钥
    public static int connetedMode = 1;//临时记录设备连接的方式,0是广域网模式，1是局域网近场通讯
    public static boolean isDeviceReturn = false;
}
