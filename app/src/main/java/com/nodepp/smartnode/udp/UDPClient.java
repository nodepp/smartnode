package com.nodepp.smartnode.udp;

import android.content.Context;

import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.esptouch.task.__IEsptouchTask;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import nodepp.Nodepp;

/**
 * UDPClient 近场通讯直接给设备发送udp包的类
 * Created by yuyue on 2017/4/14.
 */
public class UDPClient extends DatagramSocket{

    private static final String TAG = UDPClient.class.getSimpleName();
    private static Context mContext;
    private static byte[] mBuffer;
    private static volatile byte[] key;
    private static DatagramPacket mReceivePacket;
    private static UDPClient client = null;
    private static int retryCount = 0;
    private static boolean isReceive = true;
    private static boolean isThrow = true;
    public UDPClient(int aPort) throws SocketException {
        super(aPort);
    }
    private void addRetryCount(){
        synchronized (UDPClient.class){
            retryCount ++;
        }synchronized (UDPClient.class){
            retryCount ++;
        }
    }
    private void reSetCount(){
        synchronized (UDPClient.class){
            retryCount =0;
        }
    }
    public static void setIsReceive(boolean receive) {
        isReceive = receive;
    }

    public static UDPClient getInstance(Context context) {
        mContext = context.getApplicationContext();//不要直接mContext = context;这样会内存泄露
        if (client == null) {
            synchronized (UDPClient.class) {
                if (client == null) {
                    try {
                        mBuffer = new byte[Constant.SOCKET_DATA_BUF_LEN];
                        mReceivePacket = new DatagramPacket(mBuffer, Constant.SOCKET_DATA_BUF_LEN);
                        client = new UDPClient(Constant.udpSocketPort);//不存在socket时才进行创建
                        client.setSoTimeout(4000);
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                    Log.i("bb", "client is creat");
                }
            }
        }else {
            synchronized (UDPClient.class) {
                if (client.isClosed()){
                    if (client != null) {
                        if (client.isConnected()) {
                            setIsReceive(false);
                            client.close();
                            Log.i(TAG, "socket is close");
                            client = null;

                        }
                    }
                }
            }
        }
        return client;
    }

    public void setThrowInvalidPackage(boolean isThrow){
        this.isThrow = isThrow;
    }
    public synchronized void closeSocket() {
        if (client != null) {
            if (client.isConnected()) {
                setIsReceive(false);
                client.close();
                Log.i(TAG, "socket is close");
                client = null;
              
            }
        }
    }

    /**
     * 给client发送数据
     *
     * @param ip
     */
    public Nodepp.Msg sendDataToClient(String ip, Nodepp.Msg msg,String random) {
        if (client == null){
            return null;
        }
        if (client.isClosed()){
            close();
        }
        Nodepp.Msg message = null;
        int seq = msg.getHead().getSeq();
        long currentIp = NetWorkUtils.getCurrentIp(mContext);
            key = Utils.pakeKey(currentIp, Constant.udpSocketPort, random);
            if (key != null){
                Log.i("key","key=encry=send="+ Utils.bytesToHexString(key));
                byte[] data = PbDataUtils.encryptDataToClient(msg,key);
                if (ip != null ) {
                    try {
                        message = sendDataAndRead(seq,ip, data);
                    } catch (IOException e) {
                        e.printStackTrace();
//                        try {//超时重试1次
//                            if (retryCount == 0) {
//                                Log.i("retry", "超时重试1次");
//                                message = sendDataAndRead(seq, ip, data);
//                                addRetryCount();
//                            }
//                        } catch (IOException e1) {
//                            e1.printStackTrace();
//                            return null;
//                        }
                    }
                }
            }
        return message;
    }
    private Nodepp.Msg sendDataAndRead(int seq,String ip, byte[] data) throws IOException {
        Nodepp.Msg message = null;
        InetAddress address = null;
        try {
            address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
        DatagramPacket localDatagramPacket = null;
        if (data != null && client != null) {
            localDatagramPacket = new DatagramPacket(data, data.length, address, Constant.deviceServerPort);
            this.client.send(localDatagramPacket);
            client.receive(mReceivePacket);
            byte[] recDatas = Arrays.copyOf(mReceivePacket.getData(), mReceivePacket.getLength());
            if (recDatas.length == -1) {
                message = null;
                Log.i(TAG, " msg==receive=null");
            } else {
                reSetCount();
                Log.i("key","key=decrypt=receive="+ Utils.bytesToHexString(key));
                message = PbDataUtils.decryptAndParserResponse(recDatas, recDatas.length,key);
            }
            Log.i(TAG, " recDatas isThrow :"+isThrow);
            if (message != null && isThrow){
                while (message.getHead().getSeq() < seq){//读取掉seq小于当前发送seq的无效包
                    client.receive(mReceivePacket);
                    recDatas = Arrays.copyOf(mReceivePacket.getData(), mReceivePacket.getLength());
                    if (recDatas.length == -1) {
                        Log.i(TAG, " recDatas.length == -1");
                        break;
                    } else {
                        Nodepp.Msg receiveMsg = PbDataUtils.decryptAndParserResponse(recDatas, recDatas.length,key);
                        if (receiveMsg == null){
                            Log.i(TAG, " receiveMsg == null");
                            break;//解密出错，返回null的时候立即退出循环
                        }else {
                            reSetCount();
                            message = receiveMsg;
                        }
                    }
                }
            }
        }
        return message;
    }
}
