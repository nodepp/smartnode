package com.nodepp.smartnode.task;

import android.content.Context;
import android.os.AsyncTask;

import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.utils.NetWorkUtils;

import java.lang.ref.WeakReference;

/**
 * 检测是否有外网连接
 * Created by yuyue on 2017/12/12.
 */
public class CheckConnectTask extends AsyncTask<Void, Void, Integer> {
    private NetWorkListener listener;
    private WeakReference<Context> mContext;
    public CheckConnectTask(Context context) {
        this.mContext = new WeakReference<Context>(context);
    }
    public void setNetWorkListener(NetWorkListener listener) {

        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Void... params) {
        //state ：-1 代表没有网络，0代可以连接到外网 ，-2代表不能连通到外网
        boolean networkOnline = NetWorkUtils.isNetworkOnline();
        Context context = mContext.get();
        if (mContext.get() != null){
            if (NetWorkUtils.isNetworkConnected(context)){
                if (networkOnline){
                    return 0;
                }else {
                    return -2;
                }
            }else {
                return -1;
            }
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer state) {
        if (listener != null) {
            listener.onSuccess(state);
        }
        super.onPostExecute(state);
    }
}
