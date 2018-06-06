package com.nodepp.smartnode.udp;

import android.content.Context;

import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.model.UDPTask;
import com.nodepp.smartnode.utils.Log;

import java.lang.ref.WeakReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import nodepp.Nodepp;

/**
 * Created by yuyue on 2018/3/19.
 */

public class UDPSocketA2S {
     /**
     * 发送网络请求
     *
     * @param context
     * @param msg
     * @param responseListener
     */
    public static void send(Context context, Nodepp.Msg msg, ResponseListener responseListener) {
//        Log.i("kk", "send===" + msg.toString());
//        UDPAsyncTaskA2S udpAsyncTask = new UDPAsyncTaskA2S(context);
//        WeakReference<UDPAsyncTaskA2S> udpAsyncTaskWeakReference = new WeakReference<>(udpAsyncTask);
//        if (msg != null){
//            UDPAsyncTaskA2S task = udpAsyncTaskWeakReference.get();
//            if (task != null){
////                task.execute(msg);
//                task.executeOnExecutor(udpA2SPool, msg);
//                task.setResponseListener(responseListener,msg.getHead().getSeq());
//            }
//        }
        UDPClientA2S.getInstance().enquene(msg,responseListener);
    }
}
