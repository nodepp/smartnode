package com.nodepp.smartnode.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.google.protobuf.ByteString;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.adapter.DeviceAdapter;
import com.nodepp.smartnode.dtls.DTLSSocket;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.task.CheckConnectTask;
import com.nodepp.smartnode.task.NetWorkListener;
import com.nodepp.smartnode.twocode.CaptureActivity;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.UDPClient;
import com.nodepp.smartnode.udp.UDPClientA2S;
import com.nodepp.smartnode.udp.UDPClientScan;
import com.nodepp.smartnode.udp.UDPClientScanA2S;
import com.nodepp.smartnode.udp.UDPSocketA2S;
import com.nodepp.smartnode.utils.CheckUpdate;
import com.nodepp.smartnode.utils.ClickUtils;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.utils.Utils;
import com.nodepp.smartnode.view.CircleImageView;
import com.nodepp.smartnode.view.PopupWindow.AddDevicePopup;
import com.nodepp.smartnode.view.TitleBar;
import com.nodepp.smartnode.view.pullToRefresh.PullToRefreshLayout;
import com.nodepp.smartnode.view.pullToRefresh.pullableview.PullableListView;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import cn.jpush.android.api.JPushInterface;
import nodepp.Nodepp;

public class MainActivity extends BaseVoiceActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int REFRESH_UI = 1;
    public static final int REFRESH_FINISH = 2;
    public static final int CONNECT_RETRY = 4;
    public static final int REFRESH_WAN = 5;
    private LinearLayout llNothing;//没有设备时显示的页面
    private PullableListView listView;//设备列表
    private DeviceAdapter socketAdapter;
    private List<Device> devices;
    private SlidingMenu menu;
    private LinearLayout llLogout;
    private LinearLayout llScanning;
    private CircleImageView ivHeadPhoto;
    private TextView tvNickName;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    private LinearLayout llVideo;
    private LinearLayout llSetting;
    private LinearLayout llHead;
    private PullToRefreshLayout llRefresh;//设备页面
    private boolean isLogin;
    private int retryCount = 0;//网络无法访问到外网时进行重试到次数
    private Button btnAddDevice;
    private UDPClientScan udpClientScan;
    private LinearLayout llStore;
    private LinearLayout llNoNetWork;
    private HashMap<String, Device> namesMap = new HashMap<>();
    private ScreenChangeReceiver screenChangeReceiver;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_UI:
                    handler.removeMessages(REFRESH_UI);
                    try {
                        devices = DBUtil.getInstance(MainActivity.this).findAll(Selector.from(Device.class).where("userName", "=", Constant.userName).orderBy("id", false));
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                    showUI((devices == null || devices.size() == 0) ? false : true);
                    break;
                case REFRESH_WAN:
                    handler.removeMessages(REFRESH_WAN);
                    try {
                        devices = DBUtil.getInstance(MainActivity.this).findAll(Selector.from(Device.class).where("userName", "=", Constant.userName).orderBy("id", false));
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                    showUI((devices == null || devices.size() == 0) ? false : true);
                    llNoNetWork.setVisibility(View.GONE);//互联网查到数据隐藏 网络提示
                    break;
                case REFRESH_FINISH:
                    llRefresh.refreshFinish(PullToRefreshLayout.SUCCEED);
                    break;
                case CONNECT_RETRY:
                    initDevice(true);
                    break;
            }
        }
    };
    private TitleBar titleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String username = SharedPreferencesUtils.getString(this, "username", "0");
        String uidSig = SharedPreferencesUtils.getString(this, "uidSig", "0");
        Constant.userName = username;
        Constant.usig = uidSig;
        initLocation();
        initMenu();
        initView();
        Intent intent = getIntent();
        isLogin = intent.getBooleanExtra("login", false);
        init();//版本检测
        initDevice(true);
        //注册屏幕广播接收者
        registerScreenChangeReceiver();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * @param isQueryFromWAN true代表不仅查询LAN设备同时通过WAN查询设备状态，false代表只从LAN查询设备状态
     */
    private void initDevice(final boolean isQueryFromWAN) {
        final List<Device> listInternet = new ArrayList<Device>();
        try {
            devices = DBUtil.getInstance(MainActivity.this).findAll(Selector.from(Device.class).where("userName", "=", Constant.userName).orderBy("id", false));
            CheckConnectTask checkConnectTask = new CheckConnectTask(MainActivity.this);
            WeakReference<CheckConnectTask> udpAsyncTaskWeakReference = new WeakReference<>(checkConnectTask);
            CheckConnectTask task = udpAsyncTaskWeakReference.get();
            if (task == null){
                return;
            }
            task.setNetWorkListener(new NetWorkListener() {//通过ping来检测是否可以连接到外网
                @Override
                public void onSuccess(int state) {
                    Log.i(TAG, "state====" + state);
                    if (state == -1) {
                        llNoNetWork.setVisibility(View.VISIBLE);
                        setAllDeivesNotOnline();  //没有网络，把所有设备状态设置为不在线
                    } else if (state == -2) {
                        //有网络但是连接不到互联网,重试1次
                        if (retryCount < 1){
                            retryCount ++;
                            handler.sendEmptyMessage(CONNECT_RETRY);
                        }else {
                            //网络不通畅,先把所有设备设置为不在线的状态，然后通过局域网查询，能查到的设置为在线状态
                            setAllDeivesNotOnline();
                            llNoNetWork.setVisibility(View.VISIBLE);
                            scanLANDevices(devices);
                        }
                    } else if (state == 0) {
                        //有网络
                        retryCount = 0;
                        llNoNetWork.setVisibility(View.GONE);
                        if (devices != null) {
                            Constant.LANDevices.clear();
                            scanLANDevices(devices);
                            if (isQueryFromWAN) {
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (devices.size() > 0) {
                                            for (int i = 0; i < devices.size(); i++) {
                                                Device device = devices.get(i);
                                                if (device.getDid() != 0) {//0代表这个设备是群组
                                                    listInternet.add(device);
                                                }
                                            }
                                            if (listInternet != null) {
                                                queryDeviceStateFormWAN(listInternet);
                                            }
                                        }
                                    }
                                }, 1500);//延迟1s再进行WAN查询设备，确保LAN查询已经完成
                            }
                        }
                    }
                }

                @Override
                public void onFaile() {

                }
            });
            task.execute();
        } catch (DbException e) {
            e.printStackTrace();
        } finally {
            handler.sendEmptyMessageDelayed(2, 3000);
        }

    }

    /**
     * 查询局域网设备状态
     *
     * @param
     */
    private void setAllDeivesNotOnline() {
        if (devices != null) {
            if (devices.size() > 0) {
                for (int i = 0; i < devices.size(); i++) {
                    Device device = devices.get(i);
                    device.setIsOnline(false);
                    try {
                        DBUtil.getInstance(MainActivity.this).update(device, WhereBuilder.b("userName", "=", Constant.userName).and("did", "=", device.getDid()));
                    } catch (DbException e1) {
                        e1.printStackTrace();
                    }
                }
                Message message = handler.obtainMessage();
                message.obj = devices;
                message.what =1;
                handler.sendMessage(message);
            }
        }
    }

    /**
     * 扫描局域网设备
     * @param list
     */
    private void scanLANDevices(final List<Device> list) {
        if (NetWorkUtils.isWifiConnected(MainActivity.this)) {
            Thread scanThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    UDPClientScan.getInstance().sendroadcastPacket(MainActivity.this);
                    UDPClientScan.getInstance().sendroadcastPacket(MainActivity.this);
                    if (list != null) {
                        UDPClientScan.getInstance().receiveScanData(MainActivity.this,handler, list);
                    }
                }
            });
            scanThread.setPriority(10);//优先级最高
            scanThread.start();
        } else {

        }
    }

    /**
     * 从广域网查询设备的状态
     *
     * @param list 要查询到设备列表
     */
    private void queryDeviceStateFormWAN(final List<Device> list) {
        if (NetWorkUtils.isNetworkConnected(MainActivity.this)) {
            Constant.threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    UDPClientScanA2S.getInstance().receiveScanData(MainActivity.this,list,handler);
                }
            });
            Constant.threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    Nodepp.Msg msg = null;
                    for (int i = 0; i < list.size(); i++) {//从服务器查询当前所有设备的状态
                        final Device device = list.get(i);
                        long uid = Long.parseLong(Constant.userName);
                        if (device.getDeviceType() == 3) {
                            msg = PbDataUtils.setQueryCorlorLightStateParam(uid, device.getDid(), device.getTid(), Constant.usig);
                        } else {
                            msg = PbDataUtils.setQueryStateRequestParam(uid, device.getDid(), device.getTid(), Constant.usig);
                        }

                        Log.i("server",msg.toString());
                        UDPClientScanA2S.getInstance().queryDevice(msg);
                    }

                }
            });
        } else {
            JDJToast.showMessage(MainActivity.this, "网络没有连接");
        }
    }


    /**
     * 初始化定位
     */
    private void initLocation() {
        //初始化client
        mLocationClient = new AMapLocationClient(this.getApplicationContext());
        //设置定位参数
        mLocationClient.setLocationOption(getDefaultOption());
        // 设置定位监听
        mLocationClient.setLocationListener(locationListener);
        //开始定位
        mLocationClient.startLocation();
    }

    /**
     * 定位监听
     */
    public AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation loc) {
            if (null != loc) {
                //解析定位结果
                String result = Utils.getLocationStr(loc);
                Log.i(TAG, result);
            } else {
                JDJToast.showMessage(MainActivity.this, "定位失败了");
            }
        }
    };

    @Override
    protected void onDestroy() {
        if (screenChangeReceiver != null) {
            unregisterReceiver(screenChangeReceiver);
        }
        handler.removeCallbacksAndMessages(null);
        mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
        mLocationClient.onDestroy();//销毁定位客户端，同时销毁本地定位服务。
        //停止推送
//        JPushInterface.stopPush(getApplicationContext());
        new Thread() {
            @Override
            public void run() {
                UDPClient.getInstance(MainActivity.this).close();
                UDPClient.getInstance(MainActivity.this).closeSocket();
                UDPClientScanA2S.getInstance().close();
            }
        }.start();
        //清除掉所有跟次handler相关的Runnable和Message，防止发生内存泄漏了
        super.onDestroy();
    }

    /**
     * 默认的定位参数
     */

    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
//        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false,true表示单次定位
        mOption.setOnceLocationLatest(true);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        return mOption;
    }

    /**
     * 初始化左边菜单
     */
    private void initMenu() {
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);//渐变过渡
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.menu);
    }

    @Override
    protected void onResume() {
        if (menu.isMenuShowing()) {
            menu.toggle(false);//无动画关闭菜单
        }

        boolean isUpdateUserWord = SharedPreferencesUtils.getBoolean(this, Constant.userName + "isUpdateUserWord", true);
        if (Constant.isDeviceRename){
            Constant.isDeviceRename = false;
            handler.sendEmptyMessage(1);
        }else {
            initDevice(false);
        }
        if (devices != null && devices.size() > 0) {
            clientKeys.clear();//清除Map中缓存中的clientKey，重新获取，确保最新最全
            JSONArray jsonArray = new JSONArray();
            for (Device device : devices) {
                clientKeys.put(device.getTid(), device.getClientKey());
                jsonArray.put(device.getSocketName());
                namesMap.put(device.getSocketName(), device);
            }
            if (isUpdateUserWord) { //是否上传用户热词列表
                namesMap.clear();
                updateUserWordArray(jsonArray);
                SharedPreferencesUtils.saveBoolean(this, Constant.userName + "isUpdateUserWord", false);
            }
        }
        showUI((devices == null || devices.size() == 0) ? false : true);
        super.onResume();
    }

    private void initView() {
        initTitleBar();
        llNoNetWork = (LinearLayout) findViewById(R.id.ll_no_network);
        btnAddDevice = (Button) findViewById(R.id.btn_add);
        llNothing = (LinearLayout) findViewById(R.id.ll_nothing);
        listView = (PullableListView) findViewById(R.id.list_view);
        llScanning = (LinearLayout) findViewById(R.id.ll_scanning);
        llLogout = (LinearLayout) findViewById(R.id.ll_logout);
        llVideo = (LinearLayout) findViewById(R.id.ll_video);
        llSetting = (LinearLayout) findViewById(R.id.ll_setting);
        llStore = (LinearLayout) findViewById(R.id.ll_store);
        llHead = (LinearLayout) findViewById(R.id.ll_head);
        ivHeadPhoto = (CircleImageView) findViewById(R.id.civ_head_photo);
        tvNickName = (TextView) findViewById(R.id.tv_nick_name);
        llRefresh = (PullToRefreshLayout) findViewById(R.id.refresh_view);
//        llLoading = (LinearLayout) findViewById(R.id.ll_loading);
        btnAddDevice.setOnClickListener(onClickListener);
        llScanning.setOnClickListener(onClickListener);
        llLogout.setOnClickListener(onClickListener);
        llVideo.setOnClickListener(onClickListener);
        llSetting.setOnClickListener(onClickListener);
        llStore.setOnClickListener(onClickListener);

        llRefresh.cancelLoadMore();
        llRefresh.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
//                if (devices != null){
//                    for (Device device : devices){
//                        device.setConnetedMode(0);
//                        try {
//                            DBUtil.getInstance(MainActivity.this).saveOrUpdate(device);
//                        } catch (DbException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
                if (Constant.KEY_A2S == null){
                    //重新刷新的时候，如果之前没有key，则重新尝试从服务器验证用户信息进行获取key，成功后就可以使用互联网通信
                    reCheckUserInfo();
                }else {
                    initDevice(true);
                }
            }

            @Override
            public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
                pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
            }

        });
        initUserData();
        View headView = View.inflate(this,R.layout.listview_head,null);
        headView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVoiceDialog();
            }
        });
        listView.addHeaderView(headView);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_add:
//                    addSocket();
                    startActivity(new Intent(MainActivity.this, AddDeviceActivity.class));
                    break;
                case R.id.ll_scanning:
                    startActivity(new Intent(MainActivity.this, CaptureActivity.class));
                    break;
                case R.id.ll_logout:
                    logout();
                    break;
                case R.id.ll_video:
                    startActivity(new Intent(MainActivity.this, LivePlayerActivity.class));
                    break;
                case R.id.ll_setting:
                    startActivity(new Intent(MainActivity.this, SettingActivity.class));
                    break;
                case R.id.ll_store:
                    startActivity(new Intent(MainActivity.this, WebActivity.class));
                    break;
            }
        }
    };

    /**
     * 初始化用户数据
     */
    private void initUserData() {
        BitmapUtils bitmapUtils = new BitmapUtils(this);
        String photoUrl = SharedPreferencesUtils.getString(this, "photoUrl", "");
        String nickname = SharedPreferencesUtils.getString(this, "nickname", "未知");
        tvNickName.setText(nickname);
        Log.i(TAG, "photoUrl==" + photoUrl);
        if (!photoUrl.equals("")) {
            bitmapUtils.display(new ImageView(this), photoUrl, new BitmapLoadCallBack<ImageView>() {
                @Override
                public void onLoadCompleted(ImageView imageView, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
                    ivHeadPhoto.setImageBitmap(bitmap);
                }

                @Override
                public void onLoadFailed(ImageView imageView, String s, Drawable drawable) {

                }
            });
        }
    }

    //初始化titlebar
    private void initTitleBar() {
        titleBar = (TitleBar) findViewById(R.id.title_bar);
        titleBar.setRightVisible(TitleBar.BUTTON);
        titleBar.showPersonButton();
//        titleBar.setBackgroundColor(getResources().getColor(R.color.white));
//        titleBar.setTitleColor(getResources().getColor(R.color.text_color1));
//        titleBar.setRightButtonImage(R.mipmap.ic_add_device_new);
        titleBar.setRightButtonImage(R.mipmap.add_black);
        titleBar.setRightClickListener(new TitleBar.RightClickListener() {
            @Override
            public void onClick() {
                addSocket();
            }
        });
        titleBar.setLeftClickListener(new TitleBar.LeftClickListener() {
            @Override
            public void onClick() {
                menu.toggle();
            }
        });
    }

    private void showUI(boolean isSocket) {
        if (isSocket) {
            if (devices != null) {
                Log.i(TAG, "sockets===========" + devices.toString());
                llRefresh.setVisibility(View.VISIBLE);
                llNothing.setVisibility(View.GONE);
                if (socketAdapter == null) {
                    socketAdapter = new DeviceAdapter(MainActivity.this, devices);
                } else {
                    socketAdapter.refresh(devices);
                }
                listView.setAdapter(socketAdapter);
            }
        } else {
            llRefresh.setVisibility(View.GONE);
            llNothing.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (udpClientScan != null) {
            udpClientScan = null;
        }
    }

    /**
     * 退出登陆
     */
    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.is_logout));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String username = SharedPreferencesUtils.getString(MainActivity.this, "username", "");
                long did = SharedPreferencesUtils.getLong(MainActivity.this, username + "did", 0);
                String dsig = SharedPreferencesUtils.getString(MainActivity.this, username + "dsig", "");
                SharedPreferencesUtils.remove(MainActivity.this, "username");
                SharedPreferencesUtils.remove(MainActivity.this, "uid");
                SharedPreferencesUtils.remove(MainActivity.this, "uidSig");
                SharedPreferencesUtils.remove(MainActivity.this, "nickname");
//                ActivityManager.getAppManager().finishAllActivity();//关闭所有界面
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.putExtra("logout", true);
                startActivity(intent);
                dialog.dismiss();
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    private void init() {
        int localVersion = CheckUpdate.getLocalVersionCode(this);
        Log.i(TAG, "localVersion==" + localVersion);
        boolean isAutoUpdate = SharedPreferencesUtils.getBoolean(this, "isAutoUpdate", true);
        if (isAutoUpdate) {
            isUpdateApp(this, localVersion);
        }
    }

    /**
     * 启动screen状态广播接收器
     */
    private void registerScreenChangeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        screenChangeReceiver = new ScreenChangeReceiver();
        registerReceiver(screenChangeReceiver, filter);
    }
    //动态获取写入SD卡存储权限
    private void requestWriteSDCardPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
        } else {
            // 写入SD卡没有授权，请求回调
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
    }

    /**
     * 判断是否是8.0,8.0需要处理未知应用来源权限问题,否则直接安装
     */
    private boolean checkIsAndroidO() {
        if (Build.VERSION.SDK_INT >= 26) {
            boolean isCan = getPackageManager().canRequestPackageInstalls();
            if (!isCan) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, 10010);
                return false;
            } else {
             return true;
            }
        } else {
           return true;
        }

    }
    /**
     * 检查是否需要更新app
     *
     * @param context
     * @param localVersion app本地的版本号
     */
    private void isUpdateApp(Context context, final int localVersion) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0以上需要动态权限
            // 检查写入外部SD卡权限是否已经获取
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //写入外部SD卡权限没有被授权，请求权限
                requestWriteSDCardPermission();
                return;
            }
        }
        if (checkIsAndroidO()){
            return;
        }
        if (NetWorkUtils.isNetworkConnected(context)) {
            Nodepp.Msg msg = PbDataUtils.setCheckUpdateRequestParam(localVersion);
            Log.i(TAG, "send==" + msg.toString());
            UDPSocketA2S.send(context, msg, new ResponseListener() {
                @Override
                public void onSuccess(Nodepp.Msg msg) {
                    if (msg != null) {
                        Log.i("kk", "receive==" + msg.toString());
                        int verNew = msg.getVerNew();
                        String verInfo = PbDataUtils.byteString2String(msg.getVerInfo());
                        String verUrl = PbDataUtils.byteString2String(msg.getVerUrl());
                        if (verNew > localVersion) {//检测到新版本，提示更新
                            showUpdateDialog(verUrl, verNew, verInfo);
                        }
                        Log.i(TAG, "==verInfo==" + verInfo);
                        Log.i(TAG, "==verUrl==" + verUrl);
                    }
                }

                @Override
                public void onTimeout(Nodepp.Msg msg) {

                }

                @Override
                public void onFaile() {

                }
            });
        } else {
            JDJToast.showMessage(context, context.getString(R.string.no_connect_network));
        }
    }

    /**
     * 提示版本更新dialog
     * @param path
     * @param verNew
     * @param verInfo
     */
    private void showUpdateDialog(final String path, final int verNew, String verInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("发现新版本");
        String info = verInfo.replace(";", "\n");
        builder.setMessage("更新信息：\n" + info);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CheckUpdate.update(MainActivity.this, path, verNew);
                dialog.dismiss();
            }
        });
        builder.setNeutralButton("不再提醒", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferencesUtils.saveBoolean(MainActivity.this, "isAutoUpdate", false);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * 跳转到添加设备的页面
     */
    private void addSocket() {
//        SelectAddDevicePopup selectAddDevicePopup = new SelectAddDevicePopup(MainActivity.this);
//        selectAddDevicePopup.show(btnAddDevice);
        AddDevicePopup addDevicePopup = new AddDevicePopup(MainActivity.this);
        addDevicePopup.show(titleBar);
        addDevicePopup.setOnPopupListener(new AddDevicePopup.popupListener() {
            @Override
            public void commonAdd() {
                startActivity(new Intent(MainActivity.this, AddDeviceActivity.class));
            }

            @Override
            public void scanAdd() {
                startActivity(new Intent(MainActivity.this, CaptureActivity.class));
            }
        });


    }

    /**
     * 根据设备类型进入对应到控制界面
     *
     * @param device
     */
    private void goTo(Device device) {
        Intent intent = null;
        if (device.isOnline()) {
            int deviceType = device.getDeviceType();
            if (deviceType == 1) {
                intent = new Intent(this, SwitchActivity.class);
            } else if (deviceType == 3 || deviceType == 7) {//彩灯
                SharedPreferencesUtils.saveBoolean(this, device.getTid() + "isClickWhite", false);
                intent = new Intent(this, ColorControlActivity.class);
            } else if (deviceType == 2 || deviceType == 4) {////6,4路
                intent = new Intent(this, MultichannelControlActivity.class);
            } else if (deviceType == 6 || deviceType == 8) {//白灯
                intent = new Intent(this, WhiteLightActivity.class);
            } else if (deviceType == 9) {//白灯
                intent = new Intent(this, BathHeaterActivity.class);
            } else {
                JDJToast.showMessage(this, getString(R.string.unknow_device));
                return;
            }
            intent.putExtra("device", device);
            intent.putExtra("isVoice", true);//语音进入
            startActivity(intent);
        } else {
            JDJToast.showMessage(this, "设备不在线，无法操作");
        }
    }

    @Override
    public void onBackPressed() {
        if (ClickUtils.isFastClick(2000)) {
            ActivityManager.getAppManager().AppExit(this);
        } else {
            JDJToast.showMessage(this, getString(R.string.exit_app));
        }
//        moveTaskToBack(true);//不退出，直接运行到后台
    }

    /**
     * 观察者，观察网络变化
     *
     * @param observable
     * @param data
     */
    @Override
    protected void netChange(Observable observable, Object data) {
        super.netChange(observable, data);
        if (Constant.KEY_A2S == null){
            //网络发生变化的时候，如果之前没有key，则重新尝试从服务器验证用户信息进行获取key，成功后就可以使用互联网通信
            reCheckUserInfo();
        }else {
            initDevice(true);
        }
        Log.i("net","main net change ");
    }

    /**
     * 监听极光推送来的自定义消息
     *
     * @param observable
     * @param data
     */
    @Override
    protected void receivedPushData(Observable observable, Object data) {
        super.receivedPushData(observable, data);
        String message = (String) data;
        Log.i("kk", "-------receivedPushData---------" + message);
    }


    @Override
    public void voiceControl(String result) {
        Iterator iter = namesMap.entrySet().iterator();
        Log.i("kk", "-------namesMap---------" + namesMap.toString());
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            if (result.contains(key)) {
                Device device = (Device) entry.getValue();
                cancleVoice();
                goTo(device);
                break;
            }
        }
    }


    /**
     * screen状态广播接收者
     */
    private class ScreenChangeReceiver extends BroadcastReceiver {
        private String action = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) { // 开屏
                Log.i("aaa", "开屏");
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) { // 锁屏
                Log.i("aaa", "锁屏");
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) { // 解锁
                Log.i("aaa", "解锁");
                initDevice(true);
            }
        }
    }


    /**
     * 路由器只有局域网连接没有互联网，网络变化时重新检测用户合法性
     */
    private void reCheckUserInfo() {
        //判断wifi连接情况，没有连接时提示用户先连接
        final String username = SharedPreferencesUtils.getString(this, "username", "0");
        String uidSig = SharedPreferencesUtils.getString(this, "uidSig", "0");
        long uid = Long.parseLong(username);
        Nodepp.Msg msg = PbDataUtils.setCheckUserIdParam(uid, uidSig);
        Log.i(TAG, "msg send:" + msg.toString());
        DTLSSocket.send(MainActivity.this, null, msg, new ResponseListener() {
            @Override
            public void onSuccess(Nodepp.Msg msg) {
                Log.i(TAG, "msg receive:" + msg.toString());
                int result = msg.getHead().getResult();
                if (result == 404) {
                    JDJToast.showMessage(MainActivity.this, getString(R.string.user_info_error));
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                } else if (result == 0) {
                    if (msg.hasKeyClientWan()){
                        ByteString keyClientWan = msg.getKeyClientWan();
                        Log.i("appkey","key:"+keyClientWan.toStringUtf8());
                        Constant.KEY_A2S = keyClientWan.toByteArray();
                        initDevice(true);
                    }
                }
            }

            @Override
            public void onTimeout(Nodepp.Msg msg) {

            }

            @Override
            public void onFaile() {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted

                } else {
                    // Permission Denied
                    JDJToast.showMessage(MainActivity.this,"请前往设置，手动打开存储使用权限");

                }
                break;
            case 10010:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    JDJToast.showMessage(MainActivity.this,"未知应用来源应用安装被限制");
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
