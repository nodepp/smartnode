package com.nodepp.smartnode.udp;

import android.content.Context;
import android.os.AsyncTask;

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
public class UDPAsyncTaskA2S extends AsyncTask<Nodepp.Msg, Void, Nodepp.Msg> {
    private static final String TAG = UDPAsyncTaskA2S.class.getSimpleName();
    private WeakReference<Context> mContext;
    private static HashMap<Integer, ResponseListener> UDPCallBacks = new HashMap<Integer, ResponseListener>();
    private String ip;

    public UDPAsyncTaskA2S(Context context) {
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
    protected Nodepp.Msg doInBackground(Nodepp.Msg... params) {
        Nodepp.Msg message  = params[0];
        Nodepp.Msg resultMsg = null;
        Context context = mContext.get();
        if (message != null){
            Log.i(TAG,"send==="+message.toString());
            if (context != null ) {
                resultMsg = UDPClientA2S.getInstance().sendDataToServer(message);
                if (resultMsg == null){
                    resultMsg = PbDataUtils.getErrorMsg(message.getHead().getSeq());
                }
            }
        }
        return resultMsg;
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
