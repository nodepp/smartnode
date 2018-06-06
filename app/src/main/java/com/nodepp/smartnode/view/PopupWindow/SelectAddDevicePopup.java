package com.nodepp.smartnode.view.PopupWindow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.nodepp.smartnode.R;
import com.nodepp.smartnode.activity.AddDeviceActivity;
import com.nodepp.smartnode.activity.AddDeviceColorLightActivity;
import com.nodepp.smartnode.adapter.DeviceTypeAdapter;
import com.nodepp.smartnode.view.TitleBar;

/**
 * Created by yuyue on 2017/6/27.
 */
public class SelectAddDevicePopup extends PopupWindow {
    private Context mContext;
    protected final int LIST_PADDING = 10;
    private Rect mRect = new Rect();
    private final int[] mLocation = new int[2];
    private boolean mIsDirty;
    private int popupGravity = Gravity.BOTTOM;
    private AnimationDrawable lightAnimation;
    private ListView listView;

    public SelectAddDevicePopup(Context context) {
        this(context, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        initUI();
    }

    //初始化titlebar
    private void initTitleBar() {
        TitleBar titleBar = (TitleBar) getContentView().findViewById(R.id.title_bar);
        titleBar.setLeftClickListener(new TitleBar.LeftClickListener() {
            @Override
            public void onClick() {
                dismiss();
            }
        });

    }
    @SuppressWarnings("deprecation")
    public SelectAddDevicePopup(Context context, int width, int height) {
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
                R.layout.popup_select_device, null));
    }

    private void initUI() {
        initTitleBar();
        listView = (ListView) getContentView().findViewById(R.id.list_view);
        DeviceTypeAdapter deviceTypeAdapter = new DeviceTypeAdapter(mContext);
        listView.setAdapter(deviceTypeAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mContext.startActivity(new Intent(mContext, AddDeviceActivity.class));
                        break;
                    case 1:
                        mContext.startActivity(new Intent(mContext, AddDeviceColorLightActivity.class));
                        break;
                    case 2:
                        mContext.startActivity(new Intent(mContext, AddDeviceActivity.class));
                        break;
                }
                dismiss();
            }
        });
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
}

