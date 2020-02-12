
package com.lister.nestedscrolltest.mounting;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lister.nestedscrolltest.R;
import com.youth.banner.Banner;

public class NestedLinearLayout extends LinearLayout implements NestedScrollingParent2 {

    private int TOUCH_SLOP;
    private boolean mIsBeingDragged;
    private int mLastMotionY;
    private int mLastY;

    private Banner mBanner;
    private int mBannerHeight;
    private RecyclerView mRecyclerView;
    private NestedScrollingParentHelper mNestedParentHelper;
    private VelocityTracker mVelocityTracker;
    private Scroller mScroller;
    private int mMaxVelocity;
    /**
     * 是否已经将 parent 的滑动速率传递给 RecyclerView, 一次事件流只需要传递一次
     */
    private boolean mHasFlingFromParentToRV;

    public NestedLinearLayout(Context context) {
        this(context, null);
    }

    public NestedLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context);
        ViewConfiguration vc = ViewConfiguration.get(context);
        TOUCH_SLOP = vc.getScaledTouchSlop();
        mMaxVelocity = vc.getScaledMaximumFlingVelocity();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findBanner(this);
        findRecyclerView(this);
        if (mBanner == null) {
            throw new RuntimeException("there is no Banner to scroll!");
        }
        if (mRecyclerView == null) {
            throw new RuntimeException("there is no RecyclerView to scroll!");
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBannerHeight = mBanner.getMeasuredHeight();
        LayoutParams layoutParams = (LayoutParams) mRecyclerView.getLayoutParams();
        layoutParams.height = getMeasuredHeight() - getContext().getResources()
                .getDimensionPixelSize(R.dimen.comment_header_text_height);
        mRecyclerView.setLayoutParams(layoutParams);
    }

    private void findBanner(ViewGroup parent) {
        if (mBanner != null) {
            return;
        }
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof Banner) {
                mBanner = (Banner) child;
                return;
            }
        }
    }

    private void findRecyclerView(ViewGroup parent) {
        if (mRecyclerView != null) {
            return;
        }
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof RecyclerView) {
                mRecyclerView = (RecyclerView) child;
                return;
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            mHasFlingFromParentToRV = false;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mIsBeingDragged = false;
                mLastMotionY = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int y = (int) ev.getRawY();
                int diff = Math.abs(mLastMotionY - y);
                mLastMotionY = y;
                boolean canRecyclerViewScroll = true;
                if (mRecyclerView != null) {
                    canRecyclerViewScroll = mRecyclerView.canScrollVertically(-1);
                }
                if (diff > TOUCH_SLOP && !canRecyclerViewScroll
                        && !isInNestedArea((int) ev.getRawX(), (int) ev.getRawY())) {
                    mIsBeingDragged = true;
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mIsBeingDragged = false;
                break;
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mRecyclerView != null && mRecyclerView.canScrollVertically(-1)) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // scroll 处理
                mLastY = (int) event.getRawY();
                // fling 处理
                initOrResetVelocityTracker();
                resetScroller();
                break;
            case MotionEvent.ACTION_MOVE:
                // scroll 处理
                if (mLastY == 0) {
                    mLastY = (int) event.getRawY();
                }
                int y = (int) event.getRawY();
                int dy = mLastY - y;
                mLastY = y;
                scrollBy(0, dy);
                // fling 处理
                initVelocityTrackerIfNotExist();
                mVelocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_UP:
                // scroll 处理
                mLastY = 0;
                // fling 处理
                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                int velocityY = (int) -mVelocityTracker.getYVelocity();
                recycleVelocityTracker();
                parentFling(velocityY);
                break;
        }
        return true;
    }

    private void parentFling(int velocityY) {
        mScroller.fling(0, getScrollY(), 0, velocityY,
                0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int curY = mScroller.getCurrY();
            scrollTo(0, curY);
            invalidate();
            if (getScrollY() == curY && !mHasFlingFromParentToRV) {
                int velocityY = (int) mScroller.getCurrVelocity();
                if (mRecyclerView != null) {
                    mRecyclerView.fling(0, velocityY);
                    mHasFlingFromParentToRV = true;
                }
            }
        }
    }

    private boolean isInNestedArea(int x, int y) {
        if (mRecyclerView == null) {
            return false;
        }
        int[] location = new int[2];
        mRecyclerView.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + mRecyclerView.getMeasuredWidth();
        int bottom = top + mRecyclerView.getMeasuredHeight();
        if (x >= left && x <= right && y >= top && y <= bottom) {
            return true;
        }
        return false;
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExist() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void resetScroller() {
        if (mScroller != null && !mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        if (y < 0) {
            y = 0;
        }
        if (y > mBannerHeight) {
            y = mBannerHeight;
        }
        super.scrollTo(x, y);
    }

    /**
     * ---------- NestedScrolling ----------
     */

    private NestedScrollingParentHelper getNestedParentHelper() {
        if (mNestedParentHelper == null) {
            mNestedParentHelper = new NestedScrollingParentHelper(this);
        }
        return mNestedParentHelper;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        getNestedParentHelper().onNestedScrollAccepted(child, target, axes, type);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        getNestedParentHelper().onStopNestedScroll(target, type);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {

    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (dy > 0 && getScrollY() < mBannerHeight) {
            // 手指向上, 内容向下, 可上滑的距离为 Banner 高度
            consumed[1] = dy;
            scrollBy(0, dy);
        } else if (dy < 0 && !mRecyclerView.canScrollVertically(-1) && getScrollY() > 0) {
            // 手指向下, 内容向上, 当 RecyclerView 无法上滑时可以开始显示 Banner
            consumed[1] = dy;
            scrollBy(0, dy);
        }
    }
}
