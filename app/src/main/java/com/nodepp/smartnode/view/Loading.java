package com.nodepp.smartnode.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;

import com.nodepp.smartnode.R;


/**
 * Created by nodepp on 2018/3/20.
 */
 public class Loading extends ProgressDialog {
        public Loading(Context context) {
            super(context,R.style.CustomDialog);
        }
        public Loading(Context context, int theme) {
            super(context, theme);
        }
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            init(getContext());
        }
        private void init(Context context) {
            setCancelable(true);
            setCanceledOnTouchOutside(false);
            setContentView(R.layout.loading);//loading的xml文件
//            WindowManager.LayoutParams params = getWindow().getAttributes();
//            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
//            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
//            getWindow().setAttributes(params);
        }
        @Override
        public void show() {//开启
            super.show();
        }
        @Override
        public void dismiss() {//关闭
            super.dismiss();
        }
    }