package com.nodepp.smartnode.view.PopupWindow;

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
import com.nodepp.smartnode.utils.Log;

/**
 * Created by yuyue on 2017/1/7.
 */
public class ClientInfoPopup extends PopupWindow implements View.OnClickListener {
    private Context mContext;
    protected final int LIST_PADDING = 10;
    private Rect mRect = new Rect();
    private final int[] mLocation = new int[2];

    private boolean mIsDirty;
    private int popupGravity = Gravity.BOTTOM;

    TextView tv_share;
    TextView tv_delete;
    private popupListener listener;

    public ClientInfoPopup(Context context,int flag) {
        this(context, flag,LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    @SuppressWarnings("deprecation")
    public ClientInfoPopup(Context context,int flag, int width, int height) {
        //flag 0代表设备的删除分享，1代表选择设备
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
        setContentView(LayoutInflater.from(mContext).inflate(
                R.layout.popup_show_client_info, null));
        initUI(flag);
    }


    private void initUI(int flag) {
        tv_share = (TextView) getContentView().findViewById(R.id.tv_share);
        tv_delete = (TextView) getContentView().findViewById(R.id.tv_delete);
        tv_share.setOnClickListener(this);
        tv_delete.setOnClickListener(this);
        Log.i("jj","flag1=="+flag);
        if (flag == 1){
            tv_share.setText("普通设备");
            tv_delete.setText("彩灯设备");
        }else {
//            tv_share.setVisibility(View.GONE);
            tv_share.setText("分享");
            tv_delete.setText("删除");
        }
    }

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


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.tv_share:
                if (listener != null){
                    listener.share();
                }
                dismiss();
                break;
            case R.id.tv_delete:
                if (listener != null){
                    listener.delete();
                }
                dismiss();
                break;
        }

    }
    public void setOnPopupListener(popupListener listener){

        this.listener = listener;
    }
    public interface popupListener{
        void share();
        void delete();
    }
}

