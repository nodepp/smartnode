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
import com.nodepp.smartnode.service.Constants;
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
import com.nodepp.smartnode.utils.Utils;
import com.nodepp.smartnode.view.PopupWindow.ClientInfoPopup;
import com.nodepp.smartnode.view.PopupWindow.HowConfigColorLightPopup;
import com.nodepp.smartnode.view.PopupWindow.HowConfigDevicePopup;
import com.nodepp.smartnode.view.loadingdialog.LoadingDialog;

import nodepp.Nodepp;

/**
 * 添加普通设备的界面
 */
public class AddDeviceActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = AddDeviceActivity.class.getSimpleName();
    private int yourChoice = -1;
    private ImageView ivLight;
    private boolean isRetry = true;
    private AnimationDrawable lightAnimation;
    private TextView tvHowTo;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        initView();
    }

    private void initView() {
        ivLight = (ImageView) findViewById(R.id.iv_light);
//        Util.setBackground(this, ivLight, R.mipmap.light);
        Button btnCancle = (Button) findViewById(R.id.btn_cancle);
        Button btnOk = (Button) findViewById(R.id.btn_ok);
        tvHowTo = (TextView) findViewById(R.id.tv_how_to);
        tvHowTo.setText(Html.fromHtml("<u>" + "如何将设备初始化为配网状态" + "</u>"));
        btnCancle.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        tvHowTo.setOnClickListener(this);
//        ivLight.setBackgroundResource(R.drawable.reset_device_animation);
//        lightAnimation = (AnimationDrawable) ivLight.getBackground();
//        lightAnimation.start();
        loadingDialog = new LoadingDialog(this, "请求设备id中");
    }

    @Override
    protected void onResume() {
        super.onResume();
        UDPClientA2S.getInstance().setIsRetry(true);//设置重试
    }

    @Override
    protected void onPause() {
        super.onPause();
        UDPClientA2S.getInstance().setIsRetry(false);//还原
    }

    @Override
    protected void onDestroy() {
//        lightAnimation.stop();
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancle:
                finish();
                break;
            case R.id.tv_how_to:
                ClientInfoPopup clientInfoPopup = new ClientInfoPopup(AddDeviceActivity.this, 1);
                Log.i("len", "len==" + tvHowTo.getMeasuredHeight());
                clientInfoPopup.showAsDropDown(tvHowTo, Utils.Dp2Px(AddDeviceActivity.this, tvHowTo.getWidth()) - 10, -Utils.Dp2Px(AddDeviceActivity.this, tvHowTo.getHeight() / 2));
                clientInfoPopup.setOnPopupListener(new ClientInfoPopup.popupListener() {
                    @Override
                    public void share() {
                        //普通设备
                        HowConfigDevicePopup howConfigDevicePopup = new HowConfigDevicePopup(AddDeviceActivity.this);
                        howConfigDevicePopup.show(tvHowTo);
                    }

                    @Override
                    public void delete() {
                        //彩灯设备
                        HowConfigColorLightPopup howToConfigPopup = new HowConfigColorLightPopup(AddDeviceActivity.this);
                        howToConfigPopup.show(tvHowTo);
                    }
                });
                break;
            case R.id.btn_ok:
                if (ClickUtils.isFastClick(800)) {//防止用户一直快速点击
                    JDJToast.showMessage(this, getString(R.string.no_click_quick));
                } else {
                    UserInfo.isDeviceReturn = false;
                    long did = SharedPreferencesUtils.getLong(AddDeviceActivity.this, Constant.userName + "did", 0);
                    String dsig = SharedPreferencesUtils.getString(AddDeviceActivity.this, Constant.userName + "dsig", "");
                    String keyClient = SharedPreferencesUtils.getString(AddDeviceActivity.this, Constant.userName + "keyClient", "1234567890");
                    String keyClientWAN = SharedPreferencesUtils.getString(AddDeviceActivity.this, Constant.userName + "keyClientWAM", "");
                    if (did == 0 || dsig.equals("")) {
                        requestDId();
                    } else {//已经存在了的话就不再从服务器请求获取did
                        Log.i(TAG, "本地已经有一个");
                        UserInfo.did = did;
                        UserInfo.dsig = PbDataUtils.string2ByteString(dsig);
                        UserInfo.connetedMode = 1;//默认局域网模式
                        UserInfo.keyClient = PbDataUtils.string2ByteString(keyClient);
                        UserInfo.keyClientWAN = PbDataUtils.string2ByteString(keyClientWAN);
//                            UserInfo.keyClient = null;
                        startActivity(new Intent(AddDeviceActivity.this, AddDeviceActivityTwo.class));
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
                startActivity(new Intent(AddDeviceActivity.this, AddDeviceActivityTwo.class));
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
     * 请求服务器获取did
     */
    private void requestDId() {
        Log.i(TAG, "请求");
        //判断wifi连接情况，没有连接时提示用户先连接
        if (NetWorkUtils.isNetworkConnected(this)) {
            if (!AddDeviceActivity.this.isFinishing()) {
                loadingDialog.show();
            }
            long uid = Long.parseLong(Constant.userName);
            Nodepp.Msg msg = PbDataUtils.getDidFromServer(uid, Constant.usig);
            Log.i(TAG, "msg=1=" + msg.toString());
            UDPSocketA2S.send(AddDeviceActivity.this, msg, new ResponseListener() {
                @Override
                public void onSuccess(Nodepp.Msg msg) {
                    //从服务器获取到did了才能进一步添加插座
                    int result = msg.getHead().getResult();
                    int seq = msg.getHead().getSeq();
                    if (result == 0) {
                        Log.i(TAG, "msg=2=" + msg.toString());
                        UserInfo.did = msg.getDid();
                        SharedPreferencesUtils.saveLong(AddDeviceActivity.this, Constant.userName + "did", msg.getDid());
                        Log.i(TAG, "did==" + UserInfo.did);
                        UserInfo.dsig = msg.getDsig();
                        if (msg.hasKeyClientWan()) {
                            UserInfo.keyClientWAN = msg.getKeyClientWan();
                            SharedPreferencesUtils.saveString(AddDeviceActivity.this, Constant.userName  + "keyClientWAM", PbDataUtils.byteString2String(msg.getKeyClientWan()));
                        } else {
                            UserInfo.keyClientWAN = PbDataUtils.string2ByteString("abc");
                            SharedPreferencesUtils.saveString(AddDeviceActivity.this, Constant.userName  + "keyClientWAM", "abc");
                        }
                        UserInfo.keyClient = msg.getKeyClient();
                        SharedPreferencesUtils.saveString(AddDeviceActivity.this, Constant.userName  + "keyClient", PbDataUtils.byteString2String(msg.getKeyClient()));
                        SharedPreferencesUtils.saveString(AddDeviceActivity.this, Constant.userName  + "dsig", PbDataUtils.byteString2String(msg.getDsig()));
                        Log.i(TAG, "dsig=" + PbDataUtils.byteString2String(msg.getDsig()));
//                            showSelectDialog();
                        UserInfo.connetedMode = 1;//互联网模式
                        loadingDialog.dismiss();
                        startActivity(new Intent(AddDeviceActivity.this, AddDeviceActivityTwo.class));
                    } else if (result == 404) {
                        if (isRetry) {
                            requestDId();
                            isRetry = false;
                        } else {
                            JDJToast.showMessage(AddDeviceActivity.this, getString(R.string.get_did_fail));
                        }
                        loadingDialog.dismiss();
                    } else {
                        loadingDialog.dismiss();
                    }
                }

                @Override
                public void onTimeout(Nodepp.Msg msg) {

                }

                @Override
                public void onFaile() {
                    loadingDialog.dismiss();
                    JDJToast.showMessage(AddDeviceActivity.this, getString(R.string.get_did_fail));
                }
            });
        } else {
            JDJToast.showMessage(AddDeviceActivity.this, getString(R.string.no_connect_network));
        }

    }

}
