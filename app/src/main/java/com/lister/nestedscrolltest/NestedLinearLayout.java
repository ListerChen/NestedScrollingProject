
package com.lister.nestedscrolltest;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.youth.banner.Banner;

public class NestedLinearLayout extends LinearLayout implements NestedScrollingParent2 {

    private int TOUCH_SLOP;

    private Banner mBanner;
    private int mBannerHeight;
    private RecyclerView mRecyclerView;
    private NestedScrollingParentHelper mNestedParentHelper;
    private int mMaxVelocity;

    public NestedLinearLayout(Context context) {
        this(context, null);
    }

    public NestedLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
        Log.e("TAGGG", "onSizeChanged");
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
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
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
