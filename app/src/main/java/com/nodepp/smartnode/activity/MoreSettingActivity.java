package com.nodepp.smartnode.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.dtls.DTLSSocket;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.twocode.encode.GenerateCodeAsyncTask;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.Socket;
import com.nodepp.smartnode.udp.UDPClient;
import com.nodepp.smartnode.udp.UDPClientA2S;
import com.nodepp.smartnode.udp.UDPSocketA2S;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.DESUtils;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.utils.Utils;
import com.nodepp.smartnode.view.loadingdialog.LoadingDialog;

import nodepp.Nodepp;

public class MoreSettingActivity extends BaseActivity {
    private static final String TAG = MoreSettingActivity.class.getSimpleName();
    private TextView tvNewVer;
    private ImageView ivNewVer;
    private TextView tvFimewareVer;
    private LinearLayout llFirmwareVer;
    private LoadingDialog loadingDialog;
    private ToggleButton tbSwitchLebel;
    private CheckBox cbModeOne;
    private CheckBox cbModeTwo;
    private CheckBox cbModeThree;
    private Device deviceModel;
    private boolean isClickChange = true;
    private int retryTime = 0;
    private String versionInfo = "";
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_setting);
        deviceModel = (Device) getIntent().getSerializableExtra("device");
        initView();
        initData();
    }

    private void initView() {
        LinearLayout llCreateGroup = (LinearLayout) findViewById(R.id.ll_create_group);
        llFirmwareVer = (LinearLayout) findViewById(R.id.ll_firmware_ver);
        LinearLayout llChangeDeviceLebel = (LinearLayout) findViewById(R.id.ll_change_device_lebel);
        LinearLayout llDeviceMode = (LinearLayout) findViewById(R.id.ll_deice_mode);
        TextView tvChangeLebelLine = (TextView) findViewById(R.id.tv_change_lebel_line);
        TextView tvLineDeiceMode = (TextView) findViewById(R.id.tv_line_deice_mode);
        if (deviceModel.getDeviceType() == 3 || deviceModel.getDeviceType() == 6 || deviceModel.getDeviceType() == 7 || deviceModel.getDeviceType() == 8 || deviceModel.getDeviceType() == 9) {//彩灯，白灯不显示电平反转功能和模式选择
            llChangeDeviceLebel.setVisibility(View.GONE);
            llDeviceMode.setVisibility(View.GONE);
            tvChangeLebelLine.setVisibility(View.GONE);
            tvLineDeiceMode.setVisibility(View.GONE);
        }
        LinearLayout llChangeDeviceName = (LinearLayout) findViewById(R.id.ll_change_device_name);
        LinearLayout llShareDevice = (LinearLayout) findViewById(R.id.ll_share_device);
        tvNewVer = (TextView) findViewById(R.id.tv_new_version);
        tvFimewareVer = (TextView) findViewById(R.id.tv_fimeware_ver);
        ivNewVer = (ImageView) findViewById(R.id.iv_new_version);
        tbSwitchLebel = (ToggleButton) findViewById(R.id.tb_switch_lebel);
        TextView tvModeOne = (TextView) findViewById(R.id.tv_mode_one);
        TextView tvModeTwo = (TextView) findViewById(R.id.tv_mode_two);
        TextView tvModeThree = (TextView) findViewById(R.id.tv_mode_three);
        cbModeOne = (CheckBox) findViewById(R.id.cb_mode_one);
        cbModeTwo = (CheckBox) findViewById(R.id.cb_mode_two);
        cbModeThree = (CheckBox) findViewById(R.id.cb_mode_three);
        tvModeOne.setOnClickListener(OnChangeListener);
        tvModeTwo.setOnClickListener(OnChangeListener);
        tvModeThree.setOnClickListener(OnChangeListener);
        cbModeOne.setOnClickListener(OnChangeListener);
        cbModeTwo.setOnClickListener(OnChangeListener);
        cbModeThree.setOnClickListener(OnChangeListener);
        llCreateGroup.setOnClickListener(onClickListener);
        llShareDevice.setOnClickListener(onClickListener);
        llFirmwareVer.setOnClickListener(onClickListener);
        llChangeDeviceName.setOnClickListener(onClickListener);
        tbSwitchLebel.setOnCheckedChangeListener(onCheckedChanged);
        llFirmwareVer.setClickable(false);
        loadingDialog = new LoadingDialog(this, "请求固件升级中...");
    }

    @Override
    protected void onDestroy() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        UDPClientA2S.getInstance().setThrowInvalidPackage(true);
        UDPClient.getInstance(this).setThrowInvalidPackage(true);
        super.onDestroy();
    }

    private void initData() {
        if (null != deviceModel) {
            isClickChange = false;//标志，表明不是通过点击
            tbSwitchLebel.setChecked(deviceModel.getFirmwareLevel() == 1 ? true : false);
            isClickChange = true;
            tvFimewareVer.setText(String.valueOf(deviceModel.getFirmwareVersion()));
            UDPClientA2S.getInstance().setThrowInvalidPackage(false);
            UDPClient.getInstance(this).setThrowInvalidPackage(false);
            setDeviceMode(deviceModel.getDeviceMode());
            checkFirmwareInfo(this, deviceModel.getDid());//先查
            checkFirmwareLevel(MoreSettingActivity.this, deviceModel.getDid());
            queryDeviceMode();
        }

    }


    private void queryDeviceMode() {
        if (NetWorkUtils.isNetworkConnected(MoreSettingActivity.this)) {
            String s = SharedPreferencesUtils.getString(this, "username", "0");
            String uidSig = SharedPreferencesUtils.getString(this, "uidSig", "0");
            long uid = Long.parseLong(s);
            Nodepp.Msg msg = PbDataUtils.queryDeviceMode(uid, deviceModel.getDid(), uidSig);
            Log.i(TAG, "send==" + msg.toString());
            if (deviceModel.getIp() != null) {
                Socket.send(MoreSettingActivity.this, deviceModel.getDeviceMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
                    @Override
                    public void onSuccess(Nodepp.Msg msg) {
                        if (msg != null) {
                            Log.i(TAG, "receive==" + msg.toString());
                            int result = msg.getHead().getResult();
                            if (result == 0) {
                                int mode = msg.getDeviceMode();
                                if (mode != deviceModel.getDeviceMode()) {
                                    deviceModel.setDeviceMode(mode);
                                    setDeviceMode(mode);
                                }
                            }
                        }
                    }

                    @Override
                    public void onTimeout(Nodepp.Msg msg) {

                    }

                    @Override
                    public void onFaile() {
                        Log.i(TAG, "query=onFaile=");
                    }
                });
            }

        } else {
            JDJToast.showMessage(MoreSettingActivity.this, getString(R.string.no_connect_network));
        }
    }

    private void changeDeviceMode(int mode) {
        if (NetWorkUtils.isNetworkConnected(MoreSettingActivity.this)) {
            long uid = Long.parseLong(Constant.userName);
            Nodepp.Msg msg = PbDataUtils.changeDeviceMode(uid, deviceModel.getDid(), Constant.usig, mode);
            Log.i(TAG, "send==" + msg.toString());
            Socket.send(MoreSettingActivity.this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
                @Override
                public void onSuccess(Nodepp.Msg msg) {
                    if (msg != null) {
                        Log.i(TAG, "receive==" + msg.toString());
                        int result = msg.getHead().getResult();
                        if (result == 0) {
                            if (msg.hasDeviceMode()){
                                int mode = msg.getDeviceMode();
                                deviceModel.setDeviceMode(mode);
                                controlSocket(0);//重置设备状态成功
                                setDeviceMode(mode);
                                updateDeviceToDB();
                                JDJToast.showMessage(MoreSettingActivity.this, "模式修改成功");
                            }
                        } else {
                            JDJToast.showMessage(MoreSettingActivity.this, "模式失败");
                        }
                    }
                }

                @Override
                public void onTimeout(Nodepp.Msg msg) {

                }

                @Override
                public void onFaile() {
                    setDeviceMode(deviceModel.getDeviceMode());
                }
            });

        } else {
            JDJToast.showMessage(MoreSettingActivity.this, getString(R.string.no_connect_network));
        }
    }

    private void setDeviceMode(int mode) {
        switch (mode) {
            case 0:
                cbModeOne.setChecked(true);
                cbModeTwo.setChecked(false);
                cbModeThree.setChecked(false);
                break;
            case 1:
                cbModeOne.setChecked(false);
                cbModeTwo.setChecked(true);
                cbModeThree.setChecked(false);
                break;
            case 2:
                cbModeOne.setChecked(false);
                cbModeTwo.setChecked(false);
                cbModeThree.setChecked(true);
                break;
        }
    }

    private void updateDeviceToDB() {
        if (deviceModel != null) {
            try {
                DBUtil.getInstance(MoreSettingActivity.this).update(deviceModel, WhereBuilder.b("tid", "=", deviceModel.getTid()).and("userName", "=", Constant.userName));
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }

    private void setFirmwareInfo(boolean isUpdate) {
        if (isUpdate) {
            tvNewVer.setText("发现新版本");
            ivNewVer.setVisibility(View.VISIBLE);
        } else {
            tvNewVer.setText("已经是最新版本");
            ivNewVer.setVisibility(View.INVISIBLE);
        }
        llFirmwareVer.setClickable(isUpdate);
    }

    View.OnClickListener OnChangeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cb_mode_one:
                case R.id.tv_mode_one:
                    //自锁模式
                    changeDeviceMode(0);
                    break;
                case R.id.cb_mode_two:
                case R.id.tv_mode_two:
                    //点动模式
                    changeDeviceMode(1);
                    break;
                case R.id.cb_mode_three:
                case R.id.tv_mode_three:
                    //点动模式
                    changeDeviceMode(2);
                    break;
            }
        }
    };

    private void showAddDialog() {
        Log.i(TAG, "showDialog");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.change_name));
        final EditText editText = new EditText(this);
//        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        builder.setView(editText);
        if (deviceModel != null) {
            String socketName = deviceModel.getSocketName();
            editText.setText(socketName);
            editText.setSelection(editText.getText().length());
        }
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editText.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    JDJToast.showMessage(MoreSettingActivity.this, getString(R.string.device_name));
                } else {
                    if (deviceModel != null) {
                        deviceModel.setSocketName(name);
                        Constant.isDeviceRename = true;
                        SharedPreferencesUtils.saveBoolean(MoreSettingActivity.this, deviceModel.getUserName() + "isUpdateUserWord", true);
                        updateDeviceToDB();
                    }

                }
                dialog.dismiss();
                ActivityManager.getAppManager().goToActivity(MainActivity.class);
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
        if (!MoreSettingActivity.this.isFinishing()) {
            dialog.show();
        }
    }

    CompoundButton.OnCheckedChangeListener onCheckedChanged = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isClickChange) {
                if (isChecked) {
                    changeFirmwareLevel(MoreSettingActivity.this, deviceModel.getDid(), 1);
                } else {
                    changeFirmwareLevel(MoreSettingActivity.this, deviceModel.getDid(), 0);
                }
            }
        }
    };
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_back:
                    finish();
                    break;
                case R.id.ll_change_device_name:
                    showAddDialog();
                    break;
                case R.id.ll_firmware_ver:
                    showUpdateDialog(versionInfo);
                    break;
                case R.id.ll_create_group:
//                    Intent intent = new Intent(MoreSettingActivity.this, SelectGroupControlActivity.class);
//                    intent.putExtra("socket_id", socketId);//socket的did
//                    intent.putExtra("socket_tid", socketTid);//socket的tid
//                    intent.putExtra("connetedMode", connetedMode);
//                    intent.putExtra("deviceType", deviceType);
//                    startActivity(intent);
                    break;
                case R.id.ll_share_device:
                    share();
                    break;
            }
        }
    };

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("device", deviceModel);
        setResult(2, intent);
        super.finish();
    }

    public void share() {
        final LoadingDialog loadingDialog = new LoadingDialog(MoreSettingActivity.this, "正在生成分享二维码...");
        if (!MoreSettingActivity.this.isFinishing()) {
            loadingDialog.show();
        }
        long did = deviceModel.getDid();
        final long tid = deviceModel.getTid();
        long uid = Long.parseLong(Constant.userName);
        final Nodepp.Msg msg = PbDataUtils.requestShareSig(uid, did, Constant.usig);
        Log.i("kk", "requestShareSig=send=msg=" + msg.toString());
        Socket.send(MoreSettingActivity.this, 0, null, msg, "", new ResponseListener() {
            @Override
            public void onSuccess(Nodepp.Msg msg) {
                int result = msg.getHead().getResult();
                if (result != 0) {
                    JDJToast.showMessage(MoreSettingActivity.this, "分享失败了");
                    loadingDialog.dismiss();
                    return;
                } else {
                    String shareSig = PbDataUtils.byteString2String(msg.getShareVerification());
                    if (tid != 0) {
                        GenerateCodeAsyncTask task = new GenerateCodeAsyncTask(MoreSettingActivity.this);
                        String createTwoCodeString = Utils.getCreateTwoCodeString(deviceModel, msg.getShareVerification());
                        task.execute(createTwoCodeString);
                        task.setOnGenerateListener(new GenerateCodeAsyncTask.GenerateListener() {
                            @Override
                            public void onShow(Bitmap bitmap) {
                                View view = View.inflate(MoreSettingActivity.this, R.layout.two_code, null);
                                AlertDialog.Builder builder = new AlertDialog.Builder(MoreSettingActivity.this);
                                builder.setView(view);
                                ImageView imageView = (ImageView) view.findViewById(R.id.iv_two_code);
                                imageView.setImageBitmap(bitmap);
                                AlertDialog dialog = builder.create();
                                dialog.setCancelable(true);
                                dialog.setCanceledOnTouchOutside(true);
                                if (!MoreSettingActivity.this.isFinishing()) {
                                    dialog.show();
                                }
                                loadingDialog.dismiss();
                            }
                        });
                    } else {
                        loadingDialog.dismiss();
                        JDJToast.showMessage(MoreSettingActivity.this, "群组没有分享功能");
                    }
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

    private void checkFirmwareInfo(final Context context, final long did) {
        if (NetWorkUtils.isNetworkConnected(context)) {
            long uid = Long.parseLong(Constant.userName);
            Nodepp.Msg msg = PbDataUtils.setCheckFirmwareVersion(uid, did, Constant.usig, deviceModel.getDeviceType());
            Log.i(TAG, "send==" + msg.toString());
            DTLSSocket.send(context, "", msg, new ResponseListener() {

                @Override
                public void onSuccess(Nodepp.Msg msg) {
                    retryTime = 0;
                    if (msg != null) {
                        Log.i(TAG, "receive==" + msg.toString());
                        int result = msg.getHead().getResult();
                        if (result == 0) {
                            int newVer = msg.getVerNew();
                            int curVer = msg.getVerCur();
                            versionInfo = PbDataUtils.byteString2String(msg.getVerInfo());
                            if (null != deviceModel) {
                                int dbfirmwareVersion = deviceModel.getFirmwareVersion();
                                if (dbfirmwareVersion < curVer) {//当数据库存放当固件版本号小于查询到的，更新保存的数据
                                    deviceModel.setFirmwareVersion(curVer);
                                    tvFimewareVer.setText(String.valueOf(curVer));
                                    updateDeviceToDB();
                                }
                            }
                            if (curVer < newVer) {
                                if (curVer == 20101) {//固件20101版本有问题，强制要求升级
                                    shouldUpdateDialog();
                                }
                                //发现新版本
                                setFirmwareInfo(true);
                            } else {
                                setFirmwareInfo(false);
                            }
                        }
                    }
                }

                @Override
                public void onTimeout(Nodepp.Msg msg) {

                }

                @Override
                public void onFaile() {
                    if (retryTime < 3) {
                        checkFirmwareInfo(context, did);
                        retryTime++;
                    }
                }
            });

        } else {
            JDJToast.showMessage(context, context.getString(R.string.no_connect_network));
        }
    }

    private void changeFirmwareLevel(Context context, long did, int firmwareLevel) {
        if (NetWorkUtils.isNetworkConnected(context)) {
            long uid = Long.parseLong(Constant.userName);
            Nodepp.Msg msg = PbDataUtils.changeFirmwareLevel(uid, did, Constant.usig, firmwareLevel);
            Log.i(TAG, "send==" + msg.toString());
            Socket.send(context, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
                @Override
                public void onSuccess(Nodepp.Msg msg) {
                    if (msg != null) {
                        Log.i(TAG, "receive==" + msg.toString());
                        int result = msg.getHead().getResult();
                        if (result == 0) {
                            if (deviceModel != null) {
                               if (msg.hasState()){
                                   int state = msg.getState();
                                   deviceModel.setFirmwareLevel(state);
                                   isClickChange = false;
                                   tbSwitchLebel.setChecked(state == 1 ? true : false);
                                   isClickChange = true;
                                   updateDeviceToDB();
                                   JDJToast.showMessage(MoreSettingActivity.this, "电平修改成功");
                               }
                            }
                        } else if (result == 404) {

                        }
                    }
                }

                @Override
                public void onTimeout(Nodepp.Msg msg) {

                }

                @Override
                public void onFaile() {

                }
            });

        } else {
            JDJToast.showMessage(context, context.getString(R.string.no_connect_network));
        }
    }

    private void checkFirmwareLevel(Context context, long did) {
        if (NetWorkUtils.isNetworkConnected(context)) {
            long uid = Long.parseLong(Constant.userName);
            Nodepp.Msg msg = PbDataUtils.queryFirmwareLevel(uid, did, Constant.usig);
            Log.i(TAG, "send==" + msg.toString());
            Socket.send(context, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
                @Override
                public void onSuccess(Nodepp.Msg msg) {
                    if (msg != null) {
                        Log.i(TAG, "receive==" + msg.toString());
                        int result = msg.getHead().getResult();
                        if (result == 0) {
                            if (deviceModel != null) {
                                if (msg.hasState()){
                                    int state = msg.getState();
                                    if (deviceModel.getFirmwareLevel() != state) {
                                        deviceModel.setFirmwareLevel(state);
                                        isClickChange = false;
                                        tbSwitchLebel.setChecked(state == 1 ? true : false);
                                        isClickChange = true;
                                        updateDeviceToDB();
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onTimeout(Nodepp.Msg msg) {

                }

                @Override
                public void onFaile() {

                }
            });

        } else {
            JDJToast.showMessage(context, context.getString(R.string.no_connect_network));
        }
    }

    private void updateFirmware(final Context context, long tid, long did) {
        if (NetWorkUtils.isNetworkConnected(context)) {
            long uid = Long.parseLong(Constant.userName);
            Nodepp.Msg msg = PbDataUtils.setUpdateFirmwareParm(uid, tid, did, Constant.usig);
            Log.i(TAG, "send==" + msg.toString());
            loadingDialog.show();
            UDPSocketA2S.send(context, msg, new ResponseListener() {
                @Override
                public void onSuccess(Nodepp.Msg msg) {
                    if (msg != null) {
                        Log.i(TAG, "receive==" + msg.toString());
                        int result = msg.getHead().getResult();
                        if (result == 0) {
                            deviceModel.setConnetedMode(0);//升级的时候把要升级的设备控制模式改为互联网模式，避免局域网模式下升级后固件设备ip变化后控制不了
                            try {
                                DBUtil.getInstance(MoreSettingActivity.this).saveOrUpdate(deviceModel);
                            } catch (DbException e) {
                                e.printStackTrace();
                            }
                            JDJToast.showMessage(context, "请求固件更新成功");
                        } else if (result == 404) {
                            JDJToast.showMessage(context, "请求固件更新失败");
                        }
                        loadingDialog.dismiss();
                    }
                }

                @Override
                public void onTimeout(Nodepp.Msg msg) {

                }

                @Override
                public void onFaile() {
                    loadingDialog.dismiss();
                }
            });

        } else {
            JDJToast.showMessage(context, context.getString(R.string.no_connect_network));
        }
    }

    /**
     * 提示版本更新dialog
     *
     * @param verInfo
     */
    private void showUpdateDialog(String verInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("是否进行固件更新？");
        String info = verInfo.replace(";", "\n");
        builder.setMessage("更新信息：\n" + info);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateFirmware(MoreSettingActivity.this, deviceModel.getTid(), deviceModel.getDid());
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * 强制更新提示dialog
     */
    private void shouldUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请立即更新");
        builder.setMessage("当前版本会影响功能使用，请立即升级到新版本");
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateFirmware(MoreSettingActivity.this, deviceModel.getTid(), deviceModel.getDid());
                dialog.dismiss();
            }
        });

//        builder.setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
    /**
     * 控制设备状态的方法
     *
     * @param operate
     */
    private void controlSocket(int operate) {
        if (NetWorkUtils.isNetworkConnected(this)) {
            Log.i(TAG, "operate===" + operate);
            long uid = Long.parseLong(Constant.userName);
            final Nodepp.Msg msg = PbDataUtils.setRequestParam(16, 1, uid, deviceModel.getDid(), deviceModel.getTid(), operate, Constant.usig);
            Socket.send(MoreSettingActivity.this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
                @Override
                public void onSuccess(Nodepp.Msg msg) {
                    int result = msg.getHead().getResult();
                    if (result == 404) {
                        JDJToast.showMessage(MoreSettingActivity.this, getString(R.string.device_is_not_online));
                    }else if (result == 0){
                        JDJToast.showMessage(MoreSettingActivity.this, "重置设备状态成功");
                    }

                }

                @Override
                public void onTimeout(Nodepp.Msg msg) {

                }

                @Override
                public void onFaile() {


                }

            });
        } else {
            JDJToast.showMessage(this, "网络没有连接，请稍后重试");
        }
    }
}
