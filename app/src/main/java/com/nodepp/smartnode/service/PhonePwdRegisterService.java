package com.nodepp.smartnode.service;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nodepp.smartnode.R;
import com.nodepp.smartnode.activity.RegisterActivityTwo;
import com.nodepp.smartnode.helper.Util;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.utils.Utils;

import tencent.tls.platform.TLSErrInfo;
import tencent.tls.platform.TLSPwdRegListener;
import tencent.tls.platform.TLSUserInfo;

/**
 * Created by dgy on 15/8/14.
 */
public class PhonePwdRegisterService {

    private final static String TAG = "PhonePwdRegisterService";

    private Context context;
    private EditText txt_countryCode;
    private EditText txt_phoneNumber;
    private EditText txt_checkCode;
    private Button btn_requireCheckCode;
    private Button btn_verify;

    private String countryCode;
    private String phoneNumber;
    private String checkCode;

    private PwdRegListener pwdRegListener;
    private TLSService tlsService;

    public PhonePwdRegisterService(final Context context,
                                   EditText txt_countryCode,
                                   EditText txt_phoneNumber,
                                   EditText txt_checkCode,
                                   Button btn_requireCheckCode,
                                   Button btn_verify) {
        this.context = context;
        this.txt_countryCode = txt_countryCode;
        this.txt_phoneNumber = txt_phoneNumber;
        this.txt_checkCode = txt_checkCode;
        this.btn_requireCheckCode = btn_requireCheckCode;
        this.btn_verify = btn_verify;

        tlsService = TLSService.getInstance();
        pwdRegListener = new PwdRegListener();

        btn_requireCheckCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countryCode = PhonePwdRegisterService.this.txt_countryCode.getText().toString();
                countryCode = countryCode.substring(countryCode.indexOf('+') + 1);  // 解析国家码
                phoneNumber = PhonePwdRegisterService.this.txt_phoneNumber.getText().toString();

                if (!Utils.validPhoneNumber(countryCode, phoneNumber)) {
                    JDJToast.showMessage(PhonePwdRegisterService.this.context, context.getString(R.string.phone_number_error));
                    return;
                }

                Log.e(TAG, Utils.getWellFormatMobile(countryCode, phoneNumber));
                if (NetWorkUtils.isNetworkConnected(context)) {
                    tlsService.TLSPwdRegAskCode(countryCode, phoneNumber, pwdRegListener);
                }else {
                    JDJToast.showMessage(context, context.getString(R.string.no_connect_network));
                }
            }
        });

        btn_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countryCode = PhonePwdRegisterService.this.txt_countryCode.getText().toString();
                countryCode = countryCode.substring(countryCode.indexOf('+') + 1);  // 解析国家码
                phoneNumber = PhonePwdRegisterService.this.txt_phoneNumber.getText().toString();
                checkCode = PhonePwdRegisterService.this.txt_checkCode.getText().toString();

                if (!Utils.validPhoneNumber(countryCode, phoneNumber)) {
                    JDJToast.showMessage(PhonePwdRegisterService.this.context, context.getString(R.string.phone_number_error));
                    return;
                }

                if (checkCode.length() == 0) {
                    JDJToast.showMessage(PhonePwdRegisterService.this.context, context.getString(R.string.input_code));
                    return;
                }

                Log.e(TAG, Utils.getWellFormatMobile(countryCode, phoneNumber));
                if (NetWorkUtils.isNetworkConnected(context)) {
                    tlsService.TLSPwdRegVerifyCode(checkCode, pwdRegListener);
                }else {
                    JDJToast.showMessage(context, context.getString(R.string.no_connect_network));
                }
            }
        });
    }

    public class PwdRegListener implements TLSPwdRegListener {
        @Override
        public void OnPwdRegAskCodeSuccess(int reaskDuration, int expireDuration) {
            JDJToast.showMessage(context, context.getString(R.string.send_message_suucess) + expireDuration / 60 + context.getString(R.string.effective_time));

            // 在获取验证码按钮上显示重新获取验证码的时间间隔
            Utils.startTimer(btn_requireCheckCode, context.getString(R.string.get_verification_code), context.getString(R.string.get_code_again), reaskDuration, 1);
        }

        @Override
        public void OnPwdRegReaskCodeSuccess(int reaskDuration, int expireDuration) {
            JDJToast.showMessage(context, context.getString(R.string.send_message_again) + expireDuration / 60 + context.getString(R.string.effective_time));
            Utils.startTimer(btn_requireCheckCode, context.getString(R.string.get_verification_code), context.getString(R.string.get_code_again), reaskDuration, 1);
        }

        @Override
        public void OnPwdRegVerifyCodeSuccess() {
            JDJToast.showMessage(context, context.getString(R.string.check_verification_code_success));
            Intent intent = new Intent(context, RegisterActivityTwo.class);
            intent.putExtra(Constants.PHONE_NUMBER, txt_phoneNumber.getText().toString());
            context.startActivity(intent);
        }

        @Override
        public void OnPwdRegCommitSuccess(TLSUserInfo userInfo) {}

        @Override
        public void OnPwdRegFail(TLSErrInfo errInfo) {
            Utils.notOK(context, errInfo);
        }

        @Override
        public void OnPwdRegTimeout(TLSErrInfo errInfo) {
            Utils.notOK(context, errInfo);
        }
    }
}
