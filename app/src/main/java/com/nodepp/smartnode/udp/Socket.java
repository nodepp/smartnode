package com.nodepp.smartnode.udp;

import android.content.Context;

import com.nodepp.smartnode.MyApplication;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.dtls.DTLSSocket;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;

import nodepp.Nodepp;

/**
 * Created by yuyue on 2017/9/19.
 */
public class Socket {
    public static void send(Context context, int connetedMode,String ip, Nodepp.Msg msg,String random, ResponseListener responseListener) {
        if (connetedMode == 0) {
            Log.i("kk", "------send--------");
//                DTLSSocket.send(context,"", msg, responseListener);
            UDPSocketA2S.send(context, msg, responseListener);
        } else {
            if (NetWorkUtils.isWifiConnected(context)) {
                UDPSocket.send(context, ip, msg, random, responseListener);
            } else {
                Log.i("socket", "wifi没连接不能发");
            }
        }
    }
}
