package com.nodepp.smartnode.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.dtls.DTLSSocket;
import com.nodepp.smartnode.model.UserInfo;
import com.nodepp.smartnode.service.TLSConfiguration;
import com.nodepp.smartnode.service.TLSService;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.utils.DESUtils;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.utils.Utils;

import nodepp.Nodepp;
import tencent.tls.platform.TLSErrInfo;
import tencent.tls.platform.TLSPwdLoginListener;
import tencent.tls.platform.TLSUserInfo;

public class ForgetPasswordActivityFour extends BaseActivity {
    private static final String TAG = ForgetPasswordActivityFour.class.getSimpleName();
    private int CHANGE_TIME = 1;
    private int mTime = 5;
    private TextView tvTime;
    private boolean isRetry = true;
    private PwdLoginListener pwdLoginListener = new PwdLoginListener();
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    mTime--;//倒计时跳转
                    if (mTime > 0) {
                        countDown();
                    } else {
                        if (NetWorkUtils.isNetworkConnected(ForgetPasswordActivityFour.this)) {
                            tlsService.TLSPwdLogin(phone, password, pwdLoginListener);
                        } else {//若刚好自动登录时网络无连接，直接跳转登录界面，手动登录
                            JDJToast.showMessage(ForgetPasswordActivityFour.this, getString(R.string.no_connect_network));
                            startActivity(new Intent(ForgetPasswordActivityFour.this, LoginActivity.class));
                        }
                    }
                    break;
            }
        }
    };
    private TLSService tlsService;
    private String phone;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password_four);
        tlsService = TLSService.getInstance();
        Intent intent = getIntent();
        phone = intent.getStringExtra("phone");
        password = intent.getStringExtra("password");
        initView();
    }

    private void initView() {
        tvTime = (TextView) findViewById(R.id.tv_time);
        countDown();//开始倒计时
    }

    private void countDown() {
        tvTime.setText(mTime + getString(R.string.jump_to_next));
        new Thread() {
            @Override
            public void run() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Message message = handler.obtainMessage();
                        message.what = CHANGE_TIME;
                        handler.sendMessage(message);
                    }
                }, 1000);
            }
        }.start();
    }

    class PwdLoginListener implements TLSPwdLoginListener {
        @Override
        public void OnPwdLoginSuccess(TLSUserInfo userInfo) {
            TLSService.getInstance().setLastErrno(0);
            String username = userInfo.identifier;
            String usersig = tlsService.getUserSig(username);
            if (username.contains("-")) {
                UserInfo.userName = username.substring(username.indexOf("-") + 1);//获取到手机号码
            }
//            SharedPreferencesUtils.saveString(ForgetPasswordActivityFour.this, "username", username);
            SharedPreferencesUtils.saveString(ForgetPasswordActivityFour.this, "nickname", username);
            SharedPreferencesUtils.saveString(ForgetPasswordActivityFour.this, "photoUrl", "");
//            SharedPreferencesUtils.saveString(ForgetPasswordActivityFour.this, "usersig", usersig);
            SharedPreferencesUtils.saveString(ForgetPasswordActivityFour.this, "login_type", "phone");
            getUid(username, usersig);
//            startActivity(new Intent(ForgetPasswordActivityFour.this, MainActivity.class));
//            ActivityManager.getAppManager().finishLastActivity();
        }

        @Override
        public void OnPwdLoginReaskImgcodeSuccess(byte[] picData) {
        }

        @Override
        public void OnPwdLoginNeedImgcode(byte[] picData, TLSErrInfo errInfo) {
        }

        @Override
        public void OnPwdLoginFail(TLSErrInfo errInfo) {
            TLSService.getInstance().setLastErrno(-1);
            Utils.notOK(ForgetPasswordActivityFour.this, errInfo);
            startActivity(new Intent(ForgetPasswordActivityFour.this, LoginActivity.class));
        }

        @Override
        public void OnPwdLoginTimeout(TLSErrInfo errInfo) {
            TLSService.getInstance().setLastErrno(-1);
            Utils.notOK(ForgetPasswordActivityFour.this, errInfo);
            startActivity(new Intent(ForgetPasswordActivityFour.this, LoginActivity.class));
        }
    }

    private void getUid(final String username, final String usersig) {
        String login_type = SharedPreferencesUtils.getString(ForgetPasswordActivityFour.this, "login_type", "");
        if (username.equals("") || usersig.equals("") || login_type.equals("")) {
            JDJToast.showMessage(ForgetPasswordActivityFour.this, getString(R.string.user_info_error));
            return;
        }
        final int type = 1;
        long appId = TLSConfiguration.SDK_APPID;
        if (NetWorkUtils.isNetworkConnected(ForgetPasswordActivityFour.this)) {
            Nodepp.Msg msg = PbDataUtils.setRequestParamToGetUid(type, appId, username, usersig);
            DTLSSocket.send(ForgetPasswordActivityFour.this, null, msg, new ResponseListener() {
                @Override
                public void onSuccess(Nodepp.Msg msg) {
                    int result = msg.getHead().getResult();
                    if (result == 0) {
                        String uidSig = PbDataUtils.byteString2String(msg.getUsig());
                        long uid = msg.getHead().getUid();
                        SharedPreferencesUtils.saveString(ForgetPasswordActivityFour.this, "username", String.valueOf(uid));
                        String mUid = null;
                        try {
                            mUid = DESUtils.encode(String.valueOf(uid));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (msg.hasKeyClientWan()){
                            ByteString keyClientWan = msg.getKeyClientWan();
                            Constant.KEY_A2S = keyClientWan.toByteArray();
                            SharedPreferencesUtils.saveString(ForgetPasswordActivityFour.this, "uidSig", uidSig);//把uid保存下来
                            SharedPreferencesUtils.saveString(ForgetPasswordActivityFour.this, "uid", mUid);//把uid保存下来
                            startActivity(new Intent(ForgetPasswordActivityFour.this, MainActivity.class));
                            ActivityManager.getAppManager().finishLastActivity();
                        }else {
                            Intent intent = new Intent(ForgetPasswordActivityFour.this, LoginActivity.class);
                            startActivity(intent);
                            ActivityManager.getAppManager().finishLastActivity();
                        }

                    } else if (result == 404) {
                        if (isRetry) {
                            getUid(username, usersig);
                            isRetry = false;
                        } else {
                            JDJToast.showMessage(ForgetPasswordActivityFour.this, getString(R.string.get_uid_fail));
                        }

                    }
                }

                @Override
                public void onTimeout(Nodepp.Msg msg) {

                }

                @Override
                public void onFaile() {
                    JDJToast.showMessage(ForgetPasswordActivityFour.this, getString(R.string.get_uid_fail));
                }
            });
        }
    }
}
