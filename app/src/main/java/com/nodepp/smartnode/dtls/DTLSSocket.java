package com.nodepp.smartnode.dtls;

import android.content.Context;

import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.utils.Log;

import java.lang.ref.SoftReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import nodepp.Nodepp;

/**
 * Created by yuyue on 2017/7/24.
 */
public class DTLSSocket {
    /**
     * 发送网络请求
     *
     * @param context
     * @param msg
     * @param responseListener
     */
    private static String TAG = DTLSSocket.class.getSimpleName();
//    private static  BlockingQueue<Runnable> queue = new LinkedBlockingDeque<Runnable>();
//    private static ThreadPoolExecutor exec = new ThreadPoolExecutor(Constant.POOL_SIZE, Constant.MAXIMUM_POOL_SIZE, Constant.KEEPALIVE_TIME, TimeUnit.DAYS, queue);
    public static void send(Context context, String ip, Nodepp.Msg msg, ResponseListener responseListener) {
        Log.i("TAG", "send===" + msg.toString());
        DTLSAsyncTask dtlsAsyncTask = new DTLSAsyncTask(context);
        SoftReference<DTLSAsyncTask> dtlsAsyncTaskSoftReference = new SoftReference<>(dtlsAsyncTask);
        DTLSAsyncTask task = dtlsAsyncTaskSoftReference.get();
        if (msg!= null){
//            task.executeOnExecutor(exec,msg);
            task.execute(msg);
            task.setResponseListener(responseListener,msg.getHead().getSeq());
        }
    }
}
