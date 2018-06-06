package com.nodepp.smartnode.model;

import java.net.InetAddress;

import nodepp.Nodepp;

/**
 * Created by yuyue on 2017/4/14.
 */
public class UdpSocketParams {
    private Nodepp.Msg message;
    private InetAddress ip;
    private int targetPort = 20000;//20000或者20001

    public Nodepp.Msg getMessage() {
        return message;
    }

    public void setMessage(Nodepp.Msg message) {
        this.message = message;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    @Override
    public String toString() {
        return "UdpSocketParams{" +
                "message=" + message +
                ", ip=" + ip +
                ", targetPort=" + targetPort +
                '}';
    }
}
