package com.nodepp.smartnode.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.nodepp.smartnode.MyApplication;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.activity.LivePlayerActivity;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.util.List;

/**
 * Created by yuyue on 2017/2/20.
 */
public class VideoService extends Service {
    private static String TAG = "VideoService";
    private WindowManager.LayoutParams params;
    private WindowManager mWM;
    private View view;
    private int winWidth;
    private int winHeight;
    private int startX;
    private int startY;
    private boolean isMove = false;//默认是点击，没有移动
    public boolean mVideoPlay = false; //播放器的是否在播放
    public boolean mVideoPause = false;//播放器的是否暂停
    String playUrl = "rtmp://7631.liveplay.myqcloud.com/live/7631_123456";//播放地址
    private TXLivePlayer mLivePlayer = null;
    private TXLivePlayConfig mPlayConfig;
    private boolean mHWDecode = false;
    private TXCloudVideoView mPlayView;
    private OutCallReceiver receiver;
    private ITXLivePlayListener listener = null;
    public int mCurrentRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT;//RENDER_ROTATION_PORTRAIT =0 ,RENDER_ROTATION_LANDSCAPE = 270
    public int mCurrentRenderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
    private int mPlayType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;
    private int mCacheStrategy = 0;
    private static final int CACHE_STRATEGY_FAST = 1;  //极速
    private static final int CACHE_STRATEGY_SMOOTH = 2;  //流畅
    private static final int CACHE_STRATEGY_AUTO = 3;  //自动
    private static final float CACHE_TIME_FAST = 1.0f;
    private static final float CACHE_TIME_SMOOTH = 5.0f;

    private static final float CACHE_TIME_AUTO_MIN = 5.0f;
    private static final float CACHE_TIME_AUTO_MAX = 10.0f;

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "===========onBind==================");
        return new MyBind();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    public class MyBind extends Binder implements PlayerInterface {

        @Override
        public VideoService getServiceData() {
            return VideoService.this;
        }

        @Override
        public TXLivePlayer getLivePlayer() {
            if (mLivePlayer == null) {
                mLivePlayer = new TXLivePlayer(VideoService.this);
            }
            return mLivePlayer;
        }

        @Override
        public void setPlayView(TXCloudVideoView playerView) {
            VideoService.this.mLivePlayer.setPlayerView(playerView);
        }

        @Override
        public void setRenderRotation(int mCurrentRenderRotation) {
            VideoService.this.mCurrentRenderRotation = mCurrentRenderRotation;
            mLivePlayer.setRenderRotation(mCurrentRenderRotation);
        }

        @Override
        public void setRenderMode(int mCurrentRenderMode) {
            VideoService.this.mCurrentRenderMode = mCurrentRenderMode;
            mLivePlayer.setRenderMode(mCurrentRenderMode);
        }

        @Override
        public void setPlayListener(ITXLivePlayListener listener) {
            VideoService.this.listener = listener;
        }
    }

    public void setPlayView(TXCloudVideoView playerView) {
        VideoService.this.mLivePlayer.setPlayerView(playerView);
    }

    public void setRenderRotation(int mCurrentRenderRotation) {
        VideoService.this.mCurrentRenderRotation = mCurrentRenderRotation;
        mLivePlayer.setRenderRotation(mCurrentRenderRotation);
    }

    public void setRenderMode(int mCurrentRenderMode) {
        VideoService.this.mCurrentRenderMode = mCurrentRenderMode;
        mLivePlayer.setRenderMode(mCurrentRenderMode);
    }

    public void setPlayListener(ITXLivePlayListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "===========onCreate==================");
        mLivePlayer = new TXLivePlayer(this);
        mPlayConfig = new TXLivePlayConfig();
        //在服务创建时注册并开启广播
        receiver = new OutCallReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.nodepp");
        this.registerReceiver(receiver, filter);
        initFloatWindow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "===========onStartCommand==================");
        return super.onStartCommand(intent, flags, startId);
    }

    private void initFloatWindow() {
        mWM = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

        // 获取屏幕宽高
        winWidth = mWM.getDefaultDisplay().getWidth();
        winHeight = mWM.getDefaultDisplay().getHeight();

        params = new WindowManager.LayoutParams();
//        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = winWidth / 3;
        params.height = winHeight / 4;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.format = PixelFormat.TRANSLUCENT;
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.gravity = Gravity.LEFT + Gravity.TOP;// 将中心位置设置为左上方,

        params.setTitle("Toast");

        view = View.inflate(this, R.layout.video, null);// 初始化布局
        mPlayView = (TXCloudVideoView) view.findViewById(R.id.video_view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMove) {
                    Intent intent = new Intent(VideoService.this,
                            LivePlayerActivity.class);
                    if (!isExsitActivity(LivePlayerActivity.class)) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//不存在时，启动一个栈来存放activity
                    }
                    intent.putExtra("isServiceToActivity", true);
                    startActivity(intent);
                    //启动activity的时候移除悬浮窗
                    if (mWM != null && view != null) {
                        mWM.removeViewImmediate(view);
                    }

                }
            }
        });
        view.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 初始化起点坐标
                        isMove = false;
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int endX = (int) event.getRawX();
                        int endY = (int) event.getRawY();

                        // 计算移动偏移量
                        int dx = endX - startX;
                        int dy = endY - startY;
                        if (Math.abs(dx) > 2 || Math.abs(dy) > 2) {//左右或者上下移动大于2像素就是移动
                            isMove = true;
                        }
                        // 更新浮窗位置
                        params.x += dx;
                        params.y += dy;

                        // 防止坐标偏离屏幕
                        if (params.x < 0) {
                            params.x = 0;
                        }

                        if (params.y < 0) {
                            params.y = 0;
                        }

                        // 防止坐标偏离屏幕
                        if (params.x > winWidth - view.getWidth()) {
                            params.x = winWidth - view.getWidth();
                        }

                        if (params.y > winHeight - view.getHeight()) {
                            params.y = winHeight - view.getHeight();
                        }

                        mWM.updateViewLayout(view, params);

                        // 重新初始化起点坐标
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        // 记录坐标点
                        SharedPreferencesUtils.saveInt(VideoService.this, "lastX", params.x);
                        SharedPreferencesUtils.saveInt(VideoService.this, "lastY", params.y);
                        break;

                    default:
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "===========onDestroy==================");
        if (mWM != null && view != null) {
            view = null;
        }
        stopPlayRtmp(null);
        this.unregisterReceiver(receiver);
    }

    public boolean startPlayRtmp(Button mBtnPlay) {
//        //由于iOS AppStore要求新上架的app必须使用https,所以后续腾讯云的视频连接会支持https,但https会有一定的性能损耗,所以android将统一替换会http
//        if (playUrl.startsWith("https://")) {
//            playUrl = "http://" + playUrl.substring(8);
//        }

//        if (!checkPlayUrl(playUrl,mPlayType)) {
//            return false;
//        }
        Log.i(TAG, "===========startPlayRtmp==================");
        if (mBtnPlay != null) {
            mBtnPlay.setBackgroundResource(R.drawable.play_pause);
        }
        mLivePlayer.setPlayerView(mPlayView);
        if (listener != null) {
            mLivePlayer.setPlayListener(listener);
        }

        // 硬件加速在1080p解码场景下效果显著，但细节之处并不如想象的那么美好：
        // (1) 只有 4.3 以上android系统才支持
        // (2) 兼容性我们目前还仅过了小米华为等常见机型，故这里的返回值您先不要太当真
        mLivePlayer.enableHardwareDecode(mHWDecode);
        mLivePlayer.setRenderRotation(mCurrentRenderRotation);
        mLivePlayer.setRenderMode(mCurrentRenderMode);
        //设置播放器缓存策略
        //这里将播放器的策略设置为自动调整，调整的范围设定为1到4s，您也可以通过setCacheTime将播放器策略设置为采用
        //固定缓存时间。如果您什么都不调用，播放器将采用默认的策略（默认策略为自动调整，调整范围为1到4s）
        //mLivePlayer.setCacheTime(5);
        mLivePlayer.setConfig(mPlayConfig);

        int result = mLivePlayer.startPlay(playUrl, mPlayType); // result返回值：0 success;  -1 empty url; -2 invalid url; -3 invalid playType;
        if (result == -2) {
            Toast.makeText(VideoService.this, "非腾讯云链接地址，若要放开限制，请联系腾讯云商务团队", Toast.LENGTH_SHORT).show();
        }
        if (result != 0) {
            if (mBtnPlay != null) {
                mBtnPlay.setBackgroundResource(R.drawable.play_start);
            }
            return false;
        }

        return true;
    }

    public void stopPlayRtmp(Button mBtnPlay) {
        if (mBtnPlay != null) {
            mBtnPlay.setBackgroundResource(R.drawable.play_start);
        }
        if (mLivePlayer != null) {
            mLivePlayer.setPlayListener(null);
            mLivePlayer.stopPlay(true);
        }
    }

    /**
     * 判断某一个类是否存在任务栈里面
     *
     * @return
     */
    private boolean isExsitActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        ComponentName cmpName = intent.resolveActivity(getPackageManager());
        boolean flag = false;
        if (cmpName != null) { // 说明系统中存在这个activity
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfoList = am.getRunningTasks(10);
            for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                if (taskInfo.baseActivity.equals(cmpName)) { // 说明它已经启动了
                    flag = true;
                    break;  //跳出循环，优化效率
                }
            }
        }
        return flag;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "=========onUnbind============");
        return super.onUnbind(intent);
    }

    /**
     * 创建广播接收者
     */
    private class OutCallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isBigView = intent.getBooleanExtra("bigView", false);
            boolean isVisibly = intent.getBooleanExtra("isVisibly", false);
            boolean isStop = intent.getBooleanExtra("stop", false);
            Log.i(TAG, "=========接收到广播============" + isBigView);
            Log.i("time","++++++++++++++++++++++++++++++++++++++");
            if (isVisibly) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mWM != null && view != null) {
                            mWM.addView(view, params);//延迟，配合动画退出效果
                        }
                    }
                }, 700);
            }
            if (isStop) {
                stopPlayRtmp(null);
                mVideoPlay = !mVideoPlay;
            }
            MyApplication app = (MyApplication) getApplicationContext();
            if (isBigView) {
                mLivePlayer.setPlayerView(app.getVidePlayView());
            } else {
                if (!mLivePlayer.isPlaying()) {//如果是暂停状态就让播放器播放
                    mLivePlayer.resume();
                }
                mLivePlayer.setPlayerView(mPlayView);
                //切换到小窗口需要重新设置
                mLivePlayer.setRenderRotation(mCurrentRenderRotation);
                mLivePlayer.setRenderMode(mCurrentRenderMode);
            }
        }
    }

    public void setCacheStrategy(int nCacheStrategy) {
        if (mCacheStrategy == nCacheStrategy) return;
        mCacheStrategy = nCacheStrategy;
        TXLivePlayConfig playConfig = new TXLivePlayConfig();
        switch (nCacheStrategy) {
            case CACHE_STRATEGY_FAST:
                playConfig.setAutoAdjustCacheTime(true);
                playConfig.setMaxAutoAdjustCacheTime(CACHE_TIME_FAST);
                playConfig.setMinAutoAdjustCacheTime(CACHE_TIME_FAST);
                mLivePlayer.setConfig(playConfig);
                mPlayConfig = playConfig;
                break;

            case CACHE_STRATEGY_SMOOTH:
                playConfig.setAutoAdjustCacheTime(false);
                playConfig.setCacheTime(CACHE_TIME_SMOOTH);
                mLivePlayer.setConfig(playConfig);
                mPlayConfig = playConfig;
                break;

            case CACHE_STRATEGY_AUTO:
                playConfig.setAutoAdjustCacheTime(true);
                playConfig.setMaxAutoAdjustCacheTime(CACHE_TIME_SMOOTH);
                playConfig.setMinAutoAdjustCacheTime(CACHE_TIME_FAST);
                mLivePlayer.setConfig(playConfig);
                mPlayConfig = playConfig;
                break;

            default:
                break;
        }
    }


}
