package com.nodepp.smartnode.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nodepp.smartnode.R;
import com.nodepp.smartnode.dtls.DTLSSocket;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.UDPSocketA2S;
import com.nodepp.smartnode.utils.CheckUpdate;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.NetWorkUtils;
import com.nodepp.smartnode.utils.PbDataUtils;

import nodepp.Nodepp;

public class AboutActivity extends BaseActivity {

    private static final String TAG = AboutActivity.class.getSimpleName();
    private ImageView ivNewVer;
    private TextView tvNewVer;
    private String verInfo = "";
    private String verUrl = "";
    private int verNew = 0;
    private int localVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();
    }

    private void initView() {
        TextView tvVerName = (TextView) findViewById(R.id.tv_version_name);
        tvNewVer = (TextView) findViewById(R.id.tv_new_version);
        ivNewVer = (ImageView) findViewById(R.id.iv_new_version);
        LinearLayout llUpdate = (LinearLayout) findViewById(R.id.ll_update);
        LinearLayout llShareApp = (LinearLayout) findViewById(R.id.ll_share_app);
        llUpdate.setOnClickListener(onClickListener);
        llShareApp.setOnClickListener(onClickListener);
        String localVersionName = CheckUpdate.getLocalVersionName(this);
        localVersion = CheckUpdate.getLocalVersionCode(this);
        Log.i(TAG, "localVersionName=" + localVersionName);
        tvVerName.setText("V " + localVersionName);
        isUpdateApp(this, localVersion);
    }
    private void showAppTwoCode(){
        View view = View.inflate(this, R.layout.two_code, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
    private void isUpdateApp(Context context, final int localVersion) {
        if (NetWorkUtils.isNetworkConnected(context)) {
            Nodepp.Msg msg = PbDataUtils.setCheckUpdateRequestParam(localVersion);
            Log.i(TAG, "send==" + msg.toString());
            UDPSocketA2S.send(context, msg, new ResponseListener() {
                @Override
                public void onSuccess(Nodepp.Msg msg) {
                    if (msg != null) {
                        Log.i(TAG, "receive==" + msg.toString());
                        verNew = msg.getVerNew();
                        verInfo = PbDataUtils.byteString2String(msg.getVerInfo());
                        verUrl = PbDataUtils.byteString2String(msg.getVerUrl());
                        if (verNew > localVersion) {//检测到新版本，提示更新
                            ivNewVer.setVisibility(View.VISIBLE);
                            tvNewVer.setText("发现新版本");
                        } else {
                            ivNewVer.setVisibility(View.INVISIBLE);
                            tvNewVer.setText("已经是最新版本");
                        }
                    }
                }

                @Override
                public void onFaile() {

                }

                @Override
                public void onTimeout(Nodepp.Msg msg) {

                }
            });
        } else {
            JDJToast.showMessage(context, context.getString(R.string.no_connect_network));
        }
    }

    private void showUpdateDialog(final String path, final int verNew, String verInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("发现新版本");
        String info = verInfo.replace(";", "\n");
        builder.setMessage("更新信息：\n" + info);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CheckUpdate.update(AboutActivity.this, path, verNew);
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

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ll_update:
                    if (verNew > localVersion) {
                        showUpdateDialog(verUrl, verNew, verInfo);
                    } else {
                        JDJToast.showMessage(AboutActivity.this, "已经是最新版本");
                    }
                    break;
                case R.id.ll_share_app:
                    showAppTwoCode();
                    break;
            }
        }
    };
}
