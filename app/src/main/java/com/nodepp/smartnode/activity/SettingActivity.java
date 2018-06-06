package com.nodepp.smartnode.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.nodepp.smartnode.R;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;

import java.io.File;

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    private static final String url = "http://www.nodepp.com/skin/nodepp.skin";
    private static final String SKIN_NAME = "nodepp.skin";
    private static final String SKIN_DIR = Environment.getExternalStorageDirectory() + File.separator + "skin";
    private ToggleButton tbDebugInfo;
    private ToggleButton tbSelectType;
    private ToggleButton tbDebug;
    private LinearLayout llDebug;
    private LinearLayout llAbout;
    private ToggleButton tbChangeSkin;
    private LinearLayout llProblem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
    }

    private void initView() {
        llDebug = (LinearLayout) findViewById(R.id.ll_debug);
        llAbout = (LinearLayout) findViewById(R.id.ll_about);
        llProblem = (LinearLayout) findViewById(R.id.ll_problem);
        tbDebugInfo = (ToggleButton) findViewById(R.id.tb_switch);
        tbSelectType = (ToggleButton) findViewById(R.id.tb_switch1);
        tbDebug = (ToggleButton) findViewById(R.id.tb_switch_debug);
        ToggleButton tbUpdateVersion = (ToggleButton) findViewById(R.id.tb_update_version);
        tbChangeSkin = (ToggleButton) findViewById(R.id.tb_change_skin);
        tbDebugInfo.setOnCheckedChangeListener(checkedChangeListener);
        tbSelectType.setOnCheckedChangeListener(checkedChangeListener);
        tbDebug.setOnCheckedChangeListener(checkedChangeListener);
        tbChangeSkin.setOnCheckedChangeListener(checkedChangeListener);
        tbUpdateVersion.setOnCheckedChangeListener(checkedChangeListener);
        boolean isShowDebugInfo = SharedPreferencesUtils.getBoolean(SettingActivity.this, "isShowDebugInfo", false);
        boolean isShowSelectType = SharedPreferencesUtils.getBoolean(SettingActivity.this, "isShowSelectType", false);
        boolean isDebug = SharedPreferencesUtils.getBoolean(SettingActivity.this, "isDebug", false);
        boolean isAutoUpdate = SharedPreferencesUtils.getBoolean(this, "isAutoUpdate", true);
//        boolean isDefaultSkin = SharedPreferencesUtils.getBoolean(this, "isDefaultSkin", true);
//        llDebug.setVisibility(isDebug ? View.VISIBLE : View.GONE);
//        tbDebugInfo.setChecked(isShowDebugInfo);
//        tbSelectType.setChecked(isShowSelectType);
        tbDebug.setChecked(isDebug);
        tbUpdateVersion.setChecked(isAutoUpdate);
//        tbChangeSkin.setChecked(isDefaultSkin);
        llAbout.setOnClickListener(this);
        llProblem.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.ll_problem:
                Intent intent = new Intent(this, WebActivity.class);
                intent.putExtra("url","http://www.nodepp.com/problem.html");
                intent.putExtra("title","使用指南");
                startActivity(intent);
                break;
        }
    }

    CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.tb_switch:
                    SharedPreferencesUtils.saveBoolean(SettingActivity.this, "isShowDebugInfo", isChecked);
                    break;
                case R.id.tb_switch1:
                    SharedPreferencesUtils.saveBoolean(SettingActivity.this, "isShowSelectType", isChecked);
                    break;
                case R.id.tb_switch_debug:
                    SharedPreferencesUtils.saveBoolean(SettingActivity.this, "isDebug", isChecked);
                    llDebug.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    break;
                case R.id.tb_update_version:
                    SharedPreferencesUtils.saveBoolean(SettingActivity.this, "isAutoUpdate", isChecked);
                    break;
                case R.id.tb_change_skin:
                    //切换皮肤
//                    changeSkin(isChecked);
                    break;
            }

        }
    };

//    private void changeSkin(boolean isChecked) {
//        SharedPreferencesUtils.saveBoolean(SettingActivity.this, "isDefaultSkin", !isChecked);
//        if (!isChecked) {
//            SkinManager.getInstance().restoreDefaultTheme();
////            Toast.makeText(getApplicationContext(), "默认", Toast.LENGTH_SHORT).show();
//        } else {
//            File skin = new File(SKIN_DIR + File.separator + SKIN_NAME);
//            if (skin == null || !skin.exists()) {
//                showDownloadDialog();
//                return;
//            }
//            SkinManager.getInstance().load(skin.getAbsolutePath(),
//                    new ILoaderListener() {
//                        @Override
//                        public void onStart() {
//                            L.e("startloadSkin");
//                        }
//
//                        @Override
//                        public void onSuccess() {
//                            Toast.makeText(getApplicationContext(), "切换成功", Toast.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onFailed() {
//                            Toast.makeText(getApplicationContext(), "切换失败", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        }
//    }
//
//    private void showDownloadDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("找不到指定皮肤");
//        builder.setMessage("是否先下载皮肤？");
//        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                DownloadSkinTask downloadTask = new DownloadSkinTask(SettingActivity.this);
//                downloadTask.execute(url, SKIN_DIR, SKIN_NAME);
//                changeSkin(true);
//                dialog.dismiss();
//            }
//        });
//
//        builder.setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                changeSkin(false);
//                dialog.dismiss();
//            }
//        });
//        AlertDialog alertDialog = builder.create();
//        alertDialog.setCanceledOnTouchOutside(false);
//        alertDialog.show();
//    }
}
