
package com.lister.nestedscrollingproject.simple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lister.nestedscrolltest.R
import com.lister.nestedscrollingproject.util.CustomAdapter

class SimpleNestedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_nested)

        val rv: RecyclerView = findViewById(R.id.simple_recycler_view)
        val adapter = CustomAdapter()
        adapter.setData(getDataList())
        rv.isNestedScrollingEnabled = true
        rv.setHasFixedSize(true)
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun getDataList(): List<String> {
        val dataList: MutableList<String> = mutableListOf()
        for (index in 1..30) {
            dataList.add(index.toString())
        }
        return dataList
    }
}