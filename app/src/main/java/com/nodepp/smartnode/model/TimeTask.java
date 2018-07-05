package com.nodepp.smartnode.model;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

/**
 * Created by yuyue on 2016/8/10.
 */
@Table(name = "tb_time_task")  // 建议加上注解， 混淆后表名不受影响
public class TimeTask extends EntityBase{
    @Column(column = "deviceId")
    private int deviceId;//设备主键id

    @Column(column = "did")
    private long did = 0;

    @Column(column = "tid")
    private long tid = 0;

    @Column(column = "time")
    private String time;

    @Column(column = "isOpen")
    private boolean isOpen = true;//默认是开启

    @Column(column = "isRepeat")
    private boolean isRepeat = false;//是否每周重复

    @Column(column = "isSunday")
    private boolean isSunday = false;//是否选中周日

    @Column(column = "isMonday")
    private boolean isMonday = false;//是否选中周一

    @Column(column = "isTuesday")
    private boolean isTuesday = false;//是否选中周二

    @Column(column = "isWednesday")
    private boolean isWednesday = false;//是否选中周三

    @Column(column = "isThursday")
    private boolean isThursday = false;//是否选中周四

    @Column(column = "isFriday")
    private boolean isFriday = false;//是否选中周五

    @Column(column = "isSaturday")
    private boolean isSaturday = false;//是否选中周六

    @Column(column = "timeSet")
    private int timeSet;

    @Column(column = "timeRepeaat")
    private int timeRepeaat;

    @Column(column = "timeOperate")
    private int timeOperate;

    @Column(column = "timeStamp0")
    private int timeStamp0;

    @Column(column = "timeStamp1")
    private int timeStamp1;

    @Column(column = "timeStamp2")
    private int timeStamp2;

    @Column(column = "timeStamp3")
    private int timeStamp3;

    @Column(column = "timeStamp4")
    private int timeStamp4;

    @Column(column = "timeStamp5")
    private int timeStamp5;

    @Column(column = "timeStamp6")
    private int timeStamp6;

    @Column(column = "isUse")
    private boolean isUse;//定时任务是否使用

    @Column(column = "operateIndex")
    private int operateIndex;

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setIsRepeat(boolean isRepeat) {
        this.isRepeat = isRepeat;
    }

    public boolean isSunday() {
        return isSunday;
    }

    public void setIsSunday(boolean isSunday) {
        this.isSunday = isSunday;
    }

    public boolean isMonday() {
        return isMonday;
    }

    public void setIsMonday(boolean isMonday) {
        this.isMonday = isMonday;
    }

    public boolean isTuesday() {
        return isTuesday;
    }

    public void setIsTuesday(boolean isTuesday) {
        this.isTuesday = isTuesday;
    }

    public boolean isWednesday() {
        return isWednesday;
    }

    public void setIsWednesday(boolean isWednesday) {
        this.isWednesday = isWednesday;
    }

    public boolean isThursday() {
        return isThursday;
    }

    public void setIsThursday(boolean isThursday) {
        this.isThursday = isThursday;
    }

    public boolean isFriday() {
        return isFriday;
    }

    public void setIsFriday(boolean isFriday) {
        this.isFriday = isFriday;
    }

    public boolean isSaturday() {
        return isSaturday;
    }

    public void setIsSaturday(boolean isSaturday) {
        this.isSaturday = isSaturday;
    }

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

    public int getTimeStamp0() {
        return timeStamp0;
    }

    public void setTimeStamp0(int timeStamp0) {
        this.timeStamp0 = timeStamp0;
    }

    public int getTimeStamp1() {
        return timeStamp1;
    }

    public void setTimeStamp1(int timeStamp1) {
        this.timeStamp1 = timeStamp1;
    }

    public int getTimeStamp2() {
        return timeStamp2;
    }

    public void setTimeStamp2(int timeStamp2) {
        this.timeStamp2 = timeStamp2;
    }

    public int getTimeStamp3() {
        return timeStamp3;
    }

    public void setTimeStamp3(int timeStamp3) {
        this.timeStamp3 = timeStamp3;
    }

    public int getTimeStamp4() {
        return timeStamp4;
    }

    public void setTimeStamp4(int timeStamp4) {
        this.timeStamp4 = timeStamp4;
    }

    public int getTimeStamp5() {
        return timeStamp5;
    }

    public void setTimeStamp5(int timeStamp5) {
        this.timeStamp5 = timeStamp5;
    }

    public int getTimeStamp6() {
        return timeStamp6;
    }

    public void setTimeStamp6(int timeStamp6) {
        this.timeStamp6 = timeStamp6;
    }

    public boolean isUse() {
        return isUse;
    }

    public void setIsUse(boolean isUse) {
        this.isUse = isUse;
    }

    public long getDid() {
        return did;
    }

    public void setDid(long did) {
        this.did = did;
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public int getOperateIndex() {
        return operateIndex;
    }

    public void setOperateIndex(int operateIndex) {
        this.operateIndex = operateIndex;
    }

    @Override
    public String toString() {
        return "TimeTask{" +
                "deviceId=" + deviceId +
                ", did=" + did +
                ", tid=" + tid +
                ", time='" + time + '\'' +
                ", isOpen=" + isOpen +
                ", isRepeat=" + isRepeat +
                ", isSunday=" + isSunday +
                ", isMonday=" + isMonday +
                ", isTuesday=" + isTuesday +
                ", isWednesday=" + isWednesday +
                ", isThursday=" + isThursday +
                ", isFriday=" + isFriday +
                ", isSaturday=" + isSaturday +
                ", timeSet=" + timeSet +
                ", timeRepeaat=" + timeRepeaat +
                ", timeOperate=" + timeOperate +
                ", timeStamp0=" + timeStamp0 +
                ", timeStamp1=" + timeStamp1 +
                ", timeStamp2=" + timeStamp2 +
                ", timeStamp3=" + timeStamp3 +
                ", timeStamp4=" + timeStamp4 +
                ", timeStamp5=" + timeStamp5 +
                ", timeStamp6=" + timeStamp6 +
                ", isUse=" + isUse +
                ", operateIndex=" + operateIndex +
                '}';
    }
}
