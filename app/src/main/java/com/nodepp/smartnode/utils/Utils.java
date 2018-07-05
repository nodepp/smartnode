package com.nodepp.smartnode.utils;

import android.app.AppOpsManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;

import com.amap.api.location.AMapLocation;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.helper.CountDownButtonHelper;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.model.TimeTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import nodepp.Nodepp;
import tencent.tls.platform.TLSErrInfo;

public class Utils {
    private static final String TAG = "Utils";
    static {
        System.loadLibrary("mbedcrypto");
        System.loadLibrary("mbedx509");
        System.loadLibrary("mbedtls");
        System.loadLibrary("nodepp");
    }
    /**
     * @param countryCode 国家码
     * @param phoneNumber 手机号
     * @return 返回拼接后的字符串
     * @function 将国家码和手机号拼接成86-15112345678的形式
     */
    public static String getWellFormatMobile(String countryCode, String phoneNumber) {
        return countryCode + "-" + phoneNumber;
    }

    /**
     * @param phoneNumber 手机号
     * @return 有效则返回true, 无效则返回false
     * @function 判断手机号是否有效
     */
    public static boolean validPhoneNumber(String countryCode, String phoneNumber) {
        if (countryCode.equals("86"))
            return phoneNumber.length() == 11 && phoneNumber.matches("[0-9]{1,}");
        else
            return phoneNumber.matches("[0-9]{1,}");
    }

    public static boolean checkPhoneNumber(String phoneNumber) {
        return phoneNumber.length() == 11 && phoneNumber.matches("[0-9]{1,}");
    }

    /**
     * @param button        按钮控件
     * @param defaultString 按钮上默认的字符串
     * @param tmpString     计时时显示的字符串
     * @param max           失效时间（单位：s）
     * @param interval      更新间隔（单位：s）
     * @function 在按钮上启动一个定时器
     */
    public static void startTimer(Button button,
                                  String defaultString,
                                  String tmpString,
                                  int max,
                                  int interval) {
        CountDownButtonHelper timer = new CountDownButtonHelper(button,
                defaultString, tmpString, max, interval);
        timer.setOnFinishListener(new CountDownButtonHelper.OnFinishListener() {
            @Override
            public void finish() {

            }
        });
        timer.start();
    }

    /**
     * @function: 显示使用TLSSDK过程中发生的错误信息
     */
    public static void notOK(Context context, TLSErrInfo errInfo) {
        JDJToast.showMessage(context.getApplicationContext(), String.format("%s: %s",
                errInfo.ErrCode == TLSErrInfo.TIMEOUT ?
                        context.getString(R.string.net_timeout) : context.getString(R.string.error), errInfo.Msg));
    }

    /**
     * dp转pix
     *
     * @param context
     * @param dp
     * @return
     */
    public static int Dp2Px(Context context, float dp) {
        final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * px转dp
     *
     * @param context
     * @param px
     * @return
     */
    public static int Px2Dp(Context context, float px) {
        final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static long byteToLong(byte[] byt) {
        long l = 0;
        ByteArrayInputStream bais = new ByteArrayInputStream(byt);
        DataInputStream dis = new DataInputStream(bais);
        try {
            l = dis.readLong();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return l;
    }

    public static long byteToLong(byte[] bssidBytes, int offset, int count) {
        byte[] bytes = new byte[count];
        for (int i = 0; i < count; i++) {
            bytes[i] = bssidBytes[offset + count - i - 1];
        }
        long l = 0;
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);
        try {
            l = dis.readLong();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return l;
    }

    /**
     * 设置定时值
     *
     * @param isSun  是否选中星期日
     * @param isMOn  是否选中星期一
     * @param isTue  是否选中星期二
     * @param isWen  是否选中星期三
     * @param isThu  是否选中星期四
     * @param isFri  是否选中星期五
     * @param isSatu 是否选中星期六
     * @return
     */
    public static int getValue(boolean isSun, boolean isMOn, boolean isTue, boolean isWen, boolean isThu, boolean isFri, boolean isSatu) {
        int value = 0;
        value = isSun ? value | (1 << 0) : value;
        value = isMOn ? value | (1 << 1) : value;
        value = isTue ? value | (1 << 2) : value;
        value = isWen ? value | (1 << 3) : value;
        value = isThu ? value | (1 << 4) : value;
        value = isFri ? value | (1 << 5) : value;
        value = isSatu ? value | (1 << 6) : value;
        return value;
    }

    /**
     * 根据int 值获取0到6位的布尔值
     *
     * @param value
     * @return
     */
    public static ArrayList<Boolean> getSelectDayList(int value) {
        ArrayList<Boolean> daysList = new ArrayList<>();
        boolean isSun = (value & 01) == 1 ? true : false;
        boolean isMOn = (value >> 1 & 1) == 1 ? true : false;
        boolean isTue = (value >> 2 & 1) == 1 ? true : false;
        boolean isWen = (value >> 3 & 1) == 1 ? true : false;
        boolean isThu = (value >> 4 & 1) == 1 ? true : false;
        boolean isFri = (value >> 5 & 1) == 1 ? true : false;
        boolean isSatu = (value >> 6 & 1) == 1 ? true : false;
        daysList.add(isSun);
        daysList.add(isMOn);
        daysList.add(isTue);
        daysList.add(isWen);
        daysList.add(isThu);
        daysList.add(isFri);
        daysList.add(isSatu);
        return daysList;
    }

    /**
     * @param isSelectOne   是否选中开关1
     * @param isSelectTwo   是否选中开关2
     * @param isSelectThree 是否选中开关3
     * @param isSelectFour  是否选中开关4
     * @param isSelectFive  是否选中开关5
     * @param isSelectSix   是否选中开关6
     * @param isSelectSeven 是否选中开关7
     * @param isSelectEight 是否选中开关8
     * @return
     */
    public static int getSwitchValue(boolean isSelectOne, boolean isSelectTwo, boolean isSelectThree, boolean isSelectFour, boolean isSelectFive, boolean isSelectSix, boolean isSelectSeven, boolean isSelectEight) {
        int value = 0;
        value = isSelectOne ? value | (1 << 0) : value;
        value = isSelectTwo ? value | (1 << 1) : value;
        value = isSelectThree ? value | (1 << 2) : value;
        value = isSelectFour ? value | (1 << 3) : value;
        value = isSelectFive ? value | (1 << 4) : value;
        value = isSelectSix ? value | (1 << 5) : value;
        value = isSelectSeven ? value | (1 << 6) : value;
        value = isSelectEight ? value | (1 << 7) : value;
        return value;
    }

    /**
     * 根据定位结果返回定位信息的字符串
     *
     * @param
     * @return
     */
    public synchronized static String getLocationStr(AMapLocation location) {
        if (null == location) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
        if (location.getErrorCode() == 0) {
            sb.append("定位成功" + "\n");
            sb.append("定位类型: " + location.getLocationType() + "\n");
            sb.append("经    度    : " + location.getLongitude() + "\n");
            sb.append("纬    度    : " + location.getLatitude() + "\n");
            sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
            sb.append("提供者    : " + location.getProvider() + "\n");

            if (location.getProvider().equalsIgnoreCase(
                    android.location.LocationManager.GPS_PROVIDER)) {
                // 以下信息只有提供者是GPS时才会有
                sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
                sb.append("角    度    : " + location.getBearing() + "\n");
                // 获取当前提供定位服务的卫星个数
                sb.append("星    数    : "
                        + location.getSatellites() + "\n");
            } else {
                // 提供者是GPS时是没有以下信息的
                sb.append("国    家    : " + location.getCountry() + "\n");
                sb.append("省            : " + location.getProvince() + "\n");
                sb.append("市            : " + location.getCity() + "\n");
                sb.append("城市编码 : " + location.getCityCode() + "\n");
                sb.append("区            : " + location.getDistrict() + "\n");
                sb.append("区域 码   : " + location.getAdCode() + "\n");
                sb.append("地    址    : " + location.getAddress() + "\n");
                sb.append("兴趣点    : " + location.getPoiName() + "\n");
                //定位完成的时间
            }
        } else {
            //定位失败
            sb.append("定位失败" + "\n");
            sb.append("错误码:" + location.getErrorCode() + "\n");
            sb.append("错误信息:" + location.getErrorInfo() + "\n");
            sb.append("错误描述:" + location.getLocationDetail() + "\n");
        }
        //定位之后的回调时间
        return sb.toString();
    }

    /**
     * 检查是否获得悬浮窗权限
     *
     * @param context
     * @param op
     * @return
     */
    //OP_SYSTEM_ALERT_WINDOW=24   op = 24
    public static boolean checkOp(Context context, int op) {
        context = context.getApplicationContext();
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {

                Class<?> spClazz = Class.forName(manager.getClass().getName());
                Method method = manager.getClass().getDeclaredMethod("checkOp", int.class, int.class, String.class);
                int property = (Integer) method.invoke(manager, op,
                        Binder.getCallingUid(), context.getPackageName());

                if (AppOpsManager.MODE_ALLOWED == property) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {

            }
        } else {

        }
        return true;
    }

    public static void setBackground(Context context, View view, int sourceId) {
        context = context.getApplicationContext();
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), sourceId);
        BitmapDrawable bd = new BitmapDrawable(context.getResources(), bm);
        view.setBackgroundDrawable(bd);
    }

    public static void removeBackground(View view) {
        BitmapDrawable bd = (BitmapDrawable) view.getBackground();
        view.setBackgroundResource(0);//别忘了把背景设为null，避免onDraw刷新背景时候出现used a recycled bitmap错误
        bd.setCallback(null);
        bd.getBitmap().recycle();
    }

    public static ArrayList<Integer> changLedColor(int red, int green, int blue, int lightDark, int addColor) {
        ArrayList<Integer> colors = new ArrayList<>();
        if (lightDark < 5) {
            lightDark = 5;
        }
        double ratio = lightDark / 255.0;
        if (red == 0) {
            red = addColor;
        } else if (green == 0) {
            green = addColor;
        } else if (blue == 0) {
            blue = addColor;
        }
        double r = red * ratio;
        double g = green * ratio;
        double b = blue * ratio;
        colors.add((int) Math.ceil(r));
        colors.add((int) Math.ceil(g));
        colors.add((int) Math.ceil(b));
        Log.i("aa", "red===" + (int) Math.ceil(r));
        Log.i("aa", "green===" + (int) Math.ceil(g));
        Log.i("aa", "blue===" + (int) Math.ceil(b));
        return colors;
    }
    public static ArrayList<Integer> changWhiteLedColor(int lightDark, int suYan) {
        ArrayList<Integer> colors = new ArrayList<>();
        if (lightDark < 5) {
            lightDark = 5;
        }
        double ratio = lightDark / 255.0;
        double w = 255 * ratio;
        double r = suYan * ratio;
        double g = suYan * ratio;
        colors.add((int) Math.ceil(w));
        colors.add((int) Math.ceil(r));
        colors.add((int) Math.ceil(g));
        colors.add(0);
        Log.i("aa", "w===" + (int) Math.ceil(w));
        Log.i("aa", "r===" + (int) Math.ceil(r));
        Log.i("aa", "g===" + (int) Math.ceil(g));
        return colors;
    }
    public static InetAddress getBroadcastAdress(Context context) {
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        InetAddress addr = null;
            try {
                if(dhcp==null) {
                    addr = InetAddress.getByName("255.255.255.255");
                }else {
                    int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
                    byte[] quads = new byte[4];
                    for (int k = 0; k < 4; k++)
                        quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
                    addr = InetAddress.getByAddress(quads);
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        return addr;
    }
    public static ArrayList<String> getAllIp(Context context) {
        context = context.getApplicationContext();
        ArrayList<String> IPs = new ArrayList<String>();
        WifiManager wm = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        DhcpInfo di = wm.getDhcpInfo();
        long getewayIpL = di.gateway;
        long netmaskIpL = di.netmask;
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

    public static long reverseLong(long value) {
        long temp = value & 0xff;
        for (int i = 0; i < 3; i++) {
            value = (value >> 8);
            long n = value & 0xff;
            temp = (temp << 8) | n;
        }
        return temp;
    }

    public static byte[] pakeKey(long ip,int port,String value){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.write(value.getBytes());
            dos.writeInt((int) ip);
            dos.writeShort(port);
            byte[] data = bos.toByteArray();
            Log.i("key","key===="+bytesToHexString(data));
            bos.close();
            dos.close();
            return data;
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * 16进制表示的字符串转换为字节数组
     *
     * @param s 16进制表示的字符串
     * @return byte[] 字节数组
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] b = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            b[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return b;
    }
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    public native static byte[] encrypt(byte[] data, byte[] key);
    public native static byte[] decrypt(byte[] data, byte[] key);
    public native static int connect(String address);
    public native static void disConnect();
    public native static byte[] send(String address,byte[] sendData,int dataLen,int maxReadLen);
    public native static byte[] receive(String address,int maxReadLen);
    public static String bytesToHexStringNoSpace(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append("0x");
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    /**
     * 将两个ASCII字符合成一个字节；
     * 如："EF"--> 0xEF
     * @param src0 byte
     * @param src1 byte
     * @return byte
     */
    public static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
        _b0 = (byte)(_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
        byte ret = (byte)(_b0 ^ _b1);
        return ret;
    }

    /**
     * 将指定字符串src，以每两个字符分割转换为16进制形式
     * 如："2B44EFD9" --> byte[]{0x2B, 0x44, 0xEF, 0xD9}
     * @param src String
     * @return byte[]
     */
    public static byte[] HexString2Bytes(String src){
        byte[] ret = new byte[8];
        byte[] tmp = src.getBytes();
        for(int i=0; i<8; i++){
            ret[i] = uniteBytes(tmp[i*2], tmp[i*2+1]);
        }
        return ret;
    }
    //生成为0的定时任务
    public static TimeTask getTimerTaskCancle() {
        TimeTask timerTask = new TimeTask();
        timerTask.setTimeSet(0);
        timerTask.setTimeRepeaat(0);
        timerTask.setTimeOperate(0);
        timerTask.setTimeStamp0(0);
        timerTask.setTimeStamp1(0);
        timerTask.setTimeStamp2(0);
        timerTask.setTimeStamp3(0);
        timerTask.setTimeStamp4(0);
        timerTask.setTimeStamp5(0);
        timerTask.setTimeStamp6(0);
        return timerTask;
    }

    public static String timestampToString(int time){
        Date date = new Date(time *1000L);
        SimpleDateFormat format =  new SimpleDateFormat("HH:mm:ss");
        String dateStr = format.format(date);
        return dateStr;
    }

    public static List<TimeTask> convertTimerTask(Nodepp.Msg msg,int deviceId,long did,long tid){
        List<TimeTask> lists = new ArrayList<>();
        if (msg.getTimersCount() > 0) {
            for (int i = 0; i < msg.getTimersCount(); i++) {
                Nodepp.Timer timers = msg.getTimers(i);
                TimeTask timeTask = new TimeTask();
                timeTask.setDeviceId(deviceId);
                timeTask.setDid(did);
                timeTask.setTid(tid);
                timeTask.setOperateIndex(timers.getOperateIndex());
                int timeIsopen = timers.getTimeIsopen();
                timeTask.setIsOpen(timeIsopen == 0?false:true);
                int timeOperate = timers.getTimeOperate();
                timeTask.setTimeOperate(timeOperate);
                int timeRepeat = timers.getTimeRepeat();
                timeTask.setIsRepeat(timeRepeat == 0?false:true);
                timeTask.setTimeRepeaat(timeRepeat);
                int timeSet = 0;
                //根据7个时间戳生成timeSet，因为固件执行后timeSet一直是0
                for (int j = 0,value = timers.getTimeStampList().size() ;j < value;j++){
                    if (timers.getTimeStamp(j) != 0){
                        timeSet |= 1 << j;
                    }
                }
                timeTask.setIsSunday((timeSet&1) == 0?false:true);
                timeTask.setIsMonday((timeSet&2) == 0?false:true);
                timeTask.setIsTuesday((timeSet&4) == 0?false:true);
                timeTask.setIsWednesday((timeSet&8) == 0?false:true);
                timeTask.setIsThursday((timeSet&16) == 0?false:true);
                timeTask.setIsFriday((timeSet&32) == 0?false:true);
                timeTask.setIsSaturday((timeSet&64) == 0?false:true);
                timeTask.setTimeSet(timeSet);
                timeTask.setTimeStamp0(timers.getTimeStamp(0));
                timeTask.setTimeStamp1(timers.getTimeStamp(1));
                timeTask.setTimeStamp2(timers.getTimeStamp(2));
                timeTask.setTimeStamp3(timers.getTimeStamp(3));
                timeTask.setTimeStamp4(timers.getTimeStamp(4));
                timeTask.setTimeStamp5(timers.getTimeStamp(5));
                timeTask.setTimeStamp6(timers.getTimeStamp(6));
                int saveTime = 0;
                for (int j = 0,value = timers.getTimeStampList().size() ;j < value;j++){//取出不为0的间戳
                    int time = timers.getTimeStamp(j);
                    if (time > 0){
                        saveTime = time;
                        break;
                    }
                }
                //将时间戳保存为时间存起来
                if (saveTime == 0){
                    //不是执行的定时任务，不保存
                }else {
                    //有效的定时任务
                    timeTask.setTime(Utils.timestampToString(saveTime));
                    timeTask.setIsUse(true);
                    lists.add(timeTask);
                }
            }
        }
        return lists;
    }

    public static void saveOrUpdateTimeList(Context context, List<TimeTask> timeTasks, List<TimeTask> dbTimeTasks, Handler handler){
        DbUtils dbUtils = DBUtil.getInstance(context);
        List<TimeTask> sameLists = new ArrayList<TimeTask>();
        if (dbTimeTasks != null && dbTimeTasks.size() > 0) {//数据库有旧的定时任务
            //双层循环寻找查询到任务与本地任务相同的任务
            for (TimeTask dbTask : dbTimeTasks ) {
                dbTask.setIsUse(false);//本地的任务全部设置为不执行的任务
                for (TimeTask task : timeTasks) {
                    if (dbTask.getTime().equals(task.getTime()) && dbTask.getTimeSet() == task.getTimeSet() && dbTask.getTimeOperate() == task.getTimeOperate()) {
                        //相同的任务
                        task.setId(dbTask.getId());//必须设置给查询到到任务设置原来的主键，否则不会更新，而是又保存一个
                        sameLists.add(dbTask);
                        break;
                    }
                }
            }
            dbTimeTasks.removeAll(sameLists);
            int count =( dbTimeTasks == null?0:dbTimeTasks.size()) + timeTasks.size();
            try {
                if (count > 10) { //本地的加上查询到到的与本地不同的定时任务总数大于10个。把本地保存的（取消状态的定时任务）去掉，只保存固件查询到的定时任务
                    dbUtils.deleteAll(dbTimeTasks);
                }else {
                    dbUtils.saveOrUpdateAll(dbTimeTasks);
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
        try {
            dbUtils.saveOrUpdateAll(timeTasks);//查询到到定时任务进行更新或者保存
            handler.sendEmptyMessage(2);//刷新
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
    //获取手机的唯一标识
    public static String getPhoneSign(Context context){
        StringBuilder deviceId = new StringBuilder();
        // 渠道标志
        deviceId.append("android:");
        try {
            //IMEI（imei）
            TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm.getDeviceId();
            if(!TextUtils.isEmpty(imei)){
                deviceId.append("imei");
                deviceId.append(imei);
                return deviceId.toString();
            }
            //序列号（sn）
            String sn = tm.getSimSerialNumber();
            if(!TextUtils.isEmpty(sn)){
                deviceId.append("sn");
                deviceId.append(sn);
                return deviceId.toString();
            }
            //如果上面都没有， 则生成一个id：随机码
            String uuid = getUUID(context);
            if(!TextUtils.isEmpty(uuid)){
                deviceId.append("id");
                deviceId.append(uuid);
                return deviceId.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            deviceId.append("id").append(getUUID(context));
        }
        return deviceId.toString();
    }
    /**
     * 得到全局唯一UUID
     */
    public static String getUUID(Context context){
        String uuid = SharedPreferencesUtils.getString(context,"uuid","");
        if(TextUtils.isEmpty(uuid)){
            uuid = UUID.randomUUID().toString();
            SharedPreferencesUtils.saveString(context,"uuid",uuid);
        }
        return uuid;
    }

    public static String encryptTwoCode(Nodepp.Msg msg){
        String key = "www.com.nodepp.1";
        String reslut = null;
        byte[] encrypt = null;
        try {
            encrypt = Utils.encrypt(msg.toByteArray(), key.getBytes("utf-8"));
            reslut = Base64.encodeToString(encrypt, Base64.NO_WRAP);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            msg = null;
            Log.i("aaa","InvalidProtocolBufferException--"+e.toString());
        }
        return reslut;
    }

    public static Nodepp.Msg decryptTwoCode(String result){
        Nodepp.Msg msg = null;

        boolean isBase64 = isBase64Encode(result);
        Log.i("base64","isBase64Encode:"+isBase64);
        if (TextUtils.isEmpty(result)){
            return null;
        }
        if (!isBase64){
            return null;
        }
        try {
            String key = "www.com.nodepp.1";
            byte[] decode = Base64.decode(result, Base64.NO_WRAP);//一些二维码内容直接用base64解码Crash
            byte[] decrypt = Utils.decrypt(decode, key.getBytes("utf-8"));
            msg = nodepp.Nodepp.Msg.parseFrom(decrypt);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            msg = null;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            msg = null;
        }
        return msg;
    }
    public static boolean isBase64Encode(String content){
        if(content.length()%4!=0){
            return false;
        }
        String pattern = "^[a-zA-Z0-9/+]*={0,2}$";
        return Pattern.matches(pattern, content);
    }
    public static String getCreateTwoCodeString(Device device, ByteString sharesig){
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Device.Builder deviceBuilder = nodepp.Nodepp.Device.newBuilder();
        deviceBuilder.setDeviceType(device.getDeviceType());
        deviceBuilder.setDid(device.getDid());
        deviceBuilder.setTid(device.getTid());
        deviceBuilder.setRouterMac(PbDataUtils.string2ByteString(device.getRouterMac()));
        deviceBuilder.setDeviceIp(PbDataUtils.string2ByteString(device.getIp()));
        msgBuilder.addDevices(deviceBuilder);
        msgBuilder.setShareVerification(sharesig);
        Nodepp.Msg message = msgBuilder.build();
        String s = Utils.encryptTwoCode(message);
        return s;
    }

    /**
     * 判断timeStamp距离当前时间是否在24小时之内
     */
    public static boolean isIn24Hours(long timeStamp){
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - timeStamp < 24*60*60*1000 && currentTimeMillis - timeStamp > 0){
            //24小时之内
            return true;
        }else{
            return false;
        }
    }
    public static String toHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;//0x表示十六进制
    }
    // 转化十六进制编码为字符串
    public static String toStringHex(String s)
    {
        byte[] baKeyword = new byte[s.length()/2];
        for(int i = 0; i < baKeyword.length; i++)
        {
            try
            {
                baKeyword[i] = (byte)(0xff & Integer.parseInt(s.substring(i*2, i*2+2),16));
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            s = new String(baKeyword, "utf-8");//UTF-16le:Not
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
        return s;
    }

}
