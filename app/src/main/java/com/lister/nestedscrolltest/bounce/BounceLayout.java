
package com.lister.nestedscrolltest.bounce;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;

public class BounceLayout extends LinearLayout implements NestedScrollingParent2 {
    /**
     * 向外弹出时的减速度
     */
    private static final float BOUNCE_BACK_DECELERATION = 90000;
    /**
     * 回弹动画的duration
     */
    private static final int SPRING_BACK_DURATION = 250;

    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private OverScrollerInterpolator mInterpolator;
    private int mOrientation;
    private Scroller mScroller;

    private BounceAnimRunnable mBounceRunnable;
    private int mMaxOverScrollDistance;
    private int mOverScrollBorder;
    private int mOverScrollDistance = 0;

    private ValueAnimator mSpringBackAnimator;
    private int mLastSign;

    public BounceLayout(Context context) {
        this(context, null);
    }

    public BounceLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BounceLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mMaxOverScrollDistance = 200;
        mOverScrollBorder = mMaxOverScrollDistance * 3;
        mScroller = new Scroller(getContext());
        mInterpolator = new OverScrollerInterpolator(0.6f);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        mOrientation = axes;
        if (mSpringBackAnimator != null) {
            mSpringBackAnimator.cancel();
        }
        if (mBounceRunnable != null) {
            mBounceRunnable.cancel();
        }
        return isEnabled();
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        int scrollY = getScrollY();
        if (scrollY > 0 && dy < 0) {
            if (scrollY + dy >= 0) {
                consumed[1] = dy;
                recoverOverScroll(dy);
            } else {
                recoverOverScroll(-scrollY);
                consumed[1] = -scrollY;
            }
        } else if (scrollY < 0 && dy > 0) {
            if (scrollY + dy <= 0) {
                consumed[1] = dy;
                recoverOverScroll(dy);
            } else {
                recoverOverScroll(-scrollY);
                consumed[1] = -scrollY;
            }
        }
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        if (mOrientation == ViewCompat.SCROLL_AXIS_VERTICAL && dyUnconsumed != 0) {
            if (type == ViewCompat.TYPE_TOUCH) {
                startOverScroll(dyUnconsumed);
            } else {
                if (mOverScrollDistance == 0) {
                    startOverScroll(dyUnconsumed);
                    mScroller.computeScrollOffset();
                    startBounceAnimator(mScroller.getCurrVelocity() * mLastSign);
                } else {
                    startOverScroll(dyUnconsumed);
                }
                ViewCompat.stopNestedScroll(target, type);
            }
        }
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        if (mOverScrollDistance != 0) {
            startScrollBackAnimator();
        }
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        mLastSign = velocityY < 0 ? -1 : 1;
        mScroller.forceFinished(true);
        mScroller.fling(0, 0, 0, (int) velocityY, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        return false;
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return super.onNestedFling(target, velocityX, velocityY, consumed);
    }

    private void startOverScroll(int dy) {
        updateOverScrollDistance(mOverScrollDistance + dy);
    }

    private void recoverOverScroll(int dy) {
        int scrollY = getScrollY();
        float fraction = (float) (scrollY + dy) / mMaxOverScrollDistance;
        mOverScrollDistance = (int) (mInterpolator.getInterpolationBack(Math.abs(fraction)) * mOverScrollBorder);
        mOverScrollDistance = fraction < 0 ? -mOverScrollDistance : mOverScrollDistance;
        scrollBy(0, dy);
    }

    private void startBounceAnimator(float velocity) {
        if (mBounceRunnable != null) {
            mBounceRunnable.cancel();
        }
        mBounceRunnable = new BounceAnimRunnable(velocity, mOverScrollDistance);
        mBounceRunnable.start();
    }

    public void startScrollBackAnimator() {
        if (mSpringBackAnimator != null) {
            mSpringBackAnimator.cancel();
        }
        mSpringBackAnimator = ValueAnimator.ofInt(mOverScrollDistance, 0);
        mSpringBackAnimator.setInterpolator(new DecelerateInterpolator());
        mSpringBackAnimator.setDuration(SPRING_BACK_DURATION);
        mSpringBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                updateOverScrollDistance((Integer) animation.getAnimatedValue());
            }
        });
        mSpringBackAnimator.start();
    }

    private void updateOverScrollDistance(int distance) {
        mOverScrollDistance = distance;
        if (mOverScrollDistance < 0) {
            scrollTo(0, (int) (-mMaxOverScrollDistance * mInterpolator.getInterpolation(
                    Math.abs((float) mOverScrollDistance / mOverScrollBorder))));
        } else {
            scrollTo(0, (int) (mMaxOverScrollDistance * mInterpolator.getInterpolation(
                    Math.abs((float) mOverScrollDistance / mOverScrollBorder))));
        }
    }

    private class OverScrollerInterpolator implements Interpolator {

        private float mFactor;

        public OverScrollerInterpolator(float factor) {
            mFactor = factor;
        }

        public float getInterpolationBack(float input) {
            return (float) (Math.log(1 - input) / Math.log(mFactor) / 2);
        }

        @Override
        public float getInterpolation(float input) {
            return (float) (1 - Math.pow(mFactor, input * 2));
        }
    }

    private class BounceAnimRunnable implements Runnable {

        private static final int FRAME_TIME = 14;

        private final float mDeceleration;
        private float mVelocity;
        private int mStartY;
        private int mRuntime = 0;
        private int mDuration = 0;
        private boolean cancel;

        public void cancel() {
            cancel = true;
            removeCallbacks(this);
        }

        public BounceAnimRunnable(float velocity, int startY) {
            mDeceleration = mVelocity < 0 ? BOUNCE_BACK_DECELERATION : -BOUNCE_BACK_DECELERATION;
            mVelocity = velocity;
            mStartY = startY;
            mDuration = (int) ((-mVelocity / mDeceleration) * 1000);
        }

        public void start() {
            postDelayed(this, FRAME_TIME);
        }

        @Override
        public void run() {
            if (cancel) {
                return;
            }
            mRuntime += FRAME_TIME;
            float t = (float) mRuntime / 1000;
            int distance = (int) (mStartY + mVelocity * t + 0.5 * mDeceleration * t * t);
            updateOverScrollDistance(distance);
            if (mRuntime < mDuration && Math.abs(distance) < mMaxOverScrollDistance * 2) {
                postDelayed(this, FRAME_TIME);
            } else {
                startScrollBackAnimator();
            }
        }
    }
}
