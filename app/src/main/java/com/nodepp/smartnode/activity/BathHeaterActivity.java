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

import com.google.protobuf.ByteString;
import com.google.zxing.common.StringUtils;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.helper.Util;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.Socket;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import nodepp.Nodepp;

import static com.nodepp.smartnode.utils.PbDataUtils.parserPB;

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
    byte send_data1 = 0x00;
    byte send_data2 = 0x00;
    public int flagss;
    private int lightstate;
    private String tempreature_str,warmtime_str;
    private int key1,key2,key3,key4,key5,key6,key7,key8;
    private byte one_keybyte,two_keybyte;
    private byte [] receiveData;
    private int receiveDatalen;


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
            initclick(7,1,0);
        }else if(result.contains("关闭浴霸")) {
            initclick(7, 1, 1);
        }
    }


    private void initclick(int flagss, int sever,int lstate) {
        if (flagss == 1 && sever == 1) {
            if (lstate== 0 && ic_light.isSelected()) {
                send_data1 = 0x04;
                sendData(send_data1,send_data2);
                ic_light.setBackgroundResource(R.mipmap.ic_lighting_nor);
                txt_light.setTextColor(getResources().getColor(R.color.bathNoSelected));
                ic_light.setSelected(false);
            } else {
                send_data1 = 0x08;
                sendData(send_data1,send_data2);
                ic_light.setBackgroundResource(R.mipmap.ic_lighting_pre);
                txt_light.setTextColor(getResources().getColor(R.color.bathSelected));
                ic_light.setSelected(true);
            }
        } else if(sever== 0){
            JDJToast.showMessage(BathHeaterActivity.this,"控制失败");
        }
        if (flagss == 2 && sever == 1) {
            if (lstate== 0 && ic_wind.isSelected()) {
                send_data1 = 0x01;
                sendData(send_data1,send_data2);
                ic_wind.setBackgroundResource(R.mipmap.ic_wind_nor);
                txt_wind.setTextColor(getResources().getColor(R.color.bathNoSelected));
                ic_wind.setSelected(false);
            } else {
                send_data1 = 0x02;
                sendData(send_data1,send_data2);
                ic_wind.setBackgroundResource(R.mipmap.ic_wind_pre);
                txt_wind.setTextColor(getResources().getColor(R.color.bathSelected));
                ic_wind.setSelected(true);
            }
        } else if(sever==0){
            JDJToast.showMessage(BathHeaterActivity.this,"控制失败");
        }
        if (flagss == 3 && sever == 1) {
            if (lstate==0 && ic_pure.isSelected()) {
                send_data1 = 0x40;
                sendData(send_data1,send_data2);
                ic_pure.setBackgroundResource(R.mipmap.ic_pure_nor);
                txt_pure.setTextColor(getResources().getColor(R.color.bathNoSelected));
                ic_pure.setSelected(false);
            } else {
                send_data1 = (byte) 0x80;
                sendData(send_data1,send_data2);
                ic_pure.setBackgroundResource(R.mipmap.ic_pure_pre);
                txt_pure.setTextColor(getResources().getColor(R.color.bathSelected));
                ic_pure.setSelected(true);
            }
        } else if(sever==0){
            JDJToast.showMessage(BathHeaterActivity.this,"控制失败");
        }
        if (flagss == 4 && sever == 1) {
            if (lstate==0 && ic_warm.isSelected()) {
                send_data2 = 0x10;
                sendData(send_data1,send_data2);
                ic_warm.setBackgroundResource(R.mipmap.ic_warm_nor);
                txt_warm.setTextColor(getResources().getColor(R.color.bathNoSelected));
                ic_warm.setSelected(false);
            } else {
                send_data2 = 0x20;
                sendData(send_data1,send_data2);
                ic_warm.setBackgroundResource(R.mipmap.ic_warm_pre);
                txt_warm.setTextColor(getResources().getColor(R.color.bathSelected));
                ic_warm.setSelected(true);
            }
        } else if(sever==0){
            JDJToast.showMessage(BathHeaterActivity.this,"控制失败");
        }
        if (flagss == 5 && sever == 1) {
            if (lstate==0 && ic_fan.isSelected()) {
                send_data2 = 0x04;
                sendData(send_data1,send_data2);
                ic_fan.setBackgroundResource(R.mipmap.ic_fan_nor);
                txt_fan.setTextColor(getResources().getColor(R.color.bathNoSelected));
                ic_fan.setSelected(false);
            } else {
                send_data2 = 0x08;
                sendData(send_data1,send_data2);
                ic_fan.setBackgroundResource(R.mipmap.ic_fan_pre);
                txt_fan.setTextColor(getResources().getColor(R.color.bathSelected));
                ic_fan.setSelected(true);
            }
        } else if(sever==0){
            JDJToast.showMessage(BathHeaterActivity.this,"控制失败");
        }
        if (flagss == 6 && sever == 1) {
            if (lstate==0 && ic_dry.isSelected()) {
                send_data2 = 0x01;
                sendData(send_data1,send_data2);
                ic_dry.setBackgroundResource(R.mipmap.ic_dry_nor);
                txt_dry.setTextColor(getResources().getColor(R.color.bathNoSelected));
                ic_dry.setSelected(false);
            } else {
                send_data2 = 0x02;
                sendData(send_data1,send_data2);
                ic_dry.setSelected(false);
                ic_dry.setBackgroundResource(R.mipmap.ic_dry_pre);
                txt_dry.setTextColor(getResources().getColor(R.color.bathSelected));
                ic_dry.setSelected(true);
            }
        } else if(sever==0){
            JDJToast.showMessage(BathHeaterActivity.this,"控制失败");
        }
        if (flagss == 7 && sever == 1) {
            if (lstate==0 && bath_shower.isSelected()) {
                send_data1 = 0x40;
                sendData(send_data1,send_data2);
                bath_shower.setBackgroundResource(R.mipmap.close_bath);

            } else {
                send_data1 = (byte) 0x80;
                sendData(send_data1,send_data2);
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
        timer.schedule(myTask, 20000, 20000);  //定时器开始，每隔20s执行一次
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
        byte[] data = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeByte(0xcc);
            dos.writeByte(0x00);
            dos.writeByte(0xdd);
            data = bos.toByteArray();
            bos.close();
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception==" + e.toString());
            data = null;
        }
        Nodepp.Msg msg = PbDataUtils.querybathroom(uid, deviceModel.getDid(), deviceModel.getTid(),Constant.usig,data);
        Log.i(TAG,"发送的数据长度"+data.length);

        Socket.send(this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
            @Override
            public void onSuccess(Nodepp.Msg msg) {
                int result = msg.getHead().getResult();

                if (result == 0){
                    byte receiveByte [] = msg.getUserData().toByteArray();
                    Utils.bytesToHexString(receiveByte);
                    String receive_data = Utils.bytesToHexString(receiveByte);
                    Log.i(TAG,"接收的转化值"+receive_data);
                    //循环字符串数组
                    for(int i = 0;i<receiveByte.length;i++) {
                        Log.i(TAG,"看看值多少"+receiveByte[i]);
                        if (receiveByte.length == 7) {
//                            if (receiveByte[0]==(0xcc) && receiveByte[6] == 0xdd) {
                                one_keybyte = receiveByte[2];
                                two_keybyte = receiveByte[3];
                                tempreature_str = new String(String.valueOf(receiveByte[4]));
                                warmtime_str = new String(String.valueOf(receiveByte[5]));
                                String onebits = Utils.byteToBitString(one_keybyte);
                                String twobits = Utils.byteToBitString(two_keybyte);
                            Log.i(TAG,"接收的1"+one_keybyte);
                            Log.i(TAG,"接收的2"+two_keybyte);
                            Log.i(TAG,"接收的1dsada"+onebits);
                            Log.i(TAG,"接收的2dsada"+twobits);
                                //预留1100 0000
                                if (onebits.substring(0, 2).equals("10")) {
                                    key1 = 1;
                                } else if (onebits.substring(0, 2) == "01") {
                                    key1 = 2;
                                }
                                //自动沐浴状态1为开，2为关
                                if (onebits.substring(2, 4).equals("10")) {
                                    key2 = 1;
                                } else if (onebits.substring(2, 4) == "01") {
                                    key2 = 2;
                                }
                                //照明状态1为开，2为关
                                if (onebits.substring(4,6).equals("10")) {
                                    key3 = 1;
                                } else if (onebits.substring(4,6) == "01") {
                                    key3 = 2;
                                }
                                //新风状态1为开，2为关
                                if (onebits.substring(6, 8).equals("10")) {
                                    key4 = 1;
                                } else if (onebits.substring(6, 8) == "01") {
                                    key4 = 2;
                                }
                                //净化状态1为开，2为关
                                if (twobits.substring(0, 2).equals("10")) {
                                    key5 = 1;
                                } else if (onebits.substring(0, 2) == "01") {
                                    key5 = 2;
                                }
                                //风暖状态1为开，2为关
                                if (onebits.substring(2,4).equals("10")) {
                                    key6 = 1;
                                } else if (onebits.substring(2,4).equals("01")) {
                                    key6 = 2;
                                }
                                //灯暖状态1为开，2为关
                                if (onebits.substring(4, 6).equals("10")) {
                                    key7 = 1;
                                } else if (onebits.substring(4, 6).equals("01")) {
                                    key7 = 2;
                                }
                              //干燥状态1为开，2为关
                                if (onebits.substring(6,8).equals("10")) {
                                    key8 = 1;
                                } else if (onebits.substring(6,8).equals("01")) {
                                    key8 = 2;
                                }
//                            }
                        }
                        //服务器返回
                        isServer = 1;
                        ChangeBathState(tempreature_str, warmtime_str,key1,key2,key3,key4,key5,key6,key7,key8);
                    }
                }else if(result == 404){
                    JDJToast.showMessage(BathHeaterActivity.this,"控制失败");
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

    private void ChangeBathState(String tempreature_str, String warmtime_str,int key1,int key2,
                                 int key3,
                                 int key4,int key5,int key6,int key7,int key8) {

        time_bath.setText(warmtime_str+"min");
        if(tempreature_str ==null){
            tp_bath.setText(26+"°C");
        }else{
            tp_bath.setText(tempreature_str+"°C");
        }
        if(warmtime_str==null){
            int sd = 11;
            time_bath.setText(50+"min");
        }else{
            time_bath.setText(warmtime_str+"min");
        }
        if(key2==1){
            ic_light.setBackgroundResource(R.mipmap.ic_lighting_pre);
            txt_light.setTextColor(getResources().getColor(R.color.bathSelected));
            ic_light.setSelected(false);
        }else if(key2==2){
            ic_light.setBackgroundResource(R.mipmap.ic_lighting_nor);
            txt_light.setTextColor(getResources().getColor(R.color.bathNoSelected));
            ic_light.setSelected(true);
        }
        if(key3==1){
            ic_wind.setBackgroundResource(R.mipmap.ic_wind_pre);
            txt_wind.setTextColor(getResources().getColor(R.color.bathSelected));
            ic_wind.setSelected(false);
        }else if(key3==2){
            ic_wind.setBackgroundResource(R.mipmap.ic_wind_nor);
            txt_wind.setTextColor(getResources().getColor(R.color.bathNoSelected));
            ic_wind.setSelected(true);
        }
        if(key4==1){
            ic_pure.setBackgroundResource(R.mipmap.ic_pure_pre);
            txt_pure.setTextColor(getResources().getColor(R.color.bathSelected));
            ic_pure.setSelected(false);
        }else if(key4==2){
            ic_pure.setBackgroundResource(R.mipmap.ic_pure_nor);
            txt_pure.setTextColor(getResources().getColor(R.color.bathNoSelected));
            ic_pure.setSelected(true);
        }
        if(key5==1){
            ic_warm.setBackgroundResource(R.mipmap.ic_warm_pre);
            txt_warm.setTextColor(getResources().getColor(R.color.bathSelected));
            ic_warm.setSelected(false);
        }else if(key5==2){
            ic_warm.setBackgroundResource(R.mipmap.ic_warm_nor);
            txt_warm.setTextColor(getResources().getColor(R.color.bathNoSelected));
            ic_warm.setSelected(true);
        }
        if(key6==1){
            ic_fan.setBackgroundResource(R.mipmap.ic_fan_pre);
            txt_fan.setTextColor(getResources().getColor(R.color.bathSelected));
            ic_fan.setSelected(false);
        }else if(key6==2){
            ic_fan.setBackgroundResource(R.mipmap.ic_fan_nor);
            txt_fan.setTextColor(getResources().getColor(R.color.bathNoSelected));
            ic_fan.setSelected(true);
        }
        if(key7==1){
            ic_dry.setBackgroundResource(R.mipmap.ic_dry_pre);
            txt_dry.setTextColor(getResources().getColor(R.color.bathSelected));
            ic_dry.setSelected(false);
        }else if(key7==2){
            ic_dry.setBackgroundResource(R.mipmap.ic_dry_nor);
            txt_dry.setTextColor(getResources().getColor(R.color.bathNoSelected));
            ic_dry.setSelected(false);
        }
        if(key8==1){
            bath_shower.setBackgroundResource(R.mipmap.close_bath);
        }else if(key8==2){
            bath_shower.setBackgroundResource(R.mipmap.open_bath_shower);
        }

    }

    /**的
     * 7个按键发送控制浴霸
     *
     */
    private void sendData(byte send_data1,byte send_data2){
        long uid = Long.parseLong(Constant.userName);
        byte[] data = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeByte(0xcc);
            dos.writeByte(0x01);
            dos.writeByte(send_data1);
            dos.writeByte(send_data2);
            dos.writeByte(0xdd);
            data = bos.toByteArray();
            bos.close();
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception==" + e.toString());
            data = null;
        }
//        byte[] asa = Utils.HexString2Bytes(send_control);
        Nodepp.Msg msg = PbDataUtils.querybathroom(uid, deviceModel.getDid(), deviceModel.getTid(), Constant.usig, data);
        Socket.send(this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
            @Override
            public void onSuccess(Nodepp.Msg msg) {
                Log.i(TAG,"sendData=result="+msg.toString());
                int result = msg.getHead().getResult();
                Log.i(TAG,"返回的head头包"+result);
                if (result == 0){
                    byte receiveByte [] = msg.getUserData().toByteArray();
                    Utils.bytesToHexString(receiveByte);
                    String receive_data = Utils.bytesToHexString(receiveByte);
                    //服务器返回
                    Log.i(TAG,"接收的控制返回结果"+receive_data);
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
                    if (currentTimeMillis - lastControlTimeStamp > 3000){//距离最后一次控制的时间大于3s才进行状态查询
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
