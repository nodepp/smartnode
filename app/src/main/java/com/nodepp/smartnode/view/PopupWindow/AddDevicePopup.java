package com.nodepp.smartnode.view.PopupWindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nodepp.smartnode.R;
import com.nodepp.smartnode.utils.Utils;

/**
 * Created by yuyue on 2017/6/27.
 */
public class AddDevicePopup extends PopupWindow implements View.OnClickListener {
    private Context mContext;
    private Rect mRect = new Rect();
    private final int[] mLocation = new int[2];

    private boolean mIsDirty;
    private int popupWidth;
    private int popupHeight;
    private popupListener listener;
    private View parentView;

    public AddDevicePopup(Context context) {
        this(context,LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    @SuppressLint("WrongConstant")
    @SuppressWarnings("deprecation")
    public AddDevicePopup(Context context,int width, int height) {
        this.mContext = context;
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
//        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//        setAnimationStyle(R.style.popupWindowAnimation_right_to_left_in);
        setWidth(width);
        setHeight(height);
        setBackgroundDrawable(new BitmapDrawable());
        parentView = LayoutInflater.from(mContext).inflate(R.layout.popup_add_device, null);
        setContentView(parentView);
        initUI();
    }


    private void initUI() {
        getContentView().findViewById(R.id.ll_setting).setOnClickListener(this);
        getContentView().findViewById(R.id.ll_scan).setOnClickListener(this);
        //获取自身的长宽高
        parentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popupHeight = parentView.getMeasuredHeight();
        popupWidth = parentView.getMeasuredWidth();
    }
    public void show(View view) {
        view.getLocationOnScreen(mLocation);
        showAtLocation(view, Gravity.NO_GRAVITY, (mLocation[0] + view.getWidth()) -popupWidth-Utils.Dp2Px(mContext,2),mLocation[0]+view.getHeight()+ Utils.Dp2Px(mContext,20));
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.ll_setting:
                if (listener != null){
                    listener.commonAdd();
                }
                dismiss();
                break;
            case R.id.ll_scan:
                mIsDirty = false;
                if (listener != null){
                    listener.scanAdd();
                }
                dismiss();
                break;
        }

    }
    public void setOnPopupListener(popupListener listener){

        this.listener = listener;
    }
    public interface popupListener{
        void commonAdd();
        void scanAdd();
    }
}

