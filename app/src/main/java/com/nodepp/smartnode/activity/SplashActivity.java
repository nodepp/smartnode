package com.nodepp.smartnode.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;

import com.google.protobuf.ByteString;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.dtls.DTLSSocket;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.task.CheckConnectTask;
import com.nodepp.smartnode.task.NetWorkListener;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;

import java.util.List;

import cn.jpush.android.api.JPushInterface;
import nodepp.Nodepp;


public class SplashActivity extends BaseActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private LinearLayout llAll;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initDBData();
        //如果极光推送服务关了，唤醒
        JPushInterface.resumePush(getApplicationContext());
        CheckConnectTask task = new CheckConnectTask(SplashActivity.this);//ping一下网络，看是不是能连接到外网，可能存在连接到的wifi无法访问到外网
        task.setNetWorkListener(new NetWorkListener() {
            @Override
            public void onSuccess(int state) {
                if (state == -1) {
                    //没有网络连接
                    JDJToast.showMessage(SplashActivity.this,getString(R.string.no_connect_network));
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                } else if (state == -2) {
                    //有网络但是连接不到互联网
                    //网络不通畅,先把所有设备设置为不在线的状态，然后局域网能查询到的设置为在线状态
                    String username = SharedPreferencesUtils.getString(SplashActivity.this, "username", "0");
                    String uidSig = SharedPreferencesUtils.getString(SplashActivity.this, "uidSig", "0");
                    if (!username.equals("0")){
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }
                } else if (state == 0) {
                    String username = SharedPreferencesUtils.getString(SplashActivity.this, "username", "0");
                    if (username.equals("0")){
                        //不存在帐户信息
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }else {
                        checkUserId();
                    }
                }
            }

            @Override
            public void onFaile() {

            }
        });
        task.execute();

    }

    private void initDBData() {
        try {
            List<Device> devices = DBUtil.getInstance(this).findAll(Selector.from(Device.class));
            if (devices != null && devices.size() > 0){
                for (Device d:devices){
                    d.setOnline(false);
                    d.setConnetedMode(0);//默认启动app全部状态置为不在线，连接模式置为互联网模式
                }
                DBUtil.getInstance(this).updateAll(devices);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
//        Utils.removeBackground(llAll);
        super.onDestroy();
    }
    /**
     * 检测udi和usig
     */
    private void checkUserId() {
        //判断wifi连接情况，没有连接时提示用户先连接
        String username = SharedPreferencesUtils.getString(this, "username", "0");
        String uidSig = SharedPreferencesUtils.getString(this, "uidSig", "");
        if (username.equals("")){
            username = "0";//防止非数字导致
        }
        JPushInterface.setAlias(this,PbDataUtils.getCurrentSeq(),username);//设置极光推送别名
        long uid = Long.parseLong(username);
        Nodepp.Msg msg = PbDataUtils.setCheckUserIdParam(uid, uidSig);
        Log.i(TAG, "msg send:" + msg.toString());
        DTLSSocket.send(SplashActivity.this, null, msg, new ResponseListener() {
            @Override
            public void onSuccess(Nodepp.Msg msg) {
                Log.i(TAG, "msg receive:" + msg.toString());
                int result = msg.getHead().getResult();
                if (result == 404) {
                    SharedPreferencesUtils.remove(SplashActivity.this, "username");
                    SharedPreferencesUtils.remove(SplashActivity.this, "uid");
                    SharedPreferencesUtils.remove(SplashActivity.this, "uidSig");
                    SharedPreferencesUtils.remove(SplashActivity.this, "nickname");
                    JDJToast.showMessage(SplashActivity.this, getString(R.string.user_info_error));
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                } else if (result == 0) {
                    if (msg.hasKeyClientWan()){
                        ByteString keyClientWan = msg.getKeyClientWan();
                        Log.i("appkey","key:"+keyClientWan.toStringUtf8());
                        Constant.KEY_A2S = keyClientWan.toByteArray();
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }else {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }
                }
            }

            @Override
            public void onTimeout(Nodepp.Msg msg) {

            }

            @Override
            public void onFaile() {
//                    JDJToast.showMessage(MainActivity.this, getString(R.string.access_server_error));
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
//                if (!username.equals("0")){
//                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
//                    finish();
//                }else {
//                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
//                    finish();
//                }
            }
        });
    }

}
