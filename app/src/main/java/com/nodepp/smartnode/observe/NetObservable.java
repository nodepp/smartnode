package com.nodepp.smartnode.observe;

import java.util.Observable;

/**
 * Created by yuyue on 2017/9/15.
 */
public class NetObservable  extends Observable {
    //通知所有观察者
    public void notifyNetChange(){
        setChanged();
        notifyObservers();
    }
}
