package com.nodepp.smartnode.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.nodepp.smartnode.R;
import com.nodepp.smartnode.fragment.BaseFragment;
import com.nodepp.smartnode.fragment.ColorLightFragment;
import com.nodepp.smartnode.fragment.NormalLightFragment;
import com.nodepp.smartnode.fragment.SceneFragment;
import com.nodepp.smartnode.fragment.TimeFragment;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.struct.FunctionManager;
import com.nodepp.smartnode.struct.FunctionNoParamAndResult;
import com.nodepp.smartnode.udp.UDPClientA2S;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.view.TitleBar;

import java.util.ArrayList;
import java.util.Observable;

import nodepp.Nodepp;

public class ColorControlActivity extends BaseVoiceActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static String TAG = ColorControlActivity.class.getSimpleName();
    private ArrayList<Fragment> fragmentArrayList;
    private Fragment mCurrentFrgment;
    private LinearLayout llMenu;
    private LinearLayout llNoDevice;
    private ArrayList<RadioButton> radioButtons;
    private LinearLayout llControl;
    private RadioButton rbSetTime;
    private RadioGroup mainRadio;
    private Button btnVoice;
    private boolean isSetTime = true;
    private int mCurrentPageIndex = 0;// 0表示白光页面，1表示彩光页面，2表示场景页面，3表示定时有页面
    private boolean isVoice;
    private TitleBar titleBar;
    private long lastControlTimeStamp = 0;
    private Device deviceModel;
    private Nodepp.Msg currentMsg = nodepp.Nodepp.Msg.newBuilder().build();//初始化为空
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_control);
        deviceModel = (Device) getIntent().getSerializableExtra("device");
        isVoice = getIntent().getBooleanExtra("isVoice", false);
        initView();
        Log.i(TAG, "onCreate");
    }

    //实现fragment通信接口
    public void setFunctionForFragment(String fragmentTag) {
        FragmentManager fm = getFragmentManager();
        BaseFragment baseFragment = (BaseFragment) fm.findFragmentByTag(fragmentTag);
        baseFragment.setFunction(FunctionManager.getInstance());
        baseFragment.getFunction().addFunction(new FunctionNoParamAndResult(BaseFragment.SHOW_MENU) {
            @Override
            public void function() {
                Log.i("ll","-----show------");
                showMenu();
            }
        }).addFunction(new FunctionNoParamAndResult(BaseFragment.HIDE_MENU) {
            @Override
            public void function() {
                hideMenu();
            }
        }).addFunction(new FunctionNoParamAndResult(BaseFragment.HIDE_NO_DEVICE) {
            @Override
            public void function() {
                hideNoDevice();
            }
        }).addFunction(new FunctionNoParamAndResult(BaseFragment.SHOW_NO_DEVICE) {
            @Override
            public void function() {
                showNoDevice();
            }
        }).addFunction(new FunctionNoParamAndResult(TimeFragment.CHANGE_MENU_BUTTON_TEXT) {
            @Override
            public void function() {
                setButtonTextAndPic("开灯", R.mipmap.ic_normal_light_n);
                isSetTime = false;
            }
        });
    }

    private void initView() {
        initRadios();
        initTitleBar();
        llMenu = (LinearLayout) findViewById(R.id.ll_menu);
        btnVoice = (Button) findViewById(R.id.btn_voice);
        llNoDevice = (LinearLayout) findViewById(R.id.ll_no_device);
        llControl = (LinearLayout) findViewById(R.id.ll_control);
        mainRadio = (RadioGroup) findViewById(R.id.main_radio);
        btnVoice.setOnClickListener(this);
        //把几个界面进行初始化
        changeTab(3);
        changeTab(2);
        changeTab(1);
        changeTab(0);
    }
    //判断当前接收到的seq是不是比上一次的message大
    public boolean isBigSeqMessage(Nodepp.Msg receiveMsg){
        Log.i("aaaaa","receiveMsg"+receiveMsg.toString());
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
    public Nodepp.Msg getCurrentMsg(){
        return currentMsg;
    }
    //初始化titlebar
    private void initTitleBar() {
        titleBar = (TitleBar) findViewById(R.id.title_bar);
        if (deviceModel.getDeviceGroupTids().equals("") && deviceModel.getDeviceGroupDids().equals("")) {
            titleBar.setRightVisible(TitleBar.BUTTON);
        }
        titleBar.setTitle(deviceModel.getSocketName());
        titleBar.setRightClickListener(new TitleBar.RightClickListener() {
            @Override
            public void onClick() {
                Intent intent = new Intent(ColorControlActivity.this, MoreSettingActivity.class);
                intent.putExtra("device", deviceModel);
                startActivityForResult(intent,1);
            }
        });

    }

    public void hideMenu() {
        llMenu.setVisibility(View.VISIBLE);
        mainRadio.setVisibility(View.GONE);
        rbSetTime.setVisibility(View.VISIBLE);
    }

    public void showMenu() {
        llMenu.setVisibility(View.VISIBLE);
        mainRadio.setVisibility(View.VISIBLE);
        rbSetTime.setVisibility(View.GONE);
    }

    public void hideNoDevice() {
        llNoDevice.setVisibility(View.GONE);
        llControl.setVisibility(View.VISIBLE);
    }

    public void showNoDevice() {
        llNoDevice.setVisibility(View.VISIBLE);
        llControl.setVisibility(View.GONE);
    }

    private void changeTab(int index) {
        mCurrentPageIndex = index;
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        //判断当前的Fragment是否为空，不为空则隐藏
        if (null != mCurrentFrgment) {
            ft.hide(mCurrentFrgment);
        }
        //先根据Tag从FragmentTransaction事物获取之前添加的Fragment
        Fragment fragment = getFragmentManager().findFragmentByTag(fragmentArrayList.get(index).getClass().getName());

        if (null == fragment) {
            //如fragment为空，则之前未添加此Fragment。便从集合中取出
            fragment = fragmentArrayList.get(index);
        }
        mCurrentFrgment = fragment;
        //判断此Fragment是否已经添加到FragmentTransaction事物中
        if (!fragment.isAdded()) {
            ft.add(R.id.fl_content, fragment, fragment.getClass().getName());
        } else {
            ft.show(fragment);
            fragment.onResume();
        }
        if (index == 3) {
            btnVoice.setVisibility(View.INVISIBLE);
        } else {
            btnVoice.setVisibility(View.VISIBLE);
        }
        ft.commitAllowingStateLoss();
//        ft.commit();
    }

    //刷新定时任务
    public void refreshTimeTask(){
        TimeFragment timeFragment = (TimeFragment) fragmentArrayList.get(3);
        timeFragment.refreshTask();
    }

    public void setLastControlTimeStamp(long timeStamp){
        lastControlTimeStamp = timeStamp;
    }

    public long getLastControlTimeStamp(){
        return lastControlTimeStamp;
    }
    //fragment调用Activity方法
    public void setBottomMenu(Nodepp.Rgb color,int sence,int brightDark,int suYan) {
        if (mCurrentPageIndex < 3) {
            ColorLightFragment colorLightFragment = (ColorLightFragment) fragmentArrayList.get(1);
            SceneFragment sceneFragment = (SceneFragment) fragmentArrayList.get(2);
            if (sence != 99) {//0-7的场景
                sceneFragment.setCurrentSence(sence);
            }
            if (sence == 99) {//白光或者彩光
                if (color.getW() != 0) {//有白色，在白光界面
                    mCurrentPageIndex = 0;
                } else { //彩色光界面
                    mCurrentPageIndex = 1;
                    colorLightFragment.setColorLightCurrentState(color, brightDark, suYan);
                }
            } else {//0-7 为场景页面对应的8个场景
                mCurrentPageIndex = 2;
            }
            setCurrentTab(mCurrentPageIndex);
        }
    }
    public void setLightState(int state){
        Fragment colorFragment = fragmentArrayList.get(1);
        Fragment sceneFragment = fragmentArrayList.get(2);
        if (colorFragment != null){
            ((ColorLightFragment)colorFragment).showLightState(state);
        }
        if (sceneFragment != null){
            ((SceneFragment)sceneFragment).showLightState(state);
        }
    }
   private void setCurrentTab(int index){
       changeTab(index);
       for (int i = 0; i < 4; i++) {
           if (i == index) {
               radioButtons.get(i).setChecked(true);
           } else {
               radioButtons.get(i).setChecked(false);
           }
       }
   }
    /**
     * 初始化底部按钮
     */
    private void initRadios() {
        fragmentArrayList = new ArrayList<Fragment>();
        fragmentArrayList.add(new NormalLightFragment());
        fragmentArrayList.add(new ColorLightFragment());
        fragmentArrayList.add(new SceneFragment());
        fragmentArrayList.add(new TimeFragment());
        Bundle bundle = new Bundle();
        bundle.putSerializable("device", deviceModel);
        bundle.putString("random", clientKeys.get(deviceModel.getTid()));
        fragmentArrayList.get(0).setArguments(bundle);
        fragmentArrayList.get(1).setArguments(bundle);
        fragmentArrayList.get(2).setArguments(bundle);
        fragmentArrayList.get(3).setArguments(bundle);
        RadioButton menuButtonOne = (RadioButton) findViewById(R.id.rb_normal_light);
        RadioButton menuButtonTwo = (RadioButton) findViewById(R.id.rb_color_light);
        RadioButton menuButtonThree = (RadioButton) findViewById(R.id.rb_scent);
        RadioButton menuButtonFour = (RadioButton) findViewById(R.id.rb_time);
        rbSetTime = (RadioButton) findViewById(R.id.rb_set_time);
        radioButtons = new ArrayList<RadioButton>();
        radioButtons.add(menuButtonOne);
        radioButtons.add(menuButtonTwo);
        radioButtons.add(menuButtonThree);
        radioButtons.add(menuButtonFour);
        menuButtonOne.setOnCheckedChangeListener(this);
        menuButtonTwo.setOnCheckedChangeListener(this);
        menuButtonThree.setOnCheckedChangeListener(this);
        menuButtonFour.setOnCheckedChangeListener(this);
        rbSetTime.setOnCheckedChangeListener(this);

    }

    /**
     * 切换模块
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            switch (buttonView.getId()) {
                case R.id.rb_normal_light:
                    SharedPreferencesUtils.saveInt(ColorControlActivity.this, deviceModel.getTid() + "white", 255);//点击白光的时候，把白色置为255，说明最后一次操作不是彩色，是白色
                    NormalLightFragment normallLightFragment = (NormalLightFragment) fragmentArrayList.get(0);
                    normallLightFragment.showBar();
                    changeTab(0);
                    setLastControlTimeStamp(System.currentTimeMillis()+10000);//点击菜单的时候20s不进行查询操作
                    break;
                case R.id.rb_color_light:
                    changeTab(1);
                    setLastControlTimeStamp(System.currentTimeMillis()+10000);//点击菜单的时候20s不进行查询操作
                    break;
                case R.id.rb_scent:
                    changeTab(2);
                    setLastControlTimeStamp(System.currentTimeMillis()+10000);//点击菜单的时候20s不进行查询操作
                    break;
                case R.id.rb_time:
                    changeTab(3);
                    break;
                case R.id.rb_set_time:
                   //关灯状态下只有一个按钮
                    if (isSetTime) {
                        changeTab(3);//在定时界面，底部按钮显示开灯，可以点击到开灯界面
                        setButtonTextAndPic("开灯", R.mipmap.ic_normal_light_n);
                    } else {
                        changeTab(0);//在白灯开灯界面，底部按钮显示定时，可以点击到定时界面
                        setButtonTextAndPic("定时", R.mipmap.ic_timmer_n);
                    }
                    rbSetTime.setChecked(false);
                    isSetTime = !isSetTime;
                    break;
            }
        }
    }
    //切换按钮文字和图片的方法
    private void setButtonTextAndPic(String text, int picId) {
        Drawable dra = getResources().getDrawable(picId);
        dra.setBounds(0, 0, dra.getMinimumWidth(), dra.getMinimumHeight());
        rbSetTime.setCompoundDrawables(null, dra, null, null);//setBounds(left, top, right, bottom)
        rbSetTime.setText(text);
    }

   //语音按钮点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_voice:
                showVoiceDialog();
                break;
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        if (isVoice){
            showVoiceDialog();
            isVoice = false;
        }
    }


    @Override
    protected void onPause() {
        setLastControlTimeStamp(System.currentTimeMillis()+30000);//设置时间戳30s内不执行查询
        super.onPause();
    }

    @Override
    protected void netChange(Observable observable, Object data) {
        deviceModel.setDeviceMode(0);
        NormalLightFragment normallLightFragment = (NormalLightFragment) fragmentArrayList.get(0);
        ColorLightFragment colorLightFragment = (ColorLightFragment) fragmentArrayList.get(1);
        SceneFragment sceneFragment = (SceneFragment) fragmentArrayList.get(2);
        TimeFragment TimeFragment = (TimeFragment) fragmentArrayList.get(3);
        normallLightFragment.setConnectMode(deviceModel.getDeviceMode());
        colorLightFragment.setConnectMode(deviceModel.getDeviceMode());
        sceneFragment.setConnectMode(deviceModel.getDeviceMode());
        TimeFragment.setConnectMode(deviceModel.getDeviceMode());
    }
    @Override
    protected void onDestroy() {
        FunctionManager.getInstance().clear();//释放单例
        super.onDestroy();
    }

    @Override
    public void voiceControl(String result) {
        boolean isTurnOffLight= false;
        NormalLightFragment normalLightFragment = (NormalLightFragment) fragmentArrayList.get(0);
        ColorLightFragment colorLightFragment = (ColorLightFragment) fragmentArrayList.get(1);
        SceneFragment sceneFragment = (SceneFragment) fragmentArrayList.get(2);
        if (result.contains("关灯")){
            isTurnOffLight = true;
            if (mCurrentPageIndex == 0){
                //白灯页面的关灯

                normalLightFragment.controlLightState(0);
            }else if (mCurrentPageIndex == 1){
                colorLightFragment.controlLightState(0);
            }else{
                sceneFragment.controlLightState(0);
            }
        }else if (result.contains("开灯")){
            if (mCurrentPageIndex == 0){
                //白灯页面的关灯
                mCurrentPageIndex = 0;
                normalLightFragment.controlLightState(1);
            }else if (mCurrentPageIndex == 1){
                mCurrentPageIndex = 1;
                colorLightFragment.controlLightState(1);
            }else {
                sceneFragment.controlLightState(1);
            }
        }
        if (result.contains("白光") || mCurrentPageIndex == 0){
            mCurrentPageIndex = 0;
            normalLightFragment.voiceControl(result);
        }
        if (result.contains("彩光") || mCurrentPageIndex ==1){
            mCurrentPageIndex = 1;
            colorLightFragment.voiceControl(result);
        }
        if (result.contains("场景") || mCurrentPageIndex == 2){
            mCurrentPageIndex = 2;
            sceneFragment.voiceControl(result);
        }
        if (result.contains("定时")){
            mCurrentPageIndex = 3;
            cancleVoice();
        }
        if (!isTurnOffLight){
            setCurrentTab(mCurrentPageIndex);
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
