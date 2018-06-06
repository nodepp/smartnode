package com.nodepp.smartnode.udp;


import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.google.protobuf.ByteString;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nodepp.Nodepp;
import outnodepp.Outnodepp;

/**
 * UDPClient 近场通讯直接给设备发送udp包的类
 * Created by yuyue on 2018/3/19.
 */
public class UDPClientScanA2S extends DatagramSocket{

    private static final String TAG = UDPClientScanA2S.class.getSimpleName();
    public static UDPClientScanA2S client;
    public static final int SERVER_PORT = 20320;
    public static final int SOCKET_DATA_BUF_LEN = 2048;
    private static byte[] mBuffer;
    private static List<Integer> sendSeq = Collections.synchronizedList(new ArrayList<Integer>());
    private static DatagramPacket mReceivePacket;
    private List<Device> devices;

    public UDPClientScanA2S() throws SocketException {
    }

    public UDPClientScanA2S(int aPort) throws SocketException {
        super(aPort);
    }

    public UDPClientScanA2S(int aPort, InetAddress addr) throws SocketException {
        super(aPort, addr);
    }
    public static UDPClientScanA2S getInstance(){
        if (client == null){
            synchronized (UDPClientA2S.class){
                if (client == null){
                    try {
                        client = new UDPClientScanA2S();
                        client.setSoTimeout(20000);
                        mBuffer = new byte[SOCKET_DATA_BUF_LEN];
                        mReceivePacket = new DatagramPacket(mBuffer,SOCKET_DATA_BUF_LEN);
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
        return client;
    }

    public void receiveScanData(Context context,List<Device> list, Handler handler) {
        if (Constant.KEY_A2S == null){
            return;
        }
        Nodepp.Msg message = null;
        Log.d(TAG, "receiveData");
        Log.i("key_scan", "receiveScanData:" + Utils.bytesToHexString(Constant.KEY_A2S));
        long deadline = System.currentTimeMillis() + 20000;
        while (System.currentTimeMillis() < deadline) {//20s后退出
            try {
                if (client == null || mReceivePacket == null){
                    break;
                }
                client.receive(mReceivePacket);
                if (mReceivePacket.getLength() < 0) {
                    message = null;
                    Log.i(TAG, " msg==receive=null");
                } else {
                    Log.i(TAG, " msg==receive=yes");
                    byte[] recDatas = Arrays.copyOf(mReceivePacket.getData(), mReceivePacket.getLength());
                    if (recDatas.length > 0){
                        Outnodepp.Data out = Outnodepp.Data.PARSER.parseFrom(recDatas);
                        long uid = out.getUid();
                        Log.i(TAG, " uid=="+uid);
                        ByteString interdata = out.getInterdata();//加密的数据
                        byte[] decryptData = Utils.decrypt(interdata.toByteArray(), Constant.KEY_A2S);
                        message = Nodepp.Msg.PARSER.parseFrom(decryptData);
                        if (message != null){
                            Log.i(TAG, " receive msg:"+ message);
                            if (message != null) {
                                int seq = message.getHead().getSeq();
                                int result = message.getHead().getResult();
                                long did = message.getHead().getDid();
                                Log.i("kkkk", " receive seq:"+ seq);
                                if (Constant.LANDevices.get(did) != null && Constant.LANDevices.get(did)) { //局域网已经查到在线就不根据互联网进行修改
                                    Log.i(TAG, " did 在局域网中："+ did);
                                } else {
                                    try {
                                        for (Device d : list) {
                                            if (d.getDid() == did) {
                                                if (result == 404) {
                                                    d.setIsOnline(false);
                                                } else if (result == 0) {
                                                    d.setIsOnline(true);
                                                    d.setConnetedMode(0);
//                                                    Log.i("yy", "did : " + device.getDid());
                                                    Log.i(TAG, message.getHead().getDid() + " is online ");
                                                }
                                                DBUtil.getInstance(context.getApplicationContext()).update(d, WhereBuilder.b("userName", "=", Constant.userName).and("did", "=", did));
                                                break;
                                            }
                                        }

                                    } catch (DbException e) {
                                        e.printStackTrace();
                                    }
                                    handler.sendEmptyMessageDelayed(5,2000);//延时发送更新，更新的时候移除所有what=5的消息，减少重复查询更新UI
//                                    handler.sendEmptyMessage(5);
                                }
                            }
                        }else {

                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {

            }
        }

    }

    @Override
    public void close() {
        super.close();
        client = null;
    }

    public void queryDevice(Nodepp.Msg msg) {
        DatagramPacket localDatagramPacket = null;
        if (Constant.KEY_A2S != null) {
            byte[] data = PbDataUtils.encryptDataToServer(msg.getHead().getUid(), msg, Constant.KEY_A2S);
            if (data != null && client != null) {
                try {
                    InetAddress address = InetAddress.getByName(Constant.SERVER_HOST);
                    localDatagramPacket = new DatagramPacket(data, data.length,address,SERVER_PORT);
                    client.send(localDatagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
