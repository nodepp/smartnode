package com.nodepp.smartnode.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.nodepp.smartnode.R;

import java.util.ArrayList;

/**
 * Created by yuyue on 2016/9/29.
 */
public class TimePickerLayout extends LinearLayout {

    private WheelView wheelHour;
    private WheelView wheelMinute;
    private WheelView wheelSecond;

    public TimePickerLayout(Context context) {
        super(context);
    }

    public TimePickerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TimePickerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.time_picker, this);
        wheelHour = (WheelView) findViewById(R.id.hour);
        wheelMinute = (WheelView) findViewById(R.id.minute);
        wheelSecond = (WheelView) findViewById(R.id.second);
        wheelHour.setItems(getHourData());
        wheelMinute.setItems(getMinuteData());
        wheelSecond.setItems(getSecondData());
    }
    /**
     * 初始化滑轮时间控件的时
     * @return
     */
    private ArrayList<String> getHourData() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 24; i++) {
            if (i<10){
                list.add("0"+i);//小于10时进行补位
            }else {
                list.add(String.valueOf(i));
            }
        }
        return list;
    }
    public void setHour(int hour){
        wheelHour.setSeletion(hour);
    }
    public String getHour() {
        if (wheelHour == null) {
            return null;
        }
        return wheelHour.getSeletedItem();
    }
     public void setTimePickerSelecterListener( WheelView.OnWheelViewListener onSelectChangeListener){
         wheelHour.setOnWheelViewListener(onSelectChangeListener);
         wheelMinute.setOnWheelViewListener(onSelectChangeListener);
         wheelSecond.setOnWheelViewListener(onSelectChangeListener);
     }
    /**
     * 初始化滑轮时间控件的分
     * @return
     */

    private ArrayList<String> getMinuteData() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 60; i++) {
            if (i<10){
                list.add("0"+i);//小于10时进行补位
            }else {
                list.add(String.valueOf(i));
            }
        }
        return list;
    }

    public void setMinute(int minute){
        wheelMinute.setSeletion(minute);
    }
    public String getMinute() {
        if (wheelMinute == null) {
            return null;
        }
        return wheelMinute.getSeletedItem();
    }

    /**
     * 初始化滑轮时间控件的秒
     * @return
     */
    private ArrayList<String> getSecondData() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 60; i++) {
            if (i<10){
                list.add("0"+i);//小于10时进行补位
            }else {
                list.add(String.valueOf(i));
            }
        }
        return list;
    }

    public void setSecond(int second){
        wheelSecond.setSeletion(second);
    }
    public String getSecond() {
        if (wheelSecond == null) {
            return null;
        }
        return wheelSecond.getSeletedItem();
    }

}
