package com.nodepp.smartnode.esptouch.task;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;

import com.google.protobuf.ByteString;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.esptouch.EsptouchResult;
import com.nodepp.smartnode.esptouch.IEsptouchListener;
import com.nodepp.smartnode.esptouch.IEsptouchResult;
import com.nodepp.smartnode.esptouch.protocol.EsptouchGenerator;
import com.nodepp.smartnode.esptouch.udp.UDPSocketClient;
import com.nodepp.smartnode.esptouch.udp.UDPSocketServer;
import com.nodepp.smartnode.esptouch.util.ByteUtil;
import com.nodepp.smartnode.esptouch.util.EspNetUtil;
import com.nodepp.smartnode.model.UserInfo;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import nodepp.Nodepp;

public class __EsptouchTask implements __IEsptouchTask {

    /**
     * one indivisible data contain 3 9bits info
     */
    private static final int ONE_DATA_LEN = 3;

    private static final String TAG = "EsptouchTask";

    private volatile List<IEsptouchResult> mEsptouchResultList;
    private volatile boolean mIsSuc = false;
    private volatile boolean mIsInterrupt = false;
    private volatile boolean mIsExecuted = false;
    private final UDPSocketClient mSocketClient;
    private final UDPSocketServer mSocketServer;
    private final String mApSsid;
    private final String mApBssid;
    private final boolean mIsSsidHidden;
    private final String mApPassword;
    private final Context mContext;
    private AtomicBoolean mIsCancelled;
    private IEsptouchTaskParameter mParameter;
    private volatile Map<String, Integer> mBssidTaskSucCountMap;
    private IEsptouchListener mEsptouchListener;

    public __EsptouchTask(String apSsid, String apBssid, String apPassword,
                          Context context, IEsptouchTaskParameter parameter,
                          boolean isSsidHidden) {
        if (TextUtils.isEmpty(apSsid)) {
            throw new IllegalArgumentException(
                    "the apSsid should be null or empty");
        }
        if (apPassword == null) {
            apPassword = "";
        }
        mContext = context;
        mApSsid = apSsid;
        mApBssid = apBssid;
        mApPassword = apPassword;
        mIsCancelled = new AtomicBoolean(false);
        mSocketClient = new UDPSocketClient();
        mParameter = parameter;
        mSocketServer = new UDPSocketServer(mParameter.getPortListening(),
                mParameter.getWaitUdpTotalMillisecond(), context);
        mIsSsidHidden = isSsidHidden;
        mEsptouchResultList = new ArrayList<IEsptouchResult>();
        mBssidTaskSucCountMap = new HashMap<String, Integer>();
    }

    private void __putEsptouchResult(boolean isSuc, String bssid,
                                     InetAddress inetAddress) {
        synchronized (mEsptouchResultList) {
            // check whether the result receive enough UDP response
            boolean isTaskSucCountEnough = false;
            Integer count = mBssidTaskSucCountMap.get(bssid);
            if (count == null) {
                count = 0;
            }
            ++count;
            mBssidTaskSucCountMap.put(bssid, count);
            isTaskSucCountEnough = count >= mParameter
                    .getThresholdSucBroadcastCount();
            if (!isTaskSucCountEnough) {
                if (__IEsptouchTask.DEBUG) {
                    Log.d(TAG, "__putEsptouchResult(): count = " + count
                            + ", isn't enough");
                }
                return;
            }
            // check whether the result is in the mEsptouchResultList already
            boolean isExist = false;
            for (IEsptouchResult esptouchResultInList : mEsptouchResultList) {
                if (esptouchResultInList.getBssid().equals(bssid)) {
                    isExist = true;
                    break;
                }
            }
            // only add the result who isn't in the mEsptouchResultList
            if (!isExist) {
                if (__IEsptouchTask.DEBUG) {
                    Log.d(TAG, "__putEsptouchResult(): put one more result");
                }
                //结果回调
                final IEsptouchResult esptouchResult = new EsptouchResult(isSuc,
                        bssid, inetAddress);
                mEsptouchResultList.add(esptouchResult);
                if (mEsptouchListener != null) {
                    mEsptouchListener.onEsptouchResultAdded(esptouchResult);
                }
            }
        }
    }

    private List<IEsptouchResult> __getEsptouchResultList() {
        synchronized (mEsptouchResultList) {
            if (mEsptouchResultList.isEmpty()) {
                EsptouchResult esptouchResultFail = new EsptouchResult(false,
                        null, null);
                esptouchResultFail.setIsCancelled(mIsCancelled.get());
                mEsptouchResultList.add(esptouchResultFail);
            }

            return mEsptouchResultList;
        }
    }

    private synchronized void __interrupt() {
        if (!mIsInterrupt) {
            mIsInterrupt = true;
            mSocketClient.interrupt();
            mSocketServer.interrupt();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void interrupt() {
        if (__IEsptouchTask.DEBUG) {
            Log.d(TAG, "interrupt()");
        }
        mIsCancelled.set(true);
        __interrupt();
    }

    private void __listenAsyn(final int expectDataLen) {
        new Thread() {
            public void run() {
                if (__IEsptouchTask.DEBUG) {
                    Log.d(TAG, "__listenAsyn() start");
                }
                long startTimestamp = System.currentTimeMillis();
                byte[] apSsidAndPassword = ByteUtil.getBytesByString(mApSsid
                        + mApPassword);
                byte expectOneByte = (byte) (apSsidAndPassword.length + 9);
                if (__IEsptouchTask.DEBUG) {
                    Log.i(TAG, "expectOneByte: " + (0 + expectOneByte));
                }
                byte receiveOneByte = -1;
                byte[] receiveBytes = null;
                String bssid = null;
                InetAddress inetAddress = null;
                while (mEsptouchResultList.size() < mParameter
                        .getExpectTaskResultCount() && !mIsInterrupt) {
                    Log.d(TAG, "=============in==while==========");
                    receiveBytes = mSocketServer
                            .receiveSpecLenBytes(expectDataLen);
                    if (receiveBytes != null) {
                        receiveOneByte = receiveBytes[0];
                    } else {
                        receiveOneByte = -1;
                    }
                    if (receiveOneByte == expectOneByte) {
                        if (__IEsptouchTask.DEBUG) {
                            Log.i(TAG, "receive correct broadcast");
                        }
                        // change the socket's timeout
                        long consume = System.currentTimeMillis()
                                - startTimestamp;
                        int timeout = (int) (mParameter
                                .getWaitUdpTotalMillisecond() - consume);
                        if (timeout < 0) {
                            if (__IEsptouchTask.DEBUG) {
                                Log.i(TAG, "esptouch timeout");
                            }
                            break;
                        } else {
                            if (__IEsptouchTask.DEBUG) {
                                Log.i(TAG, "mSocketServer's new timeout is "
                                        + timeout + " milliseconds");
                            }
                            mSocketServer.setSoTimeout(timeout);
                            if (__IEsptouchTask.DEBUG) {
                                Log.i(TAG, "receive correct broadcast");
                            }
                            if (receiveBytes != null) {//getEsptouchResultOneLen = 1 getEsptouchResultMacLen = 6
                                bssid = ByteUtil.parseBssid(
                                        receiveBytes,
                                        mParameter.getEsptouchResultOneLen(),
                                        mParameter.getEsptouchResultMacLen());
                                inetAddress = EspNetUtil.parseInetAddr(receiveBytes,
                                        mParameter.getEsptouchResultOneLen()
                                                + mParameter.getEsptouchResultMacLen(),//1+6
                                        mParameter.getEsptouchResultIpLen());//4
//                                mSocketServer.close();//关闭esp serveSsocket
                                break;
                            }
                        }
                    } else {//通过proto获取tid
                        break;
                    }
                }
                receiveDataFromClient(bssid,inetAddress);
            }
        }.start();
    }

    private void receiveDataFromClient(String bssid, InetAddress inetAddress) {
        Nodepp.Msg msg = mSocketServer.receiveData();
        if (msg != null) {
            Log.i(TAG, "msg == " + msg.toString());
            //获取msg中的tid
            long tid = msg.getTid();
            long did = msg.getDid();
            int deviceType = msg.getDeviceType();
            UserInfo.firmwareLevel = msg.getState();
            UserInfo.firmwareVersion = msg.getVerCur();
            Log.i(TAG, "tid == " + tid);
            UserInfo.tid = tid;//接收到插座返回的tid
            UserInfo.deviceType = deviceType;//接收到插座返回的tid
            Log.i(TAG, "did == " + UserInfo.did);
            Log.i(TAG, "ip == " + Constant.ip);
            Log.i(TAG, "dsig == " + UserInfo.dsig);
            Log.i(TAG, "deviceType == " + UserInfo.deviceType);
            if (did != 0) {
                UserInfo.isDeviceReturn = true;
                UserInfo.did = did;
            }
            ByteString clientKey;
            if (UserInfo.keyClient != null) {
                clientKey = UserInfo.keyClient;
            } else {
                clientKey = PbDataUtils.string2ByteString("1234567890");
            }
            if (Constant.ip != null && Constant.tempPort != 0) {
                if (UserInfo.dsig != null) {//防止空指针
                    UDPSocketClient socketClient = new UDPSocketClient();
                    socketClient.sendDataToClient(Constant.ip, Constant.tempPort, 10, UserInfo.connetedMode, tid, UserInfo.did, UserInfo.dsig, clientKey, UserInfo.keyClientWAN);//发送did给client
                    __putEsptouchResult(true, bssid, inetAddress);//回调通知配置成功
                }
            }
        }
    }

    private boolean __execute(IEsptouchGenerator generator) {

        long startTime = System.currentTimeMillis();
        long currentTime = startTime;
        long lastTime = currentTime - mParameter.getTimeoutTotalCodeMillisecond();

        byte[][] gcBytes2 = generator.getGCBytes2();
        byte[][] dcBytes2 = generator.getDCBytes2();

        int index = 0;
        while (!mIsInterrupt) {//非中断情况下
            if (currentTime - lastTime >= mParameter.getTimeoutTotalCodeMillisecond()) {// 一开始设置让currentTime - lastTime 的值是6  mParameter.getTimeoutTotalCodeMillisecond()是2+4s，6s
                if (__IEsptouchTask.DEBUG) {
                    Log.d(TAG, "send gc code ");
                }
                // send guide code
                while (!mIsInterrupt
                        && System.currentTimeMillis() - currentTime < mParameter
                        .getTimeoutGuideCodeMillisecond()) {// mParameter.getTimeoutGuideCodeMillisecond()是2s
                    mSocketClient.sendData(gcBytes2,
                            mParameter.getTargetHostname(),
                            mParameter.getTargetPort(),
                            mParameter.getIntervalGuideCodeMillisecond());
                    // check whether the udp is send enough time
                    if (System.currentTimeMillis() - startTime > mParameter.getWaitUdpSendingMillisecond()) {//距离此时等待超过45s秒，退出循环，不再发送引导数据
                        break;
                    }
                }
                lastTime = currentTime;
            } else {//lastTime重新赋值，使currentTime - lastTime >= 6不成立
                mSocketClient.sendData(dcBytes2, index, ONE_DATA_LEN,
                        mParameter.getTargetHostname(),
                        mParameter.getTargetPort(),
                        mParameter.getIntervalDataCodeMillisecond());
                index = (index + ONE_DATA_LEN) % dcBytes2.length;
            }
            currentTime = System.currentTimeMillis();
            // check whether the udp is send enough time
            if (currentTime - startTime > mParameter.getWaitUdpSendingMillisecond()) {
                break;
            }
        }

        return mIsSuc;
    }

    private void __checkTaskValid() {
        // !!!NOTE: the esptouch task could be executed only once
        if (this.mIsExecuted) {
            throw new IllegalStateException(
                    "the Esptouch task could be executed only once");
        }
        this.mIsExecuted = true;
    }

    @Override
    public IEsptouchResult executeForResult() throws RuntimeException {
        return executeForResults(1).get(0);
    }

    @Override
    public boolean isCancelled() {
        return this.mIsCancelled.get();
    }

    @Override
    public List<IEsptouchResult> executeForResults(int expectTaskResultCount)
            throws RuntimeException {
        __checkTaskValid();

        mParameter.setExpectTaskResultCount(expectTaskResultCount);

        if (__IEsptouchTask.DEBUG) {
            Log.d(TAG, "execute()");
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException(
                    "Don't call the esptouch Task at Main(UI) thread directly.");
        }
        InetAddress localInetAddress = EspNetUtil.getLocalInetAddress(mContext);
        if (__IEsptouchTask.DEBUG) {
            Log.i(TAG, "localInetAddress: " + localInetAddress);
        }
        // generator the esptouch byte[][] to be transformed, which will cost
        // some time(maybe a bit much)
        IEsptouchGenerator generator = new EsptouchGenerator(mApSsid, mApBssid,
                mApPassword, localInetAddress, mIsSsidHidden);
        // listen the esptouch result asyn
        __listenAsyn(mParameter.getEsptouchResultTotalLen());//传入11
        boolean isSuc = false;
        for (int i = 0; i < mParameter.getTotalRepeatTime(); i++) {// mParameter.getTotalRepeatTime()为1
            isSuc = __execute(generator);
            if (isSuc) {
                return __getEsptouchResultList();
            }
        }

        if (!mIsInterrupt) {
            // wait the udp response without sending udp broadcast
            try {
                Thread.sleep(mParameter.getWaitUdpReceivingMillisecond());
            } catch (InterruptedException e) {
                // receive the udp broadcast or the user interrupt the task
                if (this.mIsSuc) {
                    return __getEsptouchResultList();
                } else {
                    this.__interrupt();
                    return __getEsptouchResultList();
                }
            }
            this.__interrupt();
        }

        return __getEsptouchResultList();
    }

    @Override
    public void setEsptouchListener(IEsptouchListener esptouchListener) {
        mEsptouchListener = esptouchListener;
    }

}
