package com.nodepp.smartnode.twocode;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.google.protobuf.ByteString;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.twocode.camera.CameraManager;
import com.nodepp.smartnode.twocode.decoding.CaptureActivityHandler;
import com.nodepp.smartnode.twocode.decoding.InactivityTimer;
import com.nodepp.smartnode.twocode.view.ViewfinderView;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.UDPClientA2S;
import com.nodepp.smartnode.udp.UDPSocketA2S;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.DESUtils;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.utils.Utils;
import com.nodepp.smartnode.view.loadingdialog.LoadingDialog;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import nodepp.Nodepp;
/**
 * Created by yuyue on 2016/9/8.
 */
public class CaptureActivity extends Activity implements Callback {
    public static final String QR_RESULT = "RESULT";

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private SurfaceView surfaceView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private boolean isOpenLight = false;
    // private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private DbUtils dbUtils;
    CameraManager cameraManager;
    private Camera camera;
    private List<Device> devices;
    private LoadingDialog loadingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_capture);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinderview);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        dbUtils = DBUtil.getInstance(this);
        Constant.isMenuOpen = true;
        loadingDialog = new LoadingDialog(this, getString(R.string.show_check_sharesig));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0以上需要动态权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        UDPClientA2S.getInstance().setIsRetry(true);//设置重试
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        cameraManager = new CameraManager(getApplication());

        viewfinderView.setCameraManager(cameraManager);

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        UDPClientA2S.getInstance().setIsRetry(false);//还原
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        cameraManager.closeDriver();
    }

    @Override
    protected void onDestroy() {
        if (loadingDialog != null && loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            // CameraManager.get().openDriver(surfaceHolder);
            cameraManager.openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    public void handleDecode(Result obj, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        showResult(obj, barcode);

    }

    private void showErrorDialog(String title,String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("无效二维码");
        builder.setMessage("请扫描正确的二维码");
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restartPreviewAfterDelay(0L);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        if (!CaptureActivity.this.isFinishing()) {
            alertDialog.show();
        }
    }

    private void showResult(final Result rawResult, Bitmap barcode) {
        String s = rawResult.getText();
        if (s == null){
            showErrorDialog("无效二维码","请扫描正确的二维码");
            return;
        }
        Log.i("twocode","result--"+s);
        if (s.contains(";")){
            final String[] split = s.split(";");
            if (split.length == 5){
                showErrorDialog("二维码过时","请使用最新版本app分享的二维码，再进行添加");
            }else {
                showErrorDialog("无效二维码","请扫描正确的二维码");
            }
        }else {
            Nodepp.Msg msg = Utils.decryptTwoCode(s);
            if (msg == null){
                showErrorDialog("无效二维码","请扫描正确的二维码");
            }else {
                Log.i("twocode","result-msg-"+msg.toString());
                showResultDialog(rawResult,barcode,msg);
            }
        }

    }
    public void showResultDialog(final Result rawResult, Bitmap barcode, final Nodepp.Msg msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Drawable drawable = new BitmapDrawable(barcode);
        builder.setIcon(drawable);
        builder.setMessage("已扫描到智能设备");
        builder.setNeutralButton(getString(R.string.add_device), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkShareSig(msg);
                dialog.dismiss();
            }

        });
        builder.setPositiveButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.putExtra("result",rawResult.getText());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.scanning_again), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                restartPreviewAfterDelay(0L);
            }
        });
        builder.setCancelable(false);
        if (!CaptureActivity.this.isFinishing()){
            builder.show();
        }
        inactivityTimer.onActivity();
        viewfinderView.drawResultBitmap(barcode);//画结果图片
        playBeepSoundAndVibrate();//启动声音效果
    }

    private void checkShareSig(Nodepp.Msg message) {
        if (!CaptureActivity.this.isFinishing()){
            loadingDialog.show();
        }
        String s = SharedPreferencesUtils.getString(CaptureActivity.this, "uid", "0");
        String uidSig = SharedPreferencesUtils.getString(CaptureActivity.this, "uidSig", "0");
        s = DESUtils.decodeValue(s);
        if (message != null && message.getDevicesCount() > 0){
            final Nodepp.Device device = message.getDevices(0);
            if (device == null){
                return;
            }
            ByteString shareSig = message.getShareVerification();
            final long did = device.getDid();
            final long tid = device.getTid();
            final int deviceType = device.getDeviceType();
            final String ip = device.getDeviceIp().toStringUtf8();
            if (s != null) {
                long uid = Long.parseLong(s);
                final Nodepp.Msg msg = PbDataUtils.checkShareSig(uid, did, uidSig, shareSig);
                UDPSocketA2S.send(CaptureActivity.this,msg, new ResponseListener() {
                    @Override
                    public void onSuccess(Nodepp.Msg msg) {
                        Log.i("kk", "checkSig=receive=msg=" + msg.toString());
                        int result = msg.getHead().getResult();
                        if (result == 404) {
                            JDJToast.showMessage(CaptureActivity.this,"分享验证失败");
                            loadingDialog.dismiss();
                        } else if (result == 0){
                            ByteString keyClient = msg.getKeyClient();
                            if (keyClient != null){
                                saveSocket(device,keyClient);
                                Log.i("kk", "keyClient=" + PbDataUtils.byteString2String(keyClient));
                                loadingDialog.dismiss();
                            }else {
                                JDJToast.showMessage(CaptureActivity.this,"分享验证失败");
                                loadingDialog.dismiss();
                                finish();
//                            ActivityManager.getAppManager().goToActivity(MainActivity.class);
                            }
                            Log.i("kk", "requestShareSig=receive=msg=" + msg.toString());
                        }
                    }

                    @Override
                    public void onTimeout(Nodepp.Msg msg) {

                    }

                    @Override
                    public void onFaile() {
                        JDJToast.showMessage(CaptureActivity.this,"验证超时");
                        loadingDialog.dismiss();
                        restartPreviewAfterDelay(0L);
                    }
                });

            }
        }

    }
    private void saveSocket(final Nodepp.Device deviceModel, final ByteString keyClient) {

//        final String username = SharedPreferencesUtils.getString(this, "username", "");
//        BigInteger bigInteger1=new BigInteger(tid);
//        long l = bigInteger1.longValue();
//        Log.i("kk", "=========l==========" + l);
        try {
            devices = dbUtils.findAll(Selector.from(Device.class).where("userName", "=", Constant.userName).and(WhereBuilder.b("tid", "=",deviceModel.getTid())));
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (devices == null || devices.size() < 1) {
            //插座不存在，直接保存到数据库
            Log.i("kk", "=========username==========" +  Constant.userName);
            Log.i("kk", "=========did==========" + deviceModel.getDid());
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.input_device_name));
            final EditText editText = new EditText(this);
//            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
            builder.setView(editText);
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Device device = new Device();
                    String name = editText.getText().toString();
                    if (TextUtils.isEmpty(name)) {
                        JDJToast.showMessage(CaptureActivity.this, getString(R.string.device_name));
                        saveSocket(deviceModel, keyClient);
                    } else {
                        //用大整型来转，因为数据是无符号的64位。
//                        BigInteger bigIntegerDid=new BigInteger(did);
//                        BigInteger bigIntegerTid=new BigInteger(tid);
//                        long lDid = bigIntegerDid.longValue();
//                        long lTid = bigIntegerTid.longValue();
                        device.setSocketName(name);
                        Random ra = new Random();//产生0，1,2,3随机数，匹配图片
                        device.setPictureIndex(ra.nextInt(4));
                        device.setUserName( Constant.userName);
                        device.setDid(deviceModel.getDid());
                        device.setTid(deviceModel.getTid());
                        device.setIp(PbDataUtils.byteString2String(deviceModel.getDeviceIp()));
                        device.setClientKey(PbDataUtils.byteString2String(keyClient));
                        device.setConnetedMode(0);
                        device.setIsOnline(true);
                        device.setDeviceType(deviceModel.getDeviceType());
                        if (deviceModel.hasRouterMac()){
                            device.setRouterMac(PbDataUtils.byteString2String(deviceModel.getRouterMac()));
                        }
                        try {
                            dbUtils.saveBindingId(device);
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                        finish();
//                        ActivityManager.getAppManager().goToActivity(MainActivity.class);
                    }
                }
            });
            builder.setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    restartPreviewAfterDelay(0L);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
//            final Button positiveButton = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            if (!CaptureActivity.this.isFinishing()){
                dialog.show();
            }
        } else {//插座已经存在，提醒用户是不是继续，进行修改插座名称
            showPromptDialog(dbUtils, devices, deviceModel, keyClient);
        }
    }

    private void showPromptDialog(final DbUtils dbUtils, final List<Device> devices,final Nodepp.Device deviceModel, final ByteString keyClient) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.add_socket_repeat_prompt));
        final EditText editText = new EditText(this);
//        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        builder.setView(editText);
        builder.setPositiveButton(getString(R.string.add_continue), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Device device = devices.get(0);
                String name = editText.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    JDJToast.showMessage(CaptureActivity.this, getString(R.string.device_name));
                    showPromptDialog(dbUtils, devices, deviceModel, keyClient);
                } else {
//                    //用大整型来转，因为数据是无符号的64位。
//                    BigInteger bigIntegerDid=new BigInteger(did);
//                    BigInteger bigIntegerTid=new BigInteger(tid);
//                    long lDid = bigIntegerDid.longValue();
//                    long lTid = bigIntegerTid.longValue();
                    device.setSocketName(name);
                    Random ra = new Random();//产生0，1,2,3随机数，匹配图片
                    device.setPictureIndex(ra.nextInt(4));
                    device.setDid(deviceModel.getDid());
                    device.setTid(deviceModel.getTid());
                    device.setIp(PbDataUtils.byteString2String(deviceModel.getDeviceIp()));
                    device.setConnetedMode(0);
                    device.setClientKey(PbDataUtils.byteString2String(keyClient));
                    device.setIsOnline(true);
                    device.setDeviceType(deviceModel.getDeviceType());
                    if (deviceModel.hasRouterMac()){
                        device.setRouterMac(PbDataUtils.byteString2String(deviceModel.getRouterMac()));
                    }
                    try {
//                        String username = SharedPreferencesUtils.getString(CaptureActivity.this, "username", "");
                        dbUtils.update(device, WhereBuilder.b("tid", "=", devices.get(0).getTid()).and("userName", "=",  Constant.userName));
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                    JDJToast.showMessage(CaptureActivity.this, getString(R.string.change_name_success));
                    dialog.dismiss();
                    finish();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                JDJToast.showMessage(CaptureActivity.this, getString(R.string.cancle_success));
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        if (!CaptureActivity.this.isFinishing()){
            alertDialog.show();
        }
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(MessageIDs.restart_preview, delayMS);
        }
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            try {
                AssetFileDescriptor fileDescriptor = getAssets().openFd("success.wav");
                this.mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
                        fileDescriptor.getLength());
                this.mediaPlayer.setVolume(0.1F, 0.1F);
                this.mediaPlayer.prepare();
            } catch (IOException e) {
                this.mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    //动态获取摄像头
    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    1);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted

                } else {
                    // Permission Denied
                    JDJToast.showMessage(CaptureActivity.this,"请前往设置，手动打开相机权限");

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}