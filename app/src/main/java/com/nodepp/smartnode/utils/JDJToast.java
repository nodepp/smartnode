package com.nodepp.smartnode.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by yuyue on 2016/8/8.
 */
public class JDJToast {
    public static Toast toast;
    public static void showMessage(Context context,String s){
        if (toast == null) {

            toast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
        }else {
            try {
                toast.setText(s);//直接覆盖还在显示的toast内容，不用等待上一条显示完
            }catch (Exception e){

            }
        }
        //第一个参数：设置toast在屏幕中显示的位置。我现在的设置是居中靠顶
        //第二个参数：相对于第一个参数设置toast位置的横向X轴的偏移量，正数向右偏移，负数向左偏移
        //第三个参数：同的第二个参数道理一样
        //如果你设置的偏移量超过了屏幕的范围，toast将在屏幕内靠近超出的那个边界显示
//        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();//显示toast信息
    }
    //Activity 销毁的时候调用一下，避免java.lang.IllegalStateException: View has already been added to the window manager.异常
    public static void reset() {
        toast = null;
    }
}
