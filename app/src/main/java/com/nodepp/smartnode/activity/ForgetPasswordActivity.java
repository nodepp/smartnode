package com.nodepp.smartnode.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nodepp.smartnode.R;
import com.nodepp.smartnode.service.TLSService;
import com.nodepp.smartnode.utils.ClickUtils;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Utils;

public class ForgetPasswordActivity extends BaseActivity implements View.OnClickListener {

    private EditText etCode;
    private EditText etPhone;
    private TLSService tlsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        initView();
    }
    private void initView() {
        Button btnOk = (Button) findViewById(R.id.btn_ok);
        etPhone = (EditText) findViewById(R.id.et_phone);
        etCode = (EditText) findViewById(R.id.et_code);
        btnOk.setOnClickListener(this);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ClickUtils.hideSoftInputView(ForgetPasswordActivity.this);//点击空白处的时候隐藏软键盘
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
         switch (v.getId()){
             case R.id.btn_ok:
                 String phoneNum = etPhone.getText().toString();
                 if (TextUtils.isEmpty(phoneNum)){
                     JDJToast.showMessage(ForgetPasswordActivity.this,getString(R.string.input_phone_number));
                     return;
                 }
                 if (!Utils.checkPhoneNumber(phoneNum)){
                     JDJToast.showMessage(ForgetPasswordActivity.this,getString(R.string.phone_number_error));
                     return;
                 }
                 Intent intent = new Intent(ForgetPasswordActivity.this, ForgetPasswordActivityTwo.class);
                 intent.putExtra("phone_number", etPhone.getText().toString());
                 startActivity(intent);
                 ActivityManager.getAppManager().finishLastActivity();
                 break;
         }
    }
}
