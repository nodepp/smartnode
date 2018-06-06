/**
 * Created by yuyue on 2017/3/15.
 */

package com.nodepp.smartnode.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.nodepp.smartnode.R;

public class OpacityBar extends View {
    private static final String STATE_PARENT = "parent";
    private static final String STATE_COLOR = "color";
    private static final String STATE_OPACITY = "opacity";
    private static final String STATE_ORIENTATION = "orientation";
    private static final boolean ORIENTATION_HORIZONTAL = true;
    private static final boolean ORIENTATION_VERTICAL = false;
    private float dimen;
    /**
     * 默认bar的方向是水平方向
     */
    private static final boolean ORIENTATION_DEFAULT = ORIENTATION_HORIZONTAL;
    private int mBarThickness;
    private int mBarLength;
    private int mPreferredBarLength;
    private int mBarPointerRadius;
    private int mBarPointerHaloRadius;
    private int mBarPointerPosition;

    /**
     * 整个bar的画笔
     */
    private Paint mBarPaint;

    /**
     * 滑动圆圈的外圆画笔
     */
    private Paint mBarPointerPaint;

    /**
     * 滑动圆圈的内圆画笔
     */
    private Paint mBarPointerHaloPaint;

    /**
     * bar图形
     */
    private RectF mBarRect = new RectF();

    /**
     * 渐变颜色的值
     */
    private Shader shader;

    /**
     * 是否在滑动bar
     */
    private boolean mIsMovingPointer;

    /**
     * 当前选中的颜色值
     */
    private int mColor;

    /**
     * 存放HSV值的数组。
     */
    private float[] mHSVColor = new float[3];

    /**
     * 255和整个bar长度的比值
     */
    private float mPosToOpacFactor;

    /**
     * 整个bar长度和255的比值
     */
    private float mOpacToPosFactor;
    private OnOpacityChangedListener onOpacityChangedListener;

    private int oldChangedListenerOpacity;

    public interface OnOpacityChangedListener {
        void onOpacityChanged(int opacity);
    }

    public void setOnOpacityChangedListener(OnOpacityChangedListener listener) {
        this.onOpacityChangedListener = listener;
    }

    public OnOpacityChangedListener getOnOpacityChangedListener() {
        return this.onOpacityChangedListener;
    }

    private ColorPicker mPicker = null;

    /**
     * bar的方向，true表示水平方向，false表示竖直方向
     */
    private boolean mOrientation;

    public OpacityBar(Context context) {
        super(context);
        init(null, 0);
    }

    public OpacityBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public OpacityBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.ColorBars, defStyle, 0);
        final Resources b = getContext().getResources();

        mBarThickness = a.getDimensionPixelSize(
                R.styleable.ColorBars_bar_thickness,
                b.getDimensionPixelSize(R.dimen.bar_thickness));
        mBarLength = a.getDimensionPixelSize(R.styleable.ColorBars_bar_length,
                b.getDimensionPixelSize(R.dimen.bar_length));
        mPreferredBarLength = mBarLength;
        mBarPointerRadius = a.getDimensionPixelSize(
                R.styleable.ColorBars_bar_pointer_radius,
                b.getDimensionPixelSize(R.dimen.bar_pointer_radius));
        mBarPointerHaloRadius = a.getDimensionPixelSize(
                R.styleable.ColorBars_bar_pointer_halo_radius,
                b.getDimensionPixelSize(R.dimen.bar_pointer_halo_radius));
        mOrientation = a.getBoolean(
                R.styleable.ColorBars_bar_orientation_horizontal, ORIENTATION_DEFAULT);

        a.recycle();

        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPaint.setShader(shader);

        mBarPointerPosition = mBarLength + mBarPointerHaloRadius;

        mBarPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mBarPointerHaloPaint.setColor(Color.WHITE);
        mBarPointerHaloPaint.setColor(Color.BLACK);
        mBarPointerHaloPaint.setAlpha(0x50);

        mBarPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPointerPaint.setColor(Color.TRANSPARENT);

        mPosToOpacFactor = 0xFF / ((float) mBarLength);
        mOpacToPosFactor = ((float) mBarLength) / 0xFF;
    }
     public void setBarPointerColor(){
         if (mBarPointerHaloPaint != null){
             mBarPointerHaloPaint.setColor(Color.WHITE);
             mBarPointerPaint.setColor(Color.WHITE);
         }

    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int intrinsicSize = mPreferredBarLength
                + (mBarPointerHaloRadius * 2);
        int measureSpec;
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            measureSpec = widthMeasureSpec;
        } else {
            measureSpec = heightMeasureSpec;
        }
        int lengthMode = MeasureSpec.getMode(measureSpec);
        int lengthSize = MeasureSpec.getSize(measureSpec);

        int length;
        if (lengthMode == MeasureSpec.EXACTLY) {
            length = lengthSize;
        } else if (lengthMode == MeasureSpec.AT_MOST) {
            length = Math.min(intrinsicSize, lengthSize);
        } else {
            length = intrinsicSize;
        }

        int barPointerHaloRadiusx2 = mBarPointerHaloRadius * 2;
        mBarLength = length - barPointerHaloRadiusx2;
        if (mOrientation == ORIENTATION_VERTICAL) {
            setMeasuredDimension(barPointerHaloRadiusx2,
                    (mBarLength + barPointerHaloRadiusx2));
        } else {
            setMeasuredDimension((mBarLength + barPointerHaloRadiusx2),
                    barPointerHaloRadiusx2);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 根据bar的方向设置长度和宽度初始值
        int x1, y1;
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            x1 = (mBarLength + mBarPointerHaloRadius);
            y1 = mBarThickness;
            mBarLength = w - (mBarPointerHaloRadius * 2);
            mBarRect.set(mBarPointerHaloRadius,
                    (mBarPointerHaloRadius - (mBarThickness / 2)),
                    (mBarLength + (mBarPointerHaloRadius)),
                    (mBarPointerHaloRadius + (mBarThickness / 2)));
        } else {
            x1 = mBarThickness;
            y1 = (mBarLength + mBarPointerHaloRadius);
            mBarLength = h - (mBarPointerHaloRadius * 2);
            mBarRect.set((mBarPointerHaloRadius - (mBarThickness / 2)),
                    mBarPointerHaloRadius,
                    (mBarPointerHaloRadius + (mBarThickness / 2)),
                    (mBarLength + (mBarPointerHaloRadius)));
        }

        // 根据mbarlength进行更新
        if (!isInEditMode()) {
            shader = new LinearGradient(mBarPointerHaloRadius, 0,
                    x1, y1, new int[]{
                    Color.HSVToColor(0x00, mHSVColor),
                    Color.HSVToColor(0xFF, mHSVColor)}, null,
                    Shader.TileMode.CLAMP);
        } else {
            shader = new LinearGradient(mBarPointerHaloRadius, 0,
                    x1, y1, new int[]{
                    0x2200FCC0,0x8800FCC0, 0xff00FCC0}, null, Shader.TileMode.CLAMP);
            Color.colorToHSV(0xff00FCC0, mHSVColor);
        }

        mBarPaint.setShader(shader);
        mPosToOpacFactor = 0xFF / ((float) mBarLength);
        mOpacToPosFactor = ((float) mBarLength) / 0xFF;

        float[] hsvColor = new float[3];
        Color.colorToHSV(mColor, hsvColor);

        if (!isInEditMode()) {
            mBarPointerPosition = Math.round((mOpacToPosFactor * Color.alpha(mColor))
                    + mBarPointerHaloRadius);
        } else {
            mBarPointerPosition = mBarLength + mBarPointerHaloRadius;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(mBarRect, mBarPaint);

        int cX, cY;
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            cX = mBarPointerPosition;
            cY = mBarPointerHaloRadius;
        } else {
            cX = mBarPointerHaloRadius;
            cY = mBarPointerPosition;
        }

        // 画滑动圆圈的小圆
        canvas.drawCircle(cX, cY, mBarPointerHaloRadius, mBarPointerHaloPaint);
        // 画滑动圆圈外面的大圆
        canvas.drawCircle(cX, cY, mBarPointerRadius, mBarPointerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            dimen = event.getX();
        } else {
            dimen = event.getY();
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsMovingPointer = true;
                if (dimen >= (mBarPointerHaloRadius)
                        && dimen <= (mBarPointerHaloRadius + mBarLength)) {
                    reflashBar(dimen);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsMovingPointer) {
                    if (dimen >= mBarPointerHaloRadius
                            && dimen <= (mBarPointerHaloRadius + mBarLength)) {
                        reflashBar(dimen);
                    } else if (dimen < mBarPointerHaloRadius) {
                        reflashBarTwo();
                    } else if (dimen > (mBarPointerHaloRadius + mBarLength)) {
                        reflashBarThree();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                changeListener();
                mIsMovingPointer = false;
                invalidate();
                break;
        }
        return true;
    }

    public void changeListener() {
        if (onOpacityChangedListener != null && oldChangedListenerOpacity != getOpacity()) {
            onOpacityChangedListener.onOpacityChanged(getOpacity());
            oldChangedListenerOpacity = getOpacity();
        }
    }

    public void reflashBarThree() {
        mBarPointerPosition = mBarPointerHaloRadius + mBarLength;
        mColor = Color.HSVToColor(mHSVColor);
        mBarPointerPaint.setColor(mColor);
        if (mPicker != null) {
            mPicker.setNewCenterColor(mColor);
        }
        invalidate();
    }

    public void reflashBarTwo() {
        mBarPointerPosition = mBarPointerHaloRadius;
        mColor = Color.TRANSPARENT;
        mBarPointerPaint.setColor(mColor);
        if (mPicker != null) {
            mPicker.setNewCenterColor(mColor);
        }
        invalidate();
    }

    public void reflashBar(float dimen) {
        this.dimen = dimen;
        mBarPointerPosition = Math.round(dimen);
        calculateColor(Math.round(dimen));
        mBarPointerPaint.setColor(mColor);
        if (mPicker != null) {
            mPicker.setNewCenterColor(mColor);
        }
        invalidate();


    }

    public float getDimen() {
        return dimen;
    }

    /**
     * 设置bar颜色
     */
    public void setColor(int color) {
        int x1, y1;
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            x1 = (mBarLength + mBarPointerHaloRadius);
            y1 = mBarThickness;
        } else {
            x1 = mBarThickness;
            y1 = (mBarLength + mBarPointerHaloRadius);
        }

        Color.colorToHSV(color, mHSVColor);
        shader = new LinearGradient(mBarPointerHaloRadius, 0,
                x1, y1, new int[]{
                Color.HSVToColor(0x00, mHSVColor), color}, null,
                Shader.TileMode.CLAMP);
        mBarPaint.setShader(shader);
        calculateColor(mBarPointerPosition);
        mBarPointerPaint.setColor(mColor);
        if (mPicker != null) {
            mPicker.setNewCenterColor(mColor);
        }
        invalidate();
    }

    /**
     * 设置透明度
     * 透明度只能在0到255之间
     */
    public void setOpacity(int opacity) {
        mBarPointerPosition = Math.round((mOpacToPosFactor * opacity))
                + mBarPointerHaloRadius;
        calculateColor(mBarPointerPosition);
        mBarPointerPaint.setColor(mColor);
        if (mPicker != null) {
            mPicker.setNewCenterColor(mColor);
        }
        invalidate();
    }

    /**
     * 获取当前选中位置的透明度值
     */
    public int getOpacity() {
        int opacity = Math
                .round((mPosToOpacFactor * (mBarPointerPosition - mBarPointerHaloRadius)));
        if (opacity < 5) {
            return 0x00;
        } else if (opacity > 250) {
            return 0xFF;
        } else {
            return opacity;
        }
    }

    /**
     * 计算当前位置颜色
     */
    private void calculateColor(int coord) {
        coord = coord - mBarPointerHaloRadius;
        if (coord < 0) {
            coord = 0;
        } else if (coord > mBarLength) {
            coord = mBarLength;
        }

        mColor = Color.HSVToColor(
                Math.round(mPosToOpacFactor * coord),
                mHSVColor);
        if (Color.alpha(mColor) > 250) {
            mColor = Color.HSVToColor(mHSVColor);
        } else if (Color.alpha(mColor) < 5) {
            mColor = Color.TRANSPARENT;
        }
    }

    /**
     * 获取当前的颜色值
     */
    public int getColor() {
        return mColor;
    }

    /**
     * 设置colorpicker,颜色选择器一旦设置了，不要换
     *
     * @param picker
     */
    public void setColorPicker(ColorPicker picker) {
        mPicker = picker;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        Bundle state = new Bundle();
        state.putParcelable(STATE_PARENT, superState);
        state.putFloatArray(STATE_COLOR, mHSVColor);
        state.putInt(STATE_OPACITY, getOpacity());

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;

        Parcelable superState = savedState.getParcelable(STATE_PARENT);
        super.onRestoreInstanceState(superState);

        setColor(Color.HSVToColor(savedState.getFloatArray(STATE_COLOR)));
        setOpacity(savedState.getInt(STATE_OPACITY));
    }
}