package com.nodepp.smartnode.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.model.UserInfo;
import com.nodepp.smartnode.utils.ClickUtils;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;

import java.net.InetAddress;
import java.util.List;
import java.util.Observable;
import java.util.Random;

/**
 * 添加插座第三个界面
 */
public class AddDeviceActivityThree extends BaseActivity implements View.OnClickListener {

    private static final String TAG = AddDeviceActivityThree.class.getSimpleName();
    private EditText etName;
    private List<Device> devices;
    private DbUtils dbUtils;
    private boolean isShowSelectType;
    private int yourChoice;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_three);
        initView();
        isShowSelectType = SharedPreferencesUtils.getBoolean(AddDeviceActivityThree.this, "isShowSelectType", false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        Button btnOk = (Button) findViewById(R.id.btn_ok);
        etName = (EditText) findViewById(R.id.et_name);
        btnOk.setOnClickListener(this);
        dbUtils = DBUtil.getInstance(this);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ClickUtils.hideSoftInputView(AddDeviceActivityThree.this);//点击空白处的时候隐藏软键盘
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                if (TextUtils.isEmpty(etName.getText().toString())) {
                    JDJToast.showMessage(AddDeviceActivityThree.this, getString(R.string.input_socket_name));
                    return;
                }
//                if (isShowSelectType) {
//                    if (!AddDeviceActivityThree.this.isFinishing()) {
//                        showSelectDialog();
//                        isShowSelectType = false;
//                    }
//                    return;
//                }
                saveSocket();
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }

    private void showSelectDialog() {
        final String[] items = {"单路控制", "多路控制", "彩色灯"};
        yourChoice = -1;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(AddDeviceActivityThree.this);
        singleChoiceDialog.setTitle("请选择设备类型再点击确认");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setCancelable(false);
        singleChoiceDialog.setSingleChoiceItems(items, UserInfo.deviceType - 1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        yourChoice = which;
                        Log.i("kk", "===which====" + which);
                    }
                });
        singleChoiceDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (yourChoice != -1) {
//                            JDJToast.showMessage(AddSocketActivityThree.this, "你选择了" + items[yourChoice]);
                            UserInfo.deviceType = yourChoice + 1;
                        }
                    }
                });
        singleChoiceDialog.show();
    }

    private void saveSocket() {
        Log.i("kk", "UserInfo.deviceType==" + UserInfo.deviceType);
        Log.i("kk", "UserInfo.tid ==" + UserInfo.tid);
        Log.i("kk", "UserInfo.did ==" + UserInfo.did);
        if (UserInfo.tid != 0) {
            try {
                devices = dbUtils.findAll(Selector.from(Device.class).where("userName", "=", Constant.userName).and((WhereBuilder.b("tid", "=", UserInfo.tid))));
            } catch (DbException e) {
                e.printStackTrace();
            }
            if (devices == null || devices.size() < 1) {
                //设备不存在，直接保存到数据库
                Log.i("kk", "=========username==========" + Constant.userName);
                Log.i("kk", "=========did==========" + UserInfo.did);
                Device device = new Device();
                String name = etName.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    JDJToast.showMessage(AddDeviceActivityThree.this, getString(R.string.device_name));
                    return;
                }
                device.setSocketName(name);
                Random ra = new Random();//产生0，1,2,3随机数，匹配图片
                device.setPictureIndex(ra.nextInt(4));
                device.setUserName(Constant.userName);
                device.setDid(UserInfo.did);//保存did
                device.setTid(UserInfo.tid);//把tid保存到数据库
                device.setFirmwareLevel(UserInfo.firmwareLevel);
                device.setFirmwareVersion(UserInfo.firmwareVersion);
                device.setDeviceMode(UserInfo.deviceMode);
                if (UserInfo.keyClient == null) {
                    device.setClientKey("1234567890");
                } else {
                    device.setClientKey(PbDataUtils.byteString2String(UserInfo.keyClient));
                }
                device.setConnetedMode(UserInfo.connetedMode);//互联网模式
                device.setRouterMac(Constant.routerMac);
                InetAddress addressIp = Constant.ip;
                String ip = addressIp.getHostAddress();
                device.setIp(ip);
                Log.i("ip", "=========ip==========" + ip);
                device.setIsOnline(true);
                device.setDeviceType(UserInfo.deviceType);//把deviceType保存到数据库
                try {
                    dbUtils.saveBindingId(device);
                    if (!UserInfo.isDeviceReturn) {//用掉did和dsig
                        SharedPreferencesUtils.saveLong(AddDeviceActivityThree.this, Constant.userName + "did", 0);
                        SharedPreferencesUtils.saveString(AddDeviceActivityThree.this, Constant.userName + "dsig", "");
                        SharedPreferencesUtils.saveString(AddDeviceActivityThree.this, Constant.userName + "keyClientWAM", "");
                        SharedPreferencesUtils.saveString(AddDeviceActivityThree.this, Constant.userName + "keyClient", "");
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
                Constant.isAddDevice = true;
                SharedPreferencesUtils.saveBoolean(this, Constant.userName + "isUpdateUserWord", true);
                Constant.isAddDevice = true;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ActivityManager.getAppManager().goToActivity(MainActivity.class);
                    }
                },1000);
            } else {//did已经存在，说明已经保存过，提醒用户是不是继续，进行修改插座名称
                showPromptDialog(dbUtils, devices);
            }
        } else {
            //提示添加插座失败，跳到第一步重新进行
            JDJToast.showMessage(this, getString(R.string.show_get_id_err));
            startActivity(new Intent(AddDeviceActivityThree.this, AddDeviceActivity.class));
            finish();
        }
    }

    private void showPromptDialog(final DbUtils dbUtils, final List<Device> devices) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.add_socket_repeat_prompt));
        builder.setPositiveButton(getString(R.string.add_continue), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Device device = devices.get(0);
                device.setDid(UserInfo.did);//保存did
                device.setTid(UserInfo.tid);//把tid保存到数据库
                device.setFirmwareLevel(UserInfo.firmwareLevel);
                device.setFirmwareVersion(UserInfo.firmwareVersion);
                device.setDeviceType(UserInfo.deviceType);
                device.setConnetedMode(UserInfo.connetedMode);
                device.setRouterMac(Constant.routerMac);
                InetAddress addressIp = Constant.ip;
                device.setDeviceMode(UserInfo.deviceMode);
                String ip = addressIp.getHostAddress();
                Log.i("ip", "=========ip==========" + ip);
                device.setIp(ip);
                if (UserInfo.keyClient == null) {
                    device.setClientKey("1234567890");
                } else {
                    device.setClientKey(PbDataUtils.byteString2String(UserInfo.keyClient));
                }
                device.setIsOnline(true);
                String name = etName.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    JDJToast.showMessage(AddDeviceActivityThree.this, getString(R.string.device_name));
                    return;
                }
                Log.i("did", "did===" + UserInfo.did);
                device.setSocketName(name);
                try {
                    dbUtils.saveOrUpdate(device);
                    SharedPreferencesUtils.saveBoolean(AddDeviceActivityThree.this, Constant.userName + "isUpdateUserWord", true);
                    Log.i("did", "tid===" + UserInfo.tid);
                    if (!UserInfo.isDeviceReturn) {
                        SharedPreferencesUtils.saveLong(AddDeviceActivityThree.this, Constant.userName + "did", 0);
                        SharedPreferencesUtils.saveString(AddDeviceActivityThree.this, Constant.userName + "dsig", "");
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                    Log.i("did", "e===" + e.toString());
                }
                JDJToast.showMessage(AddDeviceActivityThree.this, getString(R.string.change_name_success));
                Constant.isAddDevice = true;
                  handler.postDelayed(new Runnable() {
                      @Override
                      public void run() {
                          ActivityManager.getAppManager().goToActivity(MainActivity.class);
                      }
                  },1000);
//                startActivity(new Intent(AddDeviceActivityThree.this,MainActivity.class));
            }
        });
        builder.setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Device device = devices.get(0);
                device.setDid(UserInfo.did);//保存did
                device.setTid(UserInfo.tid);//把tid保存到数据库
                device.setDeviceType(UserInfo.deviceType);
                device.setConnetedMode(UserInfo.connetedMode);
                device.setRouterMac(Constant.routerMac);
                InetAddress ip = Constant.ip;
                device.setIp(ip.toString());
                device.setIsOnline(true);
                try {
                    dbUtils.update(device, WhereBuilder.b("tid", "=", UserInfo.tid).and("userName", "=", Constant.userName));
                    if (!UserInfo.isDeviceReturn) {//设备有回包才把did使用掉
                        SharedPreferencesUtils.saveLong(AddDeviceActivityThree.this, Constant.userName + "did", 0);
                        SharedPreferencesUtils.saveString(AddDeviceActivityThree.this, Constant.userName + "keyClientWAM", "");
                        SharedPreferencesUtils.saveString(AddDeviceActivityThree.this, Constant.userName + "keyClient", "");
                        SharedPreferencesUtils.saveString(AddDeviceActivityThree.this, Constant.userName + "dsig", "");
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
                JDJToast.showMessage(AddDeviceActivityThree.this, getString(R.string.change_name_success));
                Constant.isAddDevice = true;
                dialog.dismiss();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ActivityManager.getAppManager().goToActivity(MainActivity.class);
                    }
                },1000);
//                startActivity(new Intent(AddDeviceActivityThree.this,MainActivity.class));
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        if (!AddDeviceActivityThree.this.isFinishing()) {
            alertDialog.show();
        }
    }

    @Override
    public void update(Observable observable, Object data) {

    }
}
