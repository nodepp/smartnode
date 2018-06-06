package com.nodepp.smartnode.observe;

import java.util.Observable;

/**
 * Created by yuyue on 2017/9/15.
 */
public class PushObservable extends Observable {
    public void notifyDataChange(Object data){
        setChanged();
        notifyObservers(data);
    }
}
