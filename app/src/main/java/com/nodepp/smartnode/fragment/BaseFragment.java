package com.nodepp.smartnode.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nodepp.smartnode.activity.ColorControlActivity;
import com.nodepp.smartnode.struct.FunctionManager;

/**
 * Created by yuyue on 2017/8/2.
 */
public abstract class BaseFragment extends Fragment{
    private static final String TAG = BaseFragment.class.getSimpleName();
    public static final String SHOW_MENU = TAG+"showMenu";
    public static final String HIDE_MENU = TAG+"hideMenu";
    public static final String SHOW_NO_DEVICE = TAG+"showNoDeivce";
    public static final String HIDE_NO_DEVICE = TAG+"hideNoDeivce";
    public ColorControlActivity mActivity;
    protected FunctionManager mFunctions;
    public void setFunction(FunctionManager functions) {
        this.mFunctions = functions;
    }

    public FunctionManager getFunction() {
        return mFunctions;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    // fragment创建
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (ColorControlActivity) getActivity();
        mActivity.setFunctionForFragment(getTag());
    }

    //创建该Fragment的视图
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return initView();
    }

    // 当Activity的onCreate方法返回时调用
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }
    public abstract void voiceControl(String msg);
    // 子类必须实现初始化布局的方法
    public abstract View initView();

    //初始化数据，可以不实现
    public void initData() {
    }

}
