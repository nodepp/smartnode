package com.nodepp.smartnode.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.Socket;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;

import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import nodepp.Nodepp;

/**
 * Created by yuyue on 2018/6/9.
 */

public class BathHeaterActivity extends BaseVoiceActivity implements View.OnClickListener {

    private static String TAG = BathHeaterActivity.class.getSimpleName();
    private ImageView back, voice, ivMore, clock;
    private TextView txt_light, txt_wind, txt_pure, txt_warm, txt_fan, txt_dry, bath_shower, tp_bath, time_bath;
    private Button ic_light, ic_wind, ic_pure, ic_warm, ic_fan, ic_dry;
    private Device deviceModel;
    private MyTasks myTask;
    private Timer timer;
    private long lastControlTimeStamp = 0;
    private boolean isVoice;
    private int isServer;
    String send_data1 = "00";
    String send_data2 = "00";
    public int flagss;
    private int lightstate;
    private String tempreature_str,warmtime_str;
    private byte key1,key2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bath_heater);
        Intent intent = getIntent();
        deviceModel = (Device) intent.getSerializableExtra("device");
        initView();

    }


    @SuppressLint("WrongViewCast")
    private void initView() {
        ic_light = findViewById(R.id.ic_light_nor);
        ic_wind = findViewById(R.id.ic_wind_nor);
        ic_pure = findViewById(R.id.ic_pure_nor);
        ic_warm = findViewById(R.id.ic_warm_nor);
        ic_fan = findViewById(R.id.ic_fan_nor);
        ic_dry = findViewById(R.id.ic_dry_nor);
        back = findViewById(R.id.iv_back);
        clock = findViewById(R.id.ic_clock);
        voice = findViewById(R.id.iv_voice);
        ivMore = findViewById(R.id.iv_more);
        bath_shower = findViewById(R.id.open_bath_shower);
        txt_light = findViewById(R.id.txt_light_nor);
        txt_wind = findViewById(R.id.txt_wind_nor);
        txt_pure = findViewById(R.id.txt_pure_nor);
        txt_warm = findViewById(R.id.txt_warm_nor);
        txt_fan = findViewById(R.id.txt_fan_nor);
        txt_dry = findViewById(R.id.txt_dry_nor);
        tp_bath = findViewById(R.id.bath_txt_tp);
        time_bath = findViewById(R.id.ht_txt_time);
        ic_light.setOnClickListener(this);
        ic_wind.setOnClickListener(this);
        ic_pure.setOnClickListener(this);
        ic_warm.setOnClickListener(this);
        ic_fan.setOnClickListener(this);
        ic_dry.setOnClickListener(this);
        back.setOnClickListener(this);
        voice.setOnClickListener(this);
        ivMore.setOnClickListener(this);
        bath_shower.setOnClickListener(this);

    }


    @SuppressLint("ResourceAsColor")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ic_light_nor:
                    flagss = 1;
                    initclick(flagss, isServer,lightstate);
                break;
            case R.id.ic_wind_nor:
                flagss = 2;
                initclick(flagss, isServer,lightstate);
                break;
            case R.id.ic_pure_nor:
                flagss = 3;
                initclick(flagss, isServer,lightstate);
                break;
            case R.id.ic_warm_nor:
                flagss = 4;
                initclick(flagss, isServer,lightstate);
                break;
            case R.id.ic_fan_nor:
                flagss = 5;
                initclick(flagss, isServer,lightstate);
                break;
            case R.id.ic_dry_nor:
                flagss = 6;
                initclick(flagss, isServer,lightstate);
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_voice:
                showVoiceDialog();
                break;
            case R.id.open_bath_shower:
                flagss = 7;
                initclick(flagss, isServer,lightstate);
                break;
            case R.id.iv_more:
                Intent intentMore = new Intent(BathHeaterActivity.this, MoreSettingActivity.class);
                intentMore.putExtra("device", deviceModel);
                startActivityForResult(intentMore, 1);
                break;
        }

    }

//    private void setButtonAndTextState(Button btn, TextView textView) {
//        if (btn.isSelected()) {
//            btn.setSelected(false);
//            textView.setTextColor(getResources().getColor(R.color.bathNoSelected));
//        } else {
//            btn.setSelected(true);
//            textView.setTextColor(getResources().getColor(R.color.bathSelected));
//          /*  sendData("0");*/
//        }
//    }


    @Override
    public void voiceControl(String result) {
        if(result.contains("打开照明")){
            initclick(1,1,1);
        }else if(result.contains("关闭照明")) {
            initclick(1, 1, 0);
        }
        if(result.contains("打开新风")){
            initclick(2,1,1);
        }else if(result.contains("关闭新风")) {
            initclick(2, 1, 0);
        }
        if(result.contains("打开净化")){
            initclick(3,1,1);
        }else if(result.contains("关闭净化")) {
            initclick(3, 1, 0);
        }
        if(result.contains("打开风暖")){
            initclick(4,1,1);
        }else if(result.contains("关闭风暖")) {
            initclick(4, 1, 0);
        }
        if(result.contains("打开灯暖")){
            initclick(5,1,1);
        }else if(result.contains("关闭灯暖")) {
            initclick(5, 1, 0);
        }
        if(result.contains("打开干燥")){
            initclick(6,1,1);
        }else if(result.contains("关闭干燥")) {
            initclick(6, 1, 0);
        }
        if(result.contains("打开浴霸")){
            initclick(7,1,1);
        }else if(result.contains("关闭浴霸")) {
            initclick(7, 1, 0);
        }
    }


    private void initclick(int flagss, int sever,int lstate) {
        if (flagss == 1 && sever == 1) {
            if (lstate== 0 && ic_light.isSelected()) {
                send_data1 = "04";
                sendData(send_data1);
                ic_light.setSelected(false);
                txt_light.setTextColor(getResources().getColor(R.color.bathNoSelected));
            } else {
                send_data1 = "08";
                sendData(send_data1);
                ic_light.setSelected(true);
                txt_light.setTextColor(getResources().getColor(R.color.bathSelected));

            }
        } else if(sever== 0){
            JDJToast.showMessage(BathHeaterActivity.this,"控制失败");
        }
        if (flagss == 2 && sever == 1) {
            if (lstate== 0 && ic_wind.isSelected()) {
                send_data1 = "01";
                sendData(send_data1);
                ic_wind.setSelected(false);
                ic_wind.setBackgroundResource(R.mipmap.ic_wind_nor);
                txt_wind.setTextColor(getResources().getColor(R.color.bathNoSelected));
            } else {
                send_data1 = "02";
                sendData(send_data1);
                ic_wind.setSelected(true);
                ic_wind.setBackgroundResource(R.mipmap.ic_wind_pre);
                txt_wind.setTextColor(getResources().getColor(R.color.bathSelected));
            }
        } else if(sever==0){
            JDJToast.showMessage(BathHeaterActivity.this,"控制失败");
        }
        if (flagss == 3 && sever == 1) {
            if (lstate==0 && ic_pure.isSelected()) {
                send_data1 = "40";
                sendData(send_data1);
                ic_pure.setSelected(false);
                ic_pure.setBackgroundResource(R.mipmap.ic_pure_nor);
                txt_pure.setTextColor(getResources().getColor(R.color.bathNoSelected));
            } else {
                send_data1 = "80";
                sendData(send_data1);
                ic_pure.setSelected(true);
                ic_pure.setBackgroundResource(R.mipmap.ic_pure_pre);
                txt_pure.setTextColor(getResources().getColor(R.color.bathSelected));
            }
        } else if(sever==0){
            JDJToast.showMessage(BathHeaterActivity.this,"控制失败");
        }
        if (flagss == 4 && sever == 1) {
            if (lstate==0 && ic_warm.isSelected()) {
                send_data2 = "10";
                sendData(send_data2);
                ic_warm.setSelected(false);
                ic_warm.setBackgroundResource(R.mipmap.ic_warm_nor);
                txt_warm.setTextColor(getResources().getColor(R.color.bathNoSelected));
            } else {
                send_data2 = "20";
                sendData(send_data2);
                ic_warm.setSelected(true);
                ic_warm.setBackgroundResource(R.mipmap.ic_warm_pre);
                txt_warm.setTextColor(getResources().getColor(R.color.bathSelected));
            }
        } else if(sever==0){
            JDJToast.showMessage(BathHeaterActivity.this,"控制失败");
        }
        if (flagss == 5 && sever == 1) {
            if (lstate==0 && ic_fan.isSelected()) {
                send_data2 = "04";
                sendData(send_data2);
                ic_fan.setSelected(false);
                ic_fan.setBackgroundResource(R.mipmap.ic_fan_nor);
                txt_fan.setTextColor(getResources().getColor(R.color.bathNoSelected));
            } else {
                send_data2 = "08";
                sendData(send_data2);
                ic_fan.setSelected(true);
                ic_fan.setBackgroundResource(R.mipmap.ic_fan_pre);
                txt_fan.setTextColor(getResources().getColor(R.color.bathSelected));
            }
        } else if(sever==0){
            JDJToast.showMessage(BathHeaterActivity.this,"控制失败");
        }
        if (flagss == 6 && sever == 1) {
            if (lstate==0 && ic_dry.isSelected()) {
                send_data2 = "01";
                sendData(send_data2);
                ic_dry.setSelected(false);
                ic_dry.setBackgroundResource(R.mipmap.ic_dry_nor);
                txt_dry.setTextColor(getResources().getColor(R.color.bathNoSelected));
            } else {
                send_data2 = "02";
                sendData(send_data2);
                ic_dry.setSelected(true);
                ic_dry.setBackgroundResource(R.mipmap.ic_dry_pre);
                txt_dry.setTextColor(getResources().getColor(R.color.bathSelected));
            }
        } else if(sever==0){
            JDJToast.showMessage(BathHeaterActivity.this,"控制失败");
        }
        if (flagss == 7 && sever == 1) {
            if (lstate==0 && bath_shower.isSelected()) {
                send_data1 = "40";
                sendData(send_data1);
                bath_shower.setSelected(false);
                bath_shower.setBackgroundResource(R.mipmap.close_bath);
            } else {
                send_data1 = "80";
                sendData(send_data1);
                bath_shower.setSelected(true);
                bath_shower.setBackgroundResource(R.mipmap.open_bath_shower);
            }
        } else if(sever==0){
            JDJToast.showMessage(BathHeaterActivity.this,"控制失败");
        }

    }


    private void startTimer() {
        Log.i(TAG, "startTimer");
        if (timer == null) {
            timer = new Timer();
        }
        if (myTask == null) {
            myTask = new MyTasks();
        }
        timer.schedule(myTask, 20000, 30000);  //定时器开始，每隔20s执行一次
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


    /**
     * 通过串口查询浴霸并获得返回数据
     *
     */
    private void queryBathHeatherState() {
        long uid = Long.parseLong(Constant.userName);
        Nodepp.Msg msg = PbDataUtils.querybathdatas(uid, deviceModel.getDid(), deviceModel.getTid(), Constant.usig);
        JDJToast.showMessage(BathHeaterActivity.this,"发送查询"+msg.toString());
        Socket.send(this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
            @Override
            public void onSuccess(Nodepp.Msg msg) {
                Log.i(TAG,"sendData=result="+msg.toString());
                int result = msg.getHead().getResult();
                Log.i(TAG,"返回的head头包"+result);
                if (result == 0){
                    //服务器返回
                    isServer = 1;
                    tempreature_str = "19";
                    warmtime_str = "27";
                    key1 = 0x20;
                    key2 = 0x01;
                    ChangeBathState(tempreature_str,warmtime_str,key1,key2);

                }else if(result == 404){
                    JDJToast.showMessage(BathHeaterActivity.this,"返回404");
                    isServer= 0;

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

    private void ChangeBathState(String tempreature_str, String warmtime_str, byte key1, byte key2) {
        tp_bath.setText(tempreature_str+"C");
        time_bath.setText(warmtime_str+"min");
        if(key1==0x20){
            ic_light.setBackgroundResource(R.mipmap.ic_lighting_pre);
            txt_light.setTextColor(getResources().getColor(R.color.bathSelected));
        }
    }

    /**
     * 7个按键发送控制浴霸
     *
     */
    private void sendData(String datas){
        long uid = Long.parseLong(Constant.userName);
        datas ="cc"+"01"+send_data1+send_data2+"dd";
        Log.i(TAG,"datas");
        Nodepp.Msg msg = PbDataUtils.sendUserDataRequestParam(uid, deviceModel.getDid(), deviceModel.getTid(), Constant.usig, datas);
        Socket.send(this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
            @Override
            public void onSuccess(Nodepp.Msg msg) {
                Log.i(TAG,"sendData=result="+msg.toString());
                int result = msg.getHead().getResult();
                Log.i(TAG,"返回的head头包"+result);
                if (result == 0){
                    //服务器返回

                }else if(result == 404){

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

    class MyTasks extends TimerTask {

        @Override
        public void run() {
            Log.i(TAG, "执行");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    long currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis - lastControlTimeStamp > 300){//距离最后一次控制的时间大于3s才进行状态查询
                        queryBathHeatherState();
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
        queryBathHeatherState();
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



}
