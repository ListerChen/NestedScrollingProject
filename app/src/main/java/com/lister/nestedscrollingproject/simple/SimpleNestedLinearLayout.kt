
package com.lister.nestedscrollingproject.simple

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.NestedScrollingParent2
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.lister.nestedscrolltest.R

class SimpleNestedLinearLayout : LinearLayout, NestedScrollingParent2 {

    private var imageView: ImageView? = null
    private var recyclerView: RecyclerView? = null

    private var imageViewHeight: Int = 0

    private var nestedScrollParentHelper: NestedScrollingParentHelper = NestedScrollingParentHelper(this)

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
        // ...
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        var child = getChildAt(0)
        if (child != null && child is ImageView) {
            imageView = child
        } else {
            throw IllegalArgumentException("child at 0 is not ImageView")
        }
        child = getChildAt(2)
        if (child != null && child is RecyclerView) {
            recyclerView = child
        } else {
            throw IllegalArgumentException("child at 2 is not RecyclerView")
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        )
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        for (index in 0 until childCount) {
            val child = getChildAt(index) ?: continue
            val layoutParams = child.layoutParams
            if (layoutParams.height == LayoutParams.MATCH_PARENT) {
                val rvHeight = measuredHeight - resources.getDimensionPixelSize(R.dimen.simple_nested_title_height)
                val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(rvHeight, heightMode)
                measureChild(child, widthMeasureSpec, childHeightMeasureSpec)
            } else {
                val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, heightMode)
                measureChild(child, widthMeasureSpec, childHeightMeasureSpec)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        imageViewHeight = imageView?.measuredHeight ?: 0
    }

    override fun getNestedScrollAxes(): Int {
        return nestedScrollParentHelper.nestedScrollAxes
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        nestedScrollParentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        nestedScrollParentHelper.onStopNestedScroll(target, type)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (dy > 0 && scrollY < imageViewHeight) {
            var actualDy = dy
            if (scrollY + dy >= imageViewHeight) {
                actualDy = imageViewHeight - scrollY
            }
            consumed[1] = actualDy
            scrollBy(0, actualDy)
        } else if (dy < 0 && recyclerView?.canScrollVertically(-1) == false && scrollY > 0) {
            var actualDy = dy
            if (scrollY + dy < 0) {
                actualDy = -scrollY
            }
            consumed[1] = actualDy
            scrollBy(0, actualDy)
        }
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        // ...
    }

}