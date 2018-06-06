package com.nodepp.smartnode.udp;

import android.content.Context;

import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.model.UDPTask;
import com.nodepp.smartnode.utils.Log;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import nodepp.Nodepp;

/**
 * Created by yuyue on 2017/5/10.
 */
public class UDPSocket {

    private static final String TAG = "UDPSocket";
    /**
     * 发送网络请求
     *
     * @param context
     * @param msg
     * @param responseListener
     */
    public static void send(Context context, String ip, Nodepp.Msg msg, String random, ResponseListener responseListener) {
        Log.i(TAG, "send===" + msg.toString());
        UDPAsyncTask udpAsyncTask = new UDPAsyncTask(context);
        WeakReference<UDPAsyncTask> udpAsyncTaskWeakReference = new WeakReference<UDPAsyncTask>(udpAsyncTask);
        if (msg != null){
            UDPTask udpTask = new UDPTask();
            udpTask.setIp(ip);
            udpTask.setMsg(msg);
            udpTask.setRandom(random);
            UDPAsyncTask task = udpAsyncTaskWeakReference.get();
            if (task != null){
                task.executeOnExecutor(Constant.threadPool, udpTask);
                task.setResponseListener(responseListener,msg.getHead().getSeq());
            }
        }

    }
}
