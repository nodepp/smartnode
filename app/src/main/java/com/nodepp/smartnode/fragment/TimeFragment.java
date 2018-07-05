package com.nodepp.smartnode.fragment;

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
import com.nodepp.smartnode.activity.AddTimedTaskActivity;
import com.nodepp.smartnode.activity.ColorControlActivity;
import com.nodepp.smartnode.activity.MulitipleTimingActivity;
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
import com.nodepp.smartnode.view.loadingdialog.LoadingDialog;
import com.nodepp.smartnode.view.swipemenulistview.SwipeMenu;
import com.nodepp.smartnode.view.swipemenulistview.SwipeMenuCreator;
import com.nodepp.smartnode.view.swipemenulistview.SwipeMenuItem;
import com.nodepp.smartnode.view.swipemenulistview.SwipeMenuListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import nodepp.Nodepp;

/**
 * Created by yuyue on 2017/4/6.
 */
public class TimeFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = TimeFragment.class.getSimpleName();
    public static final String CHANGE_MENU_BUTTON_TEXT = TAG + "changeMenuButtonText";
    private static int REFRESH = 1;
    private SwipeMenuListView listView;
    private TimeTaskAdapter timeTaskAdapter;
    private DbUtils dbUtils;
    private List<TimeTask> lists;
    private LoadingDialog loadingDialog;
    private ArrayList<Long> deviceGroupTids = new ArrayList<Long>();
    private ArrayList<Long> deviceGroupDids = new ArrayList<Long>();
    private ArrayList<String> deviceGroupIps = new ArrayList<String>();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.i(TAG, "刷新ui");
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
    private String random;
    private Device deviceModel;


    @Override
    public void voiceControl(String msg) {

    }

    @Override
    public View initView() {
        View view = View.inflate(mActivity, R.layout.fragment_time, null);
        listView = (SwipeMenuListView) view.findViewById(R.id.list_view);
        loadingDialog = new LoadingDialog(getActivity(), getString(R.string.send_task_ing));
        ImageView ivAddTask = (ImageView) view.findViewById(R.id.iv_add_task);
        ivAddTask.setOnClickListener(this);
        mActivity = (ColorControlActivity) getActivity();
        return view;
    }

    @Override
    public void initData() {
        Bundle bundle = getArguments();
        random = bundle.getString("random");
        deviceModel = (Device) bundle.getSerializable("device");
        String[] tids = deviceModel.getDeviceGroupTids().split(";");
        String[] dids = deviceModel.getDeviceGroupDids().split(";");
        String[] Ips = deviceModel.getDeviceIps().split(";");
        if (tids != null) {
            for (int i = 0; i < tids.length; i++) {
                String tid = tids[i];
                if (!tid.equals("")) {
                    deviceGroupTids.add(Long.parseLong(tid));
                }
            }
        }
        if (dids != null) {
            for (int i = 0; i < dids.length; i++) {
                String did = dids[i];
                if (!did.equals("")) {
                    deviceGroupDids.add(Long.parseLong(did));
                }
            }
        }
        if (Ips != null) {
            for (int i = 0; i < Ips.length; i++) {
                String ip = Ips[i];
                if (!ip.equals("")) {
                    deviceGroupIps.add(ip);
                }
            }
        }
        /**
         * 从数据库读取已经保存的时间任务列表
         */
        if (mActivity != null) {
            dbUtils = DBUtil.getInstance(mActivity);
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
                control(timerTasks);
                Constant.isSendTask = false;
            }
            if (timeTaskAdapter == null) {
                timeTaskAdapter = new TimeTaskAdapter(mActivity, lists);
                listView.setAdapter(timeTaskAdapter);
            } else {
                timeTaskAdapter.refresh(lists);
            }
            timeTaskAdapter.setOnTimerListener(new TimeTaskAdapter.TimerListener() {
                @Override
                public void open() {
                    if (mActivity != null) {
                        Log.i(TAG, "showMenu");
                        mFunctions.invokeFunction(SHOW_MENU);
                    }
                }

                @Override
                public void close() {
                    if (mActivity != null) {
                        Log.i(TAG, "hideMenu");
                        mFunctions.invokeFunction(HIDE_MENU);
                        mFunctions.invokeFunction(CHANGE_MENU_BUTTON_TEXT);
                    }
                }
            });
            listView.setMenuCreator(creator);
            listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                    if (lists != null && lists.size() > 0){
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
                            control(timerTasks);//删除任务时重新发送所有的定时任务
                            timeTaskAdapter.refresh(lists);
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (lists != null) {
                        Intent intent = new Intent(mActivity, AddTimedTaskActivity.class);
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
                    List<TimeTask> timerTasks = new ArrayList<TimeTask>();
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
                    if (lists != null){
                        for (TimeTask timerTask : lists) {
                            if (timerTask.isUse()) {
                                timerTasks.add(timerTask);
                            }
                        }
                        control(timerTasks);
                    }
                }
            });
        }
    }

    private void control(List<TimeTask> lists) {
        if (deviceModel.getTid() != 0) {
            sendTimeTask(deviceModel.getDid(), deviceModel.getTid(), deviceModel.getIp(), lists);
        } else {
            if (deviceModel.getConnetedMode() == 0) {
                for (long did : deviceGroupDids) {
                    sendTimeTask(did, 1, null, lists);
                }
            } else {
                for (int i = 0; i < deviceGroupTids.size(); i++) {
                    long tid = deviceGroupTids.get(i);
                    String ip = deviceGroupIps.get(i < deviceGroupIps.size() ? i : 0);
                    sendTimeTask(0, tid, ip, lists);
                }

            }
        }

    }
    //修改连接模式
    public void setConnectMode(int mode) {
        if (deviceModel != null){
            deviceModel.setConnetedMode(mode);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.i(TAG, "定时=hidden=" + hidden);
        if (!hidden) {
            if (Constant.isSendTask) {
                initData();
            }else {
                if (lists != null) {
//                    List<TimeTask> timerTasks = null;
//                    if (lists != null || lists.size() > 0) {
//                        timerTasks = new ArrayList<TimeTask>();
//                        for (TimeTask timerTask : lists) {
//                            if (timerTask.isUse()) {
//                                timerTasks.add(timerTask);
//                            }
//                        }
//                    }
                    queryTimeTask(lists);
                }
            }

        } else {

        }
        super.onHiddenChanged(hidden);
    }

    /**
     * 查询
     *
     * @param lists
     */
    private void queryTimeTask(final List<TimeTask> lists) {
        if (lists != null){
            Log.i(TAG, "lists==0==" + lists.toString());
        }
        if (mActivity != null) {
            String s = SharedPreferencesUtils.getString(mActivity, "username", "0");
            String uidSig = SharedPreferencesUtils.getString(mActivity, "uidSig", "0");
            long uid = Long.parseLong(s);
            Nodepp.Msg msg = PbDataUtils.setQueryTimeTaskRequestParam(uid, deviceModel.getDid(), deviceModel.getTid(), uidSig);
            Log.i(TAG, "msg==" + msg.toString());
            Socket.send(mActivity, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, random, new ResponseListener() {
                @Override
                public void onSuccess(Nodepp.Msg msg) {
                    Log.i(TAG, "queryTimeTask==msg==" + msg.toString());
                    int result = msg.getHead().getResult();
                    if (result == 0) {
                        List<TimeTask> timeTasks = Utils.convertTimerTask(msg, deviceModel.getId(), deviceModel.getDid(), deviceModel.getTid());
                        if (null != timeTasks){
                            Log.i(TAG, "timeTasks==size==" + timeTasks.size());
                            if (timeTasks.size() > 0) {
                                Utils.saveOrUpdateTimeList(mActivity, timeTasks, lists, handler);
                            } else {
                                //刷新定时任务
                                if (lists != null && timeTaskAdapter != null && lists.size() > 0) {
                                    Log.i(TAG, "lists==1==" + lists.toString());
                                    for (TimeTask task : lists) {
                                        task.setIsUse(false);
                                        try {
                                            DBUtil.getInstance(mActivity).saveOrUpdate(task);
                                        } catch (DbException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    Log.i(TAG, "lists==2==" + lists.toString());
                                    Log.i(TAG, "lists==size==" + lists.size());
                                    timeTaskAdapter.refresh(lists);
                                }
                            }
                        }
                    } else {

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
    }

    /**
     * 发送定时任务
     *
     * @param lists
     */
    private void sendTimeTask(long did, long tid, String ip, List<TimeTask> lists) {
        if (mActivity != null) {
            if (NetWorkUtils.isNetworkConnected(mActivity)) {
                if (lists != null) {//确认有定时任务才进行操作
                    Log.i(TAG, "list===" + lists.toString());
                    if (mActivity != null) {
                        String username = SharedPreferencesUtils.getString(mActivity, "username", "0");
                        String uidSig = SharedPreferencesUtils.getString(mActivity, "uidSig", "0");
                        long uid = Long.parseLong(username);
                        Nodepp.Msg msg = PbDataUtils.setTimeTaskRequestParam(uid, did, tid, uidSig, lists);
                        Log.i(TAG, "msg==" + msg.toString());
                        Socket.send(mActivity, deviceModel.getConnetedMode(), ip, msg, random, new ResponseListener() {
                            @Override
                            public void onSuccess(Nodepp.Msg msg) {
                                int result = msg.getHead().getResult();
                                if (result == 0) {
                                    JDJToast.showMessage(mActivity, "定时");
                                } else {
                                    JDJToast.showMessage(mActivity, "定时失败");
                                }
                                Log.i(TAG, "sendTimeTask==" + msg.toString());
                                loadingDialog.dismiss();
                            }

                            @Override
                            public void onTimeout(Nodepp.Msg msg) {

                            }

                            @Override
                            public void onFaile() {
                                loadingDialog.dismiss();
                                JDJToast.showMessage(mActivity, "定时失败");
                            }
                        });
                    }
                }
            } else {
                JDJToast.showMessage(mActivity, "网络没有连接，请稍后重试");
            }
        }
    }

    SwipeMenuCreator creator = new SwipeMenuCreator() {
        @Override
        public void create(SwipeMenu menu) {
            SwipeMenuItem openItem = new SwipeMenuItem(mActivity);
            menu.addMenuItem(openItem);
            // create "delete" item
            SwipeMenuItem deleteItem = new SwipeMenuItem(mActivity);
            // set item background
            deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF7,
                    0x53, 0x53)));
            // set item width
            deleteItem.setWidth(Utils.Dp2Px(mActivity, 100));
            // set a icon
            deleteItem.setIcon(R.drawable.ic_delete);
            // add to menu
            menu.addMenuItem(deleteItem);
        }
    };

    @Override
    public void onResume() {
        if (Constant.isSendTask) {
            initData();
        }
        super.onResume();
    }

    public void refreshTask() {
        handler.sendEmptyMessage(REFRESH);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_add_task:
                if (lists != null && lists.size() >= 10) {
                    JDJToast.showMessage(mActivity, "已经有10个定时任务了，无法继续添加了");
                } else {
                    Intent intent = new Intent(mActivity, AddTimedTaskActivity.class);
                    intent.putExtra("deviceId", deviceModel.getId());
                    intent.putExtra("tid", deviceModel.getTid());
                    intent.putExtra("isOneSwitch", true);
                    startActivity(intent);
                }
                break;
        }
    }

}
