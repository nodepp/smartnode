package com.nodepp.smartnode.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.model.TimeTask;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.Utils;
import com.nodepp.smartnode.view.TimePickerLayout;
import com.nodepp.smartnode.view.TitleBar;
import com.nodepp.smartnode.view.WheelView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

/**
 * 智能插座定时任务详细界面
 */
public class AddTimedTaskActivity extends BaseActivity {
    private static final String TAG = AddTimedTaskActivity.class.getSimpleName();
    private TimePickerLayout timePicker;
    private TextView tvTime;//时间
    private TimeTask timeTask;
    private CheckBox cbOpen;
    private CheckBox cbClose;
    private CheckBox cbRepeat;
    private int id;
    private int deviceId;
    private CheckBox cbSunday;
    private CheckBox cbMonday;
    private CheckBox cbTuesday;
    private CheckBox cbWednesday;
    private CheckBox cbThursday;
    private CheckBox cbFriday;
    private CheckBox cbSaturday;
    private DbUtils dbUtils;
    private int weekDay;
    private LinearLayout llSunday;
    private LinearLayout llMonday;
    private LinearLayout llTuesday;
    private LinearLayout llWednesday;
    private LinearLayout llThursday;
    private LinearLayout llFriday;
    private LinearLayout llSaturday;
    private boolean isTodayAndLitlle = false;
    private long tid;
    private int operate;
    private int operateIndex;
    private boolean isOneSwitch;
    private ArrayList<String> memoNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timed_task);
        id = getIntent().getIntExtra("id", 0);//定时任务的id
        operate = getIntent().getIntExtra("operate", 0);
        Log.i("aa", "AddTimedTaskActivity=operate=" + operate);
        deviceId = getIntent().getIntExtra("deviceId", 0);//设备的id
        tid = getIntent().getIntExtra("tid", 0);
        isOneSwitch = getIntent().getBooleanExtra("isOneSwitch", false);
        memoNames = getIntent().getStringArrayListExtra("memoNames");
        if (memoNames != null) {
            Log.i("jj", "AddTimedTaskActivity==" + memoNames.toString());
        }
        dbUtils = DBUtil.getInstance(AddTimedTaskActivity.this);
        initView();
        initData();
    }

    private void initData() {
        weekDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) - 1;//减1之后周日是0，周一是1
        if (id != 0) {
            try {
                TimeTask task = dbUtils.findById(TimeTask.class, id);
                if (task != null) {//显示当前的定时任务信息
                    tvTime.setText(task.getTime());
                    switchTime(task.isOpen());
                    cbRepeat.setChecked(task.isRepeat());
                    cbSunday.setChecked(task.isSunday());
                    cbMonday.setChecked(task.isMonday());
                    cbTuesday.setChecked(task.isTuesday());
                    cbWednesday.setChecked(task.isWednesday());
                    cbThursday.setChecked(task.isThursday());
                    cbFriday.setChecked(task.isFriday());
                    cbSaturday.setChecked(task.isSaturday());
                    operateIndex = task.getOperateIndex();
                    operate = task.getTimeOperate();
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        } else {//新增的定时任务,默认定时是今天
            setCheckBoxState();
        }
        setCurrenttiem(tvTime.getText().toString());
        timePicker.setTimePickerSelecterListener(new WheelView.OnWheelViewListener(
        ) {
            @Override
            public void onSelected(int selectedIndex, String item) {
                if (!(timePicker.getHour().equals("") || timePicker.getMinute().equals("")))
                    tvTime.setText(timePicker.getHour() + ":" + timePicker.getMinute()+":" + timePicker.getSecond());
            }
        });
    }

    //新增的定时任务,默认选中当天
    private void setCheckBoxState() {
        switch (weekDay) {
            case 0:
                cbSunday.setChecked(true);
                timeTask.setIsSunday(true);
                break;
            case 1:
                cbMonday.setChecked(true);
                timeTask.setIsMonday(true);
                break;
            case 2:
                cbTuesday.setChecked(true);
                timeTask.setIsTuesday(true);
                break;
            case 3:
                cbWednesday.setChecked(true);
                timeTask.setIsWednesday(true);
                break;
            case 4:
                cbThursday.setChecked(true);
                timeTask.setIsThursday(true);
                break;
            case 5:
                cbFriday.setChecked(true);
                timeTask.setIsFriday(true);
                break;
            case 6:
                cbSaturday.setChecked(true);
                timeTask.setIsSaturday(true);
                break;

        }
    }

    //初始化
    private void initView() {
        timeTask = new TimeTask();
        initTitleBar();
        tvTime = (TextView) findViewById(R.id.tv_time);
        TextView tvOpen = (TextView) findViewById(R.id.tv_open);
        TextView tvClose = (TextView) findViewById(R.id.tv_close);
        timePicker = (TimePickerLayout) findViewById(R.id.time_picker);
        cbOpen = (CheckBox) findViewById(R.id.cb_open);
        cbClose = (CheckBox) findViewById(R.id.cb_close);
        cbRepeat = (CheckBox) findViewById(R.id.cb_repeat);
        LinearLayout llRepeat = (LinearLayout) findViewById(R.id.ll_repeat);
        cbSunday = (CheckBox) findViewById(R.id.cb_Sunday);
        cbMonday = (CheckBox) findViewById(R.id.cb_Monday);
        cbTuesday = (CheckBox) findViewById(R.id.cb_Tuesday);
        cbWednesday = (CheckBox) findViewById(R.id.cb_Wednesday);
        cbThursday = (CheckBox) findViewById(R.id.cb_Thursday);
        cbFriday = (CheckBox) findViewById(R.id.cb_Friday);
        cbSaturday = (CheckBox) findViewById(R.id.cb_Saturday);
        llSunday = (LinearLayout) findViewById(R.id.ll_Sunday);
        llMonday = (LinearLayout) findViewById(R.id.ll_Monday);
        llTuesday = (LinearLayout) findViewById(R.id.ll_Tuesday);
        llWednesday = (LinearLayout) findViewById(R.id.ll_Wednesday);
        llThursday = (LinearLayout) findViewById(R.id.ll_Thursday);
        llFriday = (LinearLayout) findViewById(R.id.ll_Friday);
        llSaturday = (LinearLayout) findViewById(R.id.ll_Saturday);
        llSunday.setOnClickListener(onClickListener);
        llMonday.setOnClickListener(onClickListener);
        llTuesday.setOnClickListener(onClickListener);
        llWednesday.setOnClickListener(onClickListener);
        llThursday.setOnClickListener(onClickListener);
        llFriday.setOnClickListener(onClickListener);
        llSaturday.setOnClickListener(onClickListener);
        TextView tvCancle = (TextView) findViewById(R.id.tv_cancle);
        TextView tvOK = (TextView) findViewById(R.id.tv_ok);
        tvCancle.setOnClickListener(OnChangeListener);
        tvOK.setOnClickListener(OnChangeListener);
        cbRepeat.setOnCheckedChangeListener(onCheckedChangeListener);
        cbSunday.setOnCheckedChangeListener(onCheckedChangeListener);
        cbMonday.setOnCheckedChangeListener(onCheckedChangeListener);
        cbTuesday.setOnCheckedChangeListener(onCheckedChangeListener);
        cbWednesday.setOnCheckedChangeListener(onCheckedChangeListener);
        cbThursday.setOnCheckedChangeListener(onCheckedChangeListener);
        cbFriday.setOnCheckedChangeListener(onCheckedChangeListener);
        cbSaturday.setOnCheckedChangeListener(onCheckedChangeListener);
        llRepeat.setOnClickListener(OnChangeListener);
        tvOpen.setOnClickListener(OnChangeListener);
        tvClose.setOnClickListener(OnChangeListener);
        cbOpen.setOnClickListener(OnChangeListener);
        cbClose.setOnClickListener(OnChangeListener);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String currentTime = dateFormat.format(date);
        tvTime.setText(currentTime);
    }

    private void initTitleBar() {
        TitleBar titleBar = (TitleBar) findViewById(R.id.title_bar);
        titleBar.setRightVisible(TitleBar.TEXT);
        titleBar.setRightClickListener(new TitleBar.RightClickListener() {
            @Override
            public void onClick() {
                if (cbOpen.isChecked() || cbClose.isChecked()){
                    setTimeTask();
                    saveTimeTask();
                }else {
                    JDJToast.showMessage(AddTimedTaskActivity.this,"请选择开启或者关闭选项");
                }
            }
        });
    }

    //增加点击范围
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ll_Sunday:
                    timeTask.setIsSunday(!cbSunday.isChecked());
                    cbSunday.setChecked(!cbSunday.isChecked());
                    break;
                case R.id.ll_Monday:
                    timeTask.setIsMonday(!cbMonday.isChecked());
                    cbMonday.setChecked(!cbMonday.isChecked());
                    break;
                case R.id.ll_Tuesday:
                    timeTask.setIsTuesday(!cbTuesday.isChecked());
                    cbTuesday.setChecked(!cbTuesday.isChecked());
                    break;
                case R.id.ll_Wednesday:
                    timeTask.setIsWednesday(!cbWednesday.isChecked());
                    cbWednesday.setChecked(!cbWednesday.isChecked());
                    break;
                case R.id.ll_Thursday:
                    timeTask.setIsThursday(!cbThursday.isChecked());
                    cbThursday.setChecked(!cbThursday.isChecked());
                    break;
                case R.id.ll_Friday:
                    timeTask.setIsFriday(!cbFriday.isChecked());
                    cbFriday.setChecked(!cbFriday.isChecked());
                    break;
                case R.id.ll_Saturday:
                    timeTask.setIsSaturday(!cbSaturday.isChecked());
                    cbSaturday.setChecked(!cbSaturday.isChecked());
                    break;
            }
        }
    };
    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.cb_repeat:
                    timeTask.setIsRepeat(isChecked);
                    break;
                case R.id.cb_Sunday:
                    timeTask.setIsSunday(isChecked);
                    break;
                case R.id.cb_Monday:
                    timeTask.setIsMonday(isChecked);
                    break;
                case R.id.cb_Tuesday:
                    timeTask.setIsTuesday(isChecked);
                    break;
                case R.id.cb_Wednesday:
                    timeTask.setIsWednesday(isChecked);
                    break;
                case R.id.cb_Thursday:
                    timeTask.setIsThursday(isChecked);
                    break;
                case R.id.cb_Friday:
                    timeTask.setIsFriday(isChecked);
                    break;
                case R.id.cb_Saturday:
                    timeTask.setIsSaturday(isChecked);
                    break;
            }
        }
    };
    View.OnClickListener OnChangeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cb_open:
                case R.id.tv_open:
                    switchTime(true);//开启
                    break;
                case R.id.cb_close:
                case R.id.tv_close:
                    switchTime(false);//关闭
                    break;
                case R.id.ll_repeat:
                    timeTask.setIsRepeat(!cbRepeat.isChecked());
                    cbRepeat.setChecked(!cbRepeat.isChecked());
                    break;
            }
        }
    };

    //是否开启
    private void switchTime(boolean b) {
        timeTask.setIsOpen(b);//true代表开启
        cbOpen.setChecked(b);
        cbClose.setChecked(!b);
        if (!isOneSwitch){
            showSelectDialog(b);//多路才显示提示多通道
        }
    }

    //将设置好的时间保存到数据库
    private void saveTimeTask() {
        timeTask.setTime(tvTime.getText().toString());
//        if (!isOneSwitch){
//            timeTask.setTimeOperate(operate);
//        }
        Log.i(TAG, "operate----" + operate);
        try {
            if (id == 0) {//新增的定时任务
                timeTask.setDeviceId(deviceId);
                timeTask.setTid(tid);
                timeTask.setIsUse(true);
                dbUtils.saveBindingId(timeTask);
            } else {//修改定时任务
                timeTask.setDeviceId(deviceId);
                timeTask.setIsUse(true);
                dbUtils.update(timeTask, WhereBuilder.b("id", "=", id));
            }
        } catch (DbException e) {
            e.printStackTrace();
        }finally {
            Constant.isSendTask = true;
            finish();
        }
    }

    private void setTimeTask() {
        //若用户什么时间都不选，默认保存的是今天
        if (!timeTask.isSunday() && !timeTask.isMonday() && !timeTask.isTuesday() && !timeTask.isWednesday() && !timeTask.isThursday() && !timeTask.isFriday() && !timeTask.isSaturday()) {
            switch (weekDay) {
                case 0:
                    timeTask.setIsSunday(true);
                    break;
                case 1:
                    timeTask.setIsMonday(true);
                    break;
                case 2:
                    timeTask.setIsTuesday(true);
                    break;
                case 3:
                    timeTask.setIsWednesday(true);
                    break;
                case 4:
                    timeTask.setIsThursday(true);
                    break;
                case 5:
                    timeTask.setIsFriday(true);
                    break;
                case 6:
                    timeTask.setIsSaturday(true);
                    break;
            }
        }
        //设置定时的所有时间戳
        int timeSet = Utils.getValue(timeTask.isSunday(), timeTask.isMonday(), timeTask.isTuesday(), timeTask.isWednesday(), timeTask.isThursday(), timeTask.isFriday(), timeTask.isSaturday());
        int timeRepeaat = timeTask.isRepeat() ? timeSet : 0;
        int timeOperate;
        if (isOneSwitch) {
            timeOperate = timeTask.isOpen() ? 1 : 0;
            operateIndex = 1;
        } else {
            timeOperate = timeTask.isOpen() ? operate : 0;
        }
        Log.i("aa", "AddTimedTaskActivity=timeOperate=" + timeOperate);
        int time = changeToTimeStamp(tvTime.getText().toString());//当前设置定时的时间戳
        LinkedList<Integer> timeStamp = getTimeStamp(time);//整个星期的时间戳
        timeTask.setTimeSet(timeSet);
        timeTask.setOperateIndex(operateIndex);
        timeTask.setTimeRepeaat(timeRepeaat);
        timeTask.setTimeOperate(timeOperate);
        timeTask.setTimeStamp0(timeStamp.get(0));//保存timeStamp0的值
        timeTask.setTimeStamp1(timeStamp.get(1));
        timeTask.setTimeStamp2(timeStamp.get(2));
        timeTask.setTimeStamp3(timeStamp.get(3));
        timeTask.setTimeStamp4(timeStamp.get(4));
        timeTask.setTimeStamp5(timeStamp.get(5));
        timeTask.setTimeStamp6(timeStamp.get(6));
    }

    /**
     * 获取周日，周一到周六的时间戳
     *
     * @param time
     * @return
     */
    private LinkedList<Integer> getTimeStamp(int time) {
        LinkedList<Integer> timeStamp = new LinkedList<Integer>();
        LinkedList<Boolean> weekLists = new LinkedList<Boolean>();
        for (int i = 0; i < 7; i++) {
            timeStamp.add(0);
        }
        weekLists.add(0, timeTask.isSunday() ? true : false);
        weekLists.add(1, timeTask.isMonday() ? true : false);
        weekLists.add(2, timeTask.isTuesday() ? true : false);
        weekLists.add(3, timeTask.isWednesday() ? true : false);
        weekLists.add(4, timeTask.isThursday() ? true : false);
        weekLists.add(5, timeTask.isFriday() ? true : false);
        weekLists.add(6, timeTask.isSaturday() ? true : false);
        for (int i = 0 + weekDay; i < 7 + weekDay; i++) {//时间是基于当前的时间，weekDay表示今天
            if (isTodayAndLitlle) {
                //日期是今天但是小于当前的时间就是下个星期的今天的定时任务
                timeStamp.set(i % 7, weekLists.get(i % 7) ? time + 24 * 60 * 60 * (i - weekDay + 7) : 0);
                isTodayAndLitlle = false;
            } else {
                //日期是此时系统世间之后的世间
                timeStamp.set(i % 7, weekLists.get(i % 7) ? time + 24 * 60 * 60 * (i - weekDay) : 0);
            }
        }
        Log.i(TAG, "timeStamp == " + timeStamp.toString());
        return timeStamp;
    }

    /**
     * 获取滑轮定时的时间和当前时间的差值，返回的是秒
     *
     * @param s
     * @return
     */
    private int changeToTimeStamp(String s) {
        LinkedList<Boolean> weekLists = new LinkedList<Boolean>();
        weekLists.add(0, timeTask.isSunday() ? true : false);
        weekLists.add(1, timeTask.isMonday() ? true : false);
        weekLists.add(2, timeTask.isTuesday() ? true : false);
        weekLists.add(3, timeTask.isWednesday() ? true : false);
        weekLists.add(4, timeTask.isThursday() ? true : false);
        weekLists.add(5, timeTask.isFriday() ? true : false);
        weekLists.add(6, timeTask.isSaturday() ? true : false);
        Date date = new Date();
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = format.format(date);
        String currentTimeStr = String.format("%s %s",dateStr,s);
        //Date或者String转化为时间戳
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
             date = dateFormat.parse(currentTimeStr);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(date == null){
            return 0;
        }else {
            int time = (int) (date.getTime()/1000);
            int now = (int) (System.currentTimeMillis() / 1000);
            if (time < now) {
                if (weekLists.get(weekDay)) {
                    //时间是今天而且小于当前就是下个星期
                    isTodayAndLitlle = true;
                }
            }
            return time;
        }
    }

    /**
     * 对时间 hh:mm 进行分割，设置到滑轮时间选择器中
     *
     * @param s
     */
    private void setCurrenttiem(String s) {
        String[] ss = s.split(":");
        if (ss != null && ss.length > 1) {
            int hour = Integer.parseInt(ss[0]);
            int minute = Integer.parseInt(ss[1]);
            timePicker.setHour(hour);
            timePicker.setMinute(minute);
            if (ss.length >2){
                int second = Integer.parseInt(ss[2]);
                timePicker.setSecond(second);
            }
        }
    }
    private boolean [] getDefaultState(int operate,boolean isOpen){
        Log.i("kk", "operate==" + operate);
        boolean isOne = (operate & 1)==0?false:true;
        boolean isTwo = (operate & 2)==0?false:true;
        boolean isThree = (operate & 4)==0?false:true;
        boolean isFour = (operate & 8)==0?false:true;
        boolean isFive = (operate & 16)==0?false:true;
        boolean isSix = (operate & 32)==0?false:true;
        boolean isSeven = (operate & 64)==0?false:true;
        boolean isEight = (operate & 128)==0?false:true;
        Log.i("kk", "isOne==" + isOne);
        Log.i("kk", "isTwo==" + isTwo);
        Log.i("kk", "isThree==" + isThree);
        Log.i("kk", "isFour==" + isFour);
        Log.i("kk", "isFive==" + isFive);
        Log.i("kk", "isSix==" + isSix);
        Log.i("kk", "isSeven==" + isSeven);
        Log.i("kk", "isEight==" + isEight);
        if (isOpen){
            boolean[] checkedItems = {isOne,isTwo,isThree,isFour,isFive,isSix,isSeven,isEight};
            return checkedItems;
        }else {
            boolean[] checkedItems = {!isOne,!isTwo,!isThree,!isFour,!isFive,!isSix,!isSeven,!isEight};
            return checkedItems;
        }
    }
    private void showSelectDialog(final boolean isOpen) {
        operateIndex = 0;
        operate = 0;
        String[] items = {"开关1", "开关2", "开关3", "开关4", "开关5", "开关6","开关7", "开关8"};
        if (memoNames != null) {
            items = memoNames.toArray(new String[memoNames.size()]);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(AddTimedTaskActivity.this);
        if (isOpen) {
            builder.setTitle("请选择要开启的通道");
        } else {
            builder.setTitle("请选择要关闭的通道");
        }
//        boolean[] checkedItems = getDefaultState(operate,isOpen);
//        boolean[]checkedItems = {true ,false,true,true,true ,false,true,true};
        builder.setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                Log.i("kk", "which==" + which);
                Log.i("kk", "isChecked==" + isChecked);
                if (isChecked) {
                    operateIndex |=1 << which;
                }else{
                    operateIndex &=~(1 << which);
                }
                if (isOpen){
                    if (isChecked) {
                        operate |=1 << which;
                    }else{
                        operate &=~(1 << which) ;
                    }
                }else {
                    if (isChecked) {
                        operate &=~(1 << which) ;
                    }else{
                        operate |=1 << which;
                    }
                }

            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("kk", "operate------------" + operate);
                Log.i("kk", "operateIndex------------" + operateIndex);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
