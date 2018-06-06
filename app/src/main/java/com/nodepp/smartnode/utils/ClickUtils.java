package com.nodepp.smartnode.utils;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by yuyue on 16-8-8.
 */
public class ClickUtils {
    private static long lastClickTime = 0;
    private static long lastTime = 0;

    /**
     *  根据传入的值，如果大于传入的值就返回ture，反之就是false
     * @param l 传入的时间间隔 毫秒
     * @return
     */
    public synchronized static boolean isFastClick(long l) {
        long time = System.currentTimeMillis();
        if ( time - lastClickTime < l) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
    public synchronized static boolean isRepeat(long l) {
        long time = System.currentTimeMillis();
        if ( time - lastTime < l) {
            return true;
        }
        lastTime = time;
        return false;
    }
    public static void hideSoftInputView(Activity activity) {
        InputMethodManager manager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(activity.getWindow().getAttributes().softInputMode != 2 && activity.getCurrentFocus() != null) {
            manager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 2);
        }

    }
}
