package com.nodepp.smartnode;


import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.iflytek.cloud.SpeechUtility;
import com.nodepp.smartnode.observe.NetObservable;
import com.nodepp.smartnode.observe.PushObservable;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.rtmp.ui.TXCloudVideoView;

import cn.jpush.android.api.JPushInterface;


/**
 * Created by yuyue on 2016/11/23.
 */
public class MyApplication extends Application {
    public static String TAG = MyApplication.class.getSimpleName();
    private TXCloudVideoView mPlayerView;
    public static NetObservable observable = new NetObservable();
    public static PushObservable pushObservable = new PushObservable();
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    @Override
    public void onCreate() {
        super.onCreate();
//        File file = new File(Environment.getExternalStorageDirectory(),"app1");
//        Debug.startMethodTracing(file.getAbsolutePath());
        new Thread(){
            @Override
            public void run() {
                // 第三个参数建议在测试阶段建议设置成true，发布时设置为false。
                CrashReport.initCrashReport(getApplicationContext(), String.valueOf(Constant.SDK_APPID), Constant.isDebug);
                SpeechUtility.createUtility(MyApplication.this, "appid=" + getString(R.string.app_id));
//        initSkinLoader();
                //暂时不启用极光推送
                JPushInterface.init(MyApplication.this);
                JPushInterface.clearAllNotifications(MyApplication.this);
                JPushInterface.setDebugMode(Constant.isDebug);
            }
        }.start();
//        Debug.stopMethodTracing();
    }
    public void setVidePlayView(TXCloudVideoView mPlayerView){

        this.mPlayerView = mPlayerView;
    }
    public TXCloudVideoView getVidePlayView(){

        return this.mPlayerView;
    }

    public NetObservable getObservable() {
        if (observable != null) {
            return observable;
        }else {
            return new NetObservable();
        }
    }
    public PushObservable getPushObservable() {
        if (pushObservable != null) {
            return pushObservable;
        }else {
            return new PushObservable();
        }
    }
    //初始化换肤实例
//    private void initSkinLoader() {
//        SkinManager.getInstance().init(this);
//        SkinManager.getInstance().load();
//    }

}
