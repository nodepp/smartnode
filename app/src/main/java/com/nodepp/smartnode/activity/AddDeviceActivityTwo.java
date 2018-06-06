package com.nodepp.smartnode.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.esptouch.EspWifiAdminSimple;
import com.nodepp.smartnode.esptouch.EsptouchTask;
import com.nodepp.smartnode.esptouch.IEsptouchListener;
import com.nodepp.smartnode.esptouch.IEsptouchResult;
import com.nodepp.smartnode.utils.ClickUtils;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.view.CircleLoadingDialog;

import java.util.List;

/**
 * 添加插座第二个界面
 */
public class AddDeviceActivityTwo extends BaseActivity implements View.OnClickListener {
    private static final String TAG = AddDeviceActivityTwo.class.getSimpleName();
    private TextView wifiName;
    private EspWifiAdminSimple mWifiAdmin;
    private EditText etPassword;
    private CircleLoadingDialog circleLoadingDialog;
    private int version;
    private ProgressDialog pd;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:

                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_two);
        mWifiAdmin = new EspWifiAdminSimple(this);
        version = Integer.parseInt(Build.VERSION.SDK);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        String apSsid = mWifiAdmin.getWifiConnectedSsid();
        if (apSsid != null) {
            wifiName.setText(apSsid);//设置wifi名称
        } else {
            wifiName.setText("");
        }
        String password = SharedPreferencesUtils.getString(AddDeviceActivityTwo.this, apSsid, "");
        Log.i(TAG, "password==" + password);
        if (!password.equals("")) {
            etPassword.setText(password);
        }
    }

    private void initView() {
        Button btnOk = (Button) findViewById(R.id.btn_ok);
        wifiName = (TextView) findViewById(R.id.tv_wifi_name);
        etPassword = (EditText) findViewById(R.id.et_password);
        btnOk.setOnClickListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ClickUtils.hideSoftInputView(AddDeviceActivityTwo.this);//点击空白处的时候隐藏软键盘
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                int connectedType = NetWorkUtils.getConnectedType(this);
                if (connectedType == 1) {
                    sendNameandPassword();
                    Constant.routerMac = NetWorkUtils.getRouterMac(this);
                } else {
                    JDJToast.showMessage(this, "请先连接wifi");
                }

                break;
        }
    }

    /**
     * 给设备发送wifi的名称和密码
     */
    private void sendNameandPassword() {
        String apSsid = wifiName.getText().toString();
        String apBssid = mWifiAdmin.getWifiConnectedBssid();
        if (TextUtils.isEmpty(apSsid)) {
            JDJToast.showMessage(AddDeviceActivityTwo.this, getString(R.string.connect_wifi_again));
            return;
        }
        String apPassword = etPassword.getText().toString();
        Log.i(TAG, "apPassword==" + apPassword);
        if (TextUtils.isEmpty(apPassword)) {
            JDJToast.showMessage(AddDeviceActivityTwo.this, getString(R.string.input_wifi_password));
            return;
        }
        SharedPreferencesUtils.saveString(AddDeviceActivityTwo.this, apSsid, apPassword);
        new EsptouchAsyncTask().execute(apSsid, apBssid, apPassword,
                "NO", "0");
    }

    /**
     * 配置成功的回调监听
     */
    private IEsptouchListener myListener = new IEsptouchListener() {

        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {

            handler.post(new Runnable() {
              @Override
              public void run() {
                  String text = getString(R.string.connect_wifi_success);
                  if (version >= Build.VERSION_CODES.KITKAT) {
                      circleLoadingDialog.setTitle(text);
                  }
                  if (version <  Build.VERSION_CODES.KITKAT) {
                      pd.dismiss();
                  } else {
                      circleLoadingDialog.dismiss();
                  }
                  JDJToast.showMessage(AddDeviceActivityTwo.this, "配置成功");
                  startActivity(new Intent(AddDeviceActivityTwo.this, AddDeviceActivityThree.class));//连上wifi跳到下一个界面
                  finish();
              }
          });
        }
    };

    /**
     * 创建异步任务
     */
    private class EsptouchAsyncTask extends AsyncTask<String, Void, List<IEsptouchResult>> {
        private EsptouchTask mEsptouchTask;
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {

            if (version < 19) {//低版本用简单动画
                pd = new ProgressDialog(AddDeviceActivityTwo.this);
                pd.setCanceledOnTouchOutside(false);
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.setMessage("正在连接中...");
                pd.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        synchronized (mLock) {
                            if (mEsptouchTask != null) {
                                mEsptouchTask.interrupt();
                            }
                        }
                        dialog.dismiss();
                    }
                });
                if (!AddDeviceActivityTwo.this.isFinishing()){
                    pd.show();
                }

            } else {//4.4以上用自定义动画
                if (circleLoadingDialog == null) {//避免重复创建
                    circleLoadingDialog = new CircleLoadingDialog(AddDeviceActivityTwo.this);
                    circleLoadingDialog.setOnRetryClickListener(new CircleLoadingDialog.OnClickListener() {
                        @Override
                        public void click() {
                            sendNameandPassword();//重新连接按钮的回调
                        }
                    });
                }
                circleLoadingDialog.setOnCancleClickListener(new CircleLoadingDialog.OnCancleClickListener() {
                    @Override
                    public void onCancle() {//关闭按钮回调
                        synchronized (mLock) {
                            if (mEsptouchTask != null) {
                                mEsptouchTask.interrupt();
                            }
                        }
                    }
                });
                if (!AddDeviceActivityTwo.this.isFinishing()){
                    circleLoadingDialog.show();
                }
            }
        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            synchronized (mLock) {
                String apSsid = params[0];
                String apBssid = params[1];
                String apPassword = params[2];
                String isSsidHiddenStr = params[3];
                String taskResultCountStr = params[4];
                boolean isSsidHidden = false;
                if (isSsidHiddenStr.equals("YES")) {
                    isSsidHidden = true;
                }
                taskResultCount = Integer.parseInt(taskResultCountStr);
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
                        isSsidHidden, AddDeviceActivityTwo.this);
                mEsptouchTask.setEsptouchListener(myListener);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                int count = 0;
                // max results to be displayed, if it is more than maxDisplayCount,
                // just show the count of redundant ones
                final int maxDisplayCount = 5;
                // the task received some results including cancelled while
                // executing before receiving enough results
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    for (IEsptouchResult resultInList : result) {
//                        sb.append(getString(R.string.connected_success) + "bssid = "
//                                + resultInList.getBssid()
//                                + ",InetAddress = "
//                                + resultInList.getInetAddress()
//                                .getHostAddress() + "\n");
                        count++;
                        if (count >= maxDisplayCount) {
                            break;
                        }
                    }
                    if (count < result.size()) {
                        sb.append("\nthere's " + (result.size() - count)
                                + " more result(s) without showing\n");
                    }
                    Log.i(TAG, "msg====" + sb.toString());
                } else {
                    if (version < 19) {
                        pd.dismiss();
                        JDJToast.showMessage(AddDeviceActivityTwo.this, getString(R.string.connect_again));
                    } else {
                        circleLoadingDialog.setTitle(getString(R.string.connect_again));
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (circleLoadingDialog != null){
            circleLoadingDialog.dismiss();
        }
        if (pd != null){
            pd.dismiss();
        }
        super.onDestroy();
    }
}
