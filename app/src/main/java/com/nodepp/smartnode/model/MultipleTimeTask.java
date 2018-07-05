package com.nodepp.smartnode.model;

import java.util.List;

/**
 * Created by yuyue on 2016/11/8.
 */
public class MultipleTimeTask {
    int timeSet;
    int timeRepeaat;
    int timeOperate;
    int operateIndex;
    boolean timeIsOpen;
    List<Integer> timeStamps;

    public int getTimeSet() {
        return timeSet;
    }

    public void setTimeSet(int timeSet) {
        this.timeSet = timeSet;
    }

    public int getTimeRepeaat() {
        return timeRepeaat;
    }

    public void setTimeRepeaat(int timeRepeaat) {
        this.timeRepeaat = timeRepeaat;
    }

    public int getTimeOperate() {
        return timeOperate;
    }

    public void setTimeOperate(int timeOperate) {
        this.timeOperate = timeOperate;
    }

    public List<Integer> getTimeStamps() {
        return timeStamps;
    }

    public void setTimeStamps(List<Integer> timeStamps) {
        this.timeStamps = timeStamps;
    }

    public boolean isTimeIsOpen() {
        return timeIsOpen;
    }

    public void setTimeIsOpen(boolean timeIsOpen) {
        this.timeIsOpen = timeIsOpen;
    }

    public int getOperateIndex() {
        return operateIndex;
    }

    public void setOperateIndex(int operateIndex) {
        this.operateIndex = operateIndex;
    }

    @Override
    public String toString() {
        return "MultipleTimeTask{" +
                "timeSet=" + timeSet +
                ", timeRepeaat=" + timeRepeaat +
                ", timeOperate=" + timeOperate +
                ", operateIndex=" + operateIndex +
                ", timeIsOpen=" + timeIsOpen +
                ", timeStamps=" + timeStamps +
                '}';
    }
}
