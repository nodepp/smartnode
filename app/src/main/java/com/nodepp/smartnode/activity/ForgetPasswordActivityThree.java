package com.nodepp.smartnode.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nodepp.smartnode.R;
import com.nodepp.smartnode.utils.ClickUtils;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.Utils;

import tencent.tls.platform.TLSErrInfo;
import tencent.tls.platform.TLSPwdResetListener;
import tencent.tls.platform.TLSUserInfo;

public class ForgetPasswordActivityThree extends BaseActivity implements View.OnClickListener {

    private static final String TAG = ForgetPasswordActivity.class.getSimpleName();
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password_three);
        initView();
    }

    private void initView() {
        Button btnOk = (Button) findViewById(R.id.btn_ok);
        etPassword = (EditText) findViewById(R.id.et_password);
        btnOk.setOnClickListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ClickUtils.hideSoftInputView(ForgetPasswordActivityThree.this);//点击空白处的时候隐藏软键盘
        return super.onTouchEvent(event);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                if (NetWorkUtils.isNetworkConnected(this)) {
                    resetPassword(etPassword.getText().toString());
                } else {
                    JDJToast.showMessage(this, getString(R.string.no_connect_network));
                }
                break;
        }
    }

    /**
     * 重置密码的回调监听
     * @param password
     */
    private void resetPassword(final String password) {
        tlsService.TLSPwdResetCommit(password, new TLSPwdResetListener() {
            @Override
            public void OnPwdResetAskCodeSuccess(int reaskDuration, int expireDuration) {
            }

            @Override
            public void OnPwdResetReaskCodeSuccess(int reaskDuration, int expireDuration) {
            }

            @Override
            public void OnPwdResetVerifyCodeSuccess() {
            }

            @Override
            public void OnPwdResetCommitSuccess(TLSUserInfo userInfo) {
                String phone = userInfo.identifier;
                Log.i(TAG, "identifier==" + phone);
                Log.i(TAG, "password==" + password);
                JDJToast.showMessage(ForgetPasswordActivityThree.this, getString(R.string.reset_password_success));
                Intent intent = new Intent(ForgetPasswordActivityThree.this, ForgetPasswordActivityFour.class);
                intent.putExtra("phone", phone);
                intent.putExtra("password", password);
                startActivity(intent);
                ActivityManager.getAppManager().finishLastActivity();
            }

            @Override
            public void OnPwdResetFail(TLSErrInfo errInfo) {
                Utils.notOK(ForgetPasswordActivityThree.this, errInfo);
            }

            @Override
            public void OnPwdResetTimeout(TLSErrInfo errInfo) {
                Utils.notOK(ForgetPasswordActivityThree.this, errInfo);
            }
        });
    }
}
