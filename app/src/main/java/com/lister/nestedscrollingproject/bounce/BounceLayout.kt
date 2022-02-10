
package com.lister.nestedscrollingproject.bounce

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.FrameLayout
import android.widget.OverScroller
import androidx.core.view.NestedScrollingParent2
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import com.lister.nestedscrollingproject.mixed.QuinticInterpolator
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow

class BounceLayout : FrameLayout, NestedScrollingParent2 {

    companion object {
        /**
         * 向外弹出时的减速度
         */
        const val BOUNCE_BACK_DECELERATION = 90000
        /**
         * 回弹动画的duration
         */
        const val SPRING_BACK_DURATION = 250
    }

    private val mNestedScrollingParentHelper: NestedScrollingParentHelper = NestedScrollingParentHelper(this)
    private val mInterpolator: OverScrollerInterpolator = OverScrollerInterpolator(0.3f)
    private var mOrientation = 0
    private val mScroller: OverScroller = OverScroller(context, QuinticInterpolator())

    private var mBounceRunnable: BounceAnimRunnable? = null
    private val mMaxOverScrollDistance = 600
    private var mOverScrollBorder = 0
    private var mOverScrollDistance = 0

    private var mSpringBackAnimator: ValueAnimator? = null
    private var mLastSign = 0

    constructor(context: Context) : super(context) {
        initSettings()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initSettings()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initSettings()
    }

    private fun initSettings() {
        mOverScrollBorder = mMaxOverScrollDistance * 6
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        mOrientation = axes
        mSpringBackAnimator?.cancel()
        mBounceRunnable?.cancel()
        return isEnabled
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        val scrollY = scrollY
        if (scrollY > 0 && dy < 0) {
            if (scrollY + dy >= 0) {
                consumed[1] = dy
                recoverOverScroll(dy)
            } else {
                recoverOverScroll(-scrollY)
                consumed[1] = -scrollY
            }
        } else if (scrollY < 0 && dy > 0) {
            if (scrollY + dy <= 0) {
                consumed[1] = dy
                recoverOverScroll(dy)
            } else {
                recoverOverScroll(-scrollY)
                consumed[1] = -scrollY
            }
        }
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        if (mOrientation == ViewCompat.SCROLL_AXIS_VERTICAL && dyUnconsumed != 0) {
            if (type == ViewCompat.TYPE_TOUCH) {
                startOverScroll(dyUnconsumed)
            } else {
                if (mOverScrollDistance == 0) {
                    mScroller.computeScrollOffset()
                    startBounceAnimator(mScroller.currVelocity * mLastSign)
                } else {
                    startOverScroll(dyUnconsumed)
                }
                ViewCompat.stopNestedScroll(target, type)
            }
        }
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        mNestedScrollingParentHelper.onStopNestedScroll(target)
        if (mOverScrollDistance != 0) {
            startScrollBackAnimator()
        }
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        mLastSign = if (velocityY < 0) -1 else 1
        mScroller.forceFinished(true)
        mScroller.fling(0, 0, 0, velocityY.toInt(),
            -Int.MAX_VALUE, Int.MAX_VALUE, -Int.MAX_VALUE, Int.MAX_VALUE)
        return false
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return false
    }

    private fun startOverScroll(dy: Int) {
        updateOverScrollDistance(mOverScrollDistance + dy)
    }

    private fun recoverOverScroll(dy: Int) {
        val scrollY = scrollY
        val fraction = (scrollY + dy).toFloat() / mMaxOverScrollDistance
        mOverScrollDistance =
            (mInterpolator.getInterpolationBack(abs(fraction)) * mOverScrollBorder).toInt()
        mOverScrollDistance = if (fraction < 0) -mOverScrollDistance else mOverScrollDistance
        scrollBy(0, dy)
    }

    private fun startBounceAnimator(velocity: Float) {
        mBounceRunnable?.cancel()
        mBounceRunnable = BounceAnimRunnable(velocity, mOverScrollDistance)
        mBounceRunnable?.start()
    }

    fun startScrollBackAnimator() {
        mSpringBackAnimator?.cancel()
        mSpringBackAnimator = ValueAnimator.ofInt(mOverScrollDistance, 0)
        mSpringBackAnimator?.interpolator = DecelerateInterpolator()
        mSpringBackAnimator?.duration = SPRING_BACK_DURATION.toLong()
        mSpringBackAnimator?.addUpdateListener { animation ->
            updateOverScrollDistance(
                animation.animatedValue as Int
            )
        }
        mSpringBackAnimator?.start()
    }

    private fun updateOverScrollDistance(distance: Int) {
        mOverScrollDistance = distance
        if (mOverScrollDistance < 0) {
            scrollTo(
                0, (-mMaxOverScrollDistance * mInterpolator.getInterpolation(
                    abs(mOverScrollDistance.toFloat() / mOverScrollBorder)
                )).toInt()
            )
        } else {
            scrollTo(
                0, (mMaxOverScrollDistance * mInterpolator.getInterpolation(
                    abs(mOverScrollDistance.toFloat() / mOverScrollBorder)
                )).toInt()
            )
        }
    }

    inner class OverScrollerInterpolator(private var factor: Float) : Interpolator {

        fun getInterpolationBack(input: Float) : Float {
            return (ln(1 - input) / ln(factor) / 2)
        }

        override fun getInterpolation(input: Float): Float {
            return (1 - factor.pow(input * 2))
        }
    }

    inner class BounceAnimRunnable : Runnable {
        /**
         * 两帧之间的间隔
         */
        private var frameInternalMillis = 10

        private var mDeceleration = 0
        private var mVelocity = 0f
        private var mStartY = 0
        private var mRuntime = 0
        private var mDuration = 0
        private var mHasCanceled = false

        constructor(velocity: Float, startY: Int) {
            mDeceleration = if (velocity < 0) {
                BOUNCE_BACK_DECELERATION
            } else {-BOUNCE_BACK_DECELERATION
            }
            mVelocity = velocity
            mStartY = startY
            mDuration = ((-mVelocity / mDeceleration) * 1000).toInt()
        }

        fun start() {
            postDelayed(this, frameInternalMillis.toLong())
        }

        fun cancel() {
            mHasCanceled = true
            removeCallbacks(this)
        }

        override fun run() {
            if (mHasCanceled) {
                return
            }
            mRuntime += frameInternalMillis
            val t = mRuntime.toFloat() / 1000
            val distance = (mStartY + mVelocity * t + 0.5 * mDeceleration * t * t).toInt()
            updateOverScrollDistance(distance)
            if (mRuntime < mDuration && abs(distance) < mMaxOverScrollDistance * 2) {
                removeCallbacks(this)
                postDelayed(this, frameInternalMillis.toLong())
            } else {
                startScrollBackAnimator()
            }
        }

    }

}