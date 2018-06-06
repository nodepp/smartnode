package com.nodepp.smartnode.view.PopupWindow;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.model.ColorsSelect;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.Socket;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.view.OpacityBar;

import java.util.ArrayList;
import java.util.List;

import nodepp.Nodepp;

/**
 * Created by yuyue on 2017/5/26.
 */
public class AdjustColorPopup extends PopupWindow {
    private Context mContext;
    protected final int LIST_PADDING = 10;
    private Rect mRect = new Rect();
    private final int[] mLocation = new int[2];
    private boolean mIsDirty;
    private int popupGravity = Gravity.BOTTOM;
    private int mScene;//场景 4代表柔光，5代表缤纷，6代表炫彩，7代表斑斓
    private int mDeviceId;//设备id主键
    private int mConnetedMode;//连接模式
    private long did;
    private long tid;
    private String Ip;//ip
    private List<ColorsSelect> colorList;
    private ColorsSelect colorsSelect;
    private DbUtils dbUtils;
    private OpacityBar lightDarkBar;
    private OpacityBar colorBar;
    private OpacityBar speedBar;
    private ArrayList<Long> deviceGroupTids;
    private ArrayList<Long> deviceGroupDids;
    private ArrayList<String> deviceGroupIps;
    private String random;

    public AdjustColorPopup(Context context, long did, long tid, int scene, int deviceId, int connetedMode, String ip, ArrayList<Long> deviceGroupDids, ArrayList<Long> deviceGroupTids, ArrayList<String> deviceGroupIps, String random) {
        this(context, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        this.mScene = scene;
        this.mDeviceId = deviceId;
        this.mConnetedMode = connetedMode;
        this.Ip = ip;
        this.tid = tid;
        this.did = did;
        this.deviceGroupDids = deviceGroupDids;
        this.deviceGroupTids = deviceGroupTids;
        this.deviceGroupIps = deviceGroupIps;
        this.random = random;
        initUI();
        colorsSelect = new ColorsSelect();
        getDBColors();
        Log.i("kk", "scene==" + scene);
    }

    @SuppressWarnings("deprecation")
    public AdjustColorPopup(Context context, int width, int height) {
        this.mContext = context;
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        setAnimationStyle(R.style.popupWindowAnimation_bottom_to_top);
        setWidth(width);
        setHeight(height);
        setBackgroundDrawable(new BitmapDrawable());
        setContentView(LayoutInflater.from(mContext).inflate(
                R.layout.popup_adjust_color_info, null));
    }

    //根据数据库数据进行初始化
    private void getDBColors() {
        dbUtils = DBUtil.getInstance(mContext);
        try {
            colorList = dbUtils.findAll(Selector.from(ColorsSelect.class).where("deviceId", "=", mDeviceId).and(WhereBuilder.b("scene", "=", mScene)));
            Log.i("ff", "adjust=colorlist==" + colorList.toString());
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (colorList != null) {
            if (colorList.size() > 0) {
                //已经存在,取出
                colorsSelect = colorList.get(0);
                lightDarkBar.setOpacity(colorsSelect.getLightDark());
                colorBar.setOpacity(255 - colorsSelect.getSuYan());
                int speed = colorsSelect.getSwitchSpeed();
                colorsSelect.setSwitchSpeed(speed);
                speedBar.setOpacity(speed);
            }
        }
    }

    private void initUI() {
        RelativeLayout rlFinish = (RelativeLayout) getContentView().findViewById(R.id.rl_finish);
        lightDarkBar = (OpacityBar) getContentView().findViewById(R.id.light_dark_bar);
        colorBar = (OpacityBar) getContentView().findViewById(R.id.color_bar);
        speedBar = (OpacityBar) getContentView().findViewById(R.id.speed_bar);
        lightDarkBar.setColor(Color.parseColor("#46B2FF"));
        colorBar.setColor(Color.parseColor("#46B2FF"));
        speedBar.setColor(Color.parseColor("#46B2FF"));
        rlFinish.setOnClickListener(onClickListener);
        lightDarkBar.setOnOpacityChangedListener(new OpacityBar.OnOpacityChangedListener() {
            @Override
            public void onOpacityChanged(int opacity) {
                colorsSelect.setLightDark(opacity);
                controlColors(Ip, did, tid, colorsSelect);
            }
        });
        //素艳
        colorBar.setOnOpacityChangedListener(new OpacityBar.OnOpacityChangedListener() {
            @Override
            public void onOpacityChanged(int opacity) {
                colorsSelect.setSuYan(255 - opacity);
                controlColors(Ip, did, tid, colorsSelect);
            }
        });
        speedBar.setOnOpacityChangedListener(new OpacityBar.OnOpacityChangedListener() {
            @Override
            public void onOpacityChanged(int opacity) {
                colorsSelect.setSwitchSpeed(opacity);
                controlColors(Ip, did, tid, colorsSelect);
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rl_finish:
                    try {
                        dbUtils.update(colorsSelect, WhereBuilder.b("deviceId", "=", mDeviceId).and("scene", "=", mScene));
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                    dismiss();
                    break;
            }
        }
    };

    public void show(View view) {
        view.getLocationOnScreen(mLocation);
        mRect.set(mLocation[0], mLocation[1], mLocation[0] + view.getWidth(),
                mLocation[1] + view.getHeight());
        if (mIsDirty) {
            populateActions();
        }
        showAtLocation(view, popupGravity, 0, 0);
    }

    private void populateActions() {
        mIsDirty = false;

    }


    private void controlColors(String Ip, long socketId, long socketTid, ColorsSelect colorsSelect) {
        if (socketTid != 0) {
            controlColorsChange(Ip, socketId, socketTid, colorsSelect);
        } else {
            if (mConnetedMode == 0) {
                for (long did : deviceGroupDids) {
                    controlColorsChange("192.168.1.2", did, 0, colorsSelect);
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
        long uid = Long.parseLong(Constant.userName);
        final Nodepp.Msg msg = PbDataUtils.setControlColorLightParam(uid, did, tid, Constant.usig, colorsSelect);
        Socket.send(mContext, mConnetedMode, ip, msg, random, new ResponseListener() {
            @Override
            public void onSuccess(Nodepp.Msg msg) {
                int result = msg.getHead().getResult();
                if (result != 0) {
//                            JDJToast.showMessage(getActivity(), getString(R.string.device_is_not_online));
                    return;
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

            @Override
            public void onTimeout(Nodepp.Msg msg) {

            }

            @Override
            public void onFaile() {

            }
        });

    }
}


