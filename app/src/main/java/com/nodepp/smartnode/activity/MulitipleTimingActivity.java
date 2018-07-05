package com.nodepp.smartnode.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

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
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.utils.Utils;
import com.nodepp.smartnode.view.swipemenulistview.SwipeMenu;
import com.nodepp.smartnode.view.swipemenulistview.SwipeMenuCreator;
import com.nodepp.smartnode.view.swipemenulistview.SwipeMenuItem;
import com.nodepp.smartnode.view.swipemenulistview.SwipeMenuListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import nodepp.Nodepp;
public class MulitipleTimingActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = MulitipleTimingActivity.class.getSimpleName();
    private static int REFRESH = 1;
    private SwipeMenuListView listView;
    private DbUtils dbUtils;
    private List<TimeTask> lists;
    private Timer timer;
    private MulitipleTimingActivity.MyTask myTask;

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
                            if (timeTaskAdapter != null) {
                                timeTaskAdapter.refresh(lists);
                            }
                            Log.i(TAG, "timeTask====" + lists.toString());
                        }
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private TimeTaskAdapter timeTaskAdapter;
    private int operate;
    private ArrayList<String> memoNames;
    private Device deviceModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mulitiple_timing);
        operate = getIntent().getIntExtra("operate", 0);
        deviceModel = (Device) getIntent().getSerializableExtra("device");
        memoNames = getIntent().getStringArrayListExtra("memoName");
        initView();
    }

    private void initView() {
        listView = (SwipeMenuListView) findViewById(R.id.list_view);
        ImageView ivAddTask = (ImageView) findViewById(R.id.iv_add_task);
        ivAddTask.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    @Override
    protected void onDestroy() {
//        stopTimer();
        super.onDestroy();
    }

    /**
     * 发送定时任务
     *
     * @param lists
     */
    private void sendTimeTask(List<TimeTask> lists) {
        if (NetWorkUtils.isNetworkConnected(this)) {
            if (lists != null) {//确认有定时任务才进行操作，lists 为0代表取消所有定时任务
                Log.i(TAG, "list===" + lists.toString());
                long uid = Long.parseLong(Constant.userName);
                final Nodepp.Msg msg = PbDataUtils.setTimeTaskRequestParam(uid, deviceModel.getDid(), deviceModel.getTid(), Constant.usig, lists);
                Log.i(TAG, "msg==" + msg.toString());
                Socket.send(MulitipleTimingActivity.this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg,clientKeys.get(deviceModel.getTid()), new ResponseListener() {
                    @Override
                    public void onSuccess(Nodepp.Msg msg) {
                        int result = msg.getHead().getResult();
                        if (result == 0) {
                            JDJToast.showMessage(MulitipleTimingActivity.this, getString(R.string.send_task_success));
                        } else if (result == 404){
                            JDJToast.showMessage(MulitipleTimingActivity.this, getString(R.string.send_task_fail));
                        }
                        Log.i(TAG, "sendTimeTask==" + msg.toString());
                    }

                    @Override
                    public void onTimeout(Nodepp.Msg msg) {

                    }

                    @Override
                    public void onFaile() {
                        JDJToast.showMessage(MulitipleTimingActivity.this, getString(R.string.send_task_fail));
                    }
                });

            } else {
                JDJToast.showMessage(MulitipleTimingActivity.this, getString(R.string.no_time_task));
            }
        } else {
            JDJToast.showMessage(MulitipleTimingActivity.this, "网络没有连接，请稍后重试");
        }

    }

    private void initData() {
        /**
         * 从数据库读取已经保存的时间任务列表
         */
        dbUtils = DBUtil.getInstance(this);
        try {
            lists = dbUtils.findAll(Selector.from(TimeTask.class).where("deviceId", "=",deviceModel.getId()).orderBy("id", false));
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
            queryTimeTask(lists);
        }
        if (timeTaskAdapter == null) {
            timeTaskAdapter = new TimeTaskAdapter(MulitipleTimingActivity.this, lists);
            listView.setAdapter(timeTaskAdapter);
        } else {
            timeTaskAdapter.refresh(lists);
        }
        if (memoNames != null){
            timeTaskAdapter.setNameLists(memoNames);
        }
        timeTaskAdapter.setOnTimerListener(new TimeTaskAdapter.TimerListener() {
            @Override
            public void open() {

            }

            @Override
            public void close() {

            }
        });
        listView.setMenuCreator(creator);
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                if (lists == null){
                    return;

                }
                if (lists.size() < 1){

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
                    Intent intent = new Intent(MulitipleTimingActivity.this, AddTimedTaskActivity.class);
                    intent.putExtra("id", lists.get(position).getId());
                    intent.putExtra("deviceId", lists.get(position).getDeviceId());
                    intent.putExtra("operate", lists.get(position).getTimeOperate());
                    if (memoNames != null){
                        intent.putStringArrayListExtra("memoNames",memoNames);
                    }else {
                       //白灯
                        intent.putExtra("isOneSwitch", true);
                    }

                    startActivity(intent);
                }
            }
        });
        //任务列表条目开关状态改变时的回调，重新发送定时任务
        timeTaskAdapter.setOnStateChangeListener(new TimeTaskAdapter.StateChangeListener() {
            @Override
            public void onChange() {
                List<TimeTask> timerTasks = new ArrayList<TimeTask>();
                try {
                    lists = dbUtils.findAll(Selector.from(TimeTask.class).where("deviceId", "=", deviceModel.getId()).orderBy("id", false));
                } catch (DbException e) {
                    e.printStackTrace();
                }
               if (lists != null){
                   for (TimeTask timerTask : lists) {
                       if (timerTask.isUse()) {
                           timerTasks.add(timerTask);
                       } else {
                           timerTasks.add(Utils.getTimerTaskCancle());
                       }
                   }
                   sendTimeTask(timerTasks);
               }
            }
        });
    }

    SwipeMenuCreator creator = new SwipeMenuCreator() {
        @Override
        public void create(SwipeMenu menu) {
            SwipeMenuItem openItem = new SwipeMenuItem(MulitipleTimingActivity.this);
            menu.addMenuItem(openItem);
            // create "delete" item
            SwipeMenuItem deleteItem = new SwipeMenuItem(MulitipleTimingActivity.this);
            // set item background
            deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF7,
                    0x53, 0x53)));
            // set item width
            deleteItem.setWidth(Utils.Dp2Px(MulitipleTimingActivity.this, 100));
            // set a icon
            deleteItem.setIcon(R.drawable.ic_delete);
            // add to menu
            menu.addMenuItem(deleteItem);
        }
    };
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_add_task:
                if (lists != null && lists.size() >= 10) {
                    JDJToast.showMessage(MulitipleTimingActivity.this, getString(R.string.time_task_full));
                } else {
                    Intent intent = new Intent(MulitipleTimingActivity.this, AddTimedTaskActivity.class);
                    intent.putExtra("deviceId",deviceModel.getId());
                    intent.putExtra("operate", operate);
                    if (memoNames != null){
                        //多路
                        intent.putStringArrayListExtra("memoNames",memoNames);
                    }else {
                        //白灯
                        intent.putExtra("isOneSwitch", true);
                    }
                    startActivity(intent);
                }
                break;
        }
    }
    private void queryTimeTask(final List<TimeTask> lists){
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
                    if (null != timeTasks){
                        if (timeTasks.size() > 0){
                            Utils.saveOrUpdateTimeList(MulitipleTimingActivity.this,timeTasks,lists,handler);
                        }else if (timeTasks.size() == 0){
                            if (lists != null && lists.size() >0 && timeTaskAdapter != null){
                                for (TimeTask task :lists){
                                    task.setIsUse(false);
                                    try {
                                        DBUtil.getInstance(MulitipleTimingActivity.this).saveOrUpdate(task);
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
            public void onTimeout(Nodepp.Msg msg) {

            }

            @Override
            public void onFaile() {

            }
        });
    }

    //数据更新
    @Override
    protected void netChange(Observable observable, Object data) {
        super.netChange(observable, data);
        deviceModel.setConnetedMode(0);
    }

    private void startTimer() {
        Log.i(TAG, "startTimer");
        if (timer == null) {
            timer = new Timer();
        }
        if (myTask == null) {
            myTask = new MulitipleTimingActivity.MyTask();
        }
        timer.schedule(myTask, 1000, 20000);  //定时器开始，每隔20s执行一次
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
            handler.sendEmptyMessage(REFRESH);
        }
    }
}
