package com.nodepp.smartnode.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.protobuf.ByteString;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.model.UserInfo;
import com.nodepp.smartnode.service.TLSConfiguration;
import com.nodepp.smartnode.service.TLSService;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.UDPSocketA2S;
import com.nodepp.smartnode.utils.ClickUtils;
import com.nodepp.smartnode.utils.DESUtils;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.utils.Utils;
import com.nodepp.smartnode.view.loadingdialog.LoadingDialog;

import nodepp.Nodepp;
import tencent.tls.platform.TLSErrInfo;
import tencent.tls.platform.TLSPwdLoginListener;
import tencent.tls.platform.TLSPwdRegListener;
import tencent.tls.platform.TLSUserInfo;

public class RegisterActivityTwo extends BaseActivity implements View.OnClickListener {

    private static final String TAG = RegisterActivityTwo.class.getSimpleName();
    private EditText etPassword;
    private EditText etPasswordTwo;
    private PwdLoginListener pwdLoginListener = new PwdLoginListener();
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_two);
        initView();
    }

    private void initView() {
        etPassword = (EditText) findViewById(R.id.et_password);
        etPasswordTwo = (EditText) findViewById(R.id.et_password_two);
        Button btnOk = (Button) findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(this);

    }

    private void regist() {
        if (NetWorkUtils.isNetworkConnected(RegisterActivityTwo.this)) {
            final String password = checkPassword();
            if (password == null) return;
            loadingDialog = new LoadingDialog(this, getString(R.string.registing));
            if (!RegisterActivityTwo.this.isFinishing()){
                loadingDialog.show();
            }
            //注册，回调监听
            tlsService.TLSPwdRegCommit(password, new TLSPwdRegListener() {
                @Override
                public void OnPwdRegAskCodeSuccess(int reaskDuration, int expireDuration) {
                }

                @Override
                public void OnPwdRegReaskCodeSuccess(int reaskDuration, int expireDuration) {
                }

                @Override
                public void OnPwdRegVerifyCodeSuccess() {
                }

                @Override
                public void OnPwdRegCommitSuccess(TLSUserInfo userInfo) {
                    JDJToast.showMessage(RegisterActivityTwo.this, getString(R.string.register_success));
                    String phone = userInfo.identifier;
                    tlsService.TLSPwdLogin(phone, password, pwdLoginListener);
                }

                @Override
                public void OnPwdRegFail(TLSErrInfo errInfo) {
                    Utils.notOK(RegisterActivityTwo.this, errInfo);
                    loadingDialog.dismiss();
                }

                @Override
                public void OnPwdRegTimeout(TLSErrInfo errInfo) {
                    Utils.notOK(RegisterActivityTwo.this, errInfo);
                    loadingDialog.dismiss();
                }
            });
        } else {
            JDJToast.showMessage(this, getString(R.string.no_connect_network));
        }
    }

    /**
     * 对密码的长度和密码输入一致性进行检查
     *
     * @return
     */
    @Nullable
    private String checkPassword() {
        final String password = etPassword.getText().toString();
        String password2 = etPasswordTwo.getText().toString();
        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(password2)) {
            JDJToast.showMessage(RegisterActivityTwo.this, getString(R.string.check_password_is_null));
            return null;
        }
        if (password.length() < 8 || password.length() > 30) {
            JDJToast.showMessage(RegisterActivityTwo.this, getString(R.string.check_password_len));
            return null;
        }
        if (password.length() < 8 || password.length() > 30) {
            JDJToast.showMessage(RegisterActivityTwo.this, getString(R.string.check_password_len));
            return null;
        }
        if (!password.equals(password2)) {
            JDJToast.showMessage(RegisterActivityTwo.this, getString(R.string.password_is_no_same));
            return null;
        }
        return password;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ClickUtils.hideSoftInputView(RegisterActivityTwo.this);//点击空白处的时候隐藏软键盘
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                regist();
                break;
        }
    }

    /**
     * 登录的回调监听
     */
    class PwdLoginListener implements TLSPwdLoginListener {
        @Override
        public void OnPwdLoginSuccess(TLSUserInfo userInfo) {
            TLSService.getInstance().setLastErrno(0);
            String username = userInfo.identifier;
            String usersig = tlsService.getUserSig(username);
            Log.i(TAG, "usersig===" + usersig);
            if (username.contains("-")) {
                UserInfo.userName = username.substring(username.indexOf("-") + 1);//获取到手机号码
            }
            SharedPreferencesUtils.saveString(RegisterActivityTwo.this, "nickname", username);
            SharedPreferencesUtils.saveString(RegisterActivityTwo.this, "photoUrl", "");
//            SharedPreferencesUtils.saveString(RegisterActivityTwo.this, "usersig", usersig);
            SharedPreferencesUtils.saveString(RegisterActivityTwo.this, "login_type", "phone");
            getUid(username, usersig);
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
            Utils.notOK(RegisterActivityTwo.this, errInfo);
            startActivity(new Intent(RegisterActivityTwo.this, LoginActivity.class));
        }

        @Override
        public void OnPwdLoginTimeout(TLSErrInfo errInfo) {
            TLSService.getInstance().setLastErrno(-1);
            Utils.notOK(RegisterActivityTwo.this, errInfo);
            startActivity(new Intent(RegisterActivityTwo.this, LoginActivity.class));
        }
    }

    private void getUid(String username, String usersig) {
        String login_type = SharedPreferencesUtils.getString(RegisterActivityTwo.this, "login_type", "");
        if (username.equals("") || usersig.equals("") || login_type.equals("")) {
            JDJToast.showMessage(RegisterActivityTwo.this, getString(R.string.user_info_error));
            return;
        }
        int type = 1;
        long appId = TLSConfiguration.SDK_APPID;
        if (NetWorkUtils.isNetworkConnected(RegisterActivityTwo.this)) {
            Nodepp.Msg msg = PbDataUtils.setRequestParamToGetUid(type, appId, username, usersig);
            UDPSocketA2S.send(RegisterActivityTwo.this, msg, new ResponseListener() {
                @Override
                public void onSuccess(Nodepp.Msg msg) {
                    int result = msg.getHead().getResult();
                    if (result == 0) {
                        String uidSig = PbDataUtils.byteString2String(msg.getUsig());
                        long uid = msg.getHead().getUid();
                        SharedPreferencesUtils.saveString(RegisterActivityTwo.this, "username", String.valueOf(uid));
                        String mUid = null;
                        try {
                            mUid = DESUtils.encode(String.valueOf(uid));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (msg.hasKeyClientWan()){
                            ByteString keyClientWan = msg.getKeyClientWan();
                            Constant.KEY_A2S = keyClientWan.toByteArray();
                            SharedPreferencesUtils.saveString(RegisterActivityTwo.this, "uidSig", uidSig);//把uid保存下来
                            SharedPreferencesUtils.saveString(RegisterActivityTwo.this, "uid", mUid);//把uid保存下来
                            loadingDialog.dismiss();
                            startActivity(new Intent(RegisterActivityTwo.this, MainActivity.class));
                            ActivityManager.getAppManager().finishLastActivity();
                        }else {
                            startActivity(new Intent(RegisterActivityTwo.this, LoginActivity.class));
                            ActivityManager.getAppManager().finishLastActivity();
                        }

                    } else if (result == 404) {
                        loadingDialog.dismiss();
                        JDJToast.showMessage(RegisterActivityTwo.this, getString(R.string.get_uid_fail));
                    }
                }

                @Override
                public void onTimeout(Nodepp.Msg msg) {

                }

                @Override
                public void onFaile() {
                    loadingDialog.dismiss();
                    JDJToast.showMessage(RegisterActivityTwo.this, getString(R.string.get_uid_fail));
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (loadingDialog != null && loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }
}
