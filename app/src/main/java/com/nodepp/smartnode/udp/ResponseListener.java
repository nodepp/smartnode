package com.nodepp.smartnode.udp;

import nodepp.Nodepp;

/**
 * 接口，用于处理请求网络成功后返回的数据
 * Created by yuyue on 2017/5/10
 */
public interface ResponseListener {
     void onSuccess(Nodepp.Msg msg);
     void onFaile();
     void onTimeout(Nodepp.Msg msg);
}
