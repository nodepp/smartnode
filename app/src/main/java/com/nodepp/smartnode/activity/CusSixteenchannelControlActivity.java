package com.nodepp.smartnode.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.adapter.SixteenRecyleAdapter;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.Socket;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.hugeterry.coordinatortablayout.CoordinatorTabLayout;
import nodepp.Nodepp;

/**
 * 多路控制界面
 */
public class CusSixteenchannelControlActivity extends BaseVoiceActivity {
    private static final String TAG = CusSixteenchannelControlActivity.class.getSimpleName();
    private Device deviceModel;
    public static int f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16;
    public static int b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12, b13, b14, b15, b16;

    public static int[] arrayF = {f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16};
    public static int[] arrayB = {b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12, b13, b14, b15, b16};

    private Toolbar mToolbar;
    private SixteenRecyleAdapter adapter;

    private List<String> mDatas;
    private MyTasks myTask;

    byte send_data1 = 0x00;
    byte send_data2 = 0x00;
    byte send_data3 = 0x00;
    byte send_data4 = 0x00;
    private Timer timer;

    private byte one_keybyte, two_keybyte, three_keybyte, four_keybyte;
    //列表控件
    private RecyclerView recyclerView;

    private long lastControlTimeStamp = 0;
    private int SendCount =0;
    private Boolean isReceiveSucceed = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sixteen_switch);
        deviceModel = (Device) getIntent().getSerializableExtra("device");
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<String> list = new ArrayList<String>();
        for (int i = 1; i < 17; i++) {
            list.add(i + "");
        }
        adapter = new SixteenRecyleAdapter(this, list);
        recyclerView.setAdapter(adapter);

        mToolbar.setTitle(R.string.net_timeout);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        queryall();
        adapter.notifyDataSetChanged();
    }

    //创建菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent i = new Intent(CusSixteenchannelControlActivity.this, MoreSettingActivity.class);
                i.putExtra("device", deviceModel);
                startActivity(i);
        }
        JDJToast.showMessage(CusSixteenchannelControlActivity.this, "点击设置");
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void voiceControl(String result) {

    }

    //刷新后发送查询指令
    public void queryDatas(int i) {
        Log.e("查询这个传递过来的值", "111" + i);
    }

    //打开的时候的控制指令

    //查询所有
    public void queryall() {
        long uid = Long.parseLong(Constant.userName);
        byte[] data = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeByte(0x5A);
            dos.writeByte(0xA5);
            dos.writeByte(0x00);
            dos.writeByte(0x55);
            dos.writeByte(0xAA);
            data = bos.toByteArray();
            bos.close();
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
            data = null;
        }
        Nodepp.Msg msg = PbDataUtils.querybathroom(uid, deviceModel.getDid(), deviceModel.getTid(), Constant.usig, data);

        Socket.send(this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
            @Override
            public void onSuccess(Nodepp.Msg msg) {
                int result = msg.getHead().getResult();
                byte receiveByte[] = msg.getUserData().toByteArray();
                if (result == 0 && receiveByte.length == 25) {
                    android.util.Log.e(TAG, "result: " + result);
                    for (int i = 0; i < receiveByte.length; i++) {
                        if (receiveByte.length == 25) {
                            one_keybyte = receiveByte[3];
                            two_keybyte = receiveByte[4];
                            three_keybyte = receiveByte[5];
                            four_keybyte = receiveByte[6];
                            //电池数据解析转码加入arrayB数组
                            for (int r = 0; r < arrayB.length; r++) {
                                byte Recbyte[] = {receiveByte[7 + r]};
                                arrayB[r] = Integer.parseInt(Utils.bytesToHexString(Recbyte), 16);
                            }
                            final String onebits = Utils.byteToBitString(one_keybyte);
                            final String twobits = Utils.byteToBitString(two_keybyte);
                            final String threebits = Utils.byteToBitString(three_keybyte);
                            final String fourbits = Utils.byteToBitString(four_keybyte);

                            //通道一
                            if (onebits.substring(0, 2).equals("10")) {
                                arrayF[0] = 1;
                            } else if (onebits.substring(0, 2).equals("01")) {
                                arrayF[0] = 2;
                            } else if (onebits.substring(0, 2).equals("11")) {
                                arrayF[0] = 3;
                            }
                            //通道二
                            if (onebits.substring(2, 4).equals("10")) {
                                arrayF[1] = 1;
                            } else if (onebits.substring(2, 4).equals("01")) {
                                arrayF[1] = 2;
                            } else if (onebits.substring(2, 4).equals("11")) {
                                arrayF[1] = 3;
                            }
                            //通道三
                            if (onebits.substring(4, 6).equals("10")) {
                                arrayF[2] = 1;
                            } else if (onebits.substring(4, 6).equals("01")) {
                                arrayF[2] = 2;
                            } else if (onebits.substring(4, 6).equals("11")) {
                                arrayF[2] = 3;
                            }
                            //通道四
                            if (onebits.substring(6, 8).equals("10")) {
                                arrayF[3] = 1;
                            } else if (onebits.substring(6, 8).equals("01")) {
                                arrayF[3] = 2;
                            } else if (onebits.substring(6, 8).equals("11")) {
                                arrayF[3] = 3;
                            }
                            //通道五
                            if (twobits.substring(0, 2).equals("10")) {
                                arrayF[4] = 1;
                            } else if (twobits.substring(0, 2).equals("01")) {
                                arrayF[4] = 2;
                            } else if (twobits.substring(0, 2).equals("11")) {
                                arrayF[4] = 3;
                            }
                            //通道六
                            if (twobits.substring(2, 4).equals("10")) {
                                arrayF[5] = 1;
                            } else if (twobits.substring(2, 4).equals("01")) {
                                arrayF[5] = 2;
                            } else if (twobits.substring(2, 4).equals("11")) {
                                arrayF[5] = 3;
                            }
                            //通道七
                            if (twobits.substring(4, 6).equals("10")) {
                                arrayF[6] = 1;
                            } else if (twobits.substring(4, 6).equals("01")) {
                                arrayF[6] = 2;
                            } else if (twobits.substring(4, 6).equals("11")) {
                                arrayF[6] = 3;
                            }
                            //通道八
                            if (twobits.substring(6, 8).equals("10")) {
                                arrayF[7] = 1;
                            } else if (twobits.substring(6, 8).equals("01")) {
                                arrayF[7] = 2;
                            } else if (twobits.substring(6, 8).equals("11")) {
                                arrayF[7] = 3;
                            }
                            //通道九
                            if (threebits.substring(0, 2).equals("10")) {
                                arrayF[8] = 1;
                            } else if (threebits.substring(0, 2).equals("01")) {
                                arrayF[8] = 2;
                            } else if (threebits.substring(0, 2).equals("11")) {
                                arrayF[8] = 3;
                            }
                            //通道十
                            if (threebits.substring(2, 4).equals("10")) {
                                arrayF[9] = 1;
                            } else if (threebits.substring(2, 4).equals("01")) {
                                arrayF[9] = 2;
                            } else if (threebits.substring(2, 4).equals("11")) {
                                arrayF[9] = 3;
                            }
                            //通道十一
                            if (threebits.substring(4, 6).equals("10")) {
                                arrayF[10] = 1;
                            } else if (threebits.substring(4, 6).equals("01")) {
                                arrayF[10] = 2;
                            } else if (threebits.substring(4, 6).equals("11")) {
                                arrayF[10] = 3;
                            }
                            //通道十二
                            if (threebits.substring(6, 8).equals("10")) {
                                arrayF[11] = 1;
                            } else if (threebits.substring(6, 8).equals("01")) {
                                arrayF[11] = 2;
                            } else if (threebits.substring(6, 8).equals("11")) {
                                arrayF[11] = 3;
                            }//通道13
                            if (fourbits.substring(0, 2).equals("10")) {
                                arrayF[12] = 1;
                            } else if (fourbits.substring(0, 2).equals("01")) {
                                arrayF[12] = 2;
                            } else if (fourbits.substring(0, 2).equals("11")) {
                                arrayF[12] = 3;
                            }
                            //通道十四
                            if (fourbits.substring(2, 4).equals("10")) {
                                arrayF[13] = 1;
                            } else if (fourbits.substring(2, 4).equals("01")) {
                                arrayF[13] = 2;
                            } else if (fourbits.substring(2, 4).equals("11")) {
                                arrayF[13] = 3;
                            }
                            //通道十五
                            if (fourbits.substring(4, 6).equals("10")) {
                                arrayF[14] = 1;
                            } else if (fourbits.substring(4, 6).equals("01")) {
                                arrayF[14] = 2;
                            } else if (fourbits.substring(4, 6).equals("11")) {
                                arrayF[14] = 3;
                            }
                            //通道十六
                            if (fourbits.substring(6, 8).equals("10")) {
                                arrayF[15] = 1;
                            } else if (fourbits.substring(6, 8).equals("01")) {
                                arrayF[15] = 2;
                            } else if (fourbits.substring(6, 8).equals("11")) {
                                arrayF[15] = 3;
                            }
                        }
                        //服务器返回
                    }
                } else if (result == 404) {
                    JDJToast.showMessage(CusSixteenchannelControlActivity.this, "查询服务器返回失败");
                } else if (result == 204) {
                    JDJToast.showMessage(CusSixteenchannelControlActivity.this, "查询请求超时===" + result);
                } else {
                    JDJToast.showMessage(CusSixteenchannelControlActivity.this, "返回值不是查询返回值");
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

    public void contronturnon(int pos) {
        send_data1 = send_data2 = send_data3 = send_data4 = 0x00;
        if (pos == 0) {
            send_data1 = (byte) 0x80;
        }
        if (pos == 1) {
            send_data1 = 0x20;
        }
        if (pos == 2) {
            send_data1 = 0x08;
        }
        if (pos == 3) {
            send_data1 = 0x02;
        }
        if (pos == 4) {
            send_data2 = (byte) 0x80;
        }
        if (pos == 5) {
            send_data2 = 0x20;
        }
        if (pos == 6) {
            send_data2 = 0x08;
        }
        if (pos == 7) {
            send_data2 = 0x02;
        }
        if (pos == 8) {
            send_data3 = (byte) 0x80;
        }
        if (pos == 9) {
            send_data3 = 0x20;
        }
        if (pos == 10) {
            send_data3 = 0x08;
        }
        if (pos == 11) {
            send_data3 = 0x02;
        }
        if (pos == 12) {
            send_data4 = (byte) 0x80;
        }
        if (pos == 13) {
            send_data4 = 0x20;
        }
        if (pos == 14) {
            send_data4 = 0x08;
        }
        if (pos == 15) {
            send_data4 = 0x02;
        }
        sendData(send_data1, send_data2, send_data3, send_data4);
        sendDataControl();
    }

    public void contronturnoff(int pos) {
        send_data1 = send_data2 = send_data3 = send_data4 = 0x00;
        if (pos == 0) {
            send_data1 = (byte) 0x40;
        }
        if (pos == 1) {
            send_data1 = 0x10;
        }
        if (pos == 2) {
            send_data1 = 0x04;
        }
        if (pos == 3) {
            send_data1 = 0x01;
        }
        if (pos == 4) {
            send_data2 = (byte) 0x40;
        }
        if (pos == 5) {
            send_data2 = 0x10;
        }
        if (pos == 6) {
            send_data2 = 0x04;
        }
        if (pos == 7) {
            send_data2 = 0x01;
        }
        if (pos == 8) {
            send_data3 = (byte) 0x40;
        }
        if (pos == 9) {
            send_data3 = 0x10;
        }
        if (pos == 10) {
            send_data3 = 0x04;
        }
        if (pos == 11) {
            send_data3 = 0x01;
        }
        if (pos == 12) {
            send_data4 = (byte) 0x40;
        }
        if (pos == 13) {
            send_data4 = 0x10;
        }
        if (pos == 14) {
            send_data4 = 0x04;
        }
        if (pos == 15) {
            send_data4 = 0x01;
        }
        sendData(send_data1, send_data2, send_data3, send_data4);
        sendDataControl();
    }

    public void contronturnstop(int pos) {
        send_data1 = send_data2 = send_data3 = send_data4 = 0x00;
        if (pos == 0) {
            send_data1 = (byte) 0xc0;
        }
        if (pos == 1) {
            send_data1 = 0x30;
        }
        if (pos == 2) {
            send_data1 = 0x0c;
        }
        if (pos == 3) {
            send_data1 = 0x03;
        }
        if (pos == 4) {
            send_data2 = (byte) 0xc0;
        }
        if (pos == 5) {
            send_data2 = 0x30;
        }
        if (pos == 6) {
            send_data2 = 0x0c;
        }
        if (pos == 7) {
            send_data2 = 0x03;
        }
        if (pos == 8) {
            send_data3 = (byte) 0xc0;
        }
        if (pos == 9) {
            send_data3 = 0x30;
        }
        if (pos == 10) {
            send_data3 = 0x0c;
        }
        if (pos == 11) {
            send_data3 = 0x03;
        }
        if (pos == 12) {
            send_data4 = (byte) 0xc0;
        }
        if (pos == 13) {
            send_data4 = 0x30;
        }
        if (pos == 14) {
            send_data4 = 0x0c;
        }
        if (pos == 15) {
            send_data4 = 0x03;
        }

        sendData(send_data1, send_data2, send_data3, send_data4);
        sendDataControl();
    }

    private void sendDataControl(){
        if (isReceiveSucceed == true){
            android.util.Log.e("ddddd", "1 " );
            JDJToast.showMessage(CusSixteenchannelControlActivity.this, "数据成功返回");
        }else {
            for (int i = 0; i < 3; i++) {
                if (isReceiveSucceed == false || SendCount > 3) {
                    sendData(send_data1, send_data2, send_data3, send_data4);
                    SendCount++;
                    android.util.Log.e("ddddd", "4 " );
                    if (SendCount == 4) {
                        android.util.Log.e("ddddd", "5 " );
                        JDJToast.showMessage(CusSixteenchannelControlActivity.this, "请求超时");
                        SendCount = 0;
                        break;
                    }
                } else if (isReceiveSucceed == true) {
                    android.util.Log.e("ddddd", "2 " );
                    JDJToast.showMessage(CusSixteenchannelControlActivity.this, "数据成功返回");
                    break;
                }
            }
        }
    }

    private void sendData(byte send_data1, byte send_data2, byte send_data3, byte send_data4) {
        long uid = Long.parseLong(Constant.userName);
        byte[] data = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeByte(0x5A);
            dos.writeByte(0xA5);
            dos.writeByte(0x01);
            dos.writeByte(send_data1);
            dos.writeByte(send_data2);
            dos.writeByte(send_data3);
            dos.writeByte(send_data4);
            Log.e("查询字节", "字节一" + send_data1 + "字节二" + send_data2 + "字节三" + send_data3 + "字节四" + send_data4);
            dos.writeByte(0x55);
            dos.writeByte(0xAA);
            data = bos.toByteArray();
            bos.close();
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception==" + e.toString());
            data = null;
        }
        Nodepp.Msg msg = PbDataUtils.querybathroom(uid, deviceModel.getDid(), deviceModel.getTid(), Constant.usig, data);
        Socket.send(this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
            @Override
            public void onSuccess(Nodepp.Msg msg) {
                Log.e("M_tag", "发送成功");
                Log.e("M_tag", "sendData=result=" + msg.toString());
                int result = msg.getHead().getResult();
                Log.e("M_tag", "返回的head头包" + result);
//
                if (result == 0) {
                    byte receiveByte[] = msg.getUserData().toByteArray();
                    Utils.bytesToHexString(receiveByte);
                    String receive_data = Utils.bytesToHexString(receiveByte);
                    for (int i = 0; i < receiveByte.length; i++) {
                        Log.i(TAG, "接收的控制返回结果" + receiveByte[i]);
                    }
                    //服务器返回
                    isReceiveSucceed = true;
                    JDJToast.showMessage(CusSixteenchannelControlActivity.this, "接受成功");
                } else if (result == 404) {
                    JDJToast.showMessage(CusSixteenchannelControlActivity.this, "服务器返回失败");
                } else if (result == 204) {
//                    JDJToast.showMessage(CusSixteenchannelControlActivity.this, "请求超时");
                }
            }
            @Override
            public void onTimeout(Nodepp.Msg msg) {
                Log.i(TAG, "接收的控制返回结果" + 111);
            }

            @Override
            public void onFaile() {
                Log.e(TAG, "数据发送发送失败");
            }
        });
        lastControlTimeStamp = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume—>startTimer启动");
        startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
    }

    private void startTimer() {

        if (timer == null) {
            timer = new Timer();
        }
        if (myTask == null) {
            myTask = new MyTasks();
        }
        timer.schedule(myTask, 1000, 2000);  //定时器从进入页面1秒开始，每隔2s执行一次
    }

    private void stopTimer() {
        Log.e(TAG, "stopTimer");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (myTask != null) {
            myTask.cancel();
            myTask = null;
        }
    }

    class MyTasks extends TimerTask {

        @Override
        public void run() {
            Log.i("run执行", "执行");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    long currentTimeMillis = System.currentTimeMillis();
                    if (lastControlTimeStamp == 0) {
                        android.util.Log.e(TAG, "正常请求刷新");
                        queryall();
                        adapter.notifyDataSetChanged();
                    } else if (lastControlTimeStamp != 0) {
                        if (currentTimeMillis - lastControlTimeStamp >= 3000) {//距离最后一次控制的时间大于3s才进行状态查询
                            android.util.Log.e("run执行", "currentTimeMillis--lastControlTimeStamp " + currentTimeMillis + "--" + lastControlTimeStamp);
                            Log.e("run执行", "3s重启数据请求");
                            queryall();
                            adapter.notifyDataSetChanged();
                            lastControlTimeStamp = 0;
                        }
                    } else {
                        Log.e("run执行", "---------控制不执行-------------");
                    }
                }
            });
        }
    }

}
