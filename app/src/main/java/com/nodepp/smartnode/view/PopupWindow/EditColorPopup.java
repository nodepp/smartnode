package com.nodepp.smartnode.view.PopupWindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
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
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.Socket;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.Utils;
import com.nodepp.smartnode.view.ColorPicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nodepp.Nodepp;

/**
 * Created by yuyue on 2017/5/26.
 */
public class EditColorPopup extends PopupWindow {
    private Context mContext;
    protected final int LIST_PADDING = 10;
    private Rect mRect = new Rect();
    private final int[] mLocation = new int[2];
    private int selectIndex = -1;
    private boolean mIsDirty;
    private int popupGravity = Gravity.BOTTOM;
    private int mScene;//场景 4代表柔光，5代表缤纷，6代表炫彩，7代表斑斓
    private int mDeviceId;//设备id主键
    private int mConnetedMode;//连接模式
    private long did;
    private long tid;
    private String Ip;//ip
    private ColorPicker picker;
    private ArrayList<GradientDrawable> gradientDrawables;
    private ArrayList<CheckBox> checkBoxes;
    private ArrayList<Integer> colors = new ArrayList<Integer>();
    private ColorsSelect colorsSelect;
    private List<ColorsSelect> colorList;
    private LinearLayout llBottom;
    private ArrayList<Long> deviceGroupDids;
    private ArrayList<Long> deviceGroupTids;
    private ArrayList<String> deviceGroupIps;
    private String random;
    private List<Device> devices;

    public EditColorPopup(Context context, long did, long tid, int scene, int deviceId, int connetedMode, String ip, ArrayList<Long> deviceGroupDids, ArrayList<Long> deviceGroupTids, ArrayList<String> deviceGroupIps, String random) {
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
        Log.i("kk", "deviceGroupIps==" + deviceGroupIps.toString());
    }

    @SuppressLint("WrongConstant")
    @SuppressWarnings("deprecation")
    public EditColorPopup(Context context, int width, int height) {
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
                R.layout.popup_edit_color_info, null));
    }

    private void getDBColors() {
        DbUtils dbUtils = DBUtil.getInstance(mContext);
        try {
            colorList = dbUtils.findAll(Selector.from(ColorsSelect.class).where("deviceId", "=", mDeviceId).and(WhereBuilder.b("scene", "=", mScene)));
            Log.i("ff", "colorList1==" + colorList.toString());
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (colorList != null) {
            if (colorList.size() > 0) {
                //已经存在了，进行更新操作
                colorsSelect = colorList.get(0);
                ArrayList<Integer> colors1 = new ArrayList<Integer>();
                colors1.add(colorsSelect.getColorOneR());
                colors1.add(colorsSelect.getColorOneG());
                colors1.add(colorsSelect.getColorOneB());
                ArrayList<Integer> colors2 = new ArrayList<Integer>();
                colors2.add(colorsSelect.getColorTwoR());
                colors2.add(colorsSelect.getColorTwoG());
                colors2.add(colorsSelect.getColorTwoB());
                ArrayList<Integer> colors3 = new ArrayList<Integer>();
                colors3.add(colorsSelect.getColorThreeR());
                colors3.add(colorsSelect.getColorThreeG());
                colors3.add(colorsSelect.getColorThreeB());
                ArrayList<Integer> colors4 = new ArrayList<Integer>();
                colors4.add(colorsSelect.getColorFourR());
                colors4.add(colorsSelect.getColorFourG());
                colors4.add(colorsSelect.getColorFourB());
                ArrayList<Integer> colors5 = new ArrayList<Integer>();
                colors5.add(colorsSelect.getColorFiveR());
                colors5.add(colorsSelect.getColorFiveG());
                colors5.add(colorsSelect.getColorFiveB());
                ArrayList<Integer> colors6 = new ArrayList<Integer>();
                colors6.add(colorsSelect.getColorSixR());
                colors6.add(colorsSelect.getColorSixG());
                colors6.add(colorsSelect.getColorSixB());
                HashMap<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();
                HashMap<Integer, ArrayList<Integer>> colorsMap = new HashMap<Integer, ArrayList<Integer>>();
                map.put(0, colors1);
                map.put(1, colors2);
                map.put(2, colors3);
                map.put(3, colors4);
                map.put(4, colors5);
                map.put(5, colors6);
                if (colorsSelect.getColorSize() == 1) {
                    llBottom.setVisibility(View.GONE);
                    ArrayList<Integer> color = map.get(0);
                    picker.setColor(Color.rgb(color.get(0), color.get(1), color.get(2)));
                    selectIndex = 0;
                    colors.add(0);
                    checkBoxes.get(0).setChecked(true);
                } else {
                    llBottom.setVisibility(View.VISIBLE);
                    for (int i = 0; i < colorsSelect.getColorSize(); i++) {
                        ArrayList<Integer> color = map.get(i);
                        selectIndex = i;
                        colors.add(i);
                        checkBoxes.get(i).setChecked(true);
                        gradientDrawables.get(i).setColor(Color.rgb(color.get(0), color.get(1), color.get(2)));
                    }
                }
            }
        }
    }

    private void initUI() {
        RelativeLayout rlFinish = (RelativeLayout) getContentView().findViewById(R.id.rl_finish);
        ImageView ivDelect = (ImageView) getContentView().findViewById(R.id.iv_delect);
        llBottom = (LinearLayout) getContentView().findViewById(R.id.ll_bottom);
        picker = (ColorPicker) getContentView().findViewById(R.id.picker);
        LinearLayout llColorOne = (LinearLayout) getContentView().findViewById(R.id.ll_one);
        LinearLayout llColorTwo = (LinearLayout) getContentView().findViewById(R.id.ll_two);
        LinearLayout llColorThree = (LinearLayout) getContentView().findViewById(R.id.ll_three);
        LinearLayout llColorFour = (LinearLayout) getContentView().findViewById(R.id.ll_four);
        LinearLayout llColorFive = (LinearLayout) getContentView().findViewById(R.id.ll_five);
        LinearLayout llColorSix = (LinearLayout) getContentView().findViewById(R.id.ll_six);
        GradientDrawable gradOne = (GradientDrawable) llColorOne.getBackground();
        GradientDrawable gradTwo = (GradientDrawable) llColorTwo.getBackground();
        GradientDrawable gradThree = (GradientDrawable) llColorThree.getBackground();
        GradientDrawable gradFour = (GradientDrawable) llColorFour.getBackground();
        GradientDrawable gradFive = (GradientDrawable) llColorFive.getBackground();
        GradientDrawable gradSix = (GradientDrawable) llColorSix.getBackground();
        gradientDrawables = new ArrayList<GradientDrawable>();
        gradientDrawables.add(gradOne);
        gradientDrawables.add(gradTwo);
        gradientDrawables.add(gradThree);
        gradientDrawables.add(gradFour);
        gradientDrawables.add(gradFive);
        gradientDrawables.add(gradSix);
        for (int i = 0; i < 6; i++) {//初始化化白色
            gradientDrawables.get(i).setColor(Color.WHITE);
        }
        CheckBox cbOne = (CheckBox) getContentView().findViewById(R.id.cb_one);
        CheckBox cbTwo = (CheckBox) getContentView().findViewById(R.id.cb_two);
        CheckBox cbThree = (CheckBox) getContentView().findViewById(R.id.cb_three);
        CheckBox cbFour = (CheckBox) getContentView().findViewById(R.id.cb_four);
        CheckBox cbFive = (CheckBox) getContentView().findViewById(R.id.cb_five);
        CheckBox cbSix = (CheckBox) getContentView().findViewById(R.id.cb_six);
        checkBoxes = new ArrayList<CheckBox>();
        checkBoxes.add(cbOne);
        checkBoxes.add(cbTwo);
        checkBoxes.add(cbThree);
        checkBoxes.add(cbFour);
        checkBoxes.add(cbFive);
        checkBoxes.add(cbSix);
        rlFinish.setOnClickListener(onClickListener);
        llColorOne.setOnClickListener(onClickListener);
        llColorTwo.setOnClickListener(onClickListener);
        llColorThree.setOnClickListener(onClickListener);
        llColorFour.setOnClickListener(onClickListener);
        llColorFive.setOnClickListener(onClickListener);
        llColorSix.setOnClickListener(onClickListener);
        ivDelect.setOnClickListener(onClickListener);
        picker.setOnColorChangedListener(onColorChangedListener);
    }

    @SuppressLint("WrongConstant")
    private void setSelected(int index) {
        for (int i = 0; i < 6; i++) {
            if (i == index) {
                gradientDrawables.get(i).setShape(1);
                gradientDrawables.get(i).setStroke(Utils.Dp2Px(mContext, 1), Color.BLACK);
            } else {
                gradientDrawables.get(i).setShape(1);
                gradientDrawables.get(i).setStroke(Utils.Dp2Px(mContext, 1), Color.WHITE);
            }
        }
        if (colors != null) {
            if (index < colors.size()) {
                switch (index) {
                    case 0:
                        picker.setColor(Color.rgb(colorsSelect.getColorOneR(), colorsSelect.getColorOneG(), colorsSelect.getColorOneB()));
                        break;
                    case 1:
                        picker.setColor(Color.rgb(colorsSelect.getColorTwoR(), colorsSelect.getColorTwoG(), colorsSelect.getColorTwoB()));
                        break;
                    case 2:
                        picker.setColor(Color.rgb(colorsSelect.getColorThreeR(), colorsSelect.getColorThreeG(), colorsSelect.getColorThreeB()));
                        break;
                    case 3:
                        picker.setColor(Color.rgb(colorsSelect.getColorFourR(), colorsSelect.getColorFourG(), colorsSelect.getColorFourB()));
                        break;
                    case 4:
                        picker.setColor(Color.rgb(colorsSelect.getColorFiveR(), colorsSelect.getColorFiveG(), colorsSelect.getColorFiveB()));
                        break;
                    case 5:
                        picker.setColor(Color.rgb(colorsSelect.getColorSixR(), colorsSelect.getColorSixG(), colorsSelect.getColorSixB()));
                        break;
                }
            }
        }

    }

    private void setChecked(int index) {
        if (index > colors.size()) {
            JDJToast.showMessage(mContext, "请先设置第" + (colors.size() + 1) + "个颜色");
            return;
        }
        selectIndex = index;
        if (!checkBoxes.get(index).isChecked()) {
            //本来是没选中的，才加入arrylist
            colors.add(index);
        }
        checkBoxes.get(index).setChecked(true);
    }

    private void cancleChecked(int index) {
        checkBoxes.get(index).setChecked(false);
    }

    ColorPicker.OnColorChangedListener onColorChangedListener = new ColorPicker.OnColorChangedListener() {
        @Override
        public void onColorChanged(int color) {
            if (selectIndex != -1) {
                int a = 0xff, r = 0xff, g = 0xff, b = 0xff;
                b &= color;
                g &= color >> 8;
                r &= color >> 16;
                a &= color >> 24;
                gradientDrawables.get(selectIndex).setColor(color);
                switch (selectIndex) {
                    case 0:
                        colorsSelect.setColorOneR(r);
                        colorsSelect.setColorOneG(g);
                        colorsSelect.setColorOneB(b);
                        colorsSelect.setColorOneW(0);
                        break;
                    case 1:
                        colorsSelect.setColorTwoR(r);
                        colorsSelect.setColorTwoG(g);
                        colorsSelect.setColorTwoB(b);
                        colorsSelect.setColorTwoW(0);
                        break;
                    case 2:
                        colorsSelect.setColorThreeR(r);
                        colorsSelect.setColorThreeG(g);
                        colorsSelect.setColorThreeB(b);
                        colorsSelect.setColorThreeW(0);
                        break;
                    case 3:
                        colorsSelect.setColorFourR(r);
                        colorsSelect.setColorFourG(g);
                        colorsSelect.setColorFourB(b);
                        colorsSelect.setColorFourW(0);
                        break;
                    case 4:
                        colorsSelect.setColorFiveR(r);
                        colorsSelect.setColorFiveG(g);
                        colorsSelect.setColorFiveB(b);
                        colorsSelect.setColorFiveW(0);
                        break;
                    case 5:
                        colorsSelect.setColorSixR(r);
                        colorsSelect.setColorSixG(g);
                        colorsSelect.setColorSixB(b);
                        colorsSelect.setColorSixW(0);
                        break;
                }
                colorsSelect.setColorSize(colors.size());
                controlColors(Ip, did, tid, colorsSelect);
            }
        }
    };
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rl_finish:
                    JDJToast.showMessage(mContext, "设置成功");
                    saveColors();
                    dismiss();
                    break;
                case R.id.ll_one:
                    setChecked(0);
                    setSelected(0);
                    break;
                case R.id.ll_two:
                    setChecked(1);
                    setSelected(1);
                    break;
                case R.id.ll_three:
                    setChecked(2);
                    setSelected(2);
                    break;
                case R.id.ll_four:
                    setChecked(3);
                    setSelected(3);
                    break;
                case R.id.ll_five:
                    setChecked(4);
                    setSelected(4);
                    break;
                case R.id.ll_six:
                    setChecked(5);
                    setSelected(5);
                    break;
                case R.id.iv_delect:
                    delectColor();
                    break;
            }
        }
    };

    private void saveColors() {
        List<ColorsSelect> colorList = null;
        DbUtils dbUtils = DBUtil.getInstance(mContext);
        colorsSelect.setScene(mScene);
        colorsSelect.setDeviceId(mDeviceId);
        colorsSelect.setColorSize(colors.size());//记录多少种颜色
        try {
            colorList = dbUtils.findAll(Selector.from(ColorsSelect.class).where("deviceId", "=", mDeviceId).and(WhereBuilder.b("scene", "=", mScene)));
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (colorList != null) {
            if (colorList.size() > 0) {
                //已经存在了，进行更新操作
                try {
                    Log.i("kk", "场景编辑更新");
                    dbUtils.update(colorsSelect, WhereBuilder.b("deviceId", "=", mDeviceId).and("scene", "=", mScene));
                } catch (DbException e) {
                    e.printStackTrace();
                }
            } else {
                //不存在，直接保存
                try {
                    Log.i("kk", "场景编辑保存");
                    dbUtils.saveBindingId(colorsSelect);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void delectColor() {
        int size = colors.size();
        if (colors.size() < 3) {
            return;
        }
        if (size > 0) {
            colors.remove(size - 1);
            cancleChecked(size - 1);
            gradientDrawables.get(size - 1).setColor(Color.WHITE);
            selectIndex = size - 2;
        }
        colorsSelect.setColorSize(colors.size());
        controlColors(Ip, did, tid, colorsSelect);
    }

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
                    Log.i("kf", "ip1===" + ip);
                    controlColorsChange(ip, 0, tid, colorsSelect);
                }
            }
        }
    }

    private void controlColorsChange(String ip, long did, long tid, ColorsSelect colorsSelect) {
        long uid = Long.parseLong(Constant.userName);
//            (long uid, long did, long tid, String uidSig, ColorsSelect colorsSelect)
        final Nodepp.Msg msg = PbDataUtils.setControlColorLightParam(uid, did, tid, Constant.usig, colorsSelect);
        Log.i("kk", "send==" + msg.toString());
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


