package com.nodepp.smartnode.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nodepp.smartnode.MyApplication;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.service.VideoService;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.Utils;
import com.nodepp.smartnode.view.TitleBar;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

public class LivePlayerActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = LivePlayerActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 1;

    private boolean mVideoPlay = false;
    private boolean isPause = false;
    private boolean isVisibly = false;
    private TXCloudVideoView mPlayerView;
    private ImageView mLoadingView;
    private boolean mHWDecode = false;
    private Button mBtnPlay;
    private Button mBtnRenderRotation;
    private Button mBtnRenderMode;
    private Button mBtnHWDecode;

    private Button mBtnCacheStrategy;
    private Button mRatioFast;
    private Button mRatioSmooth;
    private Button mRatioAuto;
    private LinearLayout mLayoutCacheStrategy;

    private boolean mVideoPause = false;
    private int mPlayType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;
    private static final int CACHE_STRATEGY_FAST = 1;  //极速
    private static final int CACHE_STRATEGY_SMOOTH = 2;  //流畅
    private static final int CACHE_STRATEGY_AUTO = 3;  //自动

    private ImageView ivShrink;
    private VideoService.MyBind bind = null;
    private boolean isServiceToActivity;
    private boolean isServiceEnterStop = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        isServiceToActivity = getIntent().getBooleanExtra("isServiceToActivity", false);
        initView();
    }

    //初始化titlebar
    private void initTitleBar() {
        TitleBar titleBar = (TitleBar) findViewById(R.id.title_bar);
        titleBar.setRightVisible(TitleBar.TEXT);
        titleBar.setRightText(getString(R.string.close));
        titleBar.setRightClickListener(new TitleBar.RightClickListener() {
            @Override
            public void onClick() {
                //停止服务
                stopService(new Intent(LivePlayerActivity.this, VideoService.class));
                finish();
            }
        });

    }

    private void initView() {
        initTitleBar();
        mPlayerView = (TXCloudVideoView) findViewById(R.id.video_view);
        mPlayerView.disableLog(true);
        mLoadingView = (ImageView) findViewById(R.id.loadingImageView);
        ivShrink = (ImageView) findViewById(R.id.iv_shrink);
        ivShrink.setOnClickListener(this);
        mBtnPlay = (Button) findViewById(R.id.btnPlay);
        MyApplication app = (MyApplication) getApplicationContext();
        app.setVidePlayView(mPlayerView);
        if (isServiceToActivity) {
            Log.d(TAG, "======isServiceToActivity=====");
            Intent intent = new Intent(LivePlayerActivity.this, VideoService.class);
            startService(intent);
            bindService(intent, conn, BIND_AUTO_CREATE);
        }
        mBtnPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent();
                intent1.setAction("com.nodepp");
                intent1.putExtra("bigView", true);
                sendBroadcast(intent1);
                ivShrink.setVisibility(View.VISIBLE);
                Log.d(TAG, "click playbtn isplay:" + mVideoPlay + " ispause:" + mVideoPause + " playtype:" + mPlayType);
                if (bind != null) { //获取服务中的播放状况
                    mVideoPlay = bind.getServiceData().mVideoPlay;
                    mVideoPause = bind.getServiceData().mVideoPause;
                    Log.d(TAG, "click playbtn isplay:" + mVideoPlay + " ispause:" + mVideoPause);
                }
                if (mVideoPlay) {
                    if (mPlayType == TXLivePlayer.PLAY_TYPE_VOD_FLV || mPlayType == TXLivePlayer.PLAY_TYPE_VOD_HLS || mPlayType == TXLivePlayer.PLAY_TYPE_VOD_MP4) {
                        if (mVideoPause) {
                            if (bind != null) {
                                bind.getLivePlayer().resume();
                                Log.i(TAG, "===========resume==============");
                                mBtnPlay.setBackgroundResource(R.drawable.play_pause);
                                ivShrink.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (bind != null) {
                                bind.getLivePlayer().pause();
                                Log.i(TAG, "===========pause==============");
                                mBtnPlay.setBackgroundResource(R.drawable.play_start);
                                ivShrink.setVisibility(View.GONE);
                            }
                        }
                        mVideoPause = !mVideoPause;
                        bind.getServiceData().mVideoPause = mVideoPause;

                    } else {
                        if (bind != null) {
                            if (isServiceToActivity) {
                                isServiceEnterStop = true;
                            }
                            Log.i(TAG, "===========stopPlayRtmp==============");
                            bind.getServiceData().stopPlayRtmp(mBtnPlay);
                            stopLoadingAnimation();
                            mVideoPlay = !mVideoPlay;
                            ivShrink.setVisibility(View.GONE);
                            bind.getServiceData().mVideoPlay = mVideoPlay;
                            if (conn != null) {
                                unbindService(conn);
                                stopService(new Intent(LivePlayerActivity.this, VideoService.class));
                            }
                        }
                        mBtnPlay.setBackgroundResource(R.drawable.play_start);
                    }

                } else {
                    Log.i(TAG, "===========启动服务点击==================");
                    startLoadingAnimation();
                    Intent intent = new Intent(LivePlayerActivity.this, VideoService.class);
                    startService(intent);
                    bindService(intent, conn, BIND_AUTO_CREATE);
                }
            }
        });

        //横屏|竖屏
        mBtnRenderRotation = (Button) findViewById(R.id.btnOrientation);
        mBtnRenderRotation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bind != null) {
                    if (bind.getLivePlayer() == null) {
                        return;
                    }
                    if (bind.getServiceData().mCurrentRenderRotation == TXLiveConstants.RENDER_ROTATION_PORTRAIT) {
                        mBtnRenderRotation.setBackgroundResource(R.drawable.portrait);
                        bind.setRenderRotation(TXLiveConstants.RENDER_ROTATION_LANDSCAPE);

                    } else if (bind.getServiceData().mCurrentRenderRotation == TXLiveConstants.RENDER_ROTATION_LANDSCAPE) {
                        mBtnRenderRotation.setBackgroundResource(R.drawable.landscape);
                        bind.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
                    }
                }
            }
        });

        //平铺模式
        mBtnRenderMode = (Button) findViewById(R.id.btnRenderMode);
        mBtnRenderMode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bind.getLivePlayer() == null) {
                    return;
                }
                if (bind != null) {
                    if (bind.getServiceData().mCurrentRenderMode == TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN) {
                        mBtnRenderMode.setBackgroundResource(R.drawable.fill_mode);
                        bind.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
                    } else if (bind.getServiceData().mCurrentRenderMode == TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION) {
                        mBtnRenderMode.setBackgroundResource(R.drawable.adjust_mode);
                        bind.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
                    }
                }
            }
        });

        //硬件解码
        mBtnHWDecode = (Button) findViewById(R.id.btnHWDecode);
        mBtnHWDecode.getBackground().setAlpha(mHWDecode ? 255 : 100);
        mBtnHWDecode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mHWDecode = !mHWDecode;
                mBtnHWDecode.getBackground().setAlpha(mHWDecode ? 255 : 100);

                if (mHWDecode) {
                    Toast.makeText(LivePlayerActivity.this, "已开启硬件解码加速，切换会重启播放流程!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LivePlayerActivity.this, "已关闭硬件解码加速，切换会重启播放流程!", Toast.LENGTH_SHORT).show();
                }

                if (mVideoPlay) {
                    bind.getServiceData().stopPlayRtmp(mBtnPlay);
                    bind.getServiceData().startPlayRtmp(mBtnPlay);
                    ivShrink.setVisibility(View.VISIBLE);
                    bind.setPlayView(mPlayerView);
                }
            }
        });

        //缓存策略
        mBtnCacheStrategy = (Button) findViewById(R.id.btnCacheStrategy);
        mLayoutCacheStrategy = (LinearLayout) findViewById(R.id.layoutCacheStrategy);
        mBtnCacheStrategy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutCacheStrategy.setVisibility(mLayoutCacheStrategy.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });
        if (bind != null) {
            bind.getServiceData().setCacheStrategy(CACHE_STRATEGY_AUTO);
        }
        mRatioFast = (Button) findViewById(R.id.radio_btn_fast);
        mRatioFast.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bind != null) {
                    bind.getServiceData().setCacheStrategy(CACHE_STRATEGY_FAST);
                }
                mLayoutCacheStrategy.setVisibility(View.GONE);
            }
        });

        mRatioSmooth = (Button) findViewById(R.id.radio_btn_smooth);
        mRatioSmooth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bind != null) {
                    bind.getServiceData().setCacheStrategy(CACHE_STRATEGY_SMOOTH);
                }
                mLayoutCacheStrategy.setVisibility(View.GONE);
            }
        });

        mRatioAuto = (Button) findViewById(R.id.radio_btn_auto);
        mRatioAuto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bind != null) {
                    bind.getServiceData().setCacheStrategy(CACHE_STRATEGY_AUTO);
                }
                mLayoutCacheStrategy.setVisibility(View.GONE);
            }
        });

    }

    //根据当前后台播放器状态同步修改按钮
    private void changeRotateButtonState() {
        if (bind.getServiceData().mCurrentRenderRotation == TXLiveConstants.RENDER_ROTATION_PORTRAIT) {
            mBtnRenderRotation.setBackgroundResource(R.drawable.landscape);
        } else if (bind.getServiceData().mCurrentRenderRotation == TXLiveConstants.RENDER_ROTATION_LANDSCAPE) {
            mBtnRenderRotation.setBackgroundResource(R.drawable.portrait);
        }
        if (bind.getServiceData().mCurrentRenderMode == TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN) {
            mBtnRenderMode.setBackgroundResource(R.drawable.adjust_mode);
        } else if (bind.getServiceData().mCurrentRenderMode == TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION) {
            mBtnRenderMode.setBackgroundResource(R.drawable.fill_mode);
        }
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bind = (VideoService.MyBind) service;
            ivShrink.setVisibility(View.VISIBLE);
            Log.i(TAG, "===========ServiceConnection==================");
            if (isServiceToActivity) {
                if (isServiceEnterStop) {
                    bind.setPlayListener(playListener);
                    if (bind.getServiceData().startPlayRtmp(mBtnPlay)) {
                        startLoadingAnimation();
                        Log.i(TAG, "===========ServiceConnection======111============");
                        ivShrink.setVisibility(View.VISIBLE);
                        bind.setPlayView(mPlayerView);
                        mVideoPlay = !mVideoPlay;
                        bind.getServiceData().mVideoPlay = mVideoPlay;
                    }
                } else {
                    bind.setPlayView(mPlayerView);
                    Log.i(TAG, "===========进入修改screen==================");
                    changeRotateButtonState();
                    bind.setRenderRotation(bind.getServiceData().mCurrentRenderRotation);
                    bind.setRenderMode(bind.getServiceData().mCurrentRenderMode);
                    mVideoPlay = bind.getServiceData().mVideoPlay;//---------------------------------
                    mBtnPlay.setBackgroundResource(R.drawable.play_pause);//---------------------------------
                }
            } else {
                bind.setPlayListener(playListener);
                if (bind.getServiceData().startPlayRtmp(mBtnPlay)) {
                    startLoadingAnimation();
                    ivShrink.setVisibility(View.VISIBLE);
                    bind.setPlayView(mPlayerView);
                    mVideoPlay = !mVideoPlay;
                    bind.getServiceData().mVideoPlay = mVideoPlay;
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    ITXLivePlayListener playListener = new ITXLivePlayListener() {

        @Override
        public void onPlayEvent(int event, Bundle bundle) {
            if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
                stopLoadingAnimation();
            }

            if (event < 0) {
                JDJToast.showMessage(LivePlayerActivity.this, bundle.getString(TXLiveConstants.EVT_DESCRIPTION));
            } else if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
                stopLoadingAnimation();
            }
        }

        @Override
        public void onNetStatus(Bundle bundle) {

        }
    };

    @Override
    public void onDestroy() {
        if (isVisibly) {
            Intent intent1 = new Intent();
            intent1.setAction("com.nodepp");
            intent1.putExtra("bigView", false);
            intent1.putExtra("isVisibly", true);
            sendBroadcast(intent1);
        }
        if (conn != null &&  mVideoPlay) {
            unbindService(conn);
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "===========onPause============");
        if (bind != null && isPause) {
            bind.getLivePlayer().pause();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "===========onStop============");
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                isPause = true;
                Log.i("time","监听home键");
                break;
            default:
                break;
        }

        return super.onKeyDown(keyCode, event);

    }
    @Override
    public void onResume() {
        super.onResume();
        if (bind != null && mPlayerView != null && isPause) {
            bind.getLivePlayer().resume();
            bind.getServiceData().setPlayView(mPlayerView);
            isPause = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_shrink:
                if (Utils.checkOp(this, 24)){
                    Log.i("time","-----------------------------------");
                    isVisibly = true;
                    finish();
                    overridePendingTransition(0, R.anim.left_top_out);
                }else {
                    showPromptDialog();
                }

                break;
            default:
                break;
        }
    }

    private void showPromptDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("悬浮窗口权限被限制了...");
        builder.setMessage("是否前往权限管理界面开启悬浮窗口的权限？");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("前往", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                gotoMiuiPermissioV5();
            }
        });
        builder.show();
    }


    private void startLoadingAnimation() {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.VISIBLE);
            ((AnimationDrawable) mLoadingView.getDrawable()).start();
        }
    }

    private void stopLoadingAnimation() {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.GONE);
            ((AnimationDrawable) mLoadingView.getDrawable()).stop();
        }
    }
    /**
     * 跳转到miui5的权限管理页面
     */
    private void gotoMiuiPermissioV5() {
        Intent i = new Intent("miui.intent.action.APP_PERM_EDITOR");
        ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditor");
        i.setComponent(componentName);
        i.putExtra("extra_pkgname",getPackageName());
        try {
            startActivityForResult(i, PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            gotoMiuiPermission();
        }
    }
    /**
     * 跳转到miui6以上的权限管理页面
     */
    private void gotoMiuiPermission() {
        Intent i = new Intent("miui.intent.action.APP_PERM_EDITOR");
        ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
        i.setComponent(componentName);
        i.putExtra("extra_pkgname",getPackageName());
        try {
            startActivityForResult(i, PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            gotoMeizuPermission();
        }
    }
    /**
     * 跳转到魅族的权限管理系统
     */
    private void gotoMeizuPermission() {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", getPackageName());
        try {
            startActivityForResult(intent, PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            gotoHuaweiPermission();
        }
    }
    /**
     * 华为的权限管理页面
     */
    private void gotoHuaweiPermission() {
        try {
            Intent intent = new Intent(getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity");
            intent.setComponent(comp);
            startActivityForResult(intent, PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            startActivityForResult(getAppDetailSettingIntent(), PERMISSION_REQUEST_CODE);
        }
    }
    /**
     * 获取应用详情页面intent
     *
     * @return
     */
    private Intent getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        return localIntent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PERMISSION_REQUEST_CODE){
            Log.i(TAG,"----------------------------");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}