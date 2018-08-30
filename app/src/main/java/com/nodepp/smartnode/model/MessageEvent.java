package com.nodepp.smartnode.model;

/**
 * Created by nodepp on 2018/8/30.
 */

public class MessageEvent {
    public String msg;
    public MessageEvent(String msg) {
        this.msg = msg;
    }


    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "MessageEvent{" +
                "msg='" + msg + '\'' +
                '}';
    }
}
