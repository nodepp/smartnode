package com.nodepp.smartnode.task;


/**
 * 接口，用于处理请求网络成功后返回的数据
 * Created by yuyue on 2017/5/10
 */
public interface NetWorkListener {
     void onSuccess(int state);//state ：-1 代表没有网络，0代可以连接到外网 ，-2代表不能连通到外网
     void onFaile();
}
