package com.nodepp.smartnode.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.dtls.DTLSSocket;
import com.nodepp.smartnode.model.UserInfo;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.UDPClientA2S;
import com.nodepp.smartnode.udp.UDPSocketA2S;
import com.nodepp.smartnode.utils.ClickUtils;
import com.nodepp.smartnode.utils.DESUtils;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.view.PopupWindow.HowConfigColorLightPopup;

import nodepp.Nodepp;

/**
 * 添加插座第一个界面
 */
public class AddDeviceColorLightActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG =AddDeviceColorLightActivity.class.getSimpleName();
    private int yourChoice = -1;
    private ImageView ivLight;
    private boolean isRetry = true;
    private AnimationDrawable lightAnimation;
    private TextView tvHowTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_color_light);
        initView();
    }

    private void initView() {
        ivLight = (ImageView) findViewById(R.id.iv_light);
//        Util.setBackground(this, ivLight, R.mipmap.light);
        Button btnCancle = (Button) findViewById(R.id.btn_cancle);
        Button btnOk = (Button) findViewById(R.id.btn_ok);
        tvHowTo = (TextView) findViewById(R.id.tv_how_to);
        tvHowTo.setText(Html.fromHtml("<u>" + "如何将灯设置成红绿蓝交替闪烁" + "</u>"));
        btnCancle.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        tvHowTo.setOnClickListener(this);
        ivLight.setBackgroundResource(R.drawable.light_change_animation);
        lightAnimation = (AnimationDrawable) ivLight.getBackground();
        lightAnimation.start();
    }

    @Override
    protected void onDestroy() {
//        Util.removeBackground(ivLight);
        lightAnimation.stop();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancle:
                finish();
                break;
            case R.id.tv_how_to:
                HowConfigColorLightPopup howToConfigPopup = new HowConfigColorLightPopup(this);
                howToConfigPopup.show(tvHowTo);
                break;
            case R.id.btn_ok:
                if (ClickUtils.isFastClick(800)) {//防止用户一直快速点击
                    JDJToast.showMessage(this, getString(R.string.no_click_quick));
                } else {
                    if (Constant.userName.equals("65535")){
                        //游客
                        if(!AddDeviceColorLightActivity.this.isFinishing()){
                            showDialog();
                        }
                    }else {
                        UserInfo.isDeviceReturn = false;
                        long did = SharedPreferencesUtils.getLong(AddDeviceColorLightActivity.this, Constant.userName + "did", 0);
                        String dsig = SharedPreferencesUtils.getString(AddDeviceColorLightActivity.this, Constant.userName + "dsig", "");
                        if (did == 0 || dsig.equals("")) {
                            requestDId();
                        } else {//已经存在了的话就不再从服务器请求获取did
                            Log.i(TAG, "本地已经有一个");
                            UserInfo.did = did;
                            UserInfo.dsig = PbDataUtils.string2ByteString(dsig);
                            UserInfo.connetedMode = 1;//优先局域网模式
                            startActivity(new Intent(AddDeviceColorLightActivity.this, AddDeviceActivityTwo.class));
                        }
                    }
                }
                break;
        }
    }
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("没有登录的游客只能使用局域网模式!");
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserInfo.connetedMode = 1;
                startActivity(new Intent(AddDeviceColorLightActivity.this, AddDeviceActivityTwo.class));
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
    private void showSelectDialog() {
        int connectMode = SharedPreferencesUtils.getInt(AddDeviceColorLightActivity.this, "connectMode", 0);
        final String[] items = {"远程通讯模式", "近场通讯模式"};
        yourChoice = connectMode;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(AddDeviceColorLightActivity.this);
        singleChoiceDialog.setTitle("请选择手机与设备通讯的方式");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setCancelable(true);
        singleChoiceDialog.setSingleChoiceItems(items, connectMode,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        yourChoice = which;
                        Log.i(TAG, "===which====" + which);
                    }
                });
        singleChoiceDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (yourChoice != -1) {
                            SharedPreferencesUtils.saveInt(AddDeviceColorLightActivity.this, "connectMode", yourChoice);//0代表远程通讯，1代表近场通讯
                            JDJToast.showMessage(AddDeviceColorLightActivity.this, "你选择了" + items[yourChoice]);
                            UserInfo.connetedMode = yourChoice;
                            Log.i(TAG, "mode=" + UserInfo.connetedMode);
                            startActivity(new Intent(AddDeviceColorLightActivity.this, AddDeviceActivityTwo.class));
                            dialog.dismiss();
                            finish();
                        }
                    }
                });
        singleChoiceDialog.create().show();
    }

    /**
     * 请求服务器获取did
     */
    private void requestDId() {
        Log.i(TAG, "请求");
        //判断wifi连接情况，没有连接时提示用户先连接
        if (NetWorkUtils.isNetworkConnected(this)) {
            String s = SharedPreferencesUtils.getString(this, "uid", "0");
            String uidSig = SharedPreferencesUtils.getString(this, "uidSig", "0");
            Log.i(TAG, "-----加密uid----" + s);
            s = DESUtils.decodeValue(s);
            if (s != null) {
                long uid = Long.parseLong(s);
                Nodepp.Msg msg = PbDataUtils.getDidFromServer(uid, uidSig);
                Log.i(TAG, "msg=1=" + msg.toString());
                UDPSocketA2S.send(AddDeviceColorLightActivity.this, msg, new ResponseListener() {
                    @Override
                    public void onSuccess(Nodepp.Msg msg) {
                        //从服务器获取到did了才能进一步添加设备
                        int result = msg.getHead().getResult();
                        int seq = msg.getHead().getSeq();
                        if (result == 0) {
                            Log.i(TAG, "msg=2=" + msg.toString());
                            UserInfo.did = msg.getDid();
                            String username = SharedPreferencesUtils.getString(AddDeviceColorLightActivity.this, "username", "");
                            SharedPreferencesUtils.saveLong(AddDeviceColorLightActivity.this, username + "did", msg.getDid());
                            Log.i(TAG, "did==" + UserInfo.did);
                            UserInfo.dsig = msg.getDsig();
                            SharedPreferencesUtils.saveString(AddDeviceColorLightActivity.this, username + "dsig", PbDataUtils.byteString2String(msg.getDsig()));
                            Log.i(TAG, "dsig=" + PbDataUtils.byteString2String(msg.getDsig()));
//                            showSelectDialog();
                            UserInfo.connetedMode = 1;//优先局域网模式
                            startActivity(new Intent(AddDeviceColorLightActivity.this, AddDeviceActivityTwo.class));
                        } else if (result == 404) {
                                if (isRetry){
                                    requestDId();
                                    isRetry = false;
                                }else {
                                    JDJToast.showMessage(AddDeviceColorLightActivity.this, getString(R.string.get_did_fail));
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
            } else {
                JDJToast.showMessage(AddDeviceColorLightActivity.this, getString(R.string.no_connect_network));
            }
        }

    }

}
