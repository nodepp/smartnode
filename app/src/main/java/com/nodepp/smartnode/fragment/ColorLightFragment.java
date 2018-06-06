package com.nodepp.smartnode.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.nodepp.smartnode.R;
import com.nodepp.smartnode.activity.ColorControlActivity;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.Socket;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.view.ColorPicker;
import com.nodepp.smartnode.view.OpacityBar;

import java.util.ArrayList;

import nodepp.Nodepp;

/**
 * Created by yuyue on 2017/4/6.
 */
public class ColorLightFragment extends BaseFragment implements ColorPicker.OnColorChangedListener {

    private static final String TAG = ColorLightFragment.class.getSimpleName();
    private OpacityBar suYanBar;
    private OpacityBar brightDarkBar;
    private LinearLayout llLightOn;
    private LinearLayout llLightOff;
    private LinearLayout llLightOffAll;
    private int brightness_r = 128;
    private int brightness_g = 0xff;
    private int brightness_b = 0;
    private ColorPicker picker;
    private boolean isOnline = true;
    private boolean isFirstReume = true;
    private ArrayList<Long> deviceGroupTids = new ArrayList<Long>();
    private ArrayList<Long> deviceGroupDids = new ArrayList<Long>();
    private int addColor = 0;//素颜,即给led灯增加的颜色
    private int lightDark = 255;//亮暗
    private ArrayList<String> deviceGroupIps = new ArrayList<String>();
    private String random;
    private Device deviceModel;

    @Override
    public View initView() {
        View view = View.inflate(mActivity, R.layout.fragment_color_light, null);
        mActivity = (ColorControlActivity) getActivity();
        picker = (ColorPicker) view.findViewById(R.id.picker);
        suYanBar = (OpacityBar) view.findViewById(R.id.bar_su_yan);
        brightDarkBar = (OpacityBar) view.findViewById(R.id.bar_bright_dark);
        llLightOn = (LinearLayout) view.findViewById(R.id.ll_light_on);
        llLightOffAll = (LinearLayout) view.findViewById(R.id.ll_light_off_all);
        llLightOff = (LinearLayout) view.findViewById(R.id.ll_light_off);
        llLightOffAll.setOnClickListener(onClickListener);
        picker.addBritbessBar(brightDarkBar);
        picker.addOpacityBar(suYanBar);
        picker.setOnColorChangedListener(this);
        Bundle bundle = getArguments();
        deviceModel = (Device) bundle.getSerializable("device");
        random = bundle.getString("random");
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
        picker.setOnClickCenterListener(new ColorPicker.OnClickCenterListener() {
            @Override
            public void onClose() {
                controlLightState(0);
            }
        });
        //素艳
        suYanBar.setOnOpacityChangedListener(new OpacityBar.OnOpacityChangedListener() {
            @Override
            public void onOpacityChanged(int opacity) {
                addColor = 255 - opacity;
                control(brightness_r, brightness_g, brightness_b, 1, lightDark, addColor);
            }
        });
        //亮暗
        brightDarkBar.setOnOpacityChangedListener(new OpacityBar.OnOpacityChangedListener() {
            @Override
            public void onOpacityChanged(int opacity) {
                lightDark = opacity;
                control(brightness_r, brightness_g, brightness_b, 1, lightDark, addColor);
            }
        });
        return view;
    }

    //控制开灯和关灯
    public void controlLightState(int operate) {
        if (operate == 1) {
            if (brightness_r == 0 && brightness_g == 0 && brightness_b == 0) {
                brightness_r = 128;
                brightness_g = 0xff;
            }
            control(brightness_r, brightness_g, brightness_b, 1, lightDark, addColor);
        } else if (operate == 0) {
            control(0, 0, 0, 0, lightDark, addColor);
        }
    }

    //根据传入的状态显示界面
    public void showLightState(int state) {
        if (state == 0) {  //关灯状态
            llLightOn.setVisibility(View.GONE);
            llLightOffAll.setVisibility(View.VISIBLE);
            mFunctions.invokeFunction(HIDE_MENU);
        } else {
            llLightOn.setVisibility(View.VISIBLE);
            llLightOffAll.setVisibility(View.GONE);
            mFunctions.invokeFunction(SHOW_MENU);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void initData() {


    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ll_light_off_all://关灯的状态页面下点击开灯
                    controlLightState(1);
                    break;
            }
        }
    };

    public void setColorLightCurrentState(Nodepp.Rgb color, int brightDark, int suYan) {
        //根据查询到值初始化界面
        brightness_r = color.getR();
        brightness_g = color.getG();
        brightness_b = color.getB();
        lightDark = brightDark;
        addColor = suYan;
        if (picker != null) {
            picker.setColor(Color.rgb(color.getR(), color.getG(), color.getB()));
        }
        if (brightDarkBar != null) {
            brightDarkBar.setOpacity(brightDark);
        }
        if (suYanBar != null) {
            suYanBar.setOpacity(255 - suYan);
        }

    }

    private void control(int red, int green, int blue, int operate, int brightDark, int suYan) {
        Nodepp.Rgb.Builder color = Nodepp.Rgb.newBuilder();
        color.setR(red);
        color.setG(green);
        color.setB(blue);
        color.setW(0);
        if (deviceModel.getTid() != 0) {//局域网控制
            controlLight(deviceModel.getDid(), deviceModel.getTid(), deviceModel.getIp(), color, operate, brightDark, suYan);
        } else {
            if (deviceModel.getConnetedMode() == 0) {
                for (long did : deviceGroupDids) {
                    controlLight(did, 1, null, color, operate, brightDark, suYan);
                }
            } else {//群组
                for (int i = 0; i < deviceGroupTids.size(); i++) {
                    long tid = deviceGroupTids.get(i);
                    String ip = deviceGroupIps.get(i < deviceGroupIps.size() ? i : 0);
                    controlLight(0, tid, ip, color, operate, brightDark, suYan);
                }

            }
        }
    }

    public void setConnectMode(int mode) {
        Log.i(TAG, "color-mode===" + mode);
        if (deviceModel != null) {
            deviceModel.setConnetedMode(mode);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.i(TAG, "彩色=hidden=" + hidden);
        super.onHiddenChanged(hidden);
        if (!hidden) {//相当于Activity的onResum
        } else {//相当于onPause

        }
    }

    @Override
    public void voiceControl(String msg) {
        int flag = 0;
        Log.i(TAG, "彩色=msg=" + msg);
        if (msg.contains("红色")) {
            brightness_r = 255;
            brightness_g = 0;
            brightness_b = 0;
            lightDark = 255;
            addColor = 0;
            flag++;
        } else if (msg.contains("绿色")) {
            brightness_r = 0;
            brightness_g = 255;
            brightness_b = 0;
            lightDark = 255;
            addColor = 0;
            flag++;
        } else if (msg.contains("蓝色")) {
            brightness_r = 0;
            brightness_g = 0;
            brightness_b = 255;
            lightDark = 255;
            addColor = 0;
            flag++;
        }
        if (msg.contains("最亮")) {
            flag++;
            lightDark = 255;
        } else if (msg.contains("最暗")) {
            flag++;
            lightDark = 0;
        }
        if (msg.contains("亮一点")) {
            lightDark += 20;
            if (lightDark > 255) {
                lightDark = 255;
                if (mActivity != null) {
                    JDJToast.showMessage(mActivity, "已经是最亮");
                    return;
                }
            }
            flag++;
        } else if (msg.contains("暗一点")) {
            lightDark -= 20;
            if (lightDark < 0) {
                lightDark = 0;
                if (mActivity != null) {
                    JDJToast.showMessage(mActivity, "已经是最暗");
                    return;
                }
            }
            flag++;
        }
        if (msg.contains("最艳")) {
            flag++;
            addColor = 0;
        } else if (msg.contains("最素")) {
            flag++;
            addColor = 255;
        }
        if (msg.contains("艳一点")) {
            addColor -= 20;
            if (addColor < 0) {
                addColor = 0;
                if (mActivity != null) {
                    JDJToast.showMessage(mActivity, "已经是最艳");
                    return;
                }
            }
            flag++;
        } else if (msg.contains("素一点")) {
            addColor += 20;
            if (addColor > 255) {
                addColor = 255;
                if (mActivity != null) {
                    JDJToast.showMessage(mActivity, "已经是最素");
                    return;
                }
            }
            flag++;
        }
        if (flag > 0) {
            picker.setColor(Color.rgb(brightness_r, brightness_g, brightness_b));
            brightDarkBar.setOpacity(lightDark);
            suYanBar.setOpacity(255 - addColor);
            control(brightness_r, brightness_g, brightness_b, 1, lightDark, addColor);
        }
    }

    @Override
    public void onColorChanged(int color) {
        Log.i(TAG, "=======" + Integer.toHexString(color));
        int a = 0xff, r = 0xff, g = 0xff, b = 0xff;
        b &= color;
        g &= color >> 8;
        r &= color >> 16;
        a &= color >> 24;
        brightness_r = r;
        brightness_g = g;
        brightness_b = b;
        control(brightness_r, brightness_g, brightness_b, 1, lightDark, addColor);
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
                String username = SharedPreferencesUtils.getString(mActivity, "username", "0");
                String uidSig = SharedPreferencesUtils.getString(mActivity, "uidSig", "0");
                long uid = Long.parseLong(username);
                final Nodepp.Msg msg = PbDataUtils.setControlColorLightParam(uid, did, tid, uidSig, color, operate, 99, brightDark, suYan);
                Socket.send(mActivity, deviceModel.getConnetedMode(), ip, msg, random, new ResponseListener() {
                    @Override
                    public void onSuccess(Nodepp.Msg msg) {
                        int result = msg.getHead().getResult();
                        if (result == 404) {
                            if (deviceModel.getTid() != 0) {
                                if (!isOnline) {
                                    mFunctions.invokeFunction(SHOW_NO_DEVICE);
                                    isOnline = true;
                                } else {
                                    controlLight(did, tid, ip, color, operate, brightDark, suYan);
                                    isOnline = false;
                                }
                            }
                        } else if (result == 0) {
                            if (mActivity.isBigSeqMessage(msg)) {
                                isOnline = true;
                                mFunctions.invokeFunction(HIDE_NO_DEVICE);
                                Log.i(TAG, "msg===" + msg.toString());
                                showLightState(msg.getState());
                            }
                        }

                    }

                    @Override
                    public void onTimeout(Nodepp.Msg msg) {
                        //控制超时，设置为上一次的状态
                        if (isAdded()){
                            JDJToast.showMessage(mActivity,mActivity.getString(R.string.net_timeout));
                        }

                    }

                    @Override
                    public void onFaile() {

                    }
                });
            } else {
                JDJToast.showMessage(mActivity, "网络没有连接，请稍后重试");
            }
        }
    }
}
