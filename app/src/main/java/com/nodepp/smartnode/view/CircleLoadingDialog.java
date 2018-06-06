package com.nodepp.smartnode.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nodepp.smartnode.R;

/**
 * Created by yuyue on 2016/11/17.
 */
public class CircleLoadingDialog extends AlertDialog {

    private CirProgressBar progressbar;
    private TextView title;
    private OnClickListener listener;
    private ProgressButton pbButton;
    private ValueAnimator valueAnimator;
    private OnCancleClickListener cancleClickListener;

    public CircleLoadingDialog(Context context) {
        super(context);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.circle_dialog_loading);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView() {
        progressbar = (CirProgressBar) findViewById(R.id.progressbar);
        progressbar = (CirProgressBar) findViewById(R.id.progressbar);
        title = (TextView) findViewById(R.id.tv_title);
        ImageView ivCancle = (ImageView) findViewById(R.id.iv_cancle);
        pbButton = (ProgressButton) findViewById(R.id.btn_send);
        pbButton.setOnClickListener(onClickListener);
        ivCancle.setOnClickListener(onClickListener);
    }

    @Override
    public void show() {
        super.show();
        send();
    }

    @Override
    public void hide() {
        if (valueAnimator != null) {
            valueAnimator.end();
        }
        super.hide();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           switch (v.getId()){
               case R.id.btn_send:
                   if (listener != null){
                       send();
                       pbButton.setState(ProgressButton.NORMAL);
                       pbButton.setCurrentText("连接中");
                       pbButton.setProgress(0);
                       listener.click();
                   }
                   break;
               case R.id.iv_cancle:
                   hide();
                   if (cancleClickListener != null){
                       cancleClickListener.onCancle();
                   }
                   break;
           }
        }
    };
    public void setTitle(String s){
        title.setText(s);
    }
    private void send() {
        pbButton.setEnabled(false);
        progressbar.restart();
        title.setText("正在努力连接中...");
        pbButton.setState(ProgressButton.DOWNLOADING);
        valueAnimator = ValueAnimator.ofInt(0, 100);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progressbar.setProgress((int) animation.getAnimatedValue());
                pbButton.setProgress((int) animation.getAnimatedValue());
                pbButton.setProgressText((int) animation.getAnimatedValue());
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);//动画执行完成
                pbButton.setState(ProgressButton.INSTALLING);
                pbButton.setCurrentText("重新连接");
                pbButton.setEnabled(true);

            }
        });
        valueAnimator.setDuration(60000);
        valueAnimator.start();
    }
    public interface OnClickListener{
        void click();
    }
    public interface OnCancleClickListener{
        void onCancle();
    }
    public void setOnRetryClickListener(OnClickListener listener){
        this.listener = listener;
    }
    public void setOnCancleClickListener(OnCancleClickListener cancleClickListener){
        this.cancleClickListener = cancleClickListener;
    }
}
