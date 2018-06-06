package com.nodepp.smartnode.observe;

import java.util.Observable;

/**
 * Created by yuyue on 2017/9/15.
 */
public class NetObservable  extends Observable {
    public void notifyNetChange(){
        setChanged();
        notifyObservers();
    }
}
