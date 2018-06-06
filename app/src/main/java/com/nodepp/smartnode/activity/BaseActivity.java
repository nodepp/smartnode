package com.nodepp.smartnode.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.nodepp.smartnode.MyApplication;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.business.InitBusiness;
import com.nodepp.smartnode.observe.NetObservable;
import com.nodepp.smartnode.observe.PushObservable;
import com.nodepp.smartnode.service.TLSService;
import com.nodepp.smartnode.service.TlsBusiness;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;


/**
 * Created by yuyue on 2016/8/5.
 */
public class BaseActivity extends AppCompatActivity implements Observer {
    protected TLSService tlsService;
    private NetObservable netWorkObservable;
    private PushObservable pushObservable;
    protected static HashMap<Long,String> clientKeys = new HashMap<Long,String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSystemBar(this);
        ActivityManager.getAppManager().addActivity(this);
        initTls();
        //被观察者
        netWorkObservable = ((MyApplication) getApplication()).getObservable();
        pushObservable = ((MyApplication) getApplication()).getPushObservable();
        //添加网络变化观察者
        netWorkObservable.addObserver(this);
        //添加极光推送自定义消失变化观察者
        pushObservable.addObserver(this);
    }

    private void initTls() {
        //初始化IMSDK
        InitBusiness.start(getApplicationContext(), 1);
        //初始化TLS
        TlsBusiness.init(getApplicationContext());
        tlsService = TLSService.getInstance();
    }

    public static void initSystemBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(activity, true);
        }
        SystemBarTintManager tintManager = new SystemBarTintManager(activity);
        tintManager.setStatusBarTintEnabled(true);
        //设置状态栏的颜色
        Log.i("aaaaa","class===="+activity.getClass());
        if (activity.getClass().equals(SwitchActivity.class)) {
            tintManager.setStatusBarTintResource(R.color.switch_start);//设置成开关渐变色的初始化颜色一致
        } else if (activity.getClass().equals(LoginActivity.class)) {
            tintManager.setStatusBarTintResource(R.color.login_status_bar);//设置成开关渐变色的初始化颜色一致
        } else if (activity.getClass().equals(SplashActivity.class)) {
            tintManager.setStatusBarTintResource(R.color.splash_status_bar);
        } else if (activity.getClass().equals(WhiteLightActivity.class)||activity.getClass().equals(MultichannelControlActivity.class)){
            tintManager.setStatusBarTintResource(R.color.multiple_status_bar);
        }else{
            tintManager.setStatusBarTintResource(R.color.title_bar);//设置成标题栏的颜色
        }

    }

    @TargetApi(19)

    private static void setTranslucentStatus(Activity activity, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        netWorkObservable.deleteObserver(this);//移除观察者
        pushObservable.deleteObserver(this);
        JDJToast.reset();
    }

    protected void netChange(Observable observable, Object data){

    }
    protected void receivedPushData(Observable observable, Object data){

    }
    @Override
    public void update(Observable observable, Object data) {
        if(observable.getClass().equals(NetObservable.class)){ //网络变化观察者
            netChange(observable,data);
        }else {
            receivedPushData(observable,data);//极光自定义消息 观察者
        }
    }

}
