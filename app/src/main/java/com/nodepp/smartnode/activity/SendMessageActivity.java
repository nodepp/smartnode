package com.nodepp.smartnode.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.Socket;
import com.nodepp.smartnode.udp.UDPClientA2S;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.Utils;

import nodepp.Nodepp;


public class SendMessageActivity extends BaseActivity {
    private static final String TAG = "SendMessageActivity";
    private TextView tvmessage;
    private EditText etMessage;
    private CheckBox cbChar;
    private CheckBox cbHex;
    private Device deviceModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        Intent intent = getIntent();
        deviceModel = (Device) intent.getSerializableExtra("device");
        initView();
    }

    private void initView() {
        ImageView ivBack = (ImageView) findViewById(R.id.iv_back);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        tvmessage = (TextView) findViewById(R.id.tv_message);
        cbChar = (CheckBox) findViewById(R.id.cb_char);
        cbHex = (CheckBox) findViewById(R.id.cb_hex);
        TextView tvChar = (TextView) findViewById(R.id.tv_char);
        TextView tvHex = (TextView) findViewById(R.id.tv_hex);
        etMessage = (EditText) findViewById(R.id.et_message);
        Button btnSend = (Button) findViewById(R.id.btn_send);
        Button btnReceive = (Button) findViewById(R.id.btn_receive);
        cbChar.setOnClickListener(OnChangeListener);
        cbHex.setOnClickListener(OnChangeListener);
        tvChar.setOnClickListener(OnChangeListener);
        tvHex.setOnClickListener(OnChangeListener);
        ivBack.setOnClickListener(OnChangeListener);
        btnSend.setOnClickListener(OnChangeListener);
        btnReceive.setOnClickListener(OnChangeListener);
        tvTitle.setText("串口数据");
        etMessage.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (cbHex.isChecked()) {
                    String msg = etMessage.getText().toString();
                    if (!isHex(msg)) {
                        Toast.makeText(SendMessageActivity.this, "只能输入十六进制的数值", Toast.LENGTH_SHORT).show();
                        String hex = getHex(msg);
                        etMessage.setText(hex);
                        etMessage.setSelection(hex.length());
                    }
                }
            }
        });
    }

    View.OnClickListener OnChangeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cb_char:
                case R.id.tv_char:
                    cbChar.setChecked(true);
                    cbHex.setChecked(false);
                    String message = etMessage.getText().toString();
                    if (message.length() > 0) {
                        String s = Utils.toStringHex(message);
                        etMessage.setText(s);
                        etMessage.setSelection(s.length());
                    }
                    break;
                case R.id.cb_hex:
                case R.id.tv_hex:
                    cbChar.setChecked(false);
                    cbHex.setChecked(true);
                    String message2 = etMessage.getText().toString();
                    if (message2.length() > 0) {
                        String s = Utils.toHexString(message2);
                        etMessage.setText(s);
                        etMessage.setSelection(s.length());
                    }
                    break;
                case R.id.iv_back:
                    finish();
                    break;
                case R.id.btn_send:
                    String data = etMessage.getText().toString();
                    sendData(data);
                    break;
                case R.id.btn_receive:
                    queryData();
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        UDPClientA2S.getInstance().setIsRetry(true);
        super.onResume();
    }

    @Override
    protected void onPause() {
        UDPClientA2S.getInstance().setIsRetry(false);
        super.onPause();
    }

    private void sendData(String data){
        long uid = Long.parseLong(Constant.userName);
        Nodepp.Msg msg = PbDataUtils.sendUserDataRequestParam(uid, deviceModel.getDid(), deviceModel.getTid(), Constant.usig, data);
        Socket.send(this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
            @Override
            public void onSuccess(Nodepp.Msg msg) {
                Log.i(TAG,"sendData=result="+msg.toString());
                int result = msg.getHead().getResult();
                if (result == 0){
                    tvmessage.setText(PbDataUtils.byteString2String(msg.getUserData()));
                }else {
                    tvmessage.setText("接收失败");
                }
            }

            @Override
            public void onTimeout(Nodepp.Msg msg) {

            }

            @Override
            public void onFaile() {

            }
        });
    }

    private void queryData(){
        long uid = Long.parseLong(Constant.userName);
        Nodepp.Msg msg = PbDataUtils.queryUserDataRequestParam(uid, deviceModel.getDid(), deviceModel.getTid(), Constant.usig);
        Socket.send(this, deviceModel.getConnetedMode(), deviceModel.getIp(), msg, clientKeys.get(deviceModel.getTid()), new ResponseListener() {
            @Override
            public void onSuccess(Nodepp.Msg msg) {
                Log.i(TAG,"queryData=result="+msg.toString());
                int result = msg.getHead().getResult();
                if (result == 0){
                   tvmessage.setText(PbDataUtils.byteString2String(msg.getUserData()));
                }else {
                    tvmessage.setText("接收失败");
                }
            }

            @Override
            public void onTimeout(Nodepp.Msg msg) {

            }

            @Override
            public void onFaile() {

            }
        });
    }
    public static boolean isHex(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (((c >= 'a') && (c <= 'f')) || ((c >= 'A') && (c <= 'F')) || ((c >= '0') && (c <= '9')))
                continue;
            else
                return false;
        }
        return true;
    }

    public static String getHex(String s) {
        StringBuilder msg = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (((c >= 'a') && (c <= 'f')) || ((c >= 'A') && (c <= 'F')) || ((c >= '0') && (c <= '9'))) {
                msg.append(c);
            } else
                return msg.toString();
        }
        return msg.toString();
    }

}
