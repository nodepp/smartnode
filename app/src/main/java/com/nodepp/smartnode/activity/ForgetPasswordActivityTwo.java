package com.nodepp.smartnode.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nodepp.smartnode.R;
import com.nodepp.smartnode.utils.ClickUtils;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.PhoneUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.utils.TimeCountUtil;
import com.nodepp.smartnode.utils.Utils;

import tencent.tls.platform.TLSErrInfo;
import tencent.tls.platform.TLSPwdResetListener;
import tencent.tls.platform.TLSUserInfo;

public class ForgetPasswordActivityTwo extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ForgetPasswordActivityT";
    private Button btnGetCode;
    private String phoneNumber;
    private PwdResetListener pwdResetListener;
    private EditText etCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password_two);
        phoneNumber = getIntent().getStringExtra("phone_number");
        init();
        initView();
    }
    private void init() {
        pwdResetListener = new PwdResetListener();
    }
    private void initView() {
        Button btnOk = (Button) findViewById(R.id.btn_ok);
        btnGetCode = (Button) findViewById(R.id.btn_get_code);
        etCode = (EditText) findViewById(R.id.et_code);
        TextView tvPhoneNum = (TextView) findViewById(R.id.tv_phone_num);
        if (phoneNumber != null){
           tvPhoneNum.setText(PhoneUtils.changPhoneNum(phoneNumber));
            requestCode("86",phoneNumber);
        }
        btnOk.setOnClickListener(this);
        btnGetCode.setOnClickListener(this);
    }

    private void requestCode(String countryCode,String phoneNumber){
        if (NetWorkUtils.isNetworkConnected(this)) {
            long sendMessageTimeStamp = SharedPreferencesUtils.getLong(this, "sendMessageTimeStamp", 0);
            if (Utils.isIn24Hours(sendMessageTimeStamp)){//24小时之内
                //限制找回密码一天只能发3次短信
                int messageCount = SharedPreferencesUtils.getInt(this, "messageCount", 0);
                if (messageCount < 3){
                    //3条以内
                    messageCount ++;
                    SharedPreferencesUtils.saveInt(this, "messageCount", messageCount);
                }else {
                    JDJToast.showMessage(this, this.getString(R.string.send_message_limit));
                    return;
                }
            }else {
                SharedPreferencesUtils.saveInt(this, "messageCount", 1);
                SharedPreferencesUtils.saveLong(this, "sendMessageTimeStamp", System.currentTimeMillis());//记录当前的时间
            }
            int a = SharedPreferencesUtils.getInt(this, "messageCount", 0);
            Log.i(TAG,"current count : "+a);
            tlsService.TLSPwdResetAskCode("86", phoneNumber, pwdResetListener);
        }else {
            JDJToast.showMessage(this, getString(R.string.no_connect_network_try_again));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ClickUtils.hideSoftInputView(ForgetPasswordActivityTwo.this);//点击空白处的时候隐藏软键盘
        return super.onTouchEvent(event);
    }
    @Override
    public void onClick(View v) {
         switch (v.getId()){
             case R.id.btn_get_code:
                 requestCode("86",phoneNumber);
                 break;
             case R.id.btn_ok:
                 if (NetWorkUtils.isNetworkConnected(this)) {
                     tlsService.TLSPwdResetVerifyCode(etCode.getText().toString(), pwdResetListener);
                 }else {
                     JDJToast.showMessage(this, getString(R.string.no_connect_network));
                 }
                 break;
         }
    }

    /**
     * 重置密码的回调监听
     */
    class PwdResetListener implements TLSPwdResetListener {
        @Override
        public void OnPwdResetAskCodeSuccess(int reaskDuration, int expireDuration) {
            JDJToast.showMessage(ForgetPasswordActivityTwo.this, getString(R.string.send_message_suucess) + expireDuration / 60 + getString(R.string.effective_time));
            // 在获取验证码按钮上显示重新获取验证码的时间间隔
            Utils.startTimer(btnGetCode, getString(R.string.get_verification_code), getString(R.string.get_code_again), reaskDuration, 1);
        }

        @Override
        public void OnPwdResetReaskCodeSuccess(int reaskDuration, int expireDuration) {
            JDJToast.showMessage(ForgetPasswordActivityTwo.this, getString(R.string.send_message_again) + expireDuration / 60 + getString(R.string.effective_time));
        }

        @Override
        public void OnPwdResetVerifyCodeSuccess() {
            JDJToast.showMessage(ForgetPasswordActivityTwo.this, getString(R.string.change_password_verify_pass));
            startActivity(new Intent(ForgetPasswordActivityTwo.this, ForgetPasswordActivityThree.class));
            ActivityManager.getAppManager().finishLastActivity();
        }

        @Override
        public void OnPwdResetCommitSuccess(TLSUserInfo userInfo) {}

        @Override
        public void OnPwdResetFail(TLSErrInfo errInfo) {
            Utils.notOK(ForgetPasswordActivityTwo.this, errInfo);
        }

        @Override
        public void OnPwdResetTimeout(TLSErrInfo errInfo) {
            Utils.notOK(ForgetPasswordActivityTwo.this, errInfo);
        }
    }
}
