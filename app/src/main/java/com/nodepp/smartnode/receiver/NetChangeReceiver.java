package com.nodepp.smartnode.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.MyApplication;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.observe.NetObservable;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;

import java.util.List;

/**
 * Created by Administrator on 2017/6/18.
 */
public class NetChangeReceiver extends BroadcastReceiver {

    private List<Device> devices;


    /**
     * 获取连接类型
     *
     * @param type
     * @return
     */
    private String getConnectionType(int type) {
        String connType = "";
        if (type == ConnectivityManager.TYPE_MOBILE) {
            connType = "4G网络数据";
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            connType = "WIFI网络";
        }
        return connType;
    }
    @Override
    public void onReceive(final Context context, Intent intent) {
        NetObservable observable = ((MyApplication) context.getApplicationContext()).getObservable();
        String username = SharedPreferencesUtils.getString(context, "username", "");
        try {
            devices = DBUtil.getInstance(context).findAll(Selector.from(Device.class).where("userName", "=", username).orderBy("id", false));
        } catch (DbException e) {
            e.printStackTrace();
        }


        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {// 监听wifi的打开与关闭，与wifi的连接无关
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            Log.e("TAG", "wifiState:" + wifiState);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    break;
            }
        }


        // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            //获取联网状态的NetworkInfo对象
            NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info != null) {
                //如果当前的网络连接成功并且网络连接可用
                if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                    if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                        if (devices != null) {
                            for (Device device : devices) {
                                device.setConnetedMode(0);
                                try {
                                    DBUtil.getInstance(context).saveOrUpdate(device);
                                } catch (DbException e) {
                                    e.printStackTrace();
                                }
                            }
                            observable.notifyNetChange();
                            Log.e("TAG", getConnectionType(info.getType()) + "连上");
                        }
                    } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                        if (devices != null) {
                            for (Device device : devices) {
                                device.setConnetedMode(0);
                                try {
                                    DBUtil.getInstance(context).saveOrUpdate(device);
                                } catch (DbException e) {
                                    e.printStackTrace();
                                }
                            }
                            observable.notifyNetChange();
                            Log.e("TAG", getConnectionType(info.getType()) + "连上");
                        }
                    }
                } else {
                    if (devices != null){
                        for (Device device : devices){
                            device.setIsOnline(false);
                            device.setConnetedMode(0);
                            try {
                                DBUtil.getInstance(context).saveOrUpdate(device);
//                        DBUtil.getInstance(context).update(device, WhereBuilder.b("tid", "=", device.getTid()).and("userName", "=", username));
                            } catch (DbException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.i("net","-------------3-------------");
                        observable.notifyNetChange();
                    }
                }
            }
        }
    }
}
