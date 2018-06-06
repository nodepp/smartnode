package com.nodepp.smartnode.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nodepp.smartnode.R;

/**
 * Created by yuyue on 2017/8/8.
 */
public class TitleBar extends LinearLayout {
    private RightClickListener rightClickListener;
    private String title;
    public static final int TEXT = 1;
    public static final int BUTTON = 2;
    private TextView tvAdd;
    private Button btnRight;
    private TextView tvRight;
    private LeftClickListener leftClickListener;
    private ImageView ivBack;
    private LinearLayout llAll;
    private TextView textView;
    private ImageView ivPerson;

    public TitleBar(Context context) {
        this(context, null);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.title_bar, this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleBar);
        if (typedArray != null){
            title = typedArray.getString(R.styleable.TitleBar_titleMessage);
        }
        initView();
    }

    private void initView() {
        llAll = (LinearLayout) findViewById(R.id.ll_all);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivPerson = (ImageView) findViewById(R.id.iv_person);
        textView = (TextView) findViewById(R.id.tv_title);
        btnRight = (Button) findViewById(R.id.btn_right);
        tvRight = (TextView) findViewById(R.id.tv_right);
        ivPerson.setOnClickListener(onClickListener);
        ivBack.setOnClickListener(onClickListener);
        btnRight.setOnClickListener(onClickListener);
        tvRight.setOnClickListener(onClickListener);
        textView.setText(title);
    }
    public void showPersonButton(){
        ivBack.setVisibility(GONE);
        ivPerson.setVisibility(VISIBLE);
    }
    public void setTitle(String title){
        textView.setText(title);
    }
    public void setTitleColor(int color){
        textView.setTextColor(color);
    }
    public void setBackgroundColor(int color){
        llAll.setBackgroundColor(color);
    }
    public interface RightClickListener{
        void onClick();
    }
    public void setRightClickListener(RightClickListener rightClickListener){
        this.rightClickListener = rightClickListener;
    }
    public interface LeftClickListener{
        void onClick();
    }
    public void setLeftClickListener(LeftClickListener leftClickListener){

        this.leftClickListener = leftClickListener;
    }
    public void setLeftButtonImage(int resId){
        if (ivBack != null){
            ivBack.setImageResource(resId);
        }
    }
    public void setRightButtonImage(int resId){
        if (btnRight != null){
            btnRight.setBackgroundResource(resId);
        }
    }
    public void setRightText(String text){
        if (tvRight != null){
            tvRight.setText(text);
        }
    }
    public void setRightVisible(int flag){
        switch (flag){
            case TEXT:
                tvRight.setVisibility(VISIBLE);
                break;
            case BUTTON:
                btnRight.setVisibility(VISIBLE);
                break;
        }
    }
    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.iv_person:
                    if (leftClickListener != null){
                        leftClickListener.onClick();
                        break;
                    }
                    break;
                case R.id.iv_back:
                    if (leftClickListener != null){
                        leftClickListener.onClick();
                        break;
                    }
                    ((Activity)getContext()).finish();
                    break;
                case R.id.btn_right:
                    if (rightClickListener != null){
                        rightClickListener.onClick();
                    }
                    break;
                case R.id.tv_right:
                    if (rightClickListener != null){
                        rightClickListener.onClick();
                    }
                    break;
            }
        }
    };

}
