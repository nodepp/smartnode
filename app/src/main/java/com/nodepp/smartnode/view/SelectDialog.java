package com.nodepp.smartnode.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.nodepp.smartnode.R;

public class SelectDialog extends Dialog implements View.OnClickListener {

    Context context;
    View localView;
    MyDialogListener listen;
    private Button btnShare;
    private Button btnDelect;
    private Button btnCancle;

    public SelectDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        localView = View.inflate(context, R.layout.layout_select_dialog,null);
        localView.setAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_bottom_top));
        setContentView(localView);
        // 这句话起全屏的作用
        getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        initView();
        initListener();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.dismiss();
        return super.onTouchEvent(event);
    }

    private void initListener() {
        btnShare.setOnClickListener(this);
        btnDelect.setOnClickListener(this);
        btnCancle.setOnClickListener(this);

    }

    private void initView() {
        btnShare = (Button) findViewById(R.id.btn_share);
        btnDelect = (Button) findViewById(R.id.btn_delect);
        btnCancle = (Button) findViewById(R.id.btn_cancle);

    }
    public void setCallBackListener(MyDialogListener listen) {
        this.listen = listen;
    }

    /**
     * 定义接口回调的方法
     */
    public interface MyDialogListener{
        void share();
        void delect();
        void exit();
    }

    /**
     * 自定义dialog布局上面点击事件的回调
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_share:
                if (listen != null){
                    listen.share();
                    this.dismiss();
                }
                break;
            case R.id.btn_delect:
                if (listen != null){
                    listen.delect();
                    this.dismiss();
                }
                break;
            case R.id.btn_cancle:
                if (listen != null){
                    listen.exit();
                    this.dismiss();
                }
                break;
        }
    }
}
