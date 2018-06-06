package com.nodepp.smartnode.model;

import nodepp.Nodepp;

/**
 * Created by yuyue on 2017/5/10.
 */
public class UDPTask {
    String ip;
    Nodepp.Msg msg;
    String random;
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Nodepp.Msg getMsg() {
        return msg;
    }

    public void setMsg(Nodepp.Msg msg) {
        this.msg = msg;
    }

    public String getRandom() {
        return random;
    }

    public void setRandom(String random) {
        this.random = random;
    }
}
