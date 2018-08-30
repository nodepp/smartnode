package com.nodepp.smartnode.activity;

import android.content.Intent;
import android.os.Bundle;
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
    private CoordinatorTabLayout mCoordinatorTabLayout;
    private Device deviceModel;
    public static int f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16;
    public static int b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12, b13, b14, b15, b16;
    private Toolbar mToolbar;

    private List<String> mDatas;
    private MyTasks myTask;

    byte send_data1 = 0x00;
    byte send_data2 = 0x00;
    byte send_data3 = 0x00;
    byte send_data4 = 0x00;
    private Timer timer;

    private byte one_keybyte, two_keybyte, three_keybyte, four_keybyte;
    //刷新控件

    //列表控件
    private RecyclerView recyclerView;

    private long lastControlTimeStamp = 0;



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

        recyclerView.setAdapter(new SixteenRecyleAdapter(this, list));


        mToolbar.setTitle(R.string.net_timeout);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
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
            dos.writeByte(0xaa);
            dos.writeByte(0x00);
            dos.writeByte(0xbb);
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
                int result = msg.getHead().getResult();
                if (result == 0) {

                    byte receiveByte[] = msg.getUserData().toByteArray();
                    Utils.bytesToHexString(receiveByte);
                    for (int i = 0; i < receiveByte.length; i++) {
                        if (receiveByte.length == 23) {
                            one_keybyte = receiveByte[2];
                            two_keybyte = receiveByte[3];
                            three_keybyte = receiveByte[4];
                            four_keybyte = receiveByte[5];
                            byte rec1byte[] = {receiveByte[6]};
                            b1 = Integer.parseInt(Utils.bytesToHexString(rec1byte), 16);
                            byte rec2byte[] = {receiveByte[7]};
                            b2 = Integer.parseInt(Utils.bytesToHexString(rec2byte), 16);
                            byte rec3byte[] = {receiveByte[8]};
                            b3 = Integer.parseInt(Utils.bytesToHexString(rec3byte), 16);
                            byte rec4byte[] = {receiveByte[9]};
                            b4 = Integer.parseInt(Utils.bytesToHexString(rec4byte), 16);
                            byte rec5byte[] = {receiveByte[10]};
                            b5 = Integer.parseInt(Utils.bytesToHexString(rec5byte), 16);
                            byte rec6byte[] = {receiveByte[11]};
                            b6 = Integer.parseInt(Utils.bytesToHexString(rec2byte), 16);
                            byte rec7byte[] = {receiveByte[12]};
                            b7 = Integer.parseInt(Utils.bytesToHexString(rec2byte), 16);
                            byte rec8byte[] = {receiveByte[13]};
                            b8 = Integer.parseInt(Utils.bytesToHexString(rec2byte), 16);
                            byte rec9byte[] = {receiveByte[14]};
                            b9 = Integer.parseInt(Utils.bytesToHexString(rec2byte), 16);
                            byte rec10byte[] = {receiveByte[15]};
                            b10 = Integer.parseInt(Utils.bytesToHexString(rec2byte), 16);
                            byte rec11byte[] = {receiveByte[16]};
                            b11 = Integer.parseInt(Utils.bytesToHexString(rec2byte), 16);
                            byte rec12byte[] = {receiveByte[17]};
                            b12 = Integer.parseInt(Utils.bytesToHexString(rec2byte), 16);
                            byte rec13byte[] = {receiveByte[18]};
                            b13 = Integer.parseInt(Utils.bytesToHexString(rec13byte), 16);
                            byte rec14byte[] = {receiveByte[19]};
                            b14 = Integer.parseInt(Utils.bytesToHexString(rec14byte), 16);
                            byte rec15byte[] = {receiveByte[20]};
                            b15 = Integer.parseInt(Utils.bytesToHexString(rec15byte), 16);
                            byte rec16byte[] = {receiveByte[21]};
                            b16 = Integer.parseInt(Utils.bytesToHexString(rec16byte), 16);
                            final String onebits = Utils.byteToBitString(one_keybyte);
                            final String twobits = Utils.byteToBitString(two_keybyte);
                            final String threebits = Utils.byteToBitString(three_keybyte);
                            final String fourbits = Utils.byteToBitString(four_keybyte);

                            //通道一
                            if (onebits.substring(0, 2).equals("10")) {
                                f1 = 1;
                            } else if (onebits.substring(0, 2).equals("01")) {
                                f1 = 2;
                            } else if (onebits.substring(0, 2).equals("11")) {
                                f1 = 3;
                            }
                            //通道二
                            if (onebits.substring(2, 4).equals("10")) {
                                f2 = 1;
                            } else if (onebits.substring(2, 4).equals("01")) {
                                f2 = 2;
                            } else if (onebits.substring(2, 4).equals("11")) {
                                f2 = 3;
                            }
                            //通道三
                            if (onebits.substring(4, 6).equals("10")) {
                                f3 = 1;
                            } else if (onebits.substring(4, 6).equals("01")) {
                                f3 = 2;
                            } else if (onebits.substring(4, 6).equals("11")) {
                                f3 = 3;
                            }
                            //通道四
                            if (onebits.substring(6, 8).equals("10")) {
                                f4 = 1;
                            } else if (onebits.substring(6, 8).equals("01")) {
                                f4 = 2;
                            } else if (onebits.substring(6, 8).equals("11")) {
                                f4 = 3;
                            }
                            //通道五
                            if (twobits.substring(0, 2).equals("10")) {
                                f5 = 1;
                            } else if (twobits.substring(0, 2).equals("01")) {
                                f5 = 2;
                            } else if (twobits.substring(0, 2).equals("11")) {
                                f5 = 3;
                            }
                            //通道六
                            if (twobits.substring(2, 4).equals("10")) {
                                f6 = 1;
                            } else if (twobits.substring(2, 4).equals("01")) {
                                f6 = 2;
                            } else if (twobits.substring(2, 4).equals("11")) {
                                f6 = 3;
                            }
                            //通道七
                            if (twobits.substring(4, 6).equals("10")) {
                                f7 = 1;
                            } else if (twobits.substring(4, 6).equals("01")) {
                                f7 = 2;
                            } else if (twobits.substring(4, 6).equals("11")) {
                                f7 = 3;
                            }
                            //通道八
                            if (twobits.substring(6, 8).equals("10")) {
                                f8 = 1;
                            } else if (twobits.substring(6, 8).equals("01")) {
                                f8 = 2;
                            } else if (twobits.substring(6, 8).equals("11")) {
                                f8 = 3;
                            }//通道九
                            if (threebits.substring(0, 2).equals("10")) {
                                f9 = 1;
                            } else if (threebits.substring(0, 2).equals("01")) {
                                f9 = 2;
                            } else if (threebits.substring(0, 2).equals("11")) {
                                f9 = 3;
                            }
                            //通道二
                            if (threebits.substring(2, 4).equals("10")) {
                                f10 = 1;
                            } else if (threebits.substring(2, 4).equals("01")) {
                                f10 = 2;
                            } else if (threebits.substring(2, 4).equals("11")) {
                                f10 = 3;
                            }
                            //通道三
                            if (threebits.substring(4, 6).equals("10")) {
                                f11 = 1;
                            } else if (threebits.substring(4, 6).equals("01")) {
                                f11 = 2;
                            } else if (threebits.substring(4, 6).equals("11")) {
                                f11 = 3;
                            }
                            //通道四
                            if (threebits.substring(6, 8).equals("10")) {
                                f12 = 1;
                            } else if (threebits.substring(6, 8).equals("01")) {
                                f12 = 2;
                            } else if (threebits.substring(6, 8).equals("11")) {
                                f12 = 3;
                            }//通道13
                            if (fourbits.substring(0, 2).equals("10")) {
                                f13 = 1;
                            } else if (fourbits.substring(0, 2).equals("01")) {
                                f13 = 2;
                            } else if (fourbits.substring(0, 2).equals("11")) {
                                f13 = 3;
                            }
                            //通道二
                            if (fourbits.substring(2, 4).equals("10")) {
                                f14 = 1;
                            } else if (fourbits.substring(2, 4).equals("01")) {
                                f14 = 2;
                            } else if (fourbits.substring(2, 4).equals("11")) {
                                f14 = 3;
                            }
                            //通道三
                            if (fourbits.substring(4, 6).equals("10")) {
                                f15 = 1;
                            } else if (fourbits.substring(4, 6).equals("01")) {
                                f15 = 2;
                            } else if (fourbits.substring(4, 6).equals("11")) {
                                f15 = 3;
                            }
                            //通道四
                            if (fourbits.substring(6, 8).equals("10")) {
                                f16 = 1;
                            } else if (fourbits.substring(6, 8).equals("01")) {
                                f16 = 2;
                            } else if (fourbits.substring(6, 8).equals("11")) {
                                f16 = 3;
                            }
                        }
                        //服务器返回

                    }
                } else if (result == 404) {

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

    }

    public void contronturnoff(int pos) {
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

    }

    public void contronturnstop(int pos) {
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

    }


    private void sendData(byte send_data1, byte send_data2, byte send_data3, byte send_data4) {
        long uid = Long.parseLong(Constant.userName);
        byte[] data = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeByte(0xaa);
            dos.writeByte(0x01);
            dos.writeByte(send_data1);
            dos.writeByte(send_data2);
            dos.writeByte(send_data3);
            dos.writeByte(send_data4);
            Log.e("查询字节", "字节一" + send_data1 + "字节二" + send_data2 + "字节三" + send_data3 + "字节四" + send_data4);
            dos.writeByte(0xbb);
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
                Log.i(TAG, "sendData=result=" + msg.toString());
                int result = msg.getHead().getResult();
                Log.i(TAG, "返回的head头包" + result);
                if (result == 0) {
                    byte receiveByte[] = msg.getUserData().toByteArray();
                    Utils.bytesToHexString(receiveByte);
                    String receive_data = Utils.bytesToHexString(receiveByte);
                    for (int i = 0; i < receiveByte.length; i++) {
                        Log.i(TAG, "接收的控制返回结果" + receiveByte[i]);
                    }
                    //服务器返回

                } else if (result == 404) {
                } else if (result == 204) {
                }
            }

            @Override
            public void onTimeout(Nodepp.Msg msg) {
                Log.i(TAG, "接收的控制返回结果" + 111);

            }

            @Override
            public void onFaile() {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
    }

    private void startTimer() {
        Log.i(TAG, "startTimer");
        if (timer == null) {
            timer = new Timer();
        }
        if (myTask == null) {
            myTask = new MyTasks();
        }
        timer.schedule(myTask, 1000, 5000);  //定时器从进入页面1秒开始，每隔5s执行一次
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
    class MyTasks extends TimerTask {

        @Override
        public void run() {
            Log.i(TAG, "执行");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    long currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis - lastControlTimeStamp > 1000){//距离最后一次控制的时间大于3s才进行状态查询
                        queryall();
                    }else {
                        Log.i(TAG,"---------控制不执行-------------");
                    }
                }

            });
        }
    }

}
