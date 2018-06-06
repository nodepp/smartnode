package com.nodepp.smartnode.dtls;

import android.content.Context;
import android.os.AsyncTask;

import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import nodepp.Nodepp;

/**
 * 异步请求数据，回调同步处理
 * Created by yuyue on 2017/7/24.
 */
public class DTLSAsyncTask extends AsyncTask<Nodepp.Msg, Void, Nodepp.Msg> {
    protected WeakReference<Context> mContext;
    private static final String TAG = DTLSAsyncTask.class.getSimpleName();
    private static HashMap<Integer, ResponseListener> callBacks = new HashMap<Integer, ResponseListener>();
    private ResponseListener responseListener;

    public DTLSAsyncTask(Context context) {
        this.mContext = new WeakReference<Context>(context);
    }

    public void setResponseListener(ResponseListener responseListener, int seq) {
        Log.i(TAG,"seq="+seq);
        this.responseListener = responseListener;
        callBacks.put(seq, responseListener);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected Nodepp.Msg doInBackground(Nodepp.Msg... params) {
        Nodepp.Msg resultMsg = null;
        Nodepp.Msg msg = params[0];
        if (mContext.get() != null){
            resultMsg = DTLSClient.getInstance(mContext.get()).sendMessage(msg);
            DTLSClient.getInstance(mContext.get()).close();
        }
        if (resultMsg != null) {
            return resultMsg;
        }else {
            return PbDataUtils.getErrorMsg(msg.getHead().getSeq());
        }
    }
    @Override
    protected void onPostExecute(Nodepp.Msg msg) {
        if (msg != null) {
            ResponseListener responseListener = callBacks.remove(msg.getHead().getSeq());
            if (responseListener != null) {
                Log.i(TAG, "receive-msg=" + msg.toString());
                if (msg.getHead().getResult() == -1){
                    responseListener.onFaile();
                }else {
                    responseListener.onSuccess(msg);
                }
            }
        }
    }
}