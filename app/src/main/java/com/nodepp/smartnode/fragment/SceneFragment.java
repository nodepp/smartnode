package com.nodepp.smartnode.fragment;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.activity.ColorControlActivity;
import com.nodepp.smartnode.model.ColorsSelect;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.struct.FunctionWithParamNoResult;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.Socket;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.DESUtils;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.view.PopupWindow.AdjustColorPopup;
import com.nodepp.smartnode.view.PopupWindow.EditColorPopup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nodepp.Nodepp;

/**
 * Created by yuyue on 2017/4/6.
 */
public class SceneFragment extends BaseFragment {
    private static final String TAG = SceneFragment.class.getSimpleName();
    private ArrayList<LinearLayout> linearLayouts;
    private DbUtils dbUtils;
    private GradientDrawable mGrad;
    private int mIndex = 0;
    private ArrayList<Long> deviceGroupTids = new ArrayList<Long>();
    private ArrayList<Long> deviceGroupDids = new ArrayList<Long>();
    private boolean isFirstReume = true;
    private LinearLayout llEdt;
    private List<ColorsSelect> colorList;
    private ArrayList<String> deviceGroupIps = new ArrayList<String>();
    private String random;
    private LinearLayout llLightOnOut;
    private LinearLayout llLightOn;
    private LinearLayout llLightOffAll;
    private LinearLayout llSenceTwo;
    private LinearLayout llSenceOne;
    private Device deviceModel;

    @Override
    public View initView() {
        View view = View.inflate(mActivity, R.layout.fragment_scene, null);
        linearLayouts = new ArrayList<LinearLayout>();
        mActivity = (ColorControlActivity) getActivity();
        llLightOn = (LinearLayout) view.findViewById(R.id.ll_light_on);
        llLightOnOut = (LinearLayout) view.findViewById(R.id.ll_light_on_out);
        llLightOffAll = (LinearLayout) view.findViewById(R.id.ll_light_off_all);
        llSenceOne = (LinearLayout) view.findViewById(R.id.ll_sence_one);
        llSenceTwo = (LinearLayout) view.findViewById(R.id.ll_sence_two);
        LinearLayout llLightOff = (LinearLayout) view.findViewById(R.id.ll_light_off);
        LinearLayout llGorgeous = (LinearLayout) view.findViewById(R.id.ll_gorgeous);
        LinearLayout llColorful = (LinearLayout) view.findViewById(R.id.ll_colorful);
        LinearLayout llParty = (LinearLayout) view.findViewById(R.id.ll_party);
        LinearLayout llLambency = (LinearLayout) view.findViewById(R.id.ll_lambency);
        LinearLayout llArder = (LinearLayout) view.findViewById(R.id.ll_arder);
        LinearLayout llColorfulTwo = (LinearLayout) view.findViewById(R.id.ll_colorful_two);
        LinearLayout llNight = (LinearLayout) view.findViewById(R.id.ll_night);
        LinearLayout llRead = (LinearLayout) view.findViewById(R.id.ll_read);
        ImageView ivAdjust = (ImageView) view.findViewById(R.id.iv_adjust);
        ImageView ivEdit = (ImageView) view.findViewById(R.id.iv_edit);
        llEdt = (LinearLayout) view.findViewById(R.id.ll_edit);
        mGrad = (GradientDrawable) llLightOn.getBackground();
        linearLayouts.add(llNight);
        linearLayouts.add(llRead);
        linearLayouts.add(llParty);
        linearLayouts.add(llArder);
        linearLayouts.add(llLambency);
        linearLayouts.add(llColorful);
        linearLayouts.add(llColorfulTwo);
        linearLayouts.add(llGorgeous);
        llNight.setOnClickListener(onClickListene);
        llLightOn.setOnClickListener(onClickListene);
        llLightOff.setOnClickListener(onClickListene);
        llRead.setOnClickListener(onClickListene);
        llParty.setOnClickListener(onClickListene);
        llArder.setOnClickListener(onClickListene);
        llLambency.setOnClickListener(onClickListene);
        llColorful.setOnClickListener(onClickListene);
        llColorfulTwo.setOnClickListener(onClickListene);
        llGorgeous.setOnClickListener(onClickListene);
        ivAdjust.setOnClickListener(onClickListene);
        ivEdit.setOnClickListener(onClickListene);
        return view;
    }

    //初始化变化场景
    private void initScene(int scene) {
        if (mActivity != null) {
            try {
                colorList = dbUtils.findAll(Selector.from(ColorsSelect.class).where("deviceId", "=", deviceModel.getId()).and(WhereBuilder.b("scene", "=", scene)));
            } catch (DbException e) {
                e.printStackTrace();
            }
            //不存在时进行初始化添加
            if (colorList == null || colorList.size() < 1) {
                ColorsSelect colorsSelect = new ColorsSelect();
                if (scene == 4 || scene == 6) {
                    colorsSelect.setColorSize(1);//4和6场景只有1种颜色
                }
                colorsSelect.setDeviceId(deviceModel.getId());
                colorsSelect.setScene(scene);
                try {
                    dbUtils.saveBindingId(colorsSelect);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            } else {
//                ColorsSelect colorsSelect  = colorList.get(0);

            }
        }
    }

    @Override
    public void initData() {
        dbUtils = DBUtil.getInstance(mActivity);
        Bundle bundle = getArguments();
        deviceModel = (Device) bundle.getSerializable("device");
        random = bundle.getString("random");
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
        for (int i = 4; i < 8; i++) {
            initScene(i);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void showEditPopupWindow(View view) {
        if (mActivity != null) {
            EditColorPopup editColorPopup = new EditColorPopup(mActivity, deviceModel.getDid(), deviceModel.getTid(), mIndex, deviceModel.getId(), deviceModel.getConnetedMode(), deviceModel.getIp(), deviceGroupDids, deviceGroupTids, deviceGroupIps, random);
            editColorPopup.show(view);
        }
    }

    private void showAdjustPopupWindow(View view) {
        if (mActivity != null) {
            AdjustColorPopup adjustColorPopup = new AdjustColorPopup(mActivity, deviceModel.getDid(), deviceModel.getTid(), mIndex, deviceModel.getId(), deviceModel.getConnetedMode(), deviceModel.getIp(), deviceGroupDids, deviceGroupTids, deviceGroupIps, random);
            adjustColorPopup.show(view);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.i(TAG, "场景=hidden=" + hidden);
        super.onHiddenChanged(hidden);
    }

    private void changSelect(int index) {
        for (int i = 0; i < 8; i++) {
            if (i == index) {
                linearLayouts.get(i).setAlpha(1.0f);
            } else {
                linearLayouts.get(i).setAlpha(0.4f);
            }
        }
        switch (index) {
            case 0://夜晚 8 255 247 0
                control(8, 255, 247, 0, 1, 0);
                mIndex = 0;
                break;
            case 1://阅读 60 0 255 196
                control(60, 0, 255, 196, 1, 1);
                mIndex = 1;
                break;
            case 2://聚会 0 255 119 0
                control(0, 255, 119, 0, 1, 2);
                mIndex = 2;
                break;
            case 3://休闲 15 9 0 255
                control(15, 9, 0, 255, 1, 3);
                mIndex = 3;
                break;
            case 4:
                mIndex = 4;
                break;
            case 5:
                mIndex = 5;
                break;
            case 6:
                mIndex = 6;
                break;
            case 7:
                mIndex = 7;
                break;

        }
    }

    View.OnClickListener onClickListene = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ll_night://夜晚 8 255 247 0
                    changSelect(0);
                    llEdt.setVisibility(View.INVISIBLE);
                    break;
                case R.id.ll_read://阅读 60 0 255 196
                    changSelect(1);
                    llEdt.setVisibility(View.INVISIBLE);
                    break;
                case R.id.ll_party://聚会 0 255 119 0
                    changSelect(2);
                    llEdt.setVisibility(View.INVISIBLE);
                    break;
                case R.id.ll_arder://休闲 15 9 0 255
                    changSelect(3);
                    llEdt.setVisibility(View.INVISIBLE);
                    break;
                case R.id.ll_lambency://柔光： 绿色，深--->浅（重复）
                    changSelect(4);
                    llEdt.setVisibility(View.VISIBLE);
                    changeScenFromDB(4);
                    break;
                case R.id.ll_colorful://缤纷：红绿蓝交替变化 1s变一下
                    changSelect(5);
                    llEdt.setVisibility(View.VISIBLE);
                    changeScenFromDB(5);
                    break;
                case R.id.ll_colorful_two://炫彩 红色亮1s暗1s
                    changSelect(6);
                    llEdt.setVisibility(View.VISIBLE);
                    changeScenFromDB(6);
                    break;
                case R.id.ll_gorgeous://斑斓 红绿蓝白四种颜色0.5s交替变一次
                    changSelect(7);
                    changeScenFromDB(7);
                    llEdt.setVisibility(View.VISIBLE);
                    break;
                case R.id.iv_adjust:
                    showAdjustPopupWindow(llEdt);
                    break;
                case R.id.iv_edit:
                    showEditPopupWindow(llEdt);
                    break;
                case R.id.ll_light_off:
                    controlLightState(1);
                    break;
                case R.id.ll_light_on:
                    controlLightState(0);
                    break;

            }
        }
    };

    private void changeScenFromDB(int scene) {
        try {
            colorList = dbUtils.findAll(Selector.from(ColorsSelect.class).where("deviceId", "=", deviceModel.getId()).and(WhereBuilder.b("scene", "=", scene)));
            Log.i("ff", "colorList----" + colorList.toString());
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (colorList != null) {
            if (colorList.size() > 0) {
                //已经存在,取出
                ColorsSelect colorsSelect = colorList.get(0);
                mGrad.setColor(Color.rgb(colorsSelect.getColorOneR(), colorsSelect.getColorOneG(), colorsSelect.getColorOneB()));
                controlColors(deviceModel.getIp(), deviceModel.getDid(), deviceModel.getTid(), colorsSelect);
            }
        }
    }

    public void setConnectMode(int mode) {
        Log.i(TAG, "screen-mode===" + mode);
        if (deviceModel != null){
            deviceModel.setConnetedMode(mode);
        }
    }

    //设置当前的场景菜单按钮
    public void setCurrentSence(int sence) {
        if (llEdt != null) {
            if (sence < 4) {
                llEdt.setVisibility(View.INVISIBLE);
            } else {
                llEdt.setVisibility(View.VISIBLE);
            }
        }
        for (int i = 0; i < 8; i++) {
            LinearLayout linearLayout = linearLayouts.get(i);
            if (linearLayout != null){
                if (i == sence) {
                    linearLayout.setAlpha(1.0f);
                } else {
                    linearLayout.setAlpha(0.4f);
                }
            }

        }
    }

    private void control(int white, int red, int green, int blue, int operate, int sence) {
        Nodepp.Rgb.Builder color = Nodepp.Rgb.newBuilder();
        color.setW(white);
        color.setR(red);
        color.setG(green);
        color.setB(blue);
        if (deviceModel.getTid() != 0) {
            controlLight(deviceModel.getDid(), deviceModel.getTid(), deviceModel.getIp(), color, operate, sence);
        } else {
            if (deviceModel.getConnetedMode() == 0) {
                for (long did : deviceGroupDids) {
                    controlLight(did, 1, null, color, operate, sence);
                }
            } else {
                for (int i = 0; i < deviceGroupTids.size(); i++) {
                    long tid = deviceGroupTids.get(i);
                    String ip = deviceGroupIps.get(i < deviceGroupIps.size() ? i : 0);
                    controlLight(0, tid, ip, color, operate, sence);
                }
            }
        }

    }

    /**
     * 控制彩灯颜色的方法
     *
     * @param
     */
    private void controlLight(long did, long tid, String ip, Nodepp.Rgb.Builder color, int operate, int sence) {
        if (mActivity != null) {
            mActivity.setLastControlTimeStamp(System.currentTimeMillis());
            if (NetWorkUtils.isNetworkConnected(mActivity)) {
                String s = SharedPreferencesUtils.getString(mActivity, "username", "0");
                String uidSig = SharedPreferencesUtils.getString(mActivity, "uidSig", "0");
                long uid = Long.parseLong(s);
                final Nodepp.Msg msg = PbDataUtils.setControlColorLightParam(uid, did, tid, uidSig, color, operate, sence, 255, 0);
                Socket.send(mActivity, deviceModel.getConnetedMode(), ip, msg, random, new ResponseListener() {
                    @Override
                    public void onSuccess(Nodepp.Msg msg) {
                        int result = msg.getHead().getResult();
                        if (result == 404) {
                            mActivity.showNoDevice();
                        } else if (result == 0) {
                            if (mActivity.isBigSeqMessage(msg)){
                                mActivity.hideNoDevice();
                                Log.i(TAG, "msg===" + msg.toString());
                                if (msg.getColorsCount() < 1) {
                                    return;
                                }
                                showLightState(msg.getState());
                                Nodepp.Rgb colors = msg.getColors(0);
                                int a = colors.getW();
                                int r = colors.getR();
                                int g = colors.getG();
                                int b = colors.getB();
                                if (isAdded()){
                                    mActivity.isBigSeqMessage(msg);
                                }
                                mGrad.setColor(Color.argb(255, r, g, b));
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
            } else {
                JDJToast.showMessage(mActivity, "网络没有连接，请稍后重试");
            }
        }
    }

    public void controlLightState(int operate) {
        if (operate == 1) {
            changSelect(mIndex);
            if (mIndex < 4) {
                llEdt.setVisibility(View.INVISIBLE);
            } else {
                llEdt.setVisibility(View.VISIBLE);
                changeScenFromDB(mIndex);
            }
        } else {
            control(0, 0, 0, 0, 0, mIndex);
        }

    }

    //根据传入的状态显示界面
    public void showLightState(int state) {
        if (state == 0) {  //关灯状态
            llLightOn.setVisibility(View.GONE);
            llLightOnOut.setVisibility(View.GONE);
            llLightOffAll.setVisibility(View.VISIBLE);
            mFunctions.invokeFunction(HIDE_MENU);
            llEdt.setVisibility(View.INVISIBLE);
            llSenceOne.setVisibility(View.INVISIBLE);
            llSenceTwo.setVisibility(View.INVISIBLE);
        } else {
            if (mIndex < 4) {
                llEdt.setVisibility(View.INVISIBLE);
            } else {
                llEdt.setVisibility(View.VISIBLE);
            }
            llSenceOne.setVisibility(View.VISIBLE);
            llSenceTwo.setVisibility(View.VISIBLE);
            llLightOn.setVisibility(View.VISIBLE);
            llLightOnOut.setVisibility(View.VISIBLE);
            llLightOffAll.setVisibility(View.GONE);
            mFunctions.invokeFunction(SHOW_MENU);
        }
    }

    public void voiceControl(String msg) {
        Log.i(TAG, msg);
        if (msg.contains("夜晚")) {
            changSelect(0);
            llEdt.setVisibility(View.INVISIBLE);
        } else if (msg.contains("阅读")) {
            changSelect(1);
            llEdt.setVisibility(View.INVISIBLE);
        } else if (msg.contains("聚会")) {
            changSelect(2);
            llEdt.setVisibility(View.INVISIBLE);
        } else if (msg.contains("休闲")) {
            changSelect(3);
            llEdt.setVisibility(View.INVISIBLE);
        } else if (msg.contains("柔光")) {
            changSelect(4);
            llEdt.setVisibility(View.VISIBLE);
            changeScenFromDB(4);
        } else if (msg.contains("缤纷")) {
            changSelect(5);
            llEdt.setVisibility(View.VISIBLE);
            changeScenFromDB(5);
        } else if (msg.contains("炫彩")) {
            changSelect(6);
            llEdt.setVisibility(View.VISIBLE);
            changeScenFromDB(6);
        } else if (msg.contains("斑斓")) {
            changSelect(7);
            changeScenFromDB(7);
            llEdt.setVisibility(View.VISIBLE);
        } else {

        }
    }

    private void controlColors(String Ip, long socketId, long socketTid, ColorsSelect colorsSelect) {
        if (socketTid != 0) {
            controlColorsChange(Ip, socketId, socketTid, colorsSelect);
        } else {
            if (deviceModel.getConnetedMode() == 0) {
                for (long did : deviceGroupDids) {
                    controlColorsChange(null, did, 0, colorsSelect);
                }
            } else {
                for (int i = 0; i < deviceGroupTids.size(); i++) {
                    long tid = deviceGroupTids.get(i);
                    String ip = deviceGroupIps.get(i < deviceGroupIps.size() ? i : 0);
                    controlColorsChange(ip, 0, tid, colorsSelect);
                }
            }
        }

    }

    private void controlColorsChange(String ip, long did, long tid, ColorsSelect colorsSelect) {
        if (isAdded() && mActivity != null) {
            mActivity.setLastControlTimeStamp(System.currentTimeMillis());
            if (NetWorkUtils.isNetworkConnected(mActivity)) {
                HashMap<Integer, ArrayList> map = new HashMap<Integer, ArrayList>();
                map.put(0, new ArrayList());
                map.size();
                String s = SharedPreferencesUtils.getString(mActivity, "uid", "0");
                String uidSig = SharedPreferencesUtils.getString(mActivity, "uidSig", "0");
                s = DESUtils.decodeValue(s);
                if (s != null) {
                    long uid = Long.parseLong(s);
                    final Nodepp.Msg msg = PbDataUtils.setControlColorLightParam(uid, did, tid, uidSig, colorsSelect);
                    Socket.send(mActivity, deviceModel.getConnetedMode(), ip, msg, random, new ResponseListener() {
                        @Override
                        public void onSuccess(Nodepp.Msg msg) {
                            int result = msg.getHead().getResult();
                            if (result == 404) {
                                JDJToast.showMessage(mActivity, mActivity.getString(R.string.device_is_not_online));
                            } else if (result == 0) {
                                 showLightState(msg.getState());
                                  if (isAdded()){
                                      mActivity.isBigSeqMessage(msg);
                                  }
                                if (msg.getColorsCount() < 1) {
                                    return;
                                }
                                Nodepp.Rgb colors = msg.getColors(0);
                                int a = colors.getW();
                                int r = colors.getR();
                                int g = colors.getG();
                                int b = colors.getB();
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
            } else {
                JDJToast.showMessage(mActivity, "网络没有连接，请稍后重试");
            }
        }
    }
}
