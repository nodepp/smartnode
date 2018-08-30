package com.nodepp.smartnode.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.MyApplication;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.model.MessageEvent;
import com.nodepp.smartnode.observe.NetObservable;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by Administrator on 2017/6/18.
 */
public class NetChangeReceiver extends BroadcastReceiver {

    private List<Device> devices;

    private static final String TAG = "敬宇轩测试数据";

    @Override
    public void onReceive(final Context context, Intent intent) {
        // 这个监听wifi的打开与关闭，与wifi的连接无关
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            Log.e(TAG, "wifiState" + wifiState);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    EventBus.getDefault().post(new MessageEvent("切换到别的网络了"));
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    EventBus.getDefault().post(new MessageEvent("切换到其他网络中"));
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    EventBus.getDefault().post(new MessageEvent("切换到wifi中"));
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    EventBus.getDefault().post(new MessageEvent("切换到wifi了"));
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    EventBus.getDefault().post(new MessageEvent("未知网络，请检查网络"));
                    break;
                default:
                    break;
            }
//
        }
    }

}




