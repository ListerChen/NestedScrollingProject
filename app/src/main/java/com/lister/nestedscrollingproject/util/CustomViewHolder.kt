
package com.lister.nestedscrollingproject.util

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lister.nestedscrolltest.R

class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val textView: TextView = itemView.findViewById(R.id.item_text_view)

    fun bindData(text: String) {
        textView.text = text
    }

}