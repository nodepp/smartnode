package com.nodepp.smartnode.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.service.PhonePwdLoginService;
import com.nodepp.smartnode.utils.Log;

/**
 * Created by yuyue on 2016/9/26.
 */
public class ImgCodeActivity extends BaseActivity implements View.OnClickListener{

    private final static String TAG = ImgCodeActivity.class.getSimpleName();
    private static ImageView imgcodeView;

    private EditText imgcodeEdit;
    private int login_way;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_code);
        initView();

    }

    private void initView() {
        imgcodeEdit = (EditText) findViewById(R.id.txt_checkcode);
        imgcodeView = (ImageView) findViewById(R.id.imagecode);
        imgcodeView.setOnClickListener(this);

        Intent intent = getIntent();
        byte[] picData = intent.getByteArrayExtra(Constant.EXTRA_IMG_CHECKCODE);
        login_way = intent.getIntExtra(Constant.EXTRA_LOGIN_WAY, Constant.NON_LOGIN);

        fillImageview(picData);
        findViewById(R.id.btn_verify).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.refreshImageCode).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_verify) {
            String imgcode = imgcodeEdit.getText().toString();
            if (login_way == Constant.PHONEPWD_LOGIN) {
                tlsService.TLSPwdLoginVerifyImgcode(imgcode, PhonePwdLoginService.pwdLoginListener);
            }
            finish();
        } else if (v.getId() == R.id.imagecode
                || v.getId() == R.id.refreshImageCode) { // 刷新验证码
            tlsService.TLSPwdLoginReaskImgcode(PhonePwdLoginService.pwdLoginListener);
        } if (v.getId() == R.id.btn_cancel) {
            finish();
        }
    }

    public static void fillImageview(byte[] picData) {
        if (picData == null)
            return;
        Bitmap bm = BitmapFactory.decodeByteArray(picData, 0, picData.length);
        Log.e(TAG, "w " + bm.getWidth() + ", h " + bm.getHeight());
        imgcodeView.setImageBitmap(bm);
    }
}
