package com.nodepp.smartnode.service;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nodepp.smartnode.R;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.Utils;

import tencent.tls.platform.TLSErrInfo;
import tencent.tls.platform.TLSPwdResetListener;
import tencent.tls.platform.TLSUserInfo;

/**
 * Created by dgy on 15/8/14.
 */
public class ResetPhonePwdService {
    private final static String TAG = "ResetPhonePwdService";

    private Context context;
    private EditText txt_countryCode;
    private EditText txt_phoneNumber;
    private EditText txt_checkCode;
    private Button btn_requireCheckCode;
    private Button btn_verify;

    private String countryCode;
    private String phoneNumber;
    private String checkCode;

    private PwdResetListener pwdResetListener;
    private TLSService tlsService;

    public ResetPhonePwdService(final Context context,
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
        pwdResetListener = new PwdResetListener();

        btn_requireCheckCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countryCode = ResetPhonePwdService.this.txt_countryCode.getText().toString();
                countryCode = countryCode.substring(countryCode.indexOf('+') + 1);  // 解析国家码
                phoneNumber = ResetPhonePwdService.this.txt_phoneNumber.getText().toString();

                if (!Utils.validPhoneNumber(countryCode, phoneNumber)) {
                    JDJToast.showMessage(ResetPhonePwdService.this.context, context.getString(R.string.input_effective_phone_number));
                    return;
                }

                Log.e(TAG, Utils.getWellFormatMobile(countryCode, phoneNumber));
                tlsService.TLSPwdResetAskCode(countryCode, phoneNumber, pwdResetListener);
            }
        });

        btn_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countryCode = ResetPhonePwdService.this.txt_countryCode.getText().toString();
                countryCode = countryCode.substring(countryCode.indexOf('+') + 1);  // 解析国家码
                phoneNumber = ResetPhonePwdService.this.txt_phoneNumber.getText().toString();
                checkCode = ResetPhonePwdService.this.txt_checkCode.getText().toString();

                if (!Utils.validPhoneNumber(countryCode, phoneNumber)) {
                    JDJToast.showMessage(ResetPhonePwdService.this.context, context.getString(R.string.input_effective_phone_number));
                    return;
                }

                if (checkCode.length() == 0) {
                    JDJToast.showMessage(ResetPhonePwdService.this.context, context.getString(R.string.input_code));
                    return;
                }

                Log.e(TAG, Utils.getWellFormatMobile(countryCode, phoneNumber));

                tlsService.TLSPwdResetVerifyCode(checkCode, pwdResetListener);
            }
        });
    }

    class PwdResetListener implements TLSPwdResetListener {
        @Override
        public void OnPwdResetAskCodeSuccess(int reaskDuration, int expireDuration) {
            JDJToast.showMessage(context, context.getString(R.string.send_message_suucess) + expireDuration / 60 + context.getString(R.string.effective_time));

            // 在获取验证码按钮上显示重新获取验证码的时间间隔
            Utils.startTimer(btn_requireCheckCode, context.getString(R.string.get_verification_code), context.getString(R.string.get_code_again), reaskDuration, 1);
        }

        @Override
        public void OnPwdResetReaskCodeSuccess(int reaskDuration, int expireDuration) {
            JDJToast.showMessage(context, context.getString(R.string.send_message_again) + expireDuration / 60 + context.getString(R.string.effective_time));
            Utils.startTimer(btn_requireCheckCode, context.getString(R.string.get_verification_code), context.getString(R.string.get_code_again), reaskDuration, 1);
        }

        @Override
        public void OnPwdResetVerifyCodeSuccess() {
//            Util.showToast(context, "改密验证通过");
//            Intent intent = new Intent(context, PhonePwdLoginActivity.class);
//            intent.putExtra(Constants.EXTRA_PHONEPWD_REG_RST, Constants.PHONEPWD_RESET);
//            intent.putExtra(Constants.COUNTRY_CODE, txt_countryCode.getText().toString());
//            intent.putExtra(Constants.PHONE_NUMBER, txt_phoneNumber.getText().toString());
//            context.startActivity(intent);
//            ((Activity)context).finish();
        }

        @Override
        public void OnPwdResetCommitSuccess(TLSUserInfo userInfo) {}

        @Override
        public void OnPwdResetFail(TLSErrInfo errInfo) {
            Utils.notOK(context, errInfo);
        }

        @Override
        public void OnPwdResetTimeout(TLSErrInfo errInfo) {
            Utils.notOK(context, errInfo);
        }
    }
}
