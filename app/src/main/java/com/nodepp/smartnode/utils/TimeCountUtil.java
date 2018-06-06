package com.nodepp.smartnode.utils;

import android.os.CountDownTimer;
import android.widget.Button;

import com.nodepp.smartnode.R;

/**
 * 类描述：
 * 创建人：余越
 * 创建时间：16-3-8 下午3:29
 */
public class TimeCountUtil extends CountDownTimer {

         Button getCode;

        public TimeCountUtil(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public void setCodeView(Button v) {
            this.getCode = v;
        }

        public void onFinish() {
           getCode.setText("重新获取");
            getCode.setClickable(true);
            getCode.setBackgroundResource(R.drawable.btn_code_nor_shape);
//            getCode.setBackgroundResource(R.color.);
        }

        public void onTick(long millisUntilFinished) {
            this.getCode.setClickable(false);
//          getCode.setBackgroundColor();
            getCode.setBackgroundResource(R.drawable.btn_code_press_shape);
            this.getCode.setText(millisUntilFinished / 1000L + "秒");
        }

    }
