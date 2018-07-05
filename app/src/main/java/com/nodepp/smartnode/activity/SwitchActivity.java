package com.nodepp.smartnode.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.adapter.TimeTaskAdapter;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.model.MultipleTimeTask;
import com.nodepp.smartnode.model.TimeTask;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.Socket;
import com.nodepp.smartnode.utils.ClickUtils;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.DESUtils;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.utils.Utils;
import com.nodepp.smartnode.view.loadingdialog.LoadingDialog;
import com.nodepp.smartnode.view.swipemenulistview.SwipeMenu;
import com.nodepp.smartnode.view.swipemenulistview.SwipeMenuCreator;
import com.nodepp.smartnode.view.swipemenulistview.SwipeMenuItem;
import com.nodepp.smartnode.view.swipemenulistview.SwipeMenuListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import nodepp.Nodepp;

/**
 * 智能插座定时任务总界面
 */
public class SwitchActivity extends BaseVoiceActivity implements View.OnClickListener {
    private static final String TAG = SwitchActivity.class.getSimpleName();
    private static int REFRESH = 1;
    private List<TimeTask> lists;
    private SwipeMenuListView listView;
    private Button btnSwitchOn;
    private Button btnSwitchOff;
    private DbUtils dbUtils;
    private TimeTaskAdapter timeTaskAdapter;
    private TextView tvDebugMsg;
    private LoadingDialog loadingDialog;
    private List<Device> devices;
    private Timer timer;
    private MyTask myTask;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.i("ff", "刷新ui");
                    if (timeTaskAdapter != null) {
                        timeTaskAdapter.notifyDataSetChanged();
                    }
                    break;
                case 2:
                    try {
                        lists = dbUtils.findAll(Selector.from(TimeTask.class).where("deviceId", "=", deviceModel.getId()).orderBy("id", false));
                        if (lists == null) {
                            Log.i(TAG, "timeTask====null");
                        } else {
                            Log.i(TAG, "timeTask====" + lists.toString());
                        }
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                    if (timeTaskAdapter != null) {
                        timeTaskAdapter.refresh(lists);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private boolean isVoice;
    private TextView tvTitle;
    private long lastControlTimeStamp = 0;
    private Device deviceModel;
    private Nodepp.Msg currentMsg = nodepp.Nodepp.Msg.newBuilder().build();//初始化为空
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);
        deviceModel = (Device) getIntent().getSerializableExtra("device");
        isVoice = getIntent().getBooleanExtra("isVoice", false);
        dbUtils = DBUtil.getInstance(this);
        initView();
    }

    private void initData() {
        /**
         * 从数据库读取已经保存的时间任务列表
         */
        try {
            lists = dbUtils.findAll(Selector.from(TimeTask.class).where("deviceId", "=", deviceModel.getId()).orderBy("id", false));
            if (lists == null) {
                Log.i(TAG, "timeTask====null");
            } else {
                Log.i(TAG, "timeTask====" + lists.toString());
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (Constant.isSendTask) {
            List<TimeTask> timerTasks = new ArrayList<TimeTask>();//过滤出有效任务
            if (lists == null || lists.size() < 1) {
                timerTasks.add(Utils.getTimerTaskCancle());
            } else {
                for (TimeTask timerTask : lists) {
                    if (timerTask.isUse()) {
                        timerTasks.add(timerTask);
                    }
                }
            }
            sendTimeTask(timerTasks);
            Constant.isSendTask = false;
        }else {
            //查询定时任务
            queryTimeTask(lists);//无论本地是否有定时任务都进行查询
        }
        if (timeTaskAdapter == null) {
           timeTaskAdapter = new TimeTaskAdapter(SwitchActivity.this, lists);
           listView.setAdapter(timeTaskAdapter);
        } else {
            timeTaskAdapter.refresh(lists);
        }
        listView.setMenuCreator(creator);
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                if (lists == null){
                    return;
                }
                TimeTask task = lists.get(position);
                try {
                    lists.remove(position);
                    dbUtils.delete(task);

                    try {
                        lists = dbUtils.findAll(Selector.from(TimeTask.class).where("deviceId", "=", deviceModel.getId()).orderBy("id", false));
                        if (lists == null) {
                            Log.i(TAG, "timeTask====null");
                        } else {
                            Log.i(TAG, "timeTask====" + lists.toString());
                        }
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                    List<TimeTask> timerTasks = new ArrayList<TimeTask>();
                    if (lists == null || lists.size() < 1) {
                        timerTasks.add(Utils.getTimerTaskCancle());

                    } else {
                        for (TimeTask timerTask : lists) {
                            if (timerTask.isUse()) {
                                timerTasks.add(timerTask);
                            }
                        }
                    }
                    sendTimeTask(timerTasks);//删除任务时重新发送所有的定时任务
                    timeTaskAdapter.refresh(lists);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (lists != null) {
                    Intent intent = new Intent(SwitchActivity.this, AddTimedTaskActivity.class);
                    intent.putExtra("id", lists.get(position).getId());
                    intent.putExtra("deviceId", lists.get(position).getDeviceId());
                    intent.putExtra("isOneSwitch", true);
                    startActivity(intent);
                }
            }
        });
        //任务列表条目开关状态改变时的回调，重新发送定时任务
        timeTaskAdapter.setOnStateChangeListener(new TimeTaskAdapter.StateChangeListener() {
            @Override
            public void onChange() {

                Log.i("vvv","----------------------------");
                List<TimeTask> timerTasks = new ArrayList<TimeTask>();
                try {
                    lists = dbUtils.findAll(Selector.from(TimeTask.class).where("deviceId", "=", deviceModel.getId()).orderBy("id", false));
                } catch (DbException e) {
                    e.printStackTrace();
                }
                if (lists != null){
                    for (TimeTask timerTask : lists) {
                        if (timerTask.isUse()) {//启用的定时任务
                            timerTasks.add(timerTask);
                        }
                    }
                    sendTimeTask(timerTasks);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
        setSocketState();
        startTimer();
        resetDeviceMode();
    }

    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            lastControlTimeStamp = System.currentTimeMillis();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i("aaaa", "按住");
                    if (v.getId() == R.id.btn_switch_off) {
                        btnSwitchOn.setVisibility(View.VISIBLE);
                        controlSocket(1);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (v.getId() == R.id.btn_switch_off) {
                        btnSwitchOn.setVisibility(View.GONE);
                        controlSocket(0);
                    }
                    Log.i("aaaa", "松开");
                    break;
            }
            return true;
        }
    };
    SwipeMenuCreator creator = new SwipeMenuCreator() {
        @Override
        public void create(SwipeMenu menu) {
            SwipeMenuItem openItem = new SwipeMenuItem(SwitchActivity.this);
            menu.addMenuItem(openItem);
            // create "delete" item
            SwipeMenuItem deleteItem = new SwipeMenuItem(SwitchActivity.this);
            // set item background
            deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF7,
                    0x53, 0x53)));
            // set item width
            deleteItem.setWidth(Utils.Dp2Px(SwitchActivity.this, 100));
            // set a icon
            deleteItem.setIcon(R.drawable.ic_delete);
            // add to menu
            menu.addMenuItem(deleteItem);
        }
    };

    private void setDeviceNoOnline(Device device) {
        try {
            if (device != null) {
                device.setIsOnline(false);
                DBUtil.getInstance(SwitchActivity.this).update(device, WhereBuilder.b("userName", "=", Constant.userName).and("did", "=", device.getDid()));
                finish();
            }

        } catch (DbException e1) {
            e1.printStackTrace();
        }
    }

    private void initView() {
        ImageView ivBacke = (ImageView) findViewById(R.id.iv_back);
        ImageView ivMore = (ImageView) findViewById(R.id.iv_more);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        btnSwitchOn = (Button) findViewById(R.id.btn_switch_on);
        btnSwitchOff = (Button) findViewById(R.id.btn_switch_off);
        Button btnVoice = (Button) findViewById(R.id.btn_voice);
        ImageView ivAddTask = (ImageView) findViewById(R.id.iv_add_task);
        listView = (SwipeMenuListView) findViewById(R.id.list_view);
        tvDebugMsg = (TextView) findViewById(R.id.tv_debug_msg);
        String socketName = deviceModel.getSocketName();
        if (socketName != null){
            tvTitle.setText(socketName);
        }
        btnVoice.setOnClickListener(this);
        ivBacke.setOnClickListener(this);
        ivMore.setOnClickListener(this);
        ivAddTask.setOnClickListener(this);
        btnSwitchOn.setOnClickListener(this);
        btnSwitchOff.setOnClickListener(this);
        loadingDialog = new LoadingDialog(this, getString(R.string.send_task_ing));
    }
    /**
     * 发送定时任务
     *
     * @param lists
     */
    private void sendTimeTask(List<TimeTask> lists) {
        if (NetWorkUtils.isNetworkConnected(this)) {
            if (lists != null) {//确认有定时任务才进行操作.lists.size() == 0说明取消所有定时任务
                Log.i(TAG, "list===" + lists.toString());
                String s = SharedPreferencesUtils.getString(this, "uid", "0");
                String uidSig = SharedPreferencesUtils.getString(this, "uidSig", "0");
                Log.i(TAG, "-----加密uid----" + s);
                s = DESUtils.decodeValue(s);
                long uid = Long.parseLong(s);
                final Nodepp.Msg msg = PbDataUtils.setTimeTaskRequestParam(uid, deviceModel.getDid(), deviceModel.getTid(), uidSig, lists);
                Log.i(TAG, "msg==" + msg.toString());
                Socket.send(SwitchActivity.this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
                    @Override
                    public void onSuccess(Nodepp.Msg msg) {
                        if (SharedPreferencesUtils.getBoolean(SwitchActivity.this, "isShowDebugInfo", false)) {
//                            tvDebugMsg.setText(msg.toString());
                        }
                        int result = msg.getHead().getResult();
                        if (result == 0) {
                            JDJToast.showMessage(SwitchActivity.this, getString(R.string.send_task_success));
                        } else {
                            JDJToast.showMessage(SwitchActivity.this, getString(R.string.send_task_fail));
                        }
                        Log.i(TAG, "sendTimeTask==" + msg.toString());
                        loadingDialog.hide();
                    }

                    @Override
                    public void onFaile() {
                        JDJToast.showMessage(SwitchActivity.this, getString(R.string.send_task_fail));
                    }

                    @Override
                    public void onTimeout(Nodepp.Msg msg) {

                    }
                });

            } else {
                JDJToast.showMessage(SwitchActivity.this, getString(R.string.no_time_task));
                loadingDialog.hide();
            }
        } else {
            JDJToast.showMessage(SwitchActivity.this, "网络没有连接，请稍后重试");
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_more:
                Intent intentMore = new Intent(SwitchActivity.this, MoreSettingActivity.class);
                intentMore.putExtra("device", deviceModel);
                startActivityForResult(intentMore,1);
                break;
            case R.id.iv_add_task:
                addTimerTask();
                break;
            case R.id.btn_switch_on://显示开灯，点击后关灯
                lastControlTimeStamp = System.currentTimeMillis();
                if (ClickUtils.isFastClick(100)) {//防止用户一直快速点击
                    JDJToast.showMessage(this, getString(R.string.no_click_quick));
                } else {
                    controlSocket(0);
                }
//                btnSwitchOff.setVisibility(View.VISIBLE);
//                btnSwitchOn.setVisibility(View.GONE);

                break;
            case R.id.btn_switch_off:
                lastControlTimeStamp = System.currentTimeMillis();
//                btnSwitchOn.setVisibility(View.VISIBLE);
//                btnSwitchOff.setVisibility(View.GONE);
                if (ClickUtils.isFastClick(100)) {
                    JDJToast.showMessage(this, getString(R.string.no_click_quick));
                } else {
                    controlSocket(1);
                }
                break;
            case R.id.btn_voice:
                showVoiceDialog();
                break;
        }
    }
    private void addTimerTask(){
        if (lists != null && lists.size() >= 10) {
            JDJToast.showMessage(SwitchActivity.this, getString(R.string.time_task_full));
        } else {
            Intent intent = new Intent(SwitchActivity.this, AddTimedTaskActivity.class);
            intent.putExtra("deviceId", deviceModel.getId());
            intent.putExtra("isOneSwitch", true);
            startActivity(intent);
        }
    }
    /**
     * 查询插座的状态，然后进行设置
     */
    private void setSocketState() {
        if (NetWorkUtils.isNetworkConnected(this)) {
            String s = SharedPreferencesUtils.getString(this, "uid", "0");
            String uidSig = SharedPreferencesUtils.getString(this, "uidSig", "0");
            Log.i(TAG, "-----加密uid----" + s);
            s = DESUtils.decodeValue(s);
            if (s != null) {
                long uid = Long.parseLong(s);
                final Nodepp.Msg msg = PbDataUtils.setQueryStateRequestParam(uid, deviceModel.getDid(), deviceModel.getTid(), uidSig);
                Socket.send(SwitchActivity.this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
                    @Override
                    public void onSuccess(Nodepp.Msg msg) {
                        Log.i("kk", "onSuccess-msg-" + msg.toString());
                        if (SharedPreferencesUtils.getBoolean(SwitchActivity.this, "isShowDebugInfo", false)) {
                            if (msg != null) {
//                                tvDebugMsg.setText(msg.toString());
                            }
                        }
                        int result = msg.getHead().getResult();
                        if (result == 404) {
//                            setDeviceNoOnline(deviceModel);
                            JDJToast.showMessage(SwitchActivity.this, getString(R.string.device_is_not_online));
                        }else if (result == 0){
                            int state = msg.getState();
                            int deviceMode = msg.getDeviceMode();
                            if (deviceMode != 1){//点动模式的时候不设置state，其他模式才进行设置
                                changeState(msg.getState());
                            }
                            if (deviceModel.getDeviceMode() != deviceMode){
                                deviceModel.setDeviceMode(deviceMode);
                                resetDeviceMode();
                                Log.i(TAG, "deviceMode is change");
                            }
                        }

                    }

                    @Override
                    public void onFaile() {

                    }

                    @Override
                    public void onTimeout(Nodepp.Msg msg) {
                       if (msg.getHead().getSeq() < currentMsg.getHead().getSeq()){
                           return;
                       }
                        //设置为上一次状态,查询的包没回来
                        if (currentMsg != null){
                            int state = currentMsg.getState();
                            changeState(state);
                        }
                    }
                });
            } else {
                JDJToast.showMessage(SwitchActivity.this, getString(R.string.no_uid));
            }
        } else {
            JDJToast.showMessage(SwitchActivity.this, "网络没有连接，请稍后重试");
        }
    }

    private void resetDeviceMode() {
        if (deviceModel.getDeviceMode() == 1) {//点动
            btnSwitchOn.setVisibility(View.GONE);
            btnSwitchOff.setVisibility(View.VISIBLE);
            btnSwitchOff.setOnTouchListener(onTouchListener);
        } else {//自锁，互锁
            btnSwitchOff.setOnTouchListener(null);
        }
        if (isVoice){
            showVoiceDialog();
            isVoice = false;
        }
    }

    /**
     * 控制插座的方法
     *
     * @param operate
     */
    private void controlSocket(int operate) {
        String s = SharedPreferencesUtils.getString(this, "uid", "0");
        String uidSig = SharedPreferencesUtils.getString(this, "uidSig", "0");
        Log.i(TAG, "-----加密uid----" + s);
        s = DESUtils.decodeValue(s);
        if (s != null) {
            long uid = Long.parseLong(s);
            final Nodepp.Msg msg = PbDataUtils.setRequestParam(16, 1, uid, deviceModel.getDid(), deviceModel.getTid(), operate, uidSig);
            Socket.send(SwitchActivity.this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
                @Override
                public void onSuccess(Nodepp.Msg msg) {
                    if (SharedPreferencesUtils.getBoolean(SwitchActivity.this, "isShowDebugInfo", false)) {
                        if (msg != null) {
//                            tvDebugMsg.setText(msg.toString());
                        }
                    }
                    int result = msg.getHead().getResult();
                    if (result == 404) {
//                        setDeviceNoOnline(deviceModel);
                        JDJToast.showMessage(SwitchActivity.this, getString(R.string.device_is_not_online));
                    }else if (result == 0){
                        Log.i("kk", "controlSocket=msg=" + msg.toString());
                        if (isBigSeqMessage(msg)){
                            changeState(msg.getState());
                        }

                    }

                }

                @Override
                public void onFaile() {

                }
                @Override
                public void onTimeout(Nodepp.Msg msg) {
                    if (msg.getHead().getSeq() < currentMsg.getHead().getSeq()){
                        return;//超时的包seq如果小于当前记录的，不重置界面
                    }
                    JDJToast.showMessage(SwitchActivity.this, getString(R.string.net_timeout));
                }
            });
        }
    }

    private void changeState(int state) {
        if (state == 0) {
            btnSwitchOff.setVisibility(View.VISIBLE);
            btnSwitchOn.setVisibility(View.GONE);
        } else if (state == 1) {
            btnSwitchOn.setVisibility(View.VISIBLE);
            btnSwitchOff.setVisibility(View.GONE);
        }
    }

    //判断当前接收到的seq是不是比上一次的message大
    private boolean isBigSeqMessage(Nodepp.Msg receiveMsg){
        if (currentMsg == null){
            currentMsg = receiveMsg;
            return true;
        }else {
            if (currentMsg.getHead().getSeq() < receiveMsg.getHead().getSeq()){
                currentMsg = receiveMsg;
                return true;
            }else {
                return false;
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void startTimer() {
        Log.i(TAG, "startTimer");
        if (timer == null) {
            timer = new Timer();
        }
        if (myTask == null) {
            myTask = new MyTask();
        }
        timer.schedule(myTask, 1000, 3000);  //定时器开始，每隔3s执行一次
    }

    private void stopTimer() {
        Log.i(TAG, "stopTimer");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (myTask != null) {
            myTask.cancel();
            myTask = null;
        }
    }

    class MyTask extends TimerTask {
        @Override
        public void run() {
            Log.i(TAG, "执行");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    long currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis - lastControlTimeStamp > 2000){//距离最后一次控制的时间大于3s才进行状态查询
                        setSocketState();
                    }else {
                        Log.i(TAG,"---------控制不执行-------------");
                    }
                }
            });
        }
    }

    private void queryTimeTask(final List<TimeTask> lists) {
        long uid = Long.parseLong(Constant.userName);
        Nodepp.Msg msg = PbDataUtils.setQueryTimeTaskRequestParam(uid, deviceModel.getDid(), deviceModel.getTid(), Constant.usig);
        Log.i(TAG, "msg==" + msg.toString());
        Socket.send(this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
            @Override
            public void onSuccess(Nodepp.Msg msg) {
                Log.i(TAG, "queryTimeTask==msg==" + msg.toString());
                int result = msg.getHead().getResult();
                if (result == 0) {
                    List<TimeTask> timeTasks = Utils.convertTimerTask(msg, deviceModel.getId(), deviceModel.getDid(), deviceModel.getTid());
                    Log.i("aaa", "timeTasks==" + timeTasks.toString());
                    if (null != timeTasks){
                        if (timeTasks.size() > 0){
                            Utils.saveOrUpdateTimeList(SwitchActivity.this,timeTasks,lists,handler);
                        }else {
                            if (lists != null&&lists.size() >0 && timeTaskAdapter != null){
                                for (TimeTask task :lists){
                                    task.setIsUse(false);
                                    try {
                                        DBUtil.getInstance(SwitchActivity.this).saveOrUpdate(task);
                                    } catch (DbException e) {
                                        e.printStackTrace();
                                    }
                                }

                                //刷新定时任务
                                timeTaskAdapter.refresh(lists);

                            }
                        }
                    }
                } else if (result == 404){

                }
            }

            @Override
            public void onFaile() {

            }

            @Override
            public void onTimeout(Nodepp.Msg msg) {

            }
        });
    }

    //网络变化时，数据更新
    @Override
    protected void netChange(Observable observable, Object data) {
        deviceModel.setConnetedMode(0);//网络变化临时先切换到互联网模式
    }
    @Override
    public void voiceControl(String result) {
         Log.i("aaa","result=dan="+result);
         if (result.contains("开启")||result.contains("打开")){
             controlSocket(1);
             btnSwitchOff.setVisibility(View.GONE);
             btnSwitchOn.setVisibility(View.VISIBLE);
         }
         if (result.contains("关闭")){
             controlSocket(0);
             btnSwitchOff.setVisibility(View.VISIBLE);
             btnSwitchOn.setVisibility(View.GONE);
         }
         if (result.contains("定时")){
             cancleVoice();
             addTimerTask();
         }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Device device = (Device)data.getSerializableExtra("device");
        Log.i("hh","requestCode is "+requestCode);
        Log.i("hh","resultCode is "+resultCode);
        if (device == null){
            Log.i("hh","result device is null");
        }else {
            Log.i("hh","result device is "+device.toString());
        }

        if (requestCode == 1){
            if (resultCode == 2){
                deviceModel = device;
                Log.i("hh","deviceModel is "+deviceModel.toString());
            }
        }
    }
}
