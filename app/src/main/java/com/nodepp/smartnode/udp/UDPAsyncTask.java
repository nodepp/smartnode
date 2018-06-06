package com.nodepp.smartnode.udp;

import android.content.Context;
import android.os.AsyncTask;

import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.model.UDPTask;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import nodepp.Nodepp;

/**
 * 异步请求数据，回调同步处理
 * Created by yuyue on 2017/5/10.
 */
public class UDPAsyncTask extends AsyncTask<UDPTask, Void, Nodepp.Msg> {
    private static final String TAG = UDPAsyncTask.class.getSimpleName();
    private WeakReference<Context> mContext;
    private static HashMap<Integer, ResponseListener> UDPCallBacks = new HashMap<Integer, ResponseListener>();
    private String ip;

    public UDPAsyncTask(Context context) {
        this.mContext = new WeakReference<Context>(context);
    }

    public void setResponseListener(ResponseListener responseListener, int seq) {
        UDPCallBacks.put(seq, responseListener);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Nodepp.Msg doInBackground(UDPTask... params) {
        Nodepp.Msg msg = null;
        UDPTask task = params[0];
        ip = task.getIp();
        String random = task.getRandom();
        Context context = mContext.get();
        Nodepp.Msg m = task.getMsg();
        if (context != null) {
            if (m != null && random != null && random != null){
                msg = UDPClient.getInstance(context).sendDataToClient(ip, m, random);
            }
            return msg;
        }else {
            return PbDataUtils.getErrorMsg(msg.getHead().getSeq());
        }
    }

    @Override
    protected void onPostExecute(Nodepp.Msg msg) {
        if (msg != null) {
            ResponseListener responseListener = UDPCallBacks.remove(msg.getHead().getSeq());
            if (responseListener != null) {
                Log.i(TAG, "udp receive msg :" + msg.toString());
                if (msg.getHead().getResult() == -1){
                    responseListener.onFaile();
                }else {
                    responseListener.onSuccess(msg);
                }
            }
        }
    }
}
