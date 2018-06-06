package com.nodepp.smartnode.udp;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.esptouch.task.__IEsptouchTask;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nodepp.Nodepp;
import tencent.tls.tools.MD5;

/**
 * UDPClient 近场通讯直接给设备发送udp包的类
 * Created by yuyue on 2017/4/14.
 */
public class UDPClientScan extends DatagramSocket{

    private static final String TAG = UDPClientScan.class.getSimpleName();
    private static byte[] mBuffer;
    public static final int SOCKET_DATA_BUF_LEN = 2048;
    private static DatagramPacket mReceivePacket;
    private static UDPClientScan client = null;
    private List<Long> tidListLAN = Collections.synchronizedList(new ArrayList<Long>());//存放已经扫描到的局域网设备tid

    public UDPClientScan() throws SocketException {
    }

    public UDPClientScan(int aPort) throws SocketException {
        super(aPort);
    }

    public UDPClientScan(int aPort, InetAddress addr) throws SocketException {
        super(aPort, addr);
    }

    public static UDPClientScan getInstance(){
        if (client == null){
            synchronized (UDPClientScan.class){
                if (client == null){
                    try {
                        client = new UDPClientScan();
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
    public void closeSocket() {
        if (client != null) {
            client.close();
            Log.i(TAG, "socket is close");
            client = null;
        }
    }

    public Nodepp.Msg receiveScanData(Context context,Handler handler, List<Device> list) {
        Log.i("LAN","------clear tidListLAN------");
        tidListLAN.clear();
        if (client == null){
            return null;
        }
        long currentIp = NetWorkUtils.getCurrentIp(context.getApplicationContext());
        byte[] key = Utils.pakeKey(currentIp, client.getLocalPort(), "1234567890");
        Nodepp.Msg message = null;
        Log.d(TAG, "receiveData");
        Log.i("key_scan", "receiveScanData:" + Utils.bytesToHexString(key));
        long deadline = System.currentTimeMillis() + 30000;
        if (mReceivePacket != null && client != null) { //扫描设备回包，等待接收2os后关闭
            try {
                while (System.currentTimeMillis() < deadline) {
                    if (mReceivePacket == null || client == null){
                        break;
                    }
                    client.receive(mReceivePacket);
                    InetAddress address = mReceivePacket.getAddress();
                    byte[] recDatas = Arrays.copyOf(mReceivePacket.getData(), mReceivePacket.getLength());
                    if (recDatas.length == -1) {
                        message = null;
                        Log.w(TAG, " msg==receive=null");
                    } else {
                        message = PbDataUtils.decryptAndParserResponse(recDatas, recDatas.length, key);
                        if (message != null) {
                            Log.w(TAG, " msg==receiveScanData=" + message.toString());
                            long tid = message.getTid();
                            if (tidListLAN.contains(tid)){//查询到时候为了确保设备能收到查询包，发送了多个，接收到回包到时候，重复包不处理
                                Log.i("LAN","------局域网扫描到重复包----1--");
                                continue;
                            }
                            Log.i("LAN","----------局域网扫描到包--------");
                            tidListLAN.add(tid);
                            int deviceType = message.getDeviceType();
                            Log.w(TAG, "deviceType===" + deviceType);
                            String username = SharedPreferencesUtils.getString(context, "username", "");
                            Log.w(TAG, "username==" + username);
                            Log.w(TAG, "list==" + list.toString());
                            synchronized (UDPClientScan.class) {
                                if (list != null) {
                                    for (int i = 0; i < list.size(); i++) {
                                        Device device = list.get(i);
                                        if (tid == device.getTid()) {
                                            device.setDeviceType(deviceType);
                                            device.setConnetedMode(1);
                                            if (address != null) {
                                                device.setIp(address.getHostAddress().toString());
                                                Log.w(TAG, "-------更新------"+address.getHostAddress().toString());
                                            }
                                            String clientKey = device.getClientKey();
                                            if (clientKey.equals(PbDataUtils.byteString2String(message.getKeyClient()))) {
                                                device.setIsOnline(true);
                                                try {
                                                    Log.w(TAG, "-------更新------");
                                                    Constant.LANDevices.put(device.getDid(),true);
                                                    DBUtil.getInstance(context.getApplicationContext()).update(device, WhereBuilder.b("userName", "=", username).and("tid", "=", device.getTid()));
                                                } catch (DbException e) {
                                                    e.printStackTrace();
                                                    Log.w("abc", "-------error------" + e.toString());
                                                }
                                                handler.sendEmptyMessageDelayed(1,2000);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {

            }
        }
        return message;
    }

    public void sendroadcastPacket(Context context) {
        if (client != null){
            long currentIp = NetWorkUtils.getCurrentIp(context.getApplicationContext());
            byte[] key = Utils.pakeKey(currentIp, client.getLocalPort(), "1234567890");
            Log.i("key_scan", "sendroadcastPacket:" + Utils.bytesToHexString(key));
            InetAddress address = Utils.getBroadcastAdress(context.getApplicationContext());
            Log.i("kk", "address==" + address.toString());
            Nodepp.Msg.Builder msgBuilder = Nodepp.Msg.newBuilder();
            Nodepp.Head.Builder headBuilder = Nodepp.Head.newBuilder();
            headBuilder.setSeq(PbDataUtils.getCurrentSeq());
            headBuilder.setCmd(0x18);
            msgBuilder.setHead(headBuilder);
            Nodepp.Msg msg = msgBuilder.build();
            DatagramPacket localDatagramPacket = null;
            byte[] data = PbDataUtils.encryptDataToClient(msg, key);
            if (data != null) {
                localDatagramPacket = new DatagramPacket(data, data.length, address, Constant.broadCastPort);
                try {
                    if (client != null) {
                        this.client.send(localDatagramPacket);
                        this.client.send(localDatagramPacket);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
