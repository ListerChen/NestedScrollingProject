
package com.lister.nestedscrollingproject.mixed

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lister.nestedscrollingproject.util.CustomAdapter

class RecyclerViewWrapper (context: Context) {

    private val mContext: Context = context
    private val mRecyclerView = RecyclerView(mContext)

    fun initData() {
        val adapter = CustomAdapter()
        adapter.setData(getDataList())
        mRecyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.isNestedScrollingEnabled = true
        mRecyclerView.adapter = adapter
        mRecyclerView.layoutManager = LinearLayoutManager(
            mContext, LinearLayoutManager.VERTICAL, false)
    }

    private fun getDataList(): List<String> {
        val dataList: MutableList<String> = mutableListOf()
        for (index in 1..30) {
            dataList.add(index.toString())
        }
        return dataList
    }

    fun getRecyclerView(): RecyclerView {
        return mRecyclerView
    }

}