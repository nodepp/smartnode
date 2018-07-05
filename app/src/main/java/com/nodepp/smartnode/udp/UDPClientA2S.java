package com.nodepp.smartnode.udp;

import android.os.Handler;
import android.os.Looper;
import com.google.protobuf.ByteString;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.model.MessageBean;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import nodepp.Nodepp;
import outnodepp.Outnodepp;

/**
 * Created by nodepp on 2018/3/19.
 */

public class UDPClientA2S extends DatagramSocket {
    private static final String TAG = "UDPClientA2S";
    public static UDPClientA2S client;
    public static final int SERVER_PORT = 20320;
    public static final int SOCKET_DATA_BUF_LEN = 2048;
    public static int retryCount = 0;
    private static byte[] mBuffer;
    private static DatagramPacket mReceivePacket;
    private static volatile boolean mIsLoop= false;
    private boolean isThrow = true;
    private boolean isRetry = true;
    private static Map<Integer, MessageBean> messageMap = new Hashtable<>();

    private Handler handler = new Handler(Looper.myLooper());

    public UDPClientA2S() throws SocketException {
    }

    public UDPClientA2S(int aPort) throws SocketException {
        super(aPort);
    }

    public UDPClientA2S(int aPort, InetAddress addr) throws SocketException {
        super(aPort, addr);
    }
    public void setIsLoop(boolean isLoop){
        mIsLoop = isLoop;
    }
    public void setThrowInvalidPackage(boolean isThrow){
        this.isThrow = isThrow;
    }

    public void setIsRetry(boolean isRetry){
        this.isRetry = isRetry;
    }
    private void addRetryCount() {
        synchronized (UDPClient.class) {
            retryCount++;
        }
        synchronized (UDPClient.class) {
            retryCount++;
        }
    }

    private void reSetCount() {
        synchronized (UDPClient.class) {
            retryCount = 0;
        }
    }

    public static UDPClientA2S getInstance() {
        if (client == null) {
            synchronized (UDPClientA2S.class) {
                if (client == null) {
                    try {
                        client = new UDPClientA2S();
                        client.setSoTimeout(2000);
                        mBuffer = new byte[SOCKET_DATA_BUF_LEN];
                        mReceivePacket = new DatagramPacket(mBuffer, SOCKET_DATA_BUF_LEN);
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
        return client;
    }

    /***
     * 异步线程池执行任务
     */
    public void enquene(Nodepp.Msg msg, ResponseListener responseListener) {
        Constant.threadPool.execute(new SendRuanable(msg,responseListener));
    }

   static class SendRuanable implements Runnable {


        private Nodepp.Msg msg;
        private ResponseListener responseListener;

        public SendRuanable(Nodepp.Msg msg, ResponseListener responseListener) {

            this.msg = msg;
            this.responseListener = responseListener;
        }

        @Override
        public void run() {
            getInstance().sendMessageA2S(msg,responseListener);
        }
    }
    /**
     * 一发一收
     * 给client发送数据
     */
    public void sendMessageA2S(final Nodepp.Msg msg, final ResponseListener responseListener) {
        if (client == null || client.isClosed()) {
            close();
            return;
        }
//        synchronized (UDPClientA2S.class){
            Nodepp.Msg message = null;
            try {
                message = sendDataAndRead(msg);
            } catch (IOException e) {
                e.printStackTrace();
                try {//超时重试1次
                   if (isRetry){
                       if (retryCount == 0) {
                           Log.i("retry", "超时重试1次");
                           addRetryCount();
                           message = sendDataAndRead(msg);
                       }
                   }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }finally {

                }
            }finally {
                final Nodepp.Msg finalMessage = message;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (finalMessage != null){
                            responseListener.onSuccess(finalMessage);
                            Log.i("sendMessageAS","receive"+ finalMessage.toString());
                        }else {
                            if (isRetry){
                                responseListener.onTimeout(msg);
                                Log.i("sendMessageAS","receive onFaile");
                            }

                        }
                    }
                });
            }
//        }
    }
    /**
     * 一发一收
     * 给client发送数据
     */
    public Nodepp.Msg sendDataToServer(Nodepp.Msg msg) {
        if (client == null || client.isClosed()) {
            close();
            return null;
        }
        Nodepp.Msg message = null;
        try {
            message = sendDataAndRead(msg);
        } catch (IOException e) {
            e.printStackTrace();
            if (isRetry){
                try {//超时重试1次
                    if (retryCount == 0) {
                        Log.i("retry", "超时重试1次");
                        addRetryCount();
                        message = sendDataAndRead(msg);
                        return message;
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }finally {
                    return message;
                }
            }
        }
        return message;
    }

    private Nodepp.Msg sendDataAndRead(Nodepp.Msg msg) throws IOException {
        Nodepp.Msg resultMsg = null;
        int seq = msg.getHead().getSeq();
        if (Constant.KEY_A2S != null) {
            Log.i("key", "key=encry=send=" + Utils.bytesToHexString(Constant.KEY_A2S));
            byte[] sendData = PbDataUtils.encryptDataToServer(msg.getHead().getUid(), msg, Constant.KEY_A2S);
            DatagramPacket localDatagramPacket = null;
            if (sendData != null && client != null) {
                InetAddress address = InetAddress.getByName(Constant.SERVER_HOST);
                localDatagramPacket = new DatagramPacket(sendData, sendData.length, address, SERVER_PORT);
                this.client.send(localDatagramPacket);
                int port = client.getLocalPort();
                Log.i(TAG, "port:" + port);
                if (mReceivePacket != null){
                    client.receive(mReceivePacket);
                }
                if (mReceivePacket.getLength() < 0) {
                    resultMsg = null;
                    Log.i(TAG, " msg==receive=null");
                } else {
                    byte[] recDatas = Arrays.copyOf(mReceivePacket.getData(), mReceivePacket.getLength());
                    if (recDatas.length > 0) {
                        Outnodepp.Data out = Outnodepp.Data.PARSER.parseFrom(recDatas);
                        long uid = out.getUid();
                        Log.i(TAG, " uid==" + uid);
                        ByteString interdata = out.getInterdata();//加密的数据
                        byte[] decryptData = Utils.decrypt(interdata.toByteArray(), Constant.KEY_A2S);//解密数据
                        if (decryptData != null) {
                            reSetCount();
                            resultMsg = Nodepp.Msg.PARSER.parseFrom(decryptData);
                        }

                    }
                }
                Log.i(TAG, " recDatas isThrow :"+isThrow);
                if (resultMsg != null && isThrow){//isThrow 是否丢弃无用包
                    while (resultMsg.getHead().getSeq() < seq){//读取掉seq小于当前发送seq的无效包
                        client.receive(mReceivePacket);
                        byte[] recDatas = Arrays.copyOf(mReceivePacket.getData(), mReceivePacket.getLength());
                        if (recDatas.length == -1) {
                            Log.i(TAG, " recDatas.length == -1");
                            break;
                        } else {
                            Outnodepp.Data out = Outnodepp.Data.PARSER.parseFrom(recDatas);
                            long uid = out.getUid();
                            Log.i(TAG, " uid=while=" + uid);
                            ByteString interdata = out.getInterdata();//加密的数据
                            byte[] decryptData = Utils.decrypt(interdata.toByteArray(), Constant.KEY_A2S);//解密数据
                            Nodepp.Msg receiveMsg = null;
                            if (decryptData != null) {
                                reSetCount();
                                receiveMsg = Nodepp.Msg.PARSER.parseFrom(decryptData);
                            }
                            if (receiveMsg == null){
                                Log.i(TAG, "receiveMsg == null");
                                break;//解密出错，返回null的时候立即退出循环
                            }else {
                                reSetCount();
                                resultMsg = receiveMsg;
                            }
                        }
                    }
                }
            }

        }
        return resultMsg;
    }

    @Override
    public synchronized void close() {
        super.close();
        mIsLoop = false;
        client = null;
    }
}
