package com.nodepp.smartnode.model;

import com.nodepp.smartnode.udp.ResponseListener;

import nodepp.Nodepp;

/**
 * Created by nodepp on 2018/6/4.
 */

public class MessageBean {
    public int tryCount;
    public Nodepp.Msg message;
    public ResponseListener listener;

    public int getTryCount() {
        return tryCount;
    }

    public void setTryCount(int tryCount) {
        this.tryCount = tryCount;
    }

    public Nodepp.Msg getMessage() {
        return message;
    }

    public void setMessage(Nodepp.Msg message) {
        this.message = message;
    }

    public ResponseListener getListener() {
        return listener;
    }

    public void setListener(ResponseListener listener) {
        this.listener = listener;
    }

    @Override
    public String toString() {
        return "MessageBean{" +
                "tryCount=" + tryCount +
                ", message=" + message +
                ", listener=" + listener +
                '}';
    }
}
