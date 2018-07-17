package com.nodepp.smartnode;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 常量
 */
public class Constant {
    // TLS SDK
    public static long SDK_APPID = 1400016653;
    public static int ACCOUNT_TYPE = 7865;
    public static boolean isDebug =true ;//是否是调试模式
    public static int DB_VERSION = 20;
    public static String DB_NAME = "nodepp.db";//当前数据库的名称
    public static String MAP_KEY = "8842bdbbd91e0aba1f5bed3458b40ae6";//高德地图的key
    // 登录方式
    public final static String EXTRA_LOGIN_WAY = "com.tencent.tls.LOGIN_WAY";
    public final static int NON_LOGIN = 0;
    public final static int USRPWD_LOGIN = (1 << 3);
    public final static int PHONEPWD_LOGIN = (1 << 4);
    public final static String EXTRA_IMG_CHECKCODE = "com.tencent.tls.EXTRA_IMG_CHECKCODE";
    //socket的地址和端口号
    public static InetAddress ip;
    public static int tempPort = 0;//临时保存设备的端口
    public static int deviceServerPort = 20080;//保存设备的端口
    public static int broadCastPort = 20140;//扫描设备的端口
    //记录左滑菜单的状态
    public static final String SERVER_HOST = "a.nodepp.com";
    public static boolean isMenuOpen = false;
    public final static int port = 20443;//连接服务器的端口
    public final static int TIME_OUT = 10000;//超时时间10s
    public final static int SOCKET_DATA_BUF_LEN = 8192;
    public final static int POOL_SIZE = 10;
    public final static int MAXIMUM_POOL_SIZE = 13;
    public final static long KEEPALIVE_TIME = 5;
    public static boolean isSendTask =false;
    public static int udpSocketPort = 20001;//udp默认端口
    public static byte[] key = null;
    public static int retry = 0;
    public static boolean isAddDevice = false;
    public static boolean isDeviceRename = false;
    public static String routerMac = "";
    public static Map<Long,Boolean> LANDevices = new Hashtable<>();
    public static String VAD_BOS_TIME = "10000";
    public static String VAD_EOS_TIME = "20000";
    public static byte[] KEY_A2S = null;
    public static String userName ="0";
    public static String usig ="";
    private static BlockingQueue<Runnable> queue = new LinkedBlockingDeque<Runnable>();
    public static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(POOL_SIZE, MAXIMUM_POOL_SIZE, KEEPALIVE_TIME, TimeUnit.MINUTES, queue);
}
