
package com.lister.nestedscrollingproject.mixed

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.webkit.WebView
import android.widget.Scroller
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat

class NestedScrollWebView: WebView, NestedScrollingChild3 {

    companion object {
        const val TAG = "NestedScrollWebView"
    }

    private var touchSlop = 10
    private var lastMotionY: Int = 0
    private var initialY: Int = 0

    private val viewFlinger: ViewFlinger = ViewFlinger()
    private var velocityTracker: VelocityTracker? = null
    private var maxVelocity: Float = 0F

    private val nestedScrollingChildHelper: NestedScrollingChildHelper = NestedScrollingChildHelper(this)
    private val consumed: IntArray = IntArray(2)
    private val offsetInWindow: IntArray = IntArray(2)
    private val nestedOffsets: IntArray = IntArray(2)

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
        nestedScrollingChildHelper.isNestedScrollingEnabled = true
        val viewConfiguration: ViewConfiguration = ViewConfiguration.get(context)
        touchSlop = viewConfiguration.scaledTouchSlop
        maxVelocity = viewConfiguration.scaledMaximumFlingVelocity.toFloat()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(ev)
        if (ev != null) {
            val action = ev.action
            if (action == MotionEvent.ACTION_DOWN) {
                nestedOffsets[0] = 0
                nestedOffsets[1] = 0
                stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
            } else if (action == MotionEvent.ACTION_MOVE) {
                // nothing
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                velocityTracker?.clear()
                stopNestedScroll(ViewCompat.TYPE_TOUCH)
            }
        }
        return true
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        var eventAddedToVelocityTracker = false
        if (event?.action == MotionEvent.ACTION_DOWN) {
            nestedOffsets[0] = 0
            nestedOffsets[1] = 0
        }
        val vtev: MotionEvent = MotionEvent.obtain(event)
        vtev.offsetLocation(nestedOffsets[0].toFloat(), nestedOffsets[1].toFloat())
        if (event != null) {
            val action = event.action
            if (action == MotionEvent.ACTION_DOWN) {
                viewFlinger.stop()
                val y: Int = event.rawY.toInt()
                initialY = y
                lastMotionY = y
            } else if (action == MotionEvent.ACTION_MOVE) {
                // scroll处理
                val y: Int = event.rawY.toInt()
                var dy = lastMotionY - y
                lastMotionY = y
                // 1. dispatchNestedPreScroll
                if (dispatchNestedPreScroll(0, dy, consumed, offsetInWindow, ViewCompat.TYPE_TOUCH)) {
                    nestedOffsets[1] += offsetInWindow[1]
                    dy -= consumed[1]
                    parent.requestDisallowInterceptTouchEvent(true)
                } else {
                    Log.e(TAG, "nested error")
                }
                // 2. scroll self
                var unconsumedY = 0
                if (dy >= 0) {
                    val scrollRange = computeVerticalScrollRange() - measuredHeight
                    var actualDy = dy
                    if (scrollY + actualDy > scrollRange) {
                        actualDy = scrollRange - scrollY
                    }
                    scrollBy(0, actualDy)
                    unconsumedY = dy - actualDy
                } else {
                    var actualDy = dy
                    if (scrollY + actualDy < 0) {
                        actualDy = -scrollY
                    }
                    scrollBy(0, actualDy)
                    unconsumedY = dy - actualDy
                }
                // 3. dispatchNestedScroll
                dispatchNestedScroll(0, 0, 0, unconsumedY,
                    offsetInWindow, ViewCompat.TYPE_TOUCH)
                nestedOffsets[1] += offsetInWindow[1]
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                velocityTracker?.addMovement(event)
                eventAddedToVelocityTracker = true
                initialY = 0
                lastMotionY = 0
                stopNestedScroll(ViewCompat.TYPE_TOUCH)
                // fling处理
                velocityTracker?.let {
                    it.computeCurrentVelocity(1000, maxVelocity)
                    val velocityY: Int = -(it.yVelocity).toInt()
                    viewFlinger.fling(velocityY)
                    it.recycle()
                }
                velocityTracker = null
            } else {
                // todo 多指滑动暂不处理
            }
        }
        if (!eventAddedToVelocityTracker) {
            velocityTracker?.addMovement(vtev)
        }
        vtev.recycle()
        return true
    }

    fun canWebViewScrollDown(): Boolean {
        val range = computeVerticalScrollRange()
        return ((scrollY + measuredHeight) < range)
    }

    fun stopFling() {
        viewFlinger.stop()
        stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
    }

    inner class ViewFlinger: Runnable {

        private val scroller: Scroller = Scroller(context, QuinticInterpolator())
        private var lastFlingY: Int = 0

        override fun run() {
            if (scroller.computeScrollOffset()) {
                val y = scroller.currY
                var unconsumedY = y - lastFlingY
                lastFlingY = y

                // 1. dispatchNestedPreScroll
                consumed[0] = 0
                if (dispatchNestedPreScroll(0, unconsumedY, consumed,
                        null, ViewCompat.TYPE_NON_TOUCH)) {
                    unconsumedY -= consumed[1]
                }

                // 2. scroll self
                var actualDy = unconsumedY
                if (unconsumedY > 0) {
                    val scrollRange = computeVerticalScrollRange() - measuredHeight
                    if (scrollY + actualDy > scrollRange) {
                        actualDy = scrollRange - scrollY
                    }
                    scrollBy(0, actualDy)
                } else if (unconsumedY < 0) {
                    if (scrollY + actualDy < 0) {
                        actualDy = -scrollY
                    }
                    scrollBy(0, actualDy)
                }
                unconsumedY -= actualDy

                // 3. dispatchNestedScroll
                // consumed[1] = 0
                dispatchNestedScroll(0, 0, 0, unconsumedY,
                    null, ViewCompat.TYPE_NON_TOUCH, consumed)

                // 如果Scroller的滑动尚未结束则post到下一帧
                val scrollFinishedY = (scroller.currY == scroller.finalY)
                val notConsumedByParent = unconsumedY == consumed[1]
                val doneScrolling = (scrollFinishedY || scroller.isFinished) // || notConsumedByParent)
                if (doneScrolling) {
                    stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
                } else {
                    internalPostOnAnimation()
                }
            }
        }

        fun fling(velocityY: Int) {
            lastFlingY = 0
            scroller.fling(0, 0, 0, velocityY,
                0, 0, Int.MIN_VALUE, Int.MAX_VALUE)
            startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH)
            internalPostOnAnimation()
        }

        fun stop() {
            removeCallbacks(this)
            scroller.abortAnimation()
            stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
        }

        private fun internalPostOnAnimation() {
            removeCallbacks(this)
            ViewCompat.postOnAnimation(this@NestedScrollWebView, this)
        }

    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return nestedScrollingChildHelper.startNestedScroll(axes, type)
    }

    override fun stopNestedScroll(type: Int) {
        nestedScrollingChildHelper.stopNestedScroll(type)
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        return nestedScrollingChildHelper.hasNestedScrollingParent(type)
    }

    override fun dispatchNestedPreScroll(
        dx: Int, dy: Int, consumed: IntArray?,
        offsetInWindow: IntArray?, type: Int): Boolean {
        return nestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    /**
     * V3
     */
    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int,
                                      dxUnconsumed: Int, dyUnconsumed: Int,
                                      offsetInWindow: IntArray?, type: Int, consumed: IntArray) {
        nestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
            dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed)
    }

    /**
     * V2
     */
    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int,
                                      dxUnconsumed: Int, dyUnconsumed: Int,
                                      offsetInWindow: IntArray?, type: Int): Boolean {
        return nestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
            dxUnconsumed, dyUnconsumed, offsetInWindow, type)
    }

}