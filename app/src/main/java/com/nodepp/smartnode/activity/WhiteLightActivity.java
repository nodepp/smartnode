package com.nodepp.smartnode.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.Socket;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.Utils;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import nodepp.Nodepp;

public class WhiteLightActivity extends BaseVoiceActivity implements View.OnClickListener {

    private static String TAG = WhiteLightActivity.class.getSimpleName();
    private GradientDrawable drawable;
    private ImageView ivLight;
    private GradientDrawable mGrad;
    private LinearLayout llLightOff;
    private RelativeLayout rlCenter;
    private LinearLayout llLightDark;
    private Device deviceModel;
    private SeekBar mSeekBar;
    private MyTask myTask;
    private Timer timer;
    private int state;
    private int progress = 0;
    private boolean isVoice;
    private long lastControlTimeStamp = 0;
    private Nodepp.Msg currentMsg = nodepp.Nodepp.Msg.newBuilder().build();//初始化为空
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_light);
        deviceModel = (Device) getIntent().getSerializableExtra("device");
        isVoice = getIntent().getBooleanExtra("isVoice", false);
        if (deviceModel == null) {
            JDJToast.showMessage(this, "数据出错");
            finish();
        } else {
            initView();
        }
    }


    @SuppressLint("WrongViewCast")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initView() {
        ImageView ivBacke = (ImageView) findViewById(R.id.iv_back);
        ImageView ivMore = (ImageView) findViewById(R.id.iv_more);
        Button btnVoice = (Button) findViewById(R.id.btn_voice);
        ivBacke.setOnClickListener(this);
        ivMore.setOnClickListener(this);
        btnVoice.setOnClickListener(this);
        ivLight = (ImageView) findViewById(R.id.iv_light);
        mGrad = (GradientDrawable) findViewById(R.id.rl_light_bg).getBackground();
        llLightOff = (LinearLayout) findViewById(R.id.ll_light_off);
        rlCenter = (RelativeLayout) findViewById(R.id.rl_center);
        llLightDark = (LinearLayout) findViewById(R.id.ll_light_dark);
        LinearLayout llTiming = (LinearLayout) findViewById(R.id.ll_timing);
        drawable = new GradientDrawable();
        drawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        int[] color = new int[]{0xFFFFFFFF, 0xFFFFFFFF, 0x016FC3FF};
        drawable.setColors(color);
        drawable.setShape(GradientDrawable.OVAL);
        mGrad.setColor(Color.argb(85, 179, 201, 254));
        llTiming.setOnClickListener(this);
        llLightOff.setOnClickListener(this);
        rlCenter.setOnClickListener(this);
        mSeekBar = (SeekBar) findViewById(R.id.progress);
        mSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    private void setLightState(boolean isOpen) {
        if (isOpen) {
            llLightOff.setVisibility(View.INVISIBLE);
            rlCenter.setVisibility(View.VISIBLE);
            llLightDark.setVisibility(View.VISIBLE);
        } else {
            llLightOff.setVisibility(View.VISIBLE);
            rlCenter.setVisibility(View.INVISIBLE);
            llLightDark.setVisibility(View.INVISIBLE);
        }
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // 当拖动条的滑块位置发生改变时触发该方法,在这里直接使用参数progress，即当前滑块代表的进度值
            double radius = progress * 0.65;
            drawable.setGradientRadius(Utils.Dp2Px(WhiteLightActivity.this, (float) radius));
            int alpha = (int) (85 + progress * 0.66);
            mGrad.setColor(Color.argb(alpha, 179, 201, 254));
            ivLight.setBackground(drawable);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //开始滑动
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //停止滑动
            progress = seekBar.getProgress();
            Log.i("kk", "progress==" + progress);
            changeBrightDark(progress, 1);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_more:
                Intent intentMore = new Intent(WhiteLightActivity.this, MoreSettingActivity.class);
                intentMore.putExtra("device", deviceModel);
                startActivityForResult(intentMore,1);
                break;
            case R.id.btn_voice://语音
                showVoiceDialog();
                break;
            case R.id.rl_center://点击关灯
                Log.i("kk", "点击关灯");
                changeBrightDark(progress, 0);
                break;
            case R.id.ll_light_off://点击开灯
                Log.i("kk", "点击开灯");
                changeBrightDark(progress, 1);
                break;
            case R.id.ll_timing:
                addTimerTask();
                break;

        }
    }

    private void addTimerTask() {
        Intent intent = new Intent(this, MulitipleTimingActivity.class);
        intent.putExtra("device", deviceModel);
        intent.putExtra("operate", state);
        startActivity(intent);
    }

    private void queryWhiteLightState() {
        long uid = Long.parseLong(Constant.userName);
        Nodepp.Msg msg = PbDataUtils.queryWhiteLightState(uid, deviceModel.getDid(), Constant.usig);
        Socket.send(WhiteLightActivity.this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {

            @Override
            public void onSuccess(Nodepp.Msg msg) {
                int result = msg.getHead().getResult();
                if (result == 404) {
                    JDJToast.showMessage(WhiteLightActivity.this, "设备不在线");
                } else if (result == 0){
                    state = msg.getState();
                    setLightState(state == 0 ? false : true);
                    isBigSeqMessage(msg);
                    progress = msg.getBrightDark();
                    mSeekBar.setProgress(progress);
                }
            }

            @Override
            public void onTimeout(Nodepp.Msg msg) {
                if (msg.getHead().getSeq() < currentMsg.getHead().getSeq()){//收到到包seq比较小
                    return;
                }
                //设置为上一次状态,发出去的包没回来
                if (currentMsg != null){
                    setLightState(currentMsg.getState() == 0 ? false : true);
                    mSeekBar.setProgress(currentMsg.getBrightDark());
                }
                JDJToast.showMessage(WhiteLightActivity.this, getString(R.string.net_timeout));
            }

            @Override
            public void onFaile() {

            }
        });

    }

    //判断当前接收到的seq是不是比上一次的message大
    private boolean isBigSeqMessage(Nodepp.Msg receiveMsg){
        if (currentMsg == null){
            currentMsg = receiveMsg;
            return true;
        }else {
            if (currentMsg.getHead().getSeq() < receiveMsg.getHead().getSeq()){
                currentMsg = receiveMsg;
                return true;
            }else {
                return false;
            }
        }
    }
    private void changeBrightDark(int brightDark, int operate) {
        lastControlTimeStamp = System.currentTimeMillis();
        long uid = Long.parseLong(Constant.userName);
        Nodepp.Msg msg = PbDataUtils.changeWhiteLightBrightDark(uid, deviceModel.getDid(), Constant.usig, brightDark, operate);
        Socket.send(WhiteLightActivity.this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
            @Override
            public void onSuccess(Nodepp.Msg msg) {
                int result = msg.getHead().getResult();
                if (result == 404) {
                    JDJToast.showMessage(WhiteLightActivity.this, "设备不在线");
                } else if (result == 0){
                    if (isBigSeqMessage(msg)){
                        int state = msg.getOperate();//控制的时候operate就是状态
                        setLightState(state == 0 ? false : true);
                    }
                }
            }

            @Override
            public void onTimeout(Nodepp.Msg msg) {
                if (msg.getHead().getSeq() < currentMsg.getHead().getSeq()){
                    return;
                }
            }

            @Override
            public void onFaile() {

            }
        });

    }

    private void startTimer() {
        Log.i(TAG, "startTimer");
        if (timer == null) {
            timer = new Timer();
        }
        if (myTask == null) {
            myTask = new MyTask();
        }
        timer.schedule(myTask, 2000, 20000);  //定时器开始，每隔20s执行一次
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
            Log.i(TAG, "执行");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    long currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis - lastControlTimeStamp > 3000){//距离最后一次控制的时间大于3s才进行状态查询
                        queryWhiteLightState();
                    }else {
                        Log.i(TAG,"---------控制不执行-------------");
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryWhiteLightState();
        startTimer();
        if (isVoice) {
            showVoiceDialog();
            isVoice = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
    }

    //网络变化时，数据更新
    @Override
    protected void netChange(Observable observable, Object data) {
        deviceModel.setConnetedMode(0);//网络变化先切换到互联网模式
    }


    @Override
    public void voiceControl(String result) {
        int operate = -1;
        Log.i("aaa", "result=white=" + result);
        if (result.contains("开灯")) {
            operate = 1;
        } else if (result.contains("关灯")) {
            operate = 0;
        }
        if (result.contains("亮一点")) {
            progress += 20;
            if (progress > 255) {
                progress = 255;
                JDJToast.showMessage(WhiteLightActivity.this, "已经是最亮");
            }
            operate = 1;
        } else if (result.contains("暗一点")) {
            progress -= 20;
            if (progress < 0) {
                progress = 0;
                JDJToast.showMessage(WhiteLightActivity.this, "已经是最暗");
            }
            operate = 1;
        }
        if (result.contains("最亮")) {
            progress = 255;
            operate = 1;
        } else if (result.contains("最暗")) {
            progress = 0;
            operate = 1;
        }

        if (result.contains("定时")) {
            cancleVoice();
            addTimerTask();
        }
        if (operate != -1) {
//            setLightState(operate == 0?false:true);
            changeBrightDark(progress, operate);
            mSeekBar.setProgress(progress);
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Device device = (Device)data.getSerializableExtra("device");
        Log.i("hh","requestCode is "+requestCode);
        Log.i("hh","resultCode is "+resultCode);
        if (device == null){
            Log.i("hh","result device is null");
        }else {
            Log.i("hh","result device is "+device.toString());
        }

        if (requestCode == 1){
            if (resultCode == 2){
                deviceModel = device;
                Log.i("hh","deviceModel is "+deviceModel.toString());
            }
        }
    }
}
