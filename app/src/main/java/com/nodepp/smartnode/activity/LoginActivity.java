package com.nodepp.smartnode.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.dtls.DTLSClient;
import com.nodepp.smartnode.dtls.DTLSSocket;
import com.nodepp.smartnode.model.UserInfo;
import com.nodepp.smartnode.service.TLSConfiguration;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.utils.ClickUtils;
import com.nodepp.smartnode.utils.DESUtils;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.view.loadingdialog.LoadingDialog;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;
import nodepp.Nodepp;

/**
 * Created by yuyue on 2016/8/5.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private final static String TAG = LoginActivity.class.getSimpleName();
    private String SCOPE = "all"; // 所有权限
    private Tencent mTencent;
    private LoadingDialog loadingDialog;
    private boolean isRetry = true;
    private EditText etPassword;
    private boolean isLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        isLogout = getIntent().getBooleanExtra("logout", false);
        init();
        initView();
    }

    private void init() {
        mTencent = Tencent.createInstance(TLSConfiguration.QQ_APP_ID, getApplicationContext());
    }

    private void initView() {
        Button btnLogin = (Button) findViewById(R.id.login_btn);
        TextView tvRegister = (TextView) findViewById(R.id.tv_register);
        LinearLayout QQLogin = (LinearLayout) findViewById(R.id.ll_qq_login);
        TextView tvForgetPass = (TextView) findViewById(R.id.tv_forget_pass);
        LinearLayout llAll = (LinearLayout) findViewById(R.id.ll_all);
        ImageView ivChangeHost = (ImageView) findViewById(R.id.iv_change_host);
        ivChangeHost.setOnClickListener(this);
        if (Constant.isDebug){
            ivChangeHost.setVisibility(View.VISIBLE);
        }
        CheckBox cbEye = (CheckBox) findViewById(R.id.cb_password);
        etPassword = (EditText) findViewById(R.id.et_password);
        cbEye.setOnCheckedChangeListener(onCheckedChangeListener);
        llAll.setOnTouchListener(onTouchListener);
        tvRegister.setOnClickListener(this);
        tvForgetPass.setOnClickListener(this);
        QQLogin.setOnClickListener(this);
        ivChangeHost.setOnClickListener(this);
        //登录验证，跳到主页
        tlsService.initPhonePwdLoginService(this, (EditText) findViewById(R.id.et_phone), etPassword, btnLogin);
    }

    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ClickUtils.hideSoftInputView(LoginActivity.this);//点击空白处的时候隐藏软键盘
            return false;
        }
    };
    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.cb_password:
                    if (isChecked) {
                        //选中显示密码
                        etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    } else {
                        //隐藏密码
                        etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }
                    //把光标设置在文字结尾
                    etPassword.setSelection(etPassword.getText().length());
                    break;
            }
        }
    };

    private void showChangeNetDialog() {
        final EditText editText = new EditText(LoginActivity.this);
        editText.setHint("请输入");
        editText.setText(SharedPreferencesUtils.getString(LoginActivity.this, "host",""));
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("请输入需要连接的地址：");
        builder.setView(editText);
        builder.setCancelable(false);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String address = editText.getText().toString().trim();
                SharedPreferencesUtils.saveString(LoginActivity.this, "host", address);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_change_host:
                //设置host
                showChangeNetDialog();
                break;
            case R.id.tv_register:
                //跳到注册页面
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
//                checkSDKLogin();
                break;
            case R.id.tv_forget_pass:
                //跳转到忘记密码界面
                startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
                break;
            case R.id.ll_qq_login:
                SharedPreferencesUtils.saveBoolean(LoginActivity.this, "verify", true);
                loadingDialog = new LoadingDialog(this, getString(R.string.qq_login_show));
                if (!LoginActivity.this.isFinishing()){
                    loadingDialog.show();
                }
                if (NetWorkUtils.isNetworkConnected(this)) {
                    if (!mTencent.isSessionValid()) {
                        //调用qq第三方登录
                        mTencent.login(this, SCOPE, new IUiListener() {
                            @Override
                            public void onComplete(Object o) {
                                JSONObject jsonObject = (JSONObject) o;
                                Log.i(TAG, "qq=jsonObject=" + jsonObject.toString());
                                try {
                                    String openid = jsonObject.getString("openid");
                                    String accessToken = jsonObject.getString("access_token");
                                    SharedPreferencesUtils.saveString(LoginActivity.this, "login_type", "qq");
                                    Log.i(TAG, "openid==" + openid);
                                    Log.i(TAG, "accessToken==" + accessToken);
                                    requestUserInfo(openid, accessToken);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(UiError uiError) {
                                loadingDialog.hide();
                                JDJToast.showMessage(LoginActivity.this, getString(R.string.authorize_fail));
                            }

                            @Override
                            public void onCancel() {
                                loadingDialog.hide();
                                JDJToast.showMessage(LoginActivity.this, getString(R.string.authorize_cancle));
                            }
                        });
                    }
                } else {
                    JDJToast.showMessage(LoginActivity.this, getString(R.string.no_connect_network));
                    loadingDialog.hide();
                }
                break;
        }
    }
    private void checkSDKLogin() {
        EditText etPhone = (EditText) findViewById(R.id.et_phone);
        String s = etPhone.getText().toString().trim();
        long appid = Long.parseLong(s);
        if (NetWorkUtils.isNetworkConnected(LoginActivity.this)) {
            loadingDialog = new LoadingDialog(this,"请求uid中...");
            loadingDialog.show();
            Nodepp.Msg msg = PbDataUtils.getSDKUid(this,s,65539 ,"PKdhtXMmr18n2L9K");
            Log.i(TAG, "getUid==send===" + msg.toString());
            DTLSSocket.send(LoginActivity.this, null, msg, new ResponseListener() {
                @Override
                public void onSuccess(Nodepp.Msg msg) {
                    int result = msg.getHead().getResult();
                    if (result == 0) {
                        loadingDialog.hide();
                        String uidSig = PbDataUtils.byteString2String(msg.getUsig());
                        long uid = msg.getUid();
                        showResultDialog(uid,uidSig);
                    } else if (result == 1000){
                        loadingDialog.hide();
                    }else if (result == 404) {
                        loadingDialog.hide();
                    }
                }

                @Override
                public void onTimeout(Nodepp.Msg msg) {

                }

                @Override
                public void onFaile() {
                    JDJToast.showMessage(LoginActivity.this, getString(R.string.access_server_error));
                    loadingDialog.hide();
                }
            });

        }
    }
    private void showResultDialog(long uid,String uidSig) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        TextView textView = new TextView(this);
        textView.setText("uid:"+uid+";  uidSig:"+uidSig);
        textView.setTextColor(Color.GRAY);
        textView.setTextSize(20);
        builder.setView(textView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        if (loadingDialog != null){
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == Constants.REQUEST_LOGIN) {
            Tencent.onActivityResultData(requestCode, resultCode, data, new IUiListener() {
                @Override
                public void onComplete(Object response) {
                    JSONObject object = (JSONObject) response;
                    try {
                        String openid = object.getString("openid");
                        String access_token = object.getString("access_token");
                        Log.i(TAG, "qq==openid==" + openid);
                        Log.i(TAG, "qq==access_token==" + access_token);
                        UserInfo.userName = openid;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(UiError uiError) {
                    SharedPreferencesUtils.saveString(LoginActivity.this, "username", "0");
                    SharedPreferencesUtils.saveString(LoginActivity.this, "usersig", "0");
                }

                @Override
                public void onCancel() {
                    SharedPreferencesUtils.saveString(LoginActivity.this, "username", "0");
                    SharedPreferencesUtils.saveString(LoginActivity.this, "usersig", "0");
                }
            });
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    /**
     * 通过参数进行https请求，获得nickname和头像地址
     *
     * @param openid
     * @param access_token
     */
    private void requestUserInfo(final String openid, final String access_token) {
        HttpUtils httpUtils = new HttpUtils();
        String url = "https://graph.qq.com/user/get_user_info?access_token=" + access_token + "&oauth_consumer_key=" + TLSConfiguration.QQ_APP_ID + "&openid=" + openid;
        httpUtils.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result;
                Log.i(TAG, "qq==responseInfo==" + result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String nickname = jsonObject.getString("nickname");
                    String photoUrl = jsonObject.getString("figureurl_qq_2");
                    SharedPreferencesUtils.saveString(LoginActivity.this, "nickname", nickname);
                    SharedPreferencesUtils.saveString(LoginActivity.this, "photoUrl", photoUrl);
                    Log.i(TAG, "qq==nickname==" + nickname);
                    Log.i(TAG, "qq==photoUrl==" + photoUrl);
                    getUid(openid, access_token);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Log.i(TAG, "qq==onFailure==" + s);
            }
        });
    }

    private void getUid(final String username, final String usersig) {
        int type = 2;
        long appId = Long.parseLong(TLSConfiguration.QQ_APP_ID);
        if (NetWorkUtils.isNetworkConnected(LoginActivity.this)) {
            Nodepp.Msg msg = PbDataUtils.setRequestParamToGetUid(type, appId, username, usersig);
            Log.i(TAG, "------------------username---------------------" + username);
            Log.i(TAG, "------------------usersig---------------------" + usersig);
            Log.i(TAG, "getUid==send===" + msg.toString());
            DTLSSocket.send(LoginActivity.this, null, msg, new ResponseListener() {
                @Override
                public void onSuccess(Nodepp.Msg msg) {
                    Log.i(TAG, "getUid==receive===" + msg.toString());
                    Log.i(TAG, "-----------------in----------------------");
                    int result = msg.getHead().getResult();
                    if (result == 0) {
                        String uidSig = PbDataUtils.byteString2String(msg.getUsig());
                        long uid = msg.getHead().getUid();
                        SharedPreferencesUtils.saveString(LoginActivity.this, "username", String.valueOf(uid));
                        String mUid = null;
                        try {
                            mUid = DESUtils.encode(String.valueOf(uid));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (msg.hasKeyClientWan()){
                            ByteString keyClientWan = msg.getKeyClientWan();
                            Log.i("appkey","key:"+keyClientWan.toStringUtf8());
                            Constant.KEY_A2S = keyClientWan.toByteArray();
                            SharedPreferencesUtils.saveString(LoginActivity.this, "uidSig", uidSig);//把uidSig保存下来
                            Log.i(TAG, "-----获取uidSig-----" + uidSig);
                            SharedPreferencesUtils.saveString(LoginActivity.this, "uid", mUid);//把uid保存下来
                            Log.i(TAG, "-----请求获取-uid----" + uid);
                            loadingDialog.hide();
                            JDJToast.showMessage(LoginActivity.this, getString(R.string.authorize_success));
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("login", true);
                            startActivity(intent);
                            LoginActivity.this.finish();
                        }else {
                            JDJToast.showMessage(LoginActivity.this,"登陆出错");
                        }
                    } else if (result == 404) {
                        Log.i(TAG, "-----result----" + result);
                        if (isRetry) {
                            getUid(username, usersig);
                            isRetry = false;
                        } else {
                            JDJToast.showMessage(LoginActivity.this, getString(R.string.get_uid_fail));
                            loadingDialog.hide();
                        }
                    }
                }

                @Override
                public void onTimeout(Nodepp.Msg msg) {

                }

                @Override
                public void onFaile() {
                    JDJToast.showMessage(LoginActivity.this, getString(R.string.access_server_error));
                    loadingDialog.hide();
                }
            });

        }
    }

}
