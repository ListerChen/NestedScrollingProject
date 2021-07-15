
package com.lister.nestedscrollingproject.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lister.nestedscrolltest.R

class CustomAdapter : RecyclerView.Adapter<CustomViewHolder>() {

    private var dataList: List<String> = mutableListOf()

    fun setData(data: List<String>) {
        dataList = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recycler_view, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bindData(dataList[position])
    }

    override fun getItemCount(): Int {
        return if (dataList.isNullOrEmpty()) {0} else {dataList.size}
    }

}