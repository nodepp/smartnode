package com.nodepp.smartnode.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.iflytek.cloud.SpeechConstant;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.model.MultipleRemarkName;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.Socket;
import com.nodepp.smartnode.udp.UDPClient;
import com.nodepp.smartnode.udp.UDPClientA2S;
import com.nodepp.smartnode.utils.ClickUtils;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.DESUtils;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.utils.Utils;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import nodepp.Nodepp;

/**
 * 多路控制界面
 */
public class MultichannelControlActivity extends BaseVoiceActivity implements View.OnClickListener {
    private static final String TAG = MultichannelControlActivity.class.getSimpleName();
    boolean isSelectOne = false;
    boolean isSelectTwo = false;
    boolean isSelectThree = false;
    boolean isSelectFour = false;
    boolean isSelectFive = false;
    boolean isSelectSix = false;
    boolean isSelectSeven = false;
    boolean isSelectEight = false;
    boolean isSetStates = false;
    private ToggleButton tbSwitchOne;
    private ToggleButton tbSwitchTwo;
    private ToggleButton tbSwitchThree;
    private ToggleButton tbSwitchFour;
    private ToggleButton tbSwitchFive;
    private ToggleButton tbSwitchSix;
    private int switchValue = 0;
    private int currentCheck = 0;
    private DbUtils dbUtils;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private List<MultipleRemarkName> nameList;
    private MultipleRemarkName multipleRemarkName;
    private TextView tvOne;
    private TextView tvTwo;
    private TextView tvThree;
    private TextView tvFour;
    private TextView tvFive;
    private TextView tvSix;
    private LinearLayout llTiming;
    private MyTask myTask;
    private Timer timer;

    private HashMap<String, Integer> namesMap = new HashMap<>();
    private boolean isVoice;
    private TextView tvTitle;
    private long lastControlTimeStamp = 0;
    private Device deviceModel;
    private Nodepp.Msg currentMsg = nodepp.Nodepp.Msg.newBuilder().build();//初始化为空
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceModel = (Device) getIntent().getSerializableExtra("device");
        isVoice = getIntent().getBooleanExtra("isVoice", false);
        if (deviceModel.getDeviceType() == 4) {
            setContentView(R.layout.activity_four_switch);
        } else if (deviceModel.getDeviceType() == 2) {
            setContentView(R.layout.activity_six_switch);
        }else if (deviceModel.getDeviceType() == 10){
            setContentView(R.layout.activity_two_switch);
        }
        initView();

    }
    private void initData() {
        try {
            nameList = DBUtil.getInstance(MultichannelControlActivity.this).findAll(Selector.from(MultipleRemarkName.class).where("userName", "=", Constant.userName).and((WhereBuilder.b("tid", "=", deviceModel.getTid()))));
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (nameList != null && nameList.size() > 0) {
            multipleRemarkName = nameList.get(0);
        } else {
            multipleRemarkName = new MultipleRemarkName();
            multipleRemarkName.setTid(deviceModel.getTid());
            multipleRemarkName.setDid(deviceModel.getDid());
            multipleRemarkName.setUserName(deviceModel.getUserName());
            try {
                DBUtil.getInstance(MultichannelControlActivity.this).saveBindingId(multipleRemarkName);
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
        tvOne.setText(multipleRemarkName.getChannelOneName());
        tvTwo.setText(multipleRemarkName.getChannelTwoName());
        tvThree.setText(multipleRemarkName.getChannelThreeName());
        tvFour.setText(multipleRemarkName.getChannelFourName());
        tvFive.setText(multipleRemarkName.getChannelFiveName());
        tvSix.setText(multipleRemarkName.getChannelSixName());
        setChannelsNameToMap();
    }

    private void resetDeviceMode() {
        if (deviceModel.getDeviceMode() == 1) {//点动模式，设置onTouchListener监听
            changeState(0);
            Log.i("hh","changeState(0)");
            tbSwitchOne.setOnTouchListener(onTouchListener);
            tbSwitchTwo.setOnTouchListener(onTouchListener);
            tbSwitchThree.setOnTouchListener(onTouchListener);
            tbSwitchFour.setOnTouchListener(onTouchListener);
            tbSwitchFive.setOnTouchListener(onTouchListener);
            tbSwitchSix.setOnTouchListener(onTouchListener);
        } else {//其他模式，移除onTouchListener监听
            tbSwitchOne.setOnTouchListener(null);
            tbSwitchTwo.setOnTouchListener(null);
            tbSwitchThree.setOnTouchListener(null);
            tbSwitchFour.setOnTouchListener(null);
            tbSwitchFive.setOnTouchListener(null);
            tbSwitchSix.setOnTouchListener(null);
        }
    }

    /**
     * 把每一个通道的名称和对应控制的operate中的哪一位对应存起来,然后语音通过名称就知道要操作哪一位
     */
    private void setChannelsNameToMap() {
        namesMap.clear();
        namesMap.put(multipleRemarkName.getChannelOneName(), 0);
        namesMap.put(multipleRemarkName.getChannelTwoName(), 1);
        namesMap.put(multipleRemarkName.getChannelThreeName(), 2);
        namesMap.put(multipleRemarkName.getChannelFourName(), 3);
        namesMap.put(multipleRemarkName.getChannelFiveName(), 4);
        namesMap.put(multipleRemarkName.getChannelSixName(), 5);
        namesMap.put(multipleRemarkName.getChannelSevenName(), 6);
        namesMap.put(multipleRemarkName.getChannelEightName(), 7);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UDPClientA2S.getInstance().setIsRetry(false);
        //如果是点动模式，默认所有的按钮都是关的
        setSocketState();
        startTimer();
        resetDeviceMode();
        initData();
        if (isVoice){
            showVoiceDialog();
            isVoice = false;
        }
    }


    private void initView() {
        ImageView ivBacke = (ImageView) findViewById(R.id.iv_back);
        ImageView ivMore = (ImageView) findViewById(R.id.iv_more);
        Button btnVoice = (Button) findViewById(R.id.btn_voice);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tbSwitchOne = (ToggleButton) findViewById(R.id.tb_switch_one);
        tbSwitchTwo = (ToggleButton) findViewById(R.id.tb_switch_two);
        tbSwitchThree = (ToggleButton) findViewById(R.id.tb_switch_three);
        tbSwitchFour = (ToggleButton) findViewById(R.id.tb_switch_four);
        tbSwitchFive = (ToggleButton) findViewById(R.id.tb_switch_five);
        tbSwitchSix = (ToggleButton) findViewById(R.id.tb_switch_six);
        llTiming = (LinearLayout) findViewById(R.id.ll_timing);
        tvOne = (TextView) findViewById(R.id.tv_one);
        tvTwo = (TextView) findViewById(R.id.tv_two);
        tvThree = (TextView) findViewById(R.id.tv_three);
        tvFour = (TextView) findViewById(R.id.tv_four);
        tvFive = (TextView) findViewById(R.id.tv_five);
        tvSix = (TextView) findViewById(R.id.tv_six);
        llTiming.setOnClickListener(onClickListener);
        tvOne.setOnClickListener(onClickListener);
        tvTwo.setOnClickListener(onClickListener);
        tvThree.setOnClickListener(onClickListener);
        tvFour.setOnClickListener(onClickListener);
        tvFive.setOnClickListener(onClickListener);
        tvSix.setOnClickListener(onClickListener);
        tbSwitchOne.setOnCheckedChangeListener(checkListener);
        tbSwitchTwo.setOnCheckedChangeListener(checkListener);
        tbSwitchThree.setOnCheckedChangeListener(checkListener);
        tbSwitchFour.setOnCheckedChangeListener(checkListener);
        tbSwitchFive.setOnCheckedChangeListener(checkListener);
        tbSwitchSix.setOnCheckedChangeListener(checkListener);
        ivBacke.setOnClickListener(this);
        ivMore.setOnClickListener(this);
        btnVoice.setOnClickListener(this);
        tvTitle.setText(deviceModel.getSocketName());
    }

    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            lastControlTimeStamp = System.currentTimeMillis();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i(TAG, "按住");
                    switch (v.getId()) {
                        case R.id.tb_switch_one:
                            tbSwitchOne.setChecked(true);
                            break;
                        case R.id.tb_switch_two:
                            tbSwitchTwo.setChecked(true);
                            break;
                        case R.id.tb_switch_three:
                            tbSwitchThree.setChecked(true);
                            break;
                        case R.id.tb_switch_four:
                            tbSwitchFour.setChecked(true);
                            break;
                        case R.id.tb_switch_five:
                            tbSwitchFive.setChecked(true);
                            break;
                        case R.id.tb_switch_six:
                            tbSwitchSix.setChecked(true);
                            break;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    switch (v.getId()) {
                        case R.id.tb_switch_one:
                            tbSwitchOne.setChecked(false);
                            break;
                        case R.id.tb_switch_two:
                            tbSwitchTwo.setChecked(false);
                            break;
                        case R.id.tb_switch_three:
                            tbSwitchThree.setChecked(false);
                            break;
                        case R.id.tb_switch_four:
                            tbSwitchFour.setChecked(false);
                            break;
                        case R.id.tb_switch_five:
                            tbSwitchFive.setChecked(false);
                            break;
                        case R.id.tb_switch_six:
                            tbSwitchSix.setChecked(false);
                            break;
                    }
                    break;
            }
            return true;
        }
    };
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_one:
                    showChangeNameDialog(1);
                    break;
                case R.id.tv_two:
                    showChangeNameDialog(2);
                    break;
                case R.id.tv_three:
                    showChangeNameDialog(3);
                    break;
                case R.id.tv_four:
                    showChangeNameDialog(4);
                    break;
                case R.id.tv_five:
                    showChangeNameDialog(5);
                    break;
                case R.id.tv_six:
                    showChangeNameDialog(6);
                    break;
                case R.id.ll_timing:
                    goTiming();
                    break;
            }

        }
    };
    //跳转到定时界面
    private void goTiming(){
        if (deviceModel.getDeviceMode() != 0){
            JDJToast.showMessage(MultichannelControlActivity.this,"自锁模式才支持定时功能");
            return;
        }
        Intent intent = new Intent(MultichannelControlActivity.this, MulitipleTimingActivity.class);
        intent.putExtra("device", deviceModel);
        intent.putExtra("operate", switchValue);
        ArrayList<String> names = new ArrayList<String>();
        names.add(multipleRemarkName.getChannelOneName());
        names.add(multipleRemarkName.getChannelTwoName());
        if (deviceModel.getDeviceType() == 4 || deviceModel.getDeviceType() == 2 ){//4路和六路要
            names.add(multipleRemarkName.getChannelThreeName());
            names.add(multipleRemarkName.getChannelFourName());
        }
        if (deviceModel.getDeviceType() == 2) {//多传递5，6路的名称
            names.add(multipleRemarkName.getChannelFiveName());
            names.add(multipleRemarkName.getChannelSixName());
        }
        intent.putStringArrayListExtra("memoName", names);
        startActivity(intent);
    }

    /**
     * 修改通道的名称
     * @param index 修改哪一个通道名称
     */
    private void showChangeNameDialog(final int index) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入要修改的名字");
        final EditText editText = new EditText(this);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        builder.setView(editText);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editText.getText().toString();
                 updateUserWord(name);
                if (TextUtils.isEmpty(name)) {
                    JDJToast.showMessage(MultichannelControlActivity.this, "请输入要修改的名字");
                } else {
                    switch (index) {
                        case 1:
                            multipleRemarkName.setChannelOneName(name);
                            tvOne.setText(name);
                            break;
                        case 2:
                            multipleRemarkName.setChannelTwoName(name);
                            tvTwo.setText(name);
                            break;
                        case 3:
                            multipleRemarkName.setChannelThreeName(name);
                            tvThree.setText(name);
                            break;
                        case 4:
                            multipleRemarkName.setChannelFourName(name);
                            tvFour.setText(name);
                            break;
                        case 5:
                            multipleRemarkName.setChannelFiveName(name);
                            tvFive.setText(name);
                            break;
                        case 6:
                            multipleRemarkName.setChannelSixName(name);
                            tvSix.setText(name);
                            break;
                    }
                    try {
                        DBUtil.getInstance(MultichannelControlActivity.this).update(multipleRemarkName, WhereBuilder.b("tid", "=", deviceModel.getTid()).and("userName", "=", Constant.userName));
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    CompoundButton.OnCheckedChangeListener checkListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            lastControlTimeStamp = System.currentTimeMillis();
//            if (ClickUtils.isFastClick(100)&&!isSetStates) {//防止用户一直快速点击
//                JDJToast.showMessage(MultichannelControlActivity.this, getString(R.string.no_click_quick));
//                return;
//            }
            switch (buttonView.getId()) {
                case R.id.tb_switch_one:
                    isSelectOne = isChecked;
                    currentCheck = 1;
                    break;
                case R.id.tb_switch_two:
                    isSelectTwo = isChecked;
                    currentCheck = 2;
                    break;
                case R.id.tb_switch_three:
                    isSelectThree = isChecked;
                    currentCheck = 3;
                    break;
                case R.id.tb_switch_four:
                    isSelectFour = isChecked;
                    currentCheck = 4;
                    break;
                case R.id.tb_switch_five:
                    isSelectFive = isChecked;
                    currentCheck = 5;
                    break;
                case R.id.tb_switch_six:
                    isSelectSix = isChecked;
                    currentCheck = 6;
                    break;
                case R.id.tb_switch_seven:
                    isSelectSeven = isChecked;
                    currentCheck = 7;
                    break;
                case R.id.tb_switch_eight:
                    isSelectEight = isChecked;
                    currentCheck = 8;
                    break;
            }
            if (isSetStates) {
                Log.i(TAG, "不操作");

            } else {
                if (deviceModel.getDeviceMode() == 2) {
                    //互锁
                    switch (currentCheck) {
                        case 1:
                            switchValue = isChecked ? 1 : 0;
                            break;
                        case 2:
                            switchValue = isChecked ? 2 : 0;
                            break;
                        case 3:
                            switchValue = isChecked ? 4 : 0;
                            break;
                        case 4:
                            switchValue = isChecked ? 8 : 0;
                            break;
                        case 5:
                            switchValue = isChecked ? 16 : 0;
                            break;
                        case 6:
                            switchValue = isChecked ? 32 : 0;
                            break;
                    }
                    changeState(switchValue);
                } else {
                    //自锁，点动
                    switchValue = Utils.getSwitchValue(isSelectOne, isSelectTwo, isSelectThree, isSelectFour, isSelectFive, isSelectSix, false, false);
                }
                controlSocket(switchValue);
            }
        }
    };

    /**
     * 把设备设置为不在线
     * @param device
     */
    private void setDeviceNoOnline(Device device) {
        try {
            if (device != null) {
                device.setIsOnline(false);
                DBUtil.getInstance(MultichannelControlActivity.this).update(device, WhereBuilder.b("userName", "=", Constant.userName).and("did", "=", device.getDid()));
            }

        } catch (DbException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_more:
                Intent intentMore = new Intent(MultichannelControlActivity.this, MoreSettingActivity.class);
                intentMore.putExtra("device", deviceModel);
                startActivityForResult(intentMore,1);
                break;
            case R.id.btn_voice: //语音控制
                // 移动数据分析，收集开始听写事件
                if (deviceModel.getDeviceMode() != 0){
                    JDJToast.showMessage(MultichannelControlActivity.this,"自锁模式才支持语音功能");
                    return;
                }
                showVoiceDialog();
                break;
        }
    }

    /**
     * 启动定时器
     */
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

    /**
     * 定时器执行的任务
     */
    class MyTask extends TimerTask {
        @Override
        public void run() {
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
    /**
     * 查询设备的状态，然后进行设置
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
                Socket.send(MultichannelControlActivity.this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
                    @Override
                    public void onSuccess(Nodepp.Msg msg) {
                        int result = msg.getHead().getResult();
                        if (result == 404) {
//                            setDeviceNoOnline(deviceModel);
                            JDJToast.showMessage(MultichannelControlActivity.this, getString(R.string.device_is_not_online));
                            return;
                        }else if (result == 0){
                            Log.i(TAG, "msg==" + msg.toString());
                            if (isBigSeqMessage(msg)){
                                int state = msg.getState();
                                int deviceMode = msg.getDeviceMode();
                                if (deviceMode != 1){//点动模式的时候不设置state，其他模式才进行设置
                                    changeState(state);
                                }else {
                                    //点动模式的时候直接重置按钮监听（可能是多人分享一个设备，一个人切换了模式，查询到模式变了立即改变）
                                    resetDeviceMode();
                                }
                                deviceModel.setDeviceMode(deviceMode);
                                Log.i("jjjj","query设置界面");
                            }else {
                                Log.i("jjjj","query不设置界面");
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
                        //设置为上一次状态
                        if (currentMsg != null){
                            int state = currentMsg.getState();
                            int deviceMode = currentMsg.getDeviceMode();
                            if (deviceMode != 1){//点动模式的时候不设置state，其他模式才进行设置
                                changeState(state);
                            }
                        }
                    }
                });

            } else {
                JDJToast.showMessage(MultichannelControlActivity.this, getString(R.string.no_uid));
            }
        } else {
            JDJToast.showMessage(this, "网络没有连接，请稍后重试");
        }

    }

    /**
     * 根据state 改变界面开关的状态
     * @param state
     */
    private void changeState(int state) {
        Log.i("state", "state==" + state);
        switchValue = state;
        isSetStates = true;
        tbSwitchOne.setChecked((state & 1) == 0 ? false : true);
        tbSwitchTwo.setChecked((state & 2) == 0 ? false : true);
        tbSwitchThree.setChecked((state & 4) == 0 ? false : true);
        tbSwitchFour.setChecked((state & 8) == 0 ? false : true);
        tbSwitchFive.setChecked((state & 16) == 0 ? false : true);
        tbSwitchSix.setChecked((state & 32) == 0 ? false : true);
        isSetStates = false;
    }

    @Override
    protected void onPause() {
        UDPClientA2S.getInstance().setIsRetry(true);
        stopTimer();
        super.onPause();
    }

    /**
     * 控制插座的方法
     *
     * @param operate
     */
    private void controlSocket(int operate) {
        if (NetWorkUtils.isNetworkConnected(this)) {
            Log.i(TAG, "operate===" + operate);
            long uid = Long.parseLong(Constant.userName);
            final Nodepp.Msg msg = PbDataUtils.setRequestParam(16, 1, uid, deviceModel.getDid(), deviceModel.getTid(), operate, Constant.usig);
            Socket.send(MultichannelControlActivity.this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
                @Override
                public void onSuccess(Nodepp.Msg msg) {
                    int result = msg.getHead().getResult();
                    if (result == 404) {
//                            setDeviceNoOnline(deviceModel);
                        JDJToast.showMessage(MultichannelControlActivity.this, getString(R.string.device_is_not_online));
                    }else if (result == 0){
                        if (isBigSeqMessage(msg)){
                            int state = currentMsg.getState();
                            int deviceMode = currentMsg.getDeviceMode();
                        }else {

                        }
                    }

                }

                @Override
                public void onTimeout(Nodepp.Msg msg) {
                    Log.i(TAG, "controlSocket=onFaile=");
                    if (msg.getHead().getSeq() < currentMsg.getHead().getSeq()){
                        return;
                    }
                    //发送失败，设置为上一次状态
                    if (currentMsg != null){
                        int state = currentMsg.getState();
                        int deviceMode = currentMsg.getDeviceMode();
                        if (deviceMode != 1){//点动模式的时候不设置state，其他模式才进行设置
                            changeState(state);
                        }
                    }
                }

                @Override
                public void onFaile() {


                }

            });
        } else {
            JDJToast.showMessage(this, "网络没有连接，请稍后重试");
        }
    }

    /**
     * 语音控制
     * @param result
     */
    @Override
    public void voiceControl(String result) {
        int flag = 0;
        if (result.contains("全部")) {
            if (result.contains("打开") || result.contains("开启")) {
                flag = flag +1;
                if (deviceModel.getDeviceType() == 4){
                    switchValue = 15;
                }else if (deviceModel.getDeviceType()  == 2){//6路
                    switchValue = 63;
                }
            } else if (result.contains("关闭")) {
                switchValue = 0;
                flag = flag +1;
            }
        }
        Iterator iter = namesMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            if (result.contains(key)){
                flag = flag +1;
                int index = (int) entry.getValue();
                if (result.contains("打开") || result.contains("开启")) {
                    switchValue |=(1 << index)&0xff;
                }else if (result.contains("关闭")){
                    switchValue &=~((1 << index)&0xff) ;
                }
                break;
            }
        }
        if (result.contains("定时")){
            cancleVoice();
            goTiming();
        }
        if (flag > 0){
            Log.i("switch","switch value:"+switchValue);
            controlSocket(switchValue);
            changeState(switchValue);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 观察网络变化
     * @param observable
     * @param data
     */
    @Override
    protected void netChange(Observable observable, Object data) {

        Log.i("net","多路 net change ");
        deviceModel.setConnetedMode(0);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Serializable object = data.getSerializableExtra("device");
        if (object != null){
            Device device = (Device)object;
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
}
