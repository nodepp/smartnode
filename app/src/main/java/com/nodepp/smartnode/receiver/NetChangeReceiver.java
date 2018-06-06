package com.nodepp.smartnode.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i("aaaaa","------------net--------------");

        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {//这个监听wifi的打开与关闭，与wifi的连接无关
            Log.v("my2", "收到WIFI_STATE_CHANGED_ACTION");
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1111);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    Log.v("my2", "收到"+"WIFI_STATE_DISABLED");
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    Log.v("my2", "收到"+"WIFI_STATE_DISABLING");
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.v("my2", "收到"+"WIFI_STATE_ENABLED");
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    Log.v("my2", "收到"+"WIFI_STATE_ENABLING");
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    Log.v("my2", "WIFI_STATE_UNKNOWN");

            }
        }

        NetObservable observable = ((MyApplication) context.getApplicationContext()).getObservable();
        String username = SharedPreferencesUtils.getString(context, "username", "");
        try {
            devices = DBUtil.getInstance(context).findAll(Selector.from(Device.class).where("userName", "=", username).orderBy("id", false));
        } catch (DbException e) {
            e.printStackTrace();
        }
//        int connectedType = NetWorkUtils.getConnectedType(context);//移动网络0 wifi网络1  没网络-1
//        Log.i("qqqq","------------connectedType========="+connectedType);
        if (NetWorkUtils.isNetworkConnected(context)) {   //判断网络是否可用
            Log.i("net","------------change--------------");
            if (devices != null){
                for (Device device : devices){
                    device.setConnetedMode(0);
                    try {
                        DBUtil.getInstance(context).saveOrUpdate(device);
//                        DBUtil.getInstance(context).update(device, WhereBuilder.b("tid", "=", device.getTid()).and("userName", "=", username));
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                }
            }
            Log.i("net","------------2--------------");
            observable.notifyNetChange();
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
//        EspWifiAdminSimple mWifiAdmin = new EspWifiAdminSimple(context);
//        String apSsid = mWifiAdmin.getWifiConnectedSsid();
//        if(apSsid == null){
//            apSsid = "";
//        }
//        if (netType != connectedType || !mSSID.equals(apSsid)){
//            netType = connectedType;
//            mSSID = apSsid;
//
//        }

    }
}
