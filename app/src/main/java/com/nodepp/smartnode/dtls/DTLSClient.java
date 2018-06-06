package com.nodepp.smartnode.dtls;

import android.content.Context;

import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.Utils;

import nodepp.Nodepp;

/**
 * Created by nodepp on 2018/3/7.
 */

public class DTLSClient {

    private static final String TAG = DTLSClient.class.getSimpleName();
    private static DTLSClient client = null;
    public  static long lastTime = 0;
    private static Context mContext;

    public static DTLSClient getInstance(Context context){
        mContext = context.getApplicationContext();
        if (null == client){
            synchronized (DTLSClient.class) {
                if (null == client ) {
                    client = new DTLSClient();
                }
            }
        }
        return client;
    }

    public void reConnect(){
        Log.i(TAG, "-------reconnect---------" );
        Utils.disConnect();
        Utils.connect(Constant.SERVER_HOST);
    }
    public void close(){
        if (client != null){
            Utils.disConnect();
            client = null;
        }
    }
    public synchronized Nodepp.Msg sendMessage(Nodepp.Msg msg ){
        if (lastTime == 0) {
            lastTime = System.currentTimeMillis();
        }
        long currentTimeMillis = System.currentTimeMillis();
        final long time = currentTimeMillis - lastTime;
        Log.i("kk", "time=" + time);
        if (time > 60000 ) {
            reConnect();
        }
        lastTime = currentTimeMillis;
        Nodepp.Msg resultMsg = null;
        int seq = msg.getHead().getSeq();
        byte[] data = PbDataUtils.packMessage(msg);
        resultMsg = sendAndRead(data, seq);
        if (resultMsg == null){
            resultMsg = sendAndRead(data, seq);//c层重发三次还是失败，结束握手，重新连接重试一次
        }
        close();//只会在登陆和检查用户合法性用到，所以用完就直接关闭
        return resultMsg;
    }
    private Nodepp.Msg sendAndRead(byte[] data,int seq){
        Nodepp.Msg resultMsg = null;
        byte[] recDatas = Utils.send(Constant.SERVER_HOST,data, data.length,1024);//c层超时重试3次，3次之后还是超时就关闭连接，返回null
        if (recDatas != null) {
            resultMsg = PbDataUtils.parserResponse(recDatas, recDatas.length);
            if (resultMsg != null){
                while (resultMsg.getHead().getSeq() < seq){//读掉无效的包
                    recDatas = Utils.receive(Constant.SERVER_HOST,1024);
                    if (recDatas != null){
                        Nodepp.Msg temp = PbDataUtils.parserResponse(recDatas, recDatas.length);
                        if (temp == null){
                            //数据反序列化失败，跳出当前循环
                            continue;
                        }else {
                            resultMsg = temp;
                        }
                        Log.i("kk", "result seq is little" );
                    }else {
                        Log.i("kk", "result seq is little -- null" );
                        return null;
                    }
                }
            }
            return resultMsg;
        }else {
            return null;
        }
    }
}
