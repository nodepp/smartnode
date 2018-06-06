package com.nodepp.smartnode.view.PopupWindow;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.nodepp.smartnode.R;

/**
 * Created by yuyue on 2017/6/27.
 */
public class HowConfigDevicePopup extends PopupWindow {
    private Context mContext;
    protected final int LIST_PADDING = 10;
    private Rect mRect = new Rect();
    private final int[] mLocation = new int[2];
    private boolean mIsDirty;
    private int popupGravity = Gravity.BOTTOM;
    private AnimationDrawable lightAnimation;

    public HowConfigDevicePopup(Context context) {
        this(context, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        initUI();
    }

    @SuppressWarnings("deprecation")
    public HowConfigDevicePopup(Context context, int width, int height) {
        this.mContext = context;
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        setAnimationStyle(R.style.popupWindowAnimation_bottom_to_top);
        setWidth(width);
        setHeight(height);
        setBackgroundDrawable(new BitmapDrawable());
        setContentView(LayoutInflater.from(mContext).inflate(
                R.layout.popup_how_to_config_two, null));
    }

    private void initUI() {
        ImageView ivLight = (ImageView) getContentView().findViewById(R.id.iv_light);
        RelativeLayout rlFinish = (RelativeLayout) getContentView().findViewById(R.id.rl_finish);
        rlFinish.setOnClickListener(onClickListener);
        ivLight.setBackgroundResource(R.drawable.reset_device_animation);
        lightAnimation = (AnimationDrawable) ivLight.getBackground();
        lightAnimation.start();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rl_finish:
                    lightAnimation.stop();
                    dismiss();
                    break;
            }
        }
    };

    public void show(View view) {
        view.getLocationOnScreen(mLocation);
        mRect.set(mLocation[0], mLocation[1], mLocation[0] + view.getWidth(),
                mLocation[1] + view.getHeight());
        if (mIsDirty) {
            populateActions();
        }
        showAtLocation(view, popupGravity, 0, 0);
    }

    private void populateActions() {
        mIsDirty = false;

    }
}

