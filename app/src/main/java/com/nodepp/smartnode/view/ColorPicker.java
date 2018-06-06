
package com.nodepp.smartnode.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.nodepp.smartnode.R;
import com.nodepp.smartnode.utils.Log;

public class ColorPicker extends View {
    private static final String TAG = ColorPicker.class.getSimpleName();
    private static final String STATE_PARENT = "parent";
    private static final String STATE_ANGLE = "angle";
    private static final String STATE_OLD_COLOR = "color";

    /**
     * 颜色构造  红 粉红 蓝 天蓝 绿 黄 红
     */
    private static final int[] COLORS = new int[]{0xFFFF0000, 0xFFFF00FF,
            0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000};
    private Paint mColorWheelPaint;
    private Paint mPointerHaloPaint;
    private Paint mPointerColor;
    private int mColorWheelThickness;
    private int mColorWheelRadius;
    private int mPreferredColorWheelRadius;
    private int mColorCenterRadius;
    private int mPreferredColorCenterRadius;
    private int mColorCenterHaloRadius;
    private int mPreferredColorCenterHaloRadius;
    private int mColorPointerRadius;
    private int mColorPointerHaloRadius;
    private RectF mColorWheelRectangle = new RectF(); //外圆
    private RectF
            mCenterRectangle = new RectF();
    private boolean mUserIsMovingPointer = false;
    private int mColor;//当前选中的颜色
    private int mCenterOldColor;//上一次选中的颜色

    private int mCenterNewColor;//中心圆新选中的ARGB颜色
    //在X和Y方向的偏移数
    private float mTranslationOffset;

    /**
     * x轴指针和向用户触摸的距离。
     */
    private float mSlopX;

    /**
     * y轴指针和向用户触摸的距离。
     */
    private float mSlopY;

    /**
     * 指针的位置表示为角度。
     */
    private float mAngle;

    /**
     * 用于与旧选定的中心绘制的实例
     */
    private Paint mCenterOldPaint;

    /**
     * 新选择绘制中心的实例
     */
    private Paint mCenterNewPaint;

    /**
     * 绘制中心选择的光晕
     */
    private Paint mCenterHaloPaint;
    private float[] mHSV = new float[3];
    private OpacityBar mOpacityBar = null;
    private OpacityBar mBritbessBar = null;
    private boolean mTouchAnywhereOnColorWheelEnabled = true;
    private OnColorChangedListener onColorChangedListener;
    private OnColorSelectedListener onColorSelectedListener;
    private OnClickCenterListener clickCenterListener;

    public ColorPicker(Context context) {
        super(context);
        init(null, 0);
    }

    public ColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ColorPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public interface OnColorChangedListener {
        void onColorChanged(int color);
    }

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.onColorChangedListener = listener;
    }

    public interface OnClickCenterListener {
        void onClose();
    }

    public void setOnClickCenterListener(OnClickCenterListener clickCenterListener) {
        this.clickCenterListener = clickCenterListener;
    }
    private int oldSelectedListenerColor;

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.ColorPicker, defStyle, 0);
        final Resources b = getContext().getResources();

        mColorWheelThickness = a.getDimensionPixelSize(
                R.styleable.ColorPicker_color_wheel_thickness,
                b.getDimensionPixelSize(R.dimen.color_wheel_thickness));
        mColorWheelRadius = a.getDimensionPixelSize(
                R.styleable.ColorPicker_color_wheel_radius,
                b.getDimensionPixelSize(R.dimen.color_wheel_radius));
        mPreferredColorWheelRadius = mColorWheelRadius;
        mColorCenterRadius = a.getDimensionPixelSize(
                R.styleable.ColorPicker_color_center_radius,
                b.getDimensionPixelSize(R.dimen.color_center_radius));
        mPreferredColorCenterRadius = mColorCenterRadius;
        mColorCenterHaloRadius = a.getDimensionPixelSize(
                R.styleable.ColorPicker_color_center_halo_radius,
                b.getDimensionPixelSize(R.dimen.color_center_halo_radius));
        mPreferredColorCenterHaloRadius = mColorCenterHaloRadius;
        mColorPointerRadius = a.getDimensionPixelSize(
                R.styleable.ColorPicker_color_pointer_radius,
                b.getDimensionPixelSize(R.dimen.color_pointer_radius));
        mColorPointerHaloRadius = a.getDimensionPixelSize(
                R.styleable.ColorPicker_color_pointer_halo_radius,
                b.getDimensionPixelSize(R.dimen.color_pointer_halo_radius));
        a.recycle();

        mAngle = (float) (-Math.PI / 2);

        Shader s = new SweepGradient(0, 0, COLORS, null);

        mColorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorWheelPaint.setShader(s);
        mColorWheelPaint.setStyle(Paint.Style.STROKE);
        mColorWheelPaint.setStrokeWidth(mColorWheelThickness);

        mPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mPointerHaloPaint.setColor(Color.WHITE);
        mPointerHaloPaint.setColor(Color.BLACK);
        mPointerHaloPaint.setAlpha(0x50);
        mPointerColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerColor.setColor(calculateColor(mAngle));

        mCenterNewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterNewPaint.setColor(calculateColor(mAngle));
        mCenterNewPaint.setStyle(Paint.Style.FILL);

        mCenterOldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterOldPaint.setColor(calculateColor(mAngle));
        mCenterOldPaint.setStyle(Paint.Style.FILL);

        mCenterHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterHaloPaint.setColor(Color.parseColor("#34dedede"));

        mCenterNewColor = calculateColor(mAngle);
        mCenterOldColor = calculateColor(mAngle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
       // 平移，将画布的坐标原点向左右方向移动x，向上下方向移动y.canvas的默认位置是在（0,0）.
        //例如画布原点假如落在（1，1），那么translate（10，10）就是在原点（1，1）基础上分别在x轴、y轴移动10，则原点变为（11,11）。
        canvas.translate(mTranslationOffset, mTranslationOffset);
        // 画七彩环
        canvas.drawOval(mColorWheelRectangle, mColorWheelPaint);
        float[] pointerPosition = calculatePointerPosition(mAngle);

        // 画选色的小圆环
        canvas.drawCircle(pointerPosition[0], pointerPosition[1],
                mColorPointerHaloRadius, mPointerHaloPaint);
       ////第二个参数是x半径，第三个参数是y半径
        canvas.drawCircle(pointerPosition[0], pointerPosition[1],
                mColorPointerRadius, mPointerColor);
        //第二个参数是x半径，第三个参数是y半径
        canvas.drawCircle(0, 0, mColorCenterHaloRadius, mCenterHaloPaint);
        canvas.drawArc(mCenterRectangle, 0, 360, true, mCenterNewPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int intrinsicSize = 2 * (mPreferredColorWheelRadius + mColorPointerHaloRadius);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(intrinsicSize, widthSize);
        } else {
            width = intrinsicSize;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(intrinsicSize, heightSize);
        } else {
            height = intrinsicSize;
        }

        int min = Math.min(width, height);
        setMeasuredDimension(min, min);
        mTranslationOffset = min * 0.5f;

        mColorWheelRadius = min / 2 - mColorWheelThickness - mColorPointerHaloRadius;
        mColorWheelRectangle.set(-mColorWheelRadius, -mColorWheelRadius,
                mColorWheelRadius, mColorWheelRadius);

        mColorCenterRadius = (int) ((float) mPreferredColorCenterRadius * ((float) mColorWheelRadius / (float) mPreferredColorWheelRadius));
        mColorCenterHaloRadius = (int) ((float) mPreferredColorCenterHaloRadius * ((float) mColorWheelRadius / (float) mPreferredColorWheelRadius));
        mCenterRectangle.set(-mColorCenterRadius, -mColorCenterRadius,
                mColorCenterRadius, mColorCenterRadius);
    }

    private int ave(int s, int d, float p) {
        return s + Math.round(p * (d - s));
    }

    /**
     * 通过角度计算颜色值
     */
    private int calculateColor(float angle) {
        float unit = (float) (angle / (2 * Math.PI));
        if (unit < 0) {
            unit += 1;
        }

        if (unit <= 0) {
            mColor = COLORS[0];
            return COLORS[0];
        }
        if (unit >= 1) {
            mColor = COLORS[COLORS.length - 1];
            return COLORS[COLORS.length - 1];
        }

        float p = unit * (COLORS.length - 1);
        int i = (int) p;
        p -= i;

        int c0 = COLORS[i];
        int c1 = COLORS[i + 1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);

        mColor = Color.argb(a, r, g, b);
        return Color.argb(a, r, g, b);
    }

    public int getColor() {
        return mCenterNewColor;
    }

    public void setColor(int color) {
        mAngle = colorToAngle(color);
        mPointerColor.setColor(calculateColor(mAngle));
        if (mOpacityBar != null) {
            //设置opacity的值
            mOpacityBar.setColor(mColor);
            mOpacityBar.setOpacity(Color.alpha(color));
        }
        if (mBritbessBar != null) {
            mBritbessBar.setColor(mColor);
            mBritbessBar.setOpacity(Color.alpha(color));
        }
        setNewCenterColor(color);
    }

    private float colorToAngle(int color) {
        float[] colors = new float[3];
        Color.colorToHSV(color, colors);//colors[0] 色调

        return (float) Math.toRadians(-colors[0]);//角度转弧度
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);

        // 转换坐标得到到我们的内部坐标系
        float x = event.getX() - mTranslationOffset;
        float y = event.getY() - mTranslationOffset;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 检查用户是否按下
                float[] pointerPosition = calculatePointerPosition(mAngle);
                if (x >= (pointerPosition[0] - mColorPointerHaloRadius)
                        && x <= (pointerPosition[0] + mColorPointerHaloRadius)
                        && y >= (pointerPosition[1] - mColorPointerHaloRadius)
                        && y <= (pointerPosition[1] + mColorPointerHaloRadius)) {
                    mSlopX = x - pointerPosition[0];
                    mSlopY = y - pointerPosition[1];
                    mUserIsMovingPointer = true;
                    invalidate();
                }
                // 检测是否点到中心位置
                else if (x >= -mColorCenterRadius && x <= mColorCenterRadius
                        && y >= -mColorCenterRadius && y <= mColorCenterRadius) {
                    if (clickCenterListener != null) {
                        clickCenterListener.onClose();
                    }

                } else if (Math.sqrt(x * x + y * y) <= mColorWheelRadius + mColorPointerHaloRadius
                        && Math.sqrt(x * x + y * y) >= mColorWheelRadius - mColorPointerHaloRadius
                        && mTouchAnywhereOnColorWheelEnabled) {
                    mUserIsMovingPointer = true;
                    invalidate();
                } else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mUserIsMovingPointer) {
                    mAngle = (float) Math.atan2(y - mSlopY, x - mSlopX);
                    mPointerColor.setColor(calculateColor(mAngle));
                    setNewCenterColor(mCenterNewColor = calculateColor(mAngle));
                    invalidate();
                } else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                mUserIsMovingPointer = false;
                if (onColorSelectedListener != null && mCenterNewColor != oldSelectedListenerColor) {
                    onColorSelectedListener.onColorSelected(mCenterNewColor);
                    oldSelectedListenerColor = mCenterNewColor;
                }
                if (mOpacityBar != null) {
                    mOpacityBar.setColor(mColor);
                }
                if (mBritbessBar != null) {
                    mBritbessBar.setColor(mColor);
                }
                if (x >= -mColorCenterRadius && x <= mColorCenterRadius
                        && y >= -mColorCenterRadius && y <= mColorCenterRadius) {

                } else {
                    if (onColorChangedListener != null) {
                        onColorChangedListener.onColorChanged(mColor);
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (onColorSelectedListener != null && mCenterNewColor != oldSelectedListenerColor) {
                    onColorSelectedListener.onColorSelected(mCenterNewColor);
                    oldSelectedListenerColor = mCenterNewColor;
                }
                break;
        }
        return true;
    }

    /**
     * 计算当前x,y坐标
     */
    private float[] calculatePointerPosition(float angle) {
        float x = (float) (mColorWheelRadius * Math.cos(angle));
        float y = (float) (mColorWheelRadius * Math.sin(angle));

        return new float[]{x, y};
    }


    /**
     * 把透明度控制的bar关联起来
     */
    public void addOpacityBar(OpacityBar bar) {
        mOpacityBar = bar;
        mOpacityBar.setColor(mColor);
    }

    public void addBritbessBar(OpacityBar bar) {
        mBritbessBar = bar;
        mBritbessBar.setColorPicker(this);
        mBritbessBar.setColor(mColor);
    }

    /**
     * 设置当前选择的颜色
     */
    public void setNewCenterColor(int color) {
        Log.i(TAG, "color=======" + Integer.toHexString(color));
        mCenterNewColor = color;
        mCenterNewPaint.setColor(color);
        if (mCenterOldColor == 0) {
            mCenterOldColor = color;
            mCenterOldPaint.setColor(color);
        }
        invalidate();
    }

    /**
     * 设置之前的颜色
     */
    public void setOldCenterColor(int color) {
        mCenterOldColor = color;
        mCenterOldPaint.setColor(color);
        invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        Bundle state = new Bundle();
        state.putParcelable(STATE_PARENT, superState);
        state.putFloat(STATE_ANGLE, mAngle);
        state.putInt(STATE_OLD_COLOR, mCenterOldColor);

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;

        Parcelable superState = savedState.getParcelable(STATE_PARENT);
        super.onRestoreInstanceState(superState);

        mAngle = savedState.getFloat(STATE_ANGLE);
        setOldCenterColor(savedState.getInt(STATE_OLD_COLOR));
        int currentColor = calculateColor(mAngle);
        mPointerColor.setColor(currentColor);
        setNewCenterColor(currentColor);
    }

}
