package com.nodepp.smartnode.esptouch.udp;

import com.google.protobuf.ByteString;
import com.nodepp.smartnode.esptouch.task.__IEsptouchTask;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

import nodepp.Nodepp;

/**
 * this class is used to help send UDP data according to length
 *
 * @author afunx
 */
public class UDPSocketClient {

    private static final String TAG = "UDPClient";
    private DatagramSocket mSocket;
    private volatile boolean mIsStop;
    private volatile boolean mIsClosed;

    public UDPSocketClient() {
        try {
            this.mSocket = new DatagramSocket();
            this.mIsStop = false;
            this.mIsClosed = false;
        } catch (SocketException e) {
            if (__IEsptouchTask.DEBUG) {
                Log.e(TAG, "SocketException");
            }
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public void interrupt() {
        if (__IEsptouchTask.DEBUG) {
            Log.i(TAG, "USPSocketClient is interrupt");
        }
        this.mIsStop = true;
    }

    /**
     * close the UDP socket
     */
    public synchronized void close() {
        if (!this.mIsClosed) {
            this.mSocket.close();
            this.mIsClosed = true;
        }
    }

    /**
     * send the data by UDP
     *
     * @param data       the data to be sent
     *                   the host name of target, e.g. 192.168.1.101
     * @param targetPort the port of target
     * @param interval   the milliseconds to between each UDP sent
     */
    public void sendData(byte[][] data, String targetHostName, int targetPort,
                         long interval) {
        sendData(data, 0, data.length, targetHostName, targetPort, interval);
        for (int i = 0; i < data.length; i++) {
            Log.i("data", "data" + i + "===" + Arrays.toString(data[i]));
            Log.i("data", "data" + i + "行=length==" + data[i].length);
        }
        Log.i("data", "data.length===" + data.length);
        Log.i("data", "targetHostName===" + targetHostName);
        Log.i("data", "targetPort===" + String.valueOf(targetPort));
        Log.i("data", "interval===" + String.valueOf(interval));
    }


    /**
     * send the data by UDP
     *
     * @param data       the data to be sent
     * @param offset     the offset which data to be sent
     * @param count      the count of the data
     *                   the host name of target, e.g. 192.168.1.101
     * @param targetPort the port of target
     * @param interval   the milliseconds to between each UDP sent
     */
    public void sendData(byte[][] data, int offset, int count,
                         String targetHostName, int targetPort, long interval) {
        if ((data == null) || (data.length <= 0)) {
            if (__IEsptouchTask.DEBUG) {
                Log.e(TAG, "sendData(): data == null or length <= 0");
            }
            return;
        }
        for (int i = offset; !mIsStop && i < offset + count; i++) {
            if (data[i].length == 0) {
                continue;
            }
            try {
                DatagramPacket localDatagramPacket = new DatagramPacket(
                        data[i], data[i].length,
                        InetAddress.getByName(targetHostName), targetPort);
                if (localDatagramPacket != null) {
                    this.mSocket.send(localDatagramPacket);
                }
            } catch (UnknownHostException e) {
                if (__IEsptouchTask.DEBUG) {
                    Log.e(TAG, "sendData(): UnknownHostException");
                }
                e.printStackTrace();
                mIsStop = true;
                break;
            } catch (IOException e) {
                if (__IEsptouchTask.DEBUG) {
                    Log.e(TAG, "sendData(): IOException, but just ignore it");
                }
                // for the Ap will make some troubles when the phone send too many UDP packets,
                // but we don't expect the UDP packet received by others, so just ignore it
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (__IEsptouchTask.DEBUG) {
                    Log.e(TAG, "sendData is Interrupted");
                }
                mIsStop = true;
                break;
            }
        }
        if (mIsStop) {
            close();
        }
    }

    /**
     * 给client发送did
     * @param ip
     * @param targetPort
     * @param interval
     */
    public void sendDataToClient(InetAddress ip, int targetPort, long interval,int connetedMode,long tid,long did,ByteString dsig,ByteString keyClient,ByteString keyClientWan) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setCmd(0x15);
        msgBuilder.setTid(tid);
        msgBuilder.setDid(did);
        msgBuilder.setDsig(dsig);
        msgBuilder.setRandom(keyClient);//之前定义的random，注意此时random和keyClient对应
        msgBuilder.setKeyClientWan(keyClientWan);
        msgBuilder.setConnetedMode(connetedMode);
        msgBuilder.setHead(headBuilder);
        Nodepp.Msg msg = msgBuilder.build();
        Log.i("kk",msg.toString());
        byte[] pbData = msg.toByteArray();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeByte(0xaf);    // app->client 请求包帧头
            dos.writeByte(0x0a);
            dos.writeByte(0xf5);
            dos.writeByte(0xfe);
            dos.writeInt(pbData.length);
            dos.write(pbData);
            dos.writeByte(0x3b);    // 请求包帧尾 ; ascii 为 0x3b
            byte[] data = bos.toByteArray();
            DatagramPacket localDatagramPacket = new DatagramPacket(data, data.length, ip, targetPort);
            this.mSocket.send(localDatagramPacket);
            this.mSocket.send(localDatagramPacket);
            mIsStop = true;
        } catch (UnknownHostException e) {
            if (__IEsptouchTask.DEBUG) {
                Log.e(TAG, "sendData(): UnknownHostException");
            }
            e.printStackTrace();
            mIsStop = true;
        } catch (Exception e) {

        }
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {
            e.printStackTrace();
            if (__IEsptouchTask.DEBUG) {
                Log.e(TAG, "sendData is Interrupted");
            }
            mIsStop = true;
        }
        if (mIsStop) {
            close();
        }
    }

}
