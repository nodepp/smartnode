package com.nodepp.smartnode.view.Behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

import com.nodepp.smartnode.MyApplication;
import com.nodepp.smartnode.R;

import java.lang.ref.WeakReference;

/**
 * Created by jyx on 2018/8/24.
 * 可以滚动的列表头部
 */

public class HeaderPagerBehavior extends ViewOffsetBehavior {
    private static final String TAG = "HeaderPager";
    public static final int STATE_OPENED = 0;
    public static final int STATE_CLOSED = 1;
    public static final int DURATION_SHORT = 300;
    public static final int DURATION_LONG = 600;

    private int mCurState = STATE_OPENED;
    private OnPagerStateListener mPagerStateListener;

    private OverScroller mOverScroller;

    private WeakReference<CoordinatorLayout> mParent;
    private WeakReference<View> mChild;

    public void setPagerStateListener(OnPagerStateListener pagerStateListener) {
        mPagerStateListener = pagerStateListener;
    }

    public HeaderPagerBehavior() {
        init();
    }


    public HeaderPagerBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mOverScroller = new OverScroller(MyApplication.getAppContext());
    }

    @Override
    protected void layoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        super.layoutChild(parent, child, layoutDirection);
        mParent = new WeakReference<CoordinatorLayout>(parent);
        mChild = new WeakReference<View>(child);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0 && canScroll(child, 0) && !isClosed(child);
    }


    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, View child, View target, float velocityX, float velocityY) {
        // consumed the flinging behavior until Closed
        return !isClosed(child);
    }


    private boolean isClosed(View child) {
        boolean isClosed = child.getTranslationY() == getHeaderOffsetRange();
        return isClosed;
    }

    public boolean isClosed() {
        return mCurState == STATE_CLOSED;
    }


    private void changeState(int newState) {
        if (mCurState != newState) {
            mCurState = newState;
            if (mCurState == STATE_OPENED) {
                mPagerStateListener.onPagerOpened();
            } else {
                mPagerStateListener.onPagerClosed();
            }
        }

    }

    private boolean canScroll(View child, float pendingDy) {
        int pendingTranslationY = (int) (child.getTranslationY() - pendingDy);
        if (pendingTranslationY >= getHeaderOffsetRange() && pendingTranslationY <= 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, final View child, MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP && !isClosed()) {
            handleActionUp(parent, child);
        }
        return super.onInterceptTouchEvent(parent, child, ev);
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
        //dy>0 scroll up;dy<0,scroll down
        float halfOfDis = dy / 4.0f;
        if (!canScroll(child, halfOfDis)) {
            child.setTranslationY(halfOfDis > 0 ? getHeaderOffsetRange() : 0);
        } else {
            child.setTranslationY(child.getTranslationY() - halfOfDis);
        }
        //consumed all scroll behavior after we started Nested Scrolling
        consumed[1] = dy;
    }


    private int getHeaderOffsetRange() {
        return MyApplication.getAppContext().getResources().getDimensionPixelOffset(R.dimen.header_pager_offset);
    }


    private void handleActionUp(CoordinatorLayout parent, final View child) {
        if (mFlingRunnable != null) {
            child.removeCallbacks(mFlingRunnable);
            mFlingRunnable = null;
        }
        mFlingRunnable = new FlingRunnable(parent, child);
        if (child.getTranslationY() < getHeaderOffsetRange() / 3.0f) {
            mFlingRunnable.scrollToClosed(DURATION_SHORT);
        } else {
            mFlingRunnable.scrollToOpen(DURATION_SHORT);
        }

    }

    private void onFlingFinished(CoordinatorLayout coordinatorLayout, View layout) {
        changeState(isClosed(layout) ? STATE_CLOSED : STATE_OPENED);
    }

    public void openPager() {
        openPager(DURATION_LONG);
    }

    /**
     * @param duration open animation duration
     */
    public void openPager(int duration) {
        View child = mChild.get();
        CoordinatorLayout parent = mParent.get();
        if (isClosed() && child != null) {
            if (mFlingRunnable != null) {
                child.removeCallbacks(mFlingRunnable);
                mFlingRunnable = null;
            }
            mFlingRunnable = new FlingRunnable(parent, child);
            mFlingRunnable.scrollToOpen(duration);
        }
    }

    public void closePager() {
        closePager(DURATION_LONG);
    }

    /**
     * @param duration close animation duration
     */
    public void closePager(int duration) {
        View child = mChild.get();
        CoordinatorLayout parent = mParent.get();
        if (!isClosed()) {
            if (mFlingRunnable != null) {
                child.removeCallbacks(mFlingRunnable);
                mFlingRunnable = null;
            }
            mFlingRunnable = new FlingRunnable(parent, child);
            mFlingRunnable.scrollToClosed(duration);
        }
    }


    private FlingRunnable mFlingRunnable;

    /**
     * For animation , Why not use {@link android.view.ViewPropertyAnimator } to play animation is of the
     * other {@link android.support.design.widget.CoordinatorLayout.Behavior} that depend on this could not receiving the correct result of
     * {@link View#getTranslationY()} after animation finished for whatever reason that i don't know
     */
    private class FlingRunnable implements Runnable {
        private final CoordinatorLayout mParent;
        private final View mLayout;

        FlingRunnable(CoordinatorLayout parent, View layout) {
            mParent = parent;
            mLayout = layout;
        }

        public void scrollToClosed(int duration) {
            float curTranslationY = ViewCompat.getTranslationY(mLayout);
            float dy = getHeaderOffsetRange() - curTranslationY;
            mOverScroller.startScroll(0, Math.round(curTranslationY - 0.1f), 0, Math.round(dy + 0.1f), duration);
            start();
        }

        public void scrollToOpen(int duration) {
            float curTranslationY = ViewCompat.getTranslationY(mLayout);
            mOverScroller.startScroll(0, (int) curTranslationY, 0, (int) -curTranslationY, duration);
            start();
        }

        private void start() {
            if (mOverScroller.computeScrollOffset()) {
                mFlingRunnable = new FlingRunnable(mParent, mLayout);
                ViewCompat.postOnAnimation(mLayout, mFlingRunnable);
            } else {
                onFlingFinished(mParent, mLayout);
            }
        }


        @Override
        public void run() {
            if (mLayout != null && mOverScroller != null) {
                if (mOverScroller.computeScrollOffset()) {
                    ViewCompat.setTranslationY(mLayout, mOverScroller.getCurrY());
                    ViewCompat.postOnAnimation(mLayout, this);
                } else {
                    onFlingFinished(mParent, mLayout);
                }
            }
        }
    }

    /**
     * callback for HeaderPager 's state
     */
    public interface OnPagerStateListener {
        /**
         * do callback when pager closed
         */
        void onPagerClosed();

        /**
         * do callback when pager opened
         */
        void onPagerOpened();
    }

}
