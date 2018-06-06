package com.nodepp.smartnode.service;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.protobuf.ByteString;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.activity.ImgCodeActivity;
import com.nodepp.smartnode.activity.LoginActivity;
import com.nodepp.smartnode.activity.MainActivity;
import com.nodepp.smartnode.activity.RegisterActivity;
import com.nodepp.smartnode.dtls.DTLSSocket;
import com.nodepp.smartnode.model.UserInfo;
import com.nodepp.smartnode.udp.ResponseListener;
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
import tencent.tls.platform.TLSUserInfo;

/**
 * Created by dgy on 15/8/14.
 */
public class PhonePwdLoginService {

    private final static String TAG = "PhonePwdLoginService";

    private Context context;
    private EditText txt_countrycode;
    private EditText txt_phone;
    private EditText txt_pwd;

    private String countrycode;
    private String phone;
    private String password;
    private LoadingDialog loadingDialog;
    private TLSService tlsService;
    public static PwdLoginListener pwdLoginListener;
    public static String mPhone;
    private boolean isRetry = true;

    public PhonePwdLoginService(final Context context,
                                EditText txt_phone,
                                EditText txt_pwd,
                                Button btn_login) {
        this.context = context;
        this.txt_phone = txt_phone;
        this.txt_pwd = txt_pwd;
        tlsService = TLSService.getInstance();
        pwdLoginListener = new PwdLoginListener();
        String user_phone = SharedPreferencesUtils.getString(context, "phone", "");
        this.txt_phone.setText(user_phone);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog = new LoadingDialog(context, context.getString(R.string.logining));
                SharedPreferencesUtils.saveBoolean(PhonePwdLoginService.this.context, "verify", true);
                countrycode = "86"; // 解析国家码
                phone = PhonePwdLoginService.this.txt_phone.getText().toString();
                password = PhonePwdLoginService.this.txt_pwd.getText().toString();
                mPhone = phone;
                // 验证手机号和密码的有效性
                if (TextUtils.isEmpty(phone)) {
                    JDJToast.showMessage(PhonePwdLoginService.this.context, context.getString(R.string.phone_is_no_null));
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    JDJToast.showMessage(PhonePwdLoginService.this.context, context.getString(R.string.password_is_no_null));
                    return;
                }
                Log.e(TAG, Utils.getWellFormatMobile(countrycode, phone));
                if (NetWorkUtils.isNetworkConnected(PhonePwdLoginService.this.context)) {
                    loadingDialog.show();
                    SharedPreferencesUtils.saveString(context, "phone", phone);
                    tlsService.TLSPwdLogin(Utils.getWellFormatMobile(countrycode, phone), password, pwdLoginListener);
                } else {
                    JDJToast.showMessage(PhonePwdLoginService.this.context, context.getString(R.string.no_connect_network));
                }
            }
        });
    }

    private void getUid(final String username, final String usersig) {
        String login_type = SharedPreferencesUtils.getString(context, "login_type", "");
        if (username.equals("") || usersig.equals("") || login_type.equals("")) {
            JDJToast.showMessage(context, context.getString(R.string.user_info_error));
            return;
        }
        int type = 1;
        long appId = TLSConfiguration.SDK_APPID;
        Nodepp.Msg msg = PbDataUtils.setRequestParamToGetUid(type, appId, username, usersig);
        DTLSSocket.send(context, null, msg, new ResponseListener() {
            @Override
            public void onSuccess(Nodepp.Msg msg) {
                Log.i(TAG, msg.toString());
                int result = msg.getHead().getResult();
                if (result == 0) {
                    String uidSig = PbDataUtils.byteString2String(msg.getUsig());
                    long uid = msg.getHead().getUid();
                    SharedPreferencesUtils.saveString(context, "username", String.valueOf(uid));
                    String mUid = null;
                    try {
                        mUid = DESUtils.encode(String.valueOf(uid));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (msg.hasKeyClientWan()){
                        ByteString keyClientWan = msg.getKeyClientWan();
                        Constant.KEY_A2S = keyClientWan.toByteArray();
                        SharedPreferencesUtils.saveString(context, "uidSig", uidSig);//把uid保存下来
                        Log.i(TAG, "-----获取uidSig-----" + uidSig);
                        SharedPreferencesUtils.saveString(context, "uid", mUid);//把uid保存下来
                        Log.i(TAG, "-----请求获取-uid----" + uid);
//                        loadingDialog.hide();
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.putExtra("login", true);
                        context.startActivity(intent);
                    }else {
                        JDJToast.showMessage(context, "登陆出错了");
                    }

                } else if (result == 404) {
                    if (isRetry) {
                        getUid(username, usersig);//重试一次
                        isRetry = false;
                    } else {
                        JDJToast.showMessage(context, context.getString(R.string.get_uid_fail));
                        loadingDialog.dismiss();
                    }
                }
            }

            @Override
            public void onTimeout(Nodepp.Msg msg) {

            }

            @Override
            public void onFaile() {
                JDJToast.showMessage(context, context.getString(R.string.access_server_error));
                loadingDialog.dismiss();
            }
        });
    }

    class PwdLoginListener implements TLSPwdLoginListener {
        @Override
        public void OnPwdLoginSuccess(TLSUserInfo userInfo) {
            JDJToast.showMessage(context, context.getString(R.string.login_success));
            TLSService.getInstance().setLastErrno(0);
            String username = userInfo.identifier;
            String usersig = tlsService.getUserSig(username);
            Log.i(TAG, "username===" + username);
            Log.i(TAG, "usersig===" + usersig);
            if (username.contains("-")) {
                UserInfo.userName = username.substring(username.indexOf("-") + 1);//获取到手机号码
            }
            SharedPreferencesUtils.saveString(context, "nickname", username);
            SharedPreferencesUtils.saveString(context, "photoUrl", "");
//            SharedPreferencesUtils.saveString(context, "username", username);
//            SharedPreferencesUtils.saveString(context, "usersig", usersig);
            SharedPreferencesUtils.saveString(context, "login_type", "phone");
            getUid(username, usersig);
        }

        @Override
        public void OnPwdLoginReaskImgcodeSuccess(byte[] picData) {
        }

        @Override
        public void OnPwdLoginNeedImgcode(byte[] picData, TLSErrInfo errInfo) {
            Intent intent = new Intent(context, ImgCodeActivity.class);
            intent.putExtra(Constants.EXTRA_IMG_CHECKCODE, picData);
            intent.putExtra(Constants.EXTRA_LOGIN_WAY, Constants.PHONEPWD_LOGIN);
            context.startActivity(intent);
            loadingDialog.dismiss();
        }

        @Override
        public void OnPwdLoginFail(TLSErrInfo errInfo) {
            TLSService.getInstance().setLastErrno(-1);
            Utils.notOK(context, errInfo);
            Log.i("asd", errInfo.Msg);
            if (errInfo.Msg.equals("该帐号未注册，需要验证注册。")) {
                Intent intent = new Intent(context, RegisterActivity.class);
                intent.putExtra("phone", mPhone);
                context.startActivity(intent);
            }
            loadingDialog.dismiss();
        }

        @Override
        public void OnPwdLoginTimeout(TLSErrInfo errInfo) {
            TLSService.getInstance().setLastErrno(-1);
            Utils.notOK(context, errInfo);
            loadingDialog.dismiss();
        }
    }

}
