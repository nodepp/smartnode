package com.nodepp.smartnode.fragment;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.activity.ColorControlActivity;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.Socket;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.DESUtils;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.view.OpacityBar;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import nodepp.Nodepp;

/**
 * Created by yuyue on 2017/4/6.
 */
public class NormalLightFragment extends BaseFragment {
    private static final String TAG = NormalLightFragment.class.getSimpleName();
    private LinearLayout llLightOn;
    private GradientDrawable mGrad;
    private LinearLayout llLightShow;
    private LinearLayout llLightOff;
    private LinearLayout llLightHide;
    private OpacityBar brightDarkBar;
    private OpacityBar suYanBar;
    private static boolean isSlide = false;//是否滑动
    private int brightness = 0xff;
    private int blue = 0x6f;
    private int suyan = 0xff;
    private boolean isFirstReume = true;
    private boolean isOnline = true;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    break;
            }
        }
    };
    private ArrayList<Long> deviceGroupTids = new ArrayList<Long>();
    private ArrayList<Long> deviceGroupDids = new ArrayList<Long>();
    private ArrayList<String> deviceGroupIps = new ArrayList<String>();
    private LinearLayout llLightDark;
    private LinearLayout llSuYan;
    private String random;
    private Timer timer;
    private MyTask myTask;
    private Device deviceModel;

    @Override
    public View initView() {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.fragment_normal_light, null);
        brightDarkBar = (OpacityBar) view.findViewById(R.id.bar_bright_dark);
        suYanBar = (OpacityBar) view.findViewById(R.id.bar_su_yan);
        llLightOn = (LinearLayout) view.findViewById(R.id.ll_light_on);
        llLightShow = (LinearLayout) view.findViewById(R.id.ll_light_on_all);
        llLightHide = (LinearLayout) view.findViewById(R.id.ll_light_off_all);
        llLightOff = (LinearLayout) view.findViewById(R.id.ll_light_off);
        llLightDark = (LinearLayout) view.findViewById(R.id.ll_light_dark);
        llSuYan = (LinearLayout) view.findViewById(R.id.ll_su_yan);
        mGrad = (GradientDrawable) llLightOn.getBackground();
        llLightOn.setOnClickListener(onClickListener);
        llLightOff.setOnClickListener(onClickListener);
        brightDarkBar.setColor(Color.parseColor("#46B2FF"));
        suYanBar.setColor(Color.parseColor("#46B2FF"));
        brightDarkBar.setOpacity(brightness);
        suYanBar.setOpacity(suyan);
        Bundle bundle = getArguments();
        random = bundle.getString("random");
        deviceModel = (Device) bundle.getSerializable("device");
        String[] tids = deviceModel.getDeviceGroupTids().split(";");
        String[] dids = deviceModel.getDeviceGroupDids().split(";");
        String[] Ips = deviceModel.getDeviceIps().split(";");
        if (tids != null) {
            for (int i = 0; i < tids.length; i++) {
                String tid = tids[i];
                if (!tid.equals("")) {
                    deviceGroupTids.add(Long.parseLong(tid));
                }
            }
        }
        if (dids != null) {
            for (int i = 0; i < dids.length; i++) {
                String did = dids[i];
                if (!did.equals("")) {
                    deviceGroupDids.add(Long.parseLong(did));
                }
            }
        }
        if (Ips != null) {
            for (int i = 0; i < Ips.length; i++) {
                String ip = Ips[i];
                if (!ip.equals("")) {
                    deviceGroupIps.add(ip);
                }
            }
        }
        //调节亮暗
        brightDarkBar.setOnOpacityChangedListener(new OpacityBar.OnOpacityChangedListener() {
            @Override
            public void onOpacityChanged(int opacity) {
                isSlide = true;
                brightness = opacity;
                mGrad.setColor(Color.argb(opacity, 0x46, 0xb2, 0xff));
                control(1, brightness, suyan);

            }
        });
        //调节素和艳
        suYanBar.setOnOpacityChangedListener(new OpacityBar.OnOpacityChangedListener() {
            @Override
            public void onOpacityChanged(int value) {
                isSlide = true;
                suyan = value;
                control(1, brightness, suyan);

            }
        });
        mFunctions.invokeFunction(HIDE_MENU);
        hideBar();
        showLightState(0);
        return view;
    }

    private void showLightState(int state) {
        //state 为1代表开灯状态，0代表关灯状态
        if (state == 1) {
            llLightShow.setVisibility(View.VISIBLE);
            llLightHide.setVisibility(View.GONE);
            mFunctions.invokeFunction(SHOW_MENU);
        } else {
            llLightShow.setVisibility(View.GONE);
            llLightHide.setVisibility(View.VISIBLE);
            mFunctions.invokeFunction(HIDE_MENU);
        }
    }

    public void showBar() {
        if (llLightDark != null){
            llLightDark.setVisibility(View.VISIBLE);
        }
        if (llSuYan != null){
            llSuYan.setVisibility(View.VISIBLE);
        }
    }

    public void hideBar() {
        if (llLightDark != null){
            llLightDark.setVisibility(View.INVISIBLE);
        }
        if (llSuYan != null){
            llSuYan.setVisibility(View.INVISIBLE);
        }
    }

    public void setConnectMode(int mode) {
        Log.i(TAG, "normal-mode===" + mode);
        if (deviceModel != null){
            deviceModel.setConnetedMode(mode);
        }
    }

    private void control(int operate, int brightDark, int suYan) {
        Nodepp.Rgb.Builder color = Nodepp.Rgb.newBuilder();
        color.setW(255);
        color.setR(0);
        color.setG(0);
        color.setB(0);
        if (deviceModel.getTid() != 0) {
            controlLight(deviceModel.getDid(), deviceModel.getTid(), deviceModel.getIp(), color, operate, brightDark, suYan);
        } else {
            if (deviceModel.getConnetedMode() == 0) {
                for (long did : deviceGroupDids) {
                    controlLight(did, 1, null, color, operate, brightDark, suYan);
                }
            } else {
                for (int i = 0; i < deviceGroupTids.size(); i++) {
                    long tid = deviceGroupTids.get(i);
                    String ip = deviceGroupIps.get(i < deviceGroupIps.size() ? i : 0);
                    controlLight(0, tid, ip, color, operate, brightDark, suYan);
                }
            }
        }

    }

    @Override
    public void initData() {

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ll_light_off://关闭的页面状态点击开启
                    controlLightState(1);
                    break;
                case R.id.ll_light_on://开启的页面状态下点击关闭
                    controlLightState(0);
                    isSlide = false;
                    break;
            }
        }
    };

    public void controlLightState(int operate) {
        control(operate, brightness, suyan);
    }

    @Override
    public void onResume() {
        if (isFirstReume) {
            isFirstReume = false;
            InitializationState();
            startTimer();
        }
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {//相当于onResume
            Log.i(TAG, "show");
        } else {//相当于onPause

        }
    }

    private void InitializationState() {
        if (deviceModel != null){
            Log.i(TAG, "查询");
            if (deviceModel.getTid() != 0) {
                queryLightState(deviceModel.getDid(), deviceModel.getTid(), deviceModel.getIp());
                Log.i(TAG, "单个查询");
            } else {
                if (deviceModel.getConnetedMode() == 0) {
                    for (long did : deviceGroupDids) {
                        queryLightState(did, 1, null);
                    }
                } else {
                    for (int i = 0; i < deviceGroupTids.size(); i++) {
                        long tid = deviceGroupTids.get(i);
                        String ip = deviceGroupIps.get(i < deviceGroupIps.size() ? i : 0);
                        queryLightState(0, tid, ip);
                    }
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    /**
     * 查询设备的状态，然后进行设置
     */
    private void queryLightState(final long did, final long tid, final String ip) {
        Log.i(TAG, "白色查询");
        if (mActivity != null) {
            if (NetWorkUtils.isNetworkConnected(mActivity.getApplicationContext())) {
                long uid = Long.parseLong(Constant.userName);
                Nodepp.Msg msg = PbDataUtils.setQueryCorlorLightStateParam(uid, did, tid, Constant.usig);
                Socket.send(mActivity, deviceModel.getConnetedMode(), ip, msg, random, new ResponseListener() {
                    @Override
                    public void onSuccess(Nodepp.Msg msg) {
                        int result = msg.getHead().getResult();
                        Log.i(TAG, "msg=white==receive=" + msg.toString());
                        if (result == 404) {
                            if (deviceModel.getTid() != 0) {
                                if (!isOnline) {
                                    mFunctions.invokeFunction(SHOW_NO_DEVICE);
                                    setDeviceNoOnline(deviceModel);
                                    JDJToast.showMessage(mActivity,"设备不在线了");
                                    isOnline = true;
                                } else {
                                    queryLightState(did, tid, ip);
                                    isOnline = false;
                                }
                            }
                        } else if (result == 0) {
                            if (mActivity.isBigSeqMessage(msg)){
                                changeState(msg);
                            }
                        }
                    }

                    @Override
                    public void onTimeout(Nodepp.Msg msg) {
                        if (isAdded()){
                            if (msg.getHead().getSeq() < mActivity.getCurrentMsg().getHead().getSeq()) {
                                //超时的pb的seq小于存储的pb的seq，不重置界面
                                return;
                            }
                        }
                    }

                    @Override
                    public void onFaile() {

                    }
                });
            }else {

            }
        }
    }
    private void changeState(Nodepp.Msg msg){
        isOnline = true;
        mFunctions.invokeFunction(HIDE_NO_DEVICE);
        if (msg.getColorsCount() < 1) {
            return;
        }
        Nodepp.Rgb color = msg.getColors(0);
        int state;
        if (msg.hasState()){
            state = msg.getState();
        }else {
            state = msg.getOperate();
        }
        showLightState(state);
        if (state == 1) {
            int sence = msg.getPlatform();
            brightness = msg.getBrightDark();
            suyan = msg.getSuYan();
            Log.i("query","brightDark---"+brightness);
            Log.i("query","suYan---"+suyan);
            if (sence == 99) {//白光和彩光
                if (color.getW() != 0) {
                    brightDarkBar.setOpacity(brightness);
                    mGrad.setColor(Color.argb(brightness, 0x46, 0xb2, 0xff));
                    suYanBar.setOpacity(suyan);
                    Log.i("query","白光---");
                }else {
                    Log.i("query","彩光---");
                }
            }
            if (isAdded()) {
                ColorControlActivity activity = (ColorControlActivity) getActivity();
                activity.setBottomMenu(color, sence, brightness, suyan);
                activity.setLightState(1);
            }
        }else if (state == 0){
            if (isAdded()) {
                ColorControlActivity activity = (ColorControlActivity) getActivity();
                activity.setLightState(0);
            }

        }

    }
    private void startTimer() {
        Log.i(TAG, "startTimer");
        if (timer == null) {
            timer = new Timer();
        }
        if (myTask == null) {
            myTask = new MyTask();
        }
        timer.schedule(myTask, 1000, 3000);  //定时器开始，每隔3s执行一次
    }

    private void stopTimer() {
        Log.i(TAG, "stopTimer");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (myTask != null) {
            myTask.cancel();
            myTask = null;
        }
    }

    class MyTask extends TimerTask {
        @Override
        public void run() {
            if (getActivity() != null) {
                ColorControlActivity activity = (ColorControlActivity) getActivity();
                Log.i("jj", "---查询---");
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - activity.getLastControlTimeStamp() > 3000){//距离最后一次控制的时间大于3s才进行状态查询
                    InitializationState();
                    activity.refreshTimeTask();
                }else {
                    Log.i(TAG,"---------控制不执行-------------");
                }
            }

        }
    }

    private void setDeviceNoOnline(Device device) {
        if (mActivity != null) {
            try {
                if (device != null) {
                    device.setIsOnline(false);
                    DBUtil.getInstance(mActivity).update(device, WhereBuilder.b("userName", "=", Constant.userName).and("did", "=", device.getDid()));
                }

            } catch (DbException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 控制设备的方法
     *
     * @param
     */
    private void controlLight(final long did, final long tid, final String ip, final Nodepp.Rgb.Builder color, final int operate, final int brightDark, final int suYan) {
        if (mActivity != null) {
            mActivity.setLastControlTimeStamp(System.currentTimeMillis());
            if (NetWorkUtils.isNetworkConnected(mActivity)) {
                String s = SharedPreferencesUtils.getString(mActivity, "uid", "0");
                String uidSig = SharedPreferencesUtils.getString(mActivity, "uidSig", "0");
                s = DESUtils.decodeValue(s);
                if (s != null) {
                    long uid = Long.parseLong(s);
                    final Nodepp.Msg msg = PbDataUtils.setControlColorLightParam(uid, did, tid, uidSig, color, operate, 99, brightDark, suYan);
                    Log.i(TAG, "msg=white==send=" + msg.toString());
                    Socket.send(mActivity, deviceModel.getConnetedMode(), ip, msg, random, new ResponseListener() {
                        @Override
                        public void onSuccess(Nodepp.Msg msg) {
                            Log.i(TAG, "msg=white==receive=" + msg.toString());
                            int result = msg.getHead().getResult();
                            if (result == 404) {
                                if (deviceModel.getTid() != 0) {
                                    if (!isOnline) {
                                        mFunctions.invokeFunction(SHOW_NO_DEVICE);
                                    } else {
                                        controlLight(did, tid, ip, color, operate, brightDark, suYan);
                                        isOnline = false;
                                    }
                                }
                            }else if (result == 0){
                                if (mActivity.isBigSeqMessage(msg)){
                                    isOnline = true;
                                    mFunctions.invokeFunction(HIDE_MENU);
                                    mFunctions.invokeFunction(HIDE_NO_DEVICE);
                                    Log.i(TAG, "msg===" + msg.toString());
                                    changeState(msg);

                                }
                            }
                        }

                        @Override
                        public void onTimeout(Nodepp.Msg msg) {
                            if (isAdded()){
                                JDJToast.showMessage(mActivity,mActivity.getString(R.string.net_timeout));
                            }

                        }

                        @Override
                        public void onFaile() {

                        }
                    });
                }
            } else {
                JDJToast.showMessage(mActivity, "网络没有连接，请稍后重试");
            }
        }
    }

    @Override
    public void voiceControl(String msg) {
        int flag = 0;
        if (msg.contains("最亮")) {
            flag++;
            brightness = 255;
        } else if (msg.contains("最暗")) {
            flag++;
            brightness = 0;
        }
        if (msg.contains("亮一点")) {
            brightness += 20;
            if (brightness > 255) {
                brightness = 255;
                if (mActivity != null) {
                    JDJToast.showMessage(mActivity, "已经是最亮");
                    return;
                }
            }
            flag++;
        } else if (msg.contains("暗一点")) {
            brightness -= 20;
            if (brightness < 0) {
                brightness = 0;
                if (mActivity != null) {
                    JDJToast.showMessage(mActivity, "已经是最暗");
                    return;
                }
            }
            flag++;
        }
        if (msg.contains("最艳")) {
            flag++;
            suyan = 255;
        } else if (msg.contains("最素")) {
            flag++;
            suyan = 0;
        }
        if (msg.contains("艳一点")) {
            suyan += 20;
            if (suyan > 255) {
                suyan = 255;
                if (mActivity != null) {
                    JDJToast.showMessage(mActivity, "已经是最艳");
                    return;
                }
            }
            flag++;
        } else if (msg.contains("素一点")) {
            suyan -= 20;
            if (suyan < 0) {
                suyan = 0;
                if (mActivity != null) {
                    JDJToast.showMessage(mActivity, "已经是最素");
                    return;
                }
            }
            flag++;
        }
        if (flag > 0) {
            control(1, brightness, suyan);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTimer();
    }
}
