package com.nodepp.smartnode.activity;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.JsonParser;
import com.nodepp.smartnode.utils.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by yuyue on 2016/8/5.
 */
public abstract class BaseVoiceActivity extends BaseActivity{
    private static String TAG = BaseVoiceActivity.class.getSimpleName();
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    private SpeechRecognizer mIat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIat = SpeechRecognizer.createRecognizer(BaseVoiceActivity.this, mInitListener);
        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(BaseVoiceActivity.this, mInitListener);
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
    }
    protected void showVoiceDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0以上需要动态权限
            // 检查相机权限是否已经获取
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED) {
                // 麦克风权限没有被授权，请求权限
                requestAudioPermission();
            } else {
                // 麦克风权限已经授权直接使用
                if (mIatDialog!= null){
                    mIatDialog.setListener(mRecognizerDialogListener);
                    mIatDialog.show();
                    mIatDialog.setParameter(SpeechConstant.VAD_BOS, Constant.VAD_BOS_TIME);
                    mIatDialog.setParameter(SpeechConstant.VAD_EOS, Constant.VAD_EOS_TIME);
                }
            }
        }else {
            if (mIatDialog!= null){
                mIatDialog.setListener(mRecognizerDialogListener);
                mIatDialog.show();
                mIatDialog.setParameter(SpeechConstant.VAD_BOS, Constant.VAD_BOS_TIME);
                mIatDialog.setParameter(SpeechConstant.VAD_EOS, Constant.VAD_EOS_TIME);
            }
        }
    }
    private void requestAudioPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
        } else {
            // 麦克风权限没有授权，请求回调
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
        }
    }
    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            android.util.Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                JDJToast.showMessage(BaseVoiceActivity.this, "初始化失败，错误码：" + code);
            }
        }
    };

    protected void cancleVoice(){
        if (mIatDialog != null){
            mIatDialog.dismiss();
        }
    }

    /**
     * 更新设备热词
     */
    protected  void updateUserWord(String word){
        int i = mIat.updateLexicon("userword", updateWordWithJson(word), new LexiconListener() {
            @Override
            public void onLexiconUpdated(String s, SpeechError speechError) {
                if (speechError == null) {
                    JDJToast.showMessage(BaseVoiceActivity.this, "上传成功");
                } else {
                    Log.i(TAG, "code==" + speechError.getErrorCode());
                }
            }
        });
        Log.i(TAG, "result code :" + i);
    }
    /**
     * 更新设备热词组
     */
    protected void updateUserWordArray(JSONArray array){
        int i = mIat.updateLexicon("userword", updateArrayWithJson(array), new LexiconListener() {
            @Override
            public void onLexiconUpdated(String s, SpeechError speechError) {
                if (speechError == null) {
//                    JDJToast.showMessage(BaseVoiceActivity.this, "上传成功");
                } else {
                    Log.i(TAG, "updateUserWordArray-code==" + speechError.getErrorCode());
                }
            }
        });
        Log.i(TAG, "updateUserWordArray result code :" + i);
    }
    private String updateArrayWithJson(JSONArray array){
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name","default");
                jsonObject.put("words",array);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(jsonObject);
            JSONObject object = new JSONObject();
            try {
                object.put("userword",jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return object.toString();
    }
    private String updateWordWithJson(String word){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name","default");
            jsonObject.put("words",new JSONArray().put(word));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonObject);
        JSONObject object = new JSONObject();
        try {
            object.put("userword",jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }
    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            if (results != null) {
                handleResult(results);
                if (isLast) {
                    Log.i(TAG, "================");
                }
            }
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            JDJToast.showMessage(BaseVoiceActivity.this, error.getPlainDescription(true));
        }

    };

    private void handleResult(RecognizerResult results) {
        Log.d("aaaaa", results.getResultString());
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (sn != null) {
            Log.i(TAG, text);
            voiceControl(text);
        }
    }

    @Override
    protected void onDestroy() {
        if (mIat != null){
            mIat.destroy();
        }
        if (mIatDialog != null){
            mIatDialog.destroy();
        }
        super.onDestroy();
    }

    public abstract void voiceControl(String result);

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted

                } else {
                    // Permission Denied
                    JDJToast.showMessage(BaseVoiceActivity.this,"请前往设置，手动打开麦克风权限");

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
