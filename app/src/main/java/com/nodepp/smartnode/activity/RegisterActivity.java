package com.nodepp.smartnode.activity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;

import com.nodepp.smartnode.R;
import com.nodepp.smartnode.service.TLSService;
import com.nodepp.smartnode.utils.ClickUtils;
import com.nodepp.smartnode.utils.TimeCountUtil;

public class RegisterActivity extends BaseActivity {
    TimeCountUtil time;//定时器
    private EditText etPhone;
    private EditText etCode;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        phone = getIntent().getStringExtra("phone");
        initView();
    }


    private void initView() {
        etPhone = (EditText) findViewById(R.id.et_phone);
        etCode = (EditText) findViewById(R.id.et_code);
        TLSService tlsService = TLSService.getInstance();
        tlsService.initPhonePwdRegisterService(this,
                (EditText) findViewById(R.id.et_country_code), etPhone, etCode, (Button) findViewById(R.id.btn_get_code), (Button) findViewById(R.id.btn_ok));
        if (phone != null){
            etPhone.setText(phone);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ClickUtils.hideSoftInputView(RegisterActivity.this);//点击空白处的时候隐藏软键盘
        return super.onTouchEvent(event);
    }
}
