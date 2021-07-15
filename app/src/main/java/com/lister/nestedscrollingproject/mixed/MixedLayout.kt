
package com.lister.nestedscrollingproject.mixed

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

class MixedLayout : ViewGroup, NestedScrollingParent3 {

    private val nestedScrollingParentHelper = NestedScrollingParentHelper(this)

    private val loadUrl = "https://github.com/ListerChen/NestedScrollingProject"
    private val webView: NestedScrollWebView = NestedScrollWebView(context)
    private val recyclerViewWrapper = RecyclerViewWrapper(context)
    private val recyclerView: RecyclerView = recyclerViewWrapper.getRecyclerView()
    /**
     * MixedLayout 可滑动的最大值, 实际应该 = WebView高度+RecyclerView高度-MixedLayout高度
     */
    private var layoutMaxScrollY = 0
    private val nestedScrollingV2ConsumedCompat = IntArray(2)

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
        initWebView()
        initRecyclerView()
        // add WebView
        val webViewLayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        this.addView(webView, webViewLayoutParams)
        webView.loadUrl(loadUrl)
        // add RecyclerView
        val rvLayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        this.addView(recyclerView, rvLayoutParams)
    }

    private fun initWebView() {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                return super.shouldInterceptRequest(view, request)
            }
        }
    }

    private fun initRecyclerView() {
        recyclerViewWrapper.initData()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        )
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val height = measuredHeight
        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            if (child == webView) {
                val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, heightMode)
                measureChild(webView, widthMeasureSpec, childHeightMeasureSpec)
            } else if (child == recyclerView) {
                val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, heightMode)
                measureChild(recyclerView, widthMeasureSpec, childHeightMeasureSpec)
            } else {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0..childCount) {
            val child = getChildAt(i)
            if (child == webView) {
                child.layout(0, 0, measuredWidth, measuredHeight)
            } else if (child == recyclerView) {
                child.layout(0, measuredHeight, measuredWidth, measuredHeight * 2)
            }
        }
        layoutMaxScrollY = measuredHeight
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            val action = ev.action
            if (action == MotionEvent.ACTION_DOWN) {
                recyclerView.stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
                webView.stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
    }

    override fun getNestedScrollAxes(): Int {
        return nestedScrollingParentHelper.nestedScrollAxes
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        nestedScrollingParentHelper.onStopNestedScroll(target, type)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        var actualDy = dy
        if (target == webView) {
            if (dy > 0 && !webView.canWebViewScrollDown()) {
                // WebView内容向下滑动
                if (scrollY + actualDy > layoutMaxScrollY) {
                    actualDy = layoutMaxScrollY - scrollY
                }
                scrollBy(0, actualDy)
                consumed[1] = actualDy
            } else if (dy < 0 && scrollY > 0) {
                // WebView内容向上滑动
                if (scrollY + actualDy < 0) {
                    actualDy = -scrollY
                }
                scrollBy(0, actualDy)
                consumed[1] = actualDy
            }
        } else if (target == recyclerView) {
            if (dy > 0 && scrollY < layoutMaxScrollY) {
                if (scrollY + actualDy > layoutMaxScrollY) {
                    actualDy = layoutMaxScrollY - scrollY
                }
                scrollBy(0, actualDy)
                consumed[1] = actualDy
            } else if (dy < 0) {
                if (!recyclerView.canScrollVertically(-1)) {
                    if (scrollY + actualDy < 0) {
                        actualDy = -scrollY
                        webView.stopFling()
                    }
                    scrollBy(0, actualDy)
                    consumed[1] = actualDy
                }
            }
        }
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int,
                                dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
            type, nestedScrollingV2ConsumedCompat)
    }

    /**
     * RecyclerView的Fling事件传递到了WebView 或 WebView的Fling事件传递到了RecyclerView
     */
    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int,
                                dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
        if (dyUnconsumed == 0 || nestedScrollAxes != ViewCompat.SCROLL_AXIS_VERTICAL) {
            return
        }
        consumed[1] = dyUnconsumed
        if (target == webView && dyUnconsumed > 0) {
            if (scrollY >= layoutMaxScrollY) {
                if (scrollY > layoutMaxScrollY) {
                    scrollTo(0, layoutMaxScrollY)
                }
                if (recyclerView.canScrollVertically(1)) {
                    recyclerView.scrollBy(0, dyUnconsumed)
                } else {
                    webView.stopNestedScroll(type)
                }
            }
        } else if (target == recyclerView && dyUnconsumed < 0) {
            if (scrollY <= 0) {
                if (scrollY < 0) {
                    scrollTo(0, 0)
                }
                if (webView.scrollY + dyUnconsumed > 0) {
                    webView.scrollBy(0, dyUnconsumed)
                } else {
                    recyclerView.stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
                    webView.scrollTo(0, 0)
                }

            }
        }
    }

}